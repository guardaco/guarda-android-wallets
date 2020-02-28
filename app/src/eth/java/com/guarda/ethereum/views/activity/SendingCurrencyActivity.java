package com.guarda.ethereum.views.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.widget.SwitchCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.Callback;
import com.guarda.ethereum.managers.EthereumNetworkManager;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.TransactionsManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.items.RawTransactionResponse;
import com.guarda.ethereum.models.items.TransactionResponse;
import com.guarda.ethereum.models.items.TxFeeResponse;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;
import com.guarda.ethereum.utils.DigitsInputFilter;
import com.guarda.ethereum.views.activity.base.AToolbarMenuActivity;

import org.web3j.utils.Convert;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

import static com.guarda.ethereum.models.constants.Common.DEFAULT_GAS_LIMIT;
import static com.guarda.ethereum.models.constants.Common.ETH_SHOW_PATTERN;

@AutoInjector(GuardaApp.class)
public class SendingCurrencyActivity extends AToolbarMenuActivity {

    @BindView(R.id.et_sum_send)
    EditText etSumSend;
    @BindView(R.id.et_fee_amount)
    EditText etFeeAmount;
    @BindView(R.id.et_arrival_amount)
    EditText etArrivalAmount;
    @BindView(R.id.et_send_coins_address)
    EditText etWalletAddress;
    @BindView(R.id.btn_include)
    Button btnInclude;
    @BindView(R.id.btn_exclude)
    Button btnExclude;
    @BindView(R.id.btn_confirm)
    Button btnConfirm;
    @BindView(R.id.ll_gas_container)
    LinearLayout gasContainer;
    @BindView(R.id.ll_fee_container)
    LinearLayout feeContainer;
    @BindView(R.id.switch_gas_fee)
    SwitchCompat swGasFee;
    @BindView(R.id.et_gas_limit)
    EditText etGasLimit;
    @BindView(R.id.et_custom_data)
    EditText etCustomData;
    @BindView(R.id.cl_switch_container)
    ConstraintLayout switchContainer;
    @BindView(R.id.tv_gas)
    TextView tvGas;
    @BindView(R.id.tv_fee)
    TextView tvFee;

    private BigDecimal balance = new BigDecimal(0);

    @Inject
    WalletManager walletManager;

    @Inject
    EthereumNetworkManager networkManager;

    @Inject
    TransactionsManager transactionsManager;

    @Inject
    SharedManager sharedManager;

    private String walletNumber;
    private String amountToSend;
    private boolean isInclude = false;
    private BigInteger gasPrice;
    private boolean isGasModeEnabled;
    private BigDecimal currentFeeEth;
    private BigInteger gasLimitForFeeMode;
    private String arrivalAmountToSend;


    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        setToolBarTitle(getString(R.string.title_withdraw3));
        etSumSend.setFilters(new InputFilter[]{new DigitsInputFilter(8, 8, Float.POSITIVE_INFINITY)});
        etFeeAmount.setFilters(new InputFilter[]{new DigitsInputFilter(8, 8, Float.POSITIVE_INFINITY)});
        walletNumber = getIntent().getStringExtra(Extras.WALLET_NUMBER);
        amountToSend = getIntent().getStringExtra(Extras.AMOUNT_TO_SEND);
        switchContainer.requestFocus();
        getBalance();
        checkBtnIncludeStatus(isInclude);
        setDataToView();
        initSendSumField();
        getGasPrice();
        initCustomFields();
        initFeeField();
    }

    private void getBalance() {
        btnConfirm.setEnabled(false);
        networkManager.getBalance(walletManager.getWalletFriendlyAddress(), new Callback<BigDecimal>() {
            @Override
            public void onResponse(BigDecimal response) {
                if (response != null) {
                    btnConfirm.setEnabled(true);
                    balance = response;
                }

            }
        });
    }

    private void initSendSumField() {
        etSumSend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideError(etSumSend);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String newAmount = etSumSend.getText().toString();
                if (newAmount.length() > 0) {
                    findViewById(R.id.eth_hint_sum).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.eth_hint_sum).setVisibility(View.GONE);
                }
                if (!TextUtils.isEmpty(newAmount)) {
                    if (!isAmountMoreBalance(newAmount)) {
                        hideError(etSumSend);
                        amountToSend = newAmount;
                        updateArrivalField();
                    } else {
                        showError(etSumSend, getString(R.string.withdraw_amount_more_than_balance));
                    }
                } else {
                    showError(etSumSend, getString(R.string.withdraw_amount_can_not_be_empty));
                }
            }
        });
    }

    private void initFeeField() {
        Requestor.getTxFee(new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    HashMap<String, TxFeeResponse> feesMap = (HashMap<String, TxFeeResponse>) response;
                    BigDecimal fee = new BigDecimal(feesMap.get(Common.MAIN_CURRENCY.toLowerCase()).getFee());
                    //fee to fee per Kb (1 Kb is 1000 bytes)
//                    fee = fee.divide(avgTxSizeKb, BigDecimal.ROUND_HALF_UP);
                    fee = fee.setScale(8, BigDecimal.ROUND_HALF_UP).stripTrailingZeros();
                    etFeeAmount.setText(fee.toPlainString());

                    updateArrivalField();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("psd", "getTxFee - onSuccess: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(String msg) {
                Log.d("psd", "getTxFee - onFailure: " + msg);
            }
        });

        etFeeAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideError(etFeeAmount);
            }

            @Override
            public void afterTextChanged(Editable s) {
                afterFeeTextChanged();
            }
        });
    }

    private void afterFeeTextChanged() {
        String newFee = etFeeAmount.getText().toString();
        if (newFee.length() > 0) {
            findViewById(R.id.eth_hint_fee).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.eth_hint_fee).setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(newFee)) {
            try {
                currentFeeEth = new BigDecimal(etFeeAmount.getText().toString());
                if (currentFeeEth.compareTo(BigDecimal.ZERO) > 0) {
                    updateGasLimitForFeeMode();
                    btnConfirm.setEnabled(true);
                } else {
                    btnConfirm.setEnabled(false);
                    showError(etFeeAmount, getString(R.string.et_error_fee_is_empty));
                }
            } catch (Exception e) {
                btnConfirm.setEnabled(false);
            }
            if (currentFeeEth.compareTo(BigDecimal.ZERO) > 0) {
                hideError(etFeeAmount);
                btnConfirm.setEnabled(true);
            }
        } else {
            btnConfirm.setEnabled(false);
            showError(etFeeAmount, getString(R.string.et_error_fee_is_empty));
        }
    }

    private void checkFee() {
        if (etFeeAmount.getText().toString().equalsIgnoreCase("")) {
            showError(etFeeAmount, getString(R.string.et_error_fee_is_empty));
            btnConfirm.setEnabled(false);
        } else {
            BigDecimal feeDecimal = new BigDecimal(etFeeAmount.getText().toString());
            if (feeDecimal.compareTo(BigDecimal.ZERO) == 0) {
                showError(etFeeAmount, getString(R.string.et_error_fee_is_empty));
                btnConfirm.setEnabled(false);
            } else {
                hideError(etFeeAmount);
                btnConfirm.setEnabled(true);
            }
        }
    }

    private boolean feeLessThanAmountToSend(String newFee) {
        try {
            if (TextUtils.isEmpty(etSumSend.getText().toString())) {
                return false;
            }
            BigDecimal feeDecimal = new BigDecimal(newFee);
            BigDecimal amountDecimal = new BigDecimal(etSumSend.getText().toString());
            return amountDecimal.compareTo(feeDecimal) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void updateGasLimitForFeeMode() {
        String newFeeStr = etFeeAmount.getText().toString();
        BigInteger currentFeeWei = Convert.toWei(newFeeStr, Convert.Unit.ETHER).toBigInteger();
        gasLimitForFeeMode = currentFeeWei.divide(gasPrice);
        btnConfirm.setEnabled(true);
        updateArrivalField();
    }

    private void updateArrivalField() {

        if (isInclude) {
            BigDecimal amountDecimal = new BigDecimal(amountToSend);
            BigDecimal arrivalAmountDecimal = amountDecimal.subtract(currentFeeEth);
            if (arrivalAmountDecimal.compareTo(BigDecimal.ZERO) < 0) {
                arrivalAmountDecimal = BigDecimal.ZERO;
            }
            DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
            symbols.setDecimalSeparator('.');
            DecimalFormat decimalFormat = new DecimalFormat("###0.########", symbols);
            arrivalAmountToSend = decimalFormat.format(arrivalAmountDecimal);
            etArrivalAmount.setText(arrivalAmountToSend);
        } else {
            etArrivalAmount.setText(amountToSend);
        }
    }

    private void initCustomFields() {
        gasContainer.setVisibility(!swGasFee.isChecked() ? View.VISIBLE : View.GONE);
        feeContainer.setVisibility(swGasFee.isChecked() ? View.VISIBLE : View.GONE);
        isGasModeEnabled = !swGasFee.isChecked();
        swGasFee.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                gasContainer.setVisibility(!isChecked ? View.VISIBLE : View.GONE);
                feeContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                isGasModeEnabled = !isChecked;
                if (isGasModeEnabled)
                    btnConfirm.setEnabled(true);
                else
                    afterFeeTextChanged();
            }
        });

        tvGas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swGasFee.setChecked(false);
            }
        });
        tvFee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swGasFee.setChecked(true);
            }
        });

        etGasLimit.setText(String.format(Locale.US, "%d", DEFAULT_GAS_LIMIT));

        etGasLimit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideError(etGasLimit);
            }

            @Override
            public void afterTextChanged(Editable s) {
                btnConfirm.setEnabled(true);
                if (TextUtils.isEmpty(etGasLimit.getText().toString())) {
                    showError(etGasLimit, getString(R.string.et_error_gas_is_empty));
                    btnConfirm.setEnabled(false);
                }
            }
        });
    }

    private void getGasPrice() {
        btnConfirm.setEnabled(false);
        networkManager.getGasPrice(new Callback<BigInteger>() {
            @Override
            public void onResponse(BigInteger response) {
                btnConfirm.setEnabled(true);
                gasPrice = response;
                if (gasPrice != null) {
                    updateFee();
                } else {
                    Toast.makeText(SendingCurrencyActivity.this,
                            "Can't get gas price, try again later",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateFee() {

        BigInteger defaultGasLimit = BigInteger.valueOf(DEFAULT_GAS_LIMIT);
        BigInteger fee = gasPrice.multiply(defaultGasLimit);
        currentFeeEth = Convert.fromWei(new BigDecimal(fee), Convert.Unit.ETHER);
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat(ETH_SHOW_PATTERN, symbols);
        String feeStr = decimalFormat.format(currentFeeEth);
        etFeeAmount.setText(feeStr);
        updateArrivalField();
    }

    private void setDataToView() {
        etSumSend.setText(amountToSend);
        etWalletAddress.setText(walletNumber);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_sending_currency;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_slide, R.anim.slide_in_right);
    }

    @OnClick({R.id.btn_include, R.id.btn_exclude})
    public void sendingCurrencyButtonClick(View view) {
        switch (view.getId()) {
            case R.id.btn_include:
                isInclude = true;
                checkFee();
                checkBtnIncludeStatus(isInclude);
                updateArrivalField();
                break;
            case R.id.btn_exclude:
                isInclude = false;
                checkFee();
                updateArrivalField();
                checkBtnIncludeStatus(isInclude);
                break;
        }
    }

    private void checkBtnIncludeStatus(boolean isInclude) {
        if (isInclude){
            btnExclude.setBackground(getResources().getDrawable(R.drawable.btn_enable_gray));
            btnInclude.setBackground(getResources().getDrawable(R.drawable.btn_border_blue));
        } else {
            btnInclude.setBackground(getResources().getDrawable(R.drawable.btn_enable_gray));
            btnExclude.setBackground(getResources().getDrawable(R.drawable.btn_border_blue));
        }
    }

    private String getToAddress(){
        return etWalletAddress.getText().toString();
    }

    private String getAmountToSend(){
        return etSumSend.getText().toString();
    }

    @OnClick(R.id.btn_confirm)
    public void onConfirmClick(View view) {
        String amount = etSumSend.getText().toString();
        if (!amount.isEmpty()) {
            if (!isAmountMoreBalance(amount)) {

                BigInteger gasLimit;
                final BigInteger value;
                String customData;

                if (isGasModeEnabled) {
                    gasLimit = getGasLimit();
                    value = Convert.toWei(getAmountToSend(), Convert.Unit.ETHER).toBigInteger();
                    customData = etCustomData.getText().toString();
                } else {
                    gasLimit = gasLimitForFeeMode;
                    if (!isInclude) {
                        value = Convert.toWei(getAmountToSend(), Convert.Unit.ETHER).toBigInteger();
                    } else {
                        value = Convert.toWei(arrivalAmountToSend, Convert.Unit.ETHER).toBigInteger();
                    }
                    customData = "";
                }

                Log.d("flint", "SendingCurrencyActivity.onConfirmClick()... value="+value + ", gasPrice="+gasPrice + ", gasLimit="+gasLimit);
                networkManager.sendTransaction(getToAddress(), value, gasPrice, gasLimit, customData,
                        new Callback<RawTransactionResponse>() {
                    @Override
                    public void onResponse(RawTransactionResponse response) {
                        closeProgress();
                        if (response != null) {
                            String hash = response.getHash();
                            String blockNumber = response.getBlockNumber();
                            sendRawTransactionToHolder(walletManager.getWalletFriendlyAddress(),
                                    getToAddress(),
                                    value,
                                    hash,
                                    System.currentTimeMillis(),
                                    response, blockNumber);

//                            sendRawTxToCache(walletManager.getWalletFriendlyAddress(),
//                                    getToAddress(),
//                                    value,
//                                    hash,
//                                    System.currentTimeMillis(),
//                                    response, blockNumber);
                            showCongratsActivity();
                        } else {
                            if (isGasModeEnabled) {
                                Toast.makeText(SendingCurrencyActivity.this,
                                        getString(R.string.transaction_not_sent_gas),
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(SendingCurrencyActivity.this,
                                        getString(R.string.transaction_not_sent_fee),
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                });
                showProgress(getString(R.string.progress_bar_sending_transaction));

            } else {
                showError(etSumSend, getString(R.string.withdraw_amount_more_than_balance));
            }
        } else {
            showError(etSumSend, getString(R.string.withdraw_amount_can_not_be_empty));
        }

    }

    private void sendRawTransactionToHolder(String fromAddress, String toAddress, BigInteger value,
                                            String hash, long timeStamp, RawTransactionResponse rawResponse,
                                            String blockNumber) {

        TransactionResponse transaction = new TransactionResponse();
        transaction.setFrom(fromAddress);
        transaction.setTo(toAddress);
        transaction.setHash(hash);
        transaction.setValue(value.toString());
        transaction.setTimeStamp(timeStamp / 1000);
        transaction.setRawResponse(rawResponse);
        transaction.setBlockNumber(blockNumber);
        transaction.setTicker(Common.MAIN_CURRENCY.toUpperCase());
        transactionsManager.addPendingTransaction(transaction);

    }

    private void sendRawTxToCache(String fromAddress, String toAddress, BigInteger value,
                                  String hash, long timeStamp, RawTransactionResponse rawResponse,
                                  String blockNumber) {

        if (walletManager.getWalletFriendlyAddress() == null || walletManager.getWalletFriendlyAddress().equalsIgnoreCase(""))
            return;

        String jsonFromPref = sharedManager.getTxsCache();
        Gson gson = new Gson();
        Type listType = new TypeToken<Map<String, ArrayList<TransactionResponse>>>(){}.getType();
        Map<String, ArrayList<TransactionResponse>> addrTxsMap = gson.fromJson(jsonFromPref, listType);
        if (addrTxsMap == null)
            addrTxsMap = new HashMap<>();

        ArrayList<TransactionResponse> txList = addrTxsMap.get(walletManager.getWalletFriendlyAddress());
        if (txList == null)
            txList = new ArrayList<>();

        TransactionResponse transaction = new TransactionResponse();
        transaction.setFrom(fromAddress);
        transaction.setTo(toAddress);
        transaction.setHash(hash);
        transaction.setValue(value.toString());
        transaction.setTimeStamp(timeStamp / 1000);
        transaction.setRawResponse(rawResponse);
        transaction.setBlockNumber(blockNumber);
        transaction.setTicker(Common.MAIN_CURRENCY.toUpperCase());
        txList.add(0, transaction);

        addrTxsMap.put(walletManager.getWalletFriendlyAddress(), txList);

        String jsonToPref = gson.toJson(addrTxsMap);
        sharedManager.setTxsCache(jsonToPref);
    }

    private BigInteger getGasLimit() {
        int limitFromField = Integer.parseInt(etGasLimit.getText().toString());
        return BigInteger.valueOf(limitFromField);

    }

    private boolean isAmountMoreBalance(String amount) {
        BigDecimal amountDecimal = new BigDecimal(amount);
        return balance.compareTo(amountDecimal) < 0;
    }


    private void showCongratsActivity(){
        Intent intent = new Intent(this, CongratsActivity.class);
        intent.putExtra(Extras.CONGRATS_TEXT, getString(R.string.result_transaction_sent));
        intent.putExtra(Extras.COME_FROM, Extras.FROM_WITHDRAW);
        startActivity(intent);
    }

}
