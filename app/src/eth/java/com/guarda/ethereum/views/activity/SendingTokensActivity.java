package com.guarda.ethereum.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.Callback;
import com.guarda.ethereum.managers.Converter;
import com.guarda.ethereum.managers.EthereumNetworkManager;
import com.guarda.ethereum.managers.RawNodeManager;
import com.guarda.ethereum.managers.TransactionsManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.items.RawTransactionResponse;
import com.guarda.ethereum.utils.DigitsInputFilter;
import com.guarda.ethereum.views.activity.base.AToolbarMenuActivity;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IllegalFormatConversionException;
import java.util.List;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

import static com.guarda.ethereum.models.constants.Common.DEFAULT_GAS_LIMIT_FOR_CONTRACT;
import static com.guarda.ethereum.models.constants.Common.ETH_SHOW_PATTERN;


@AutoInjector(GuardaApp.class)
public class SendingTokensActivity extends AToolbarMenuActivity {

    @BindView(R.id.et_sum_send)
    EditText etSumSend;
    @BindView(R.id.et_fee_amount)
    EditText etFeeAmount;
    @BindView(R.id.et_send_coins_address)
    EditText etWalletAddress;
    @BindView(R.id.btn_confirm)
    Button btnConfirm;
    @BindView(R.id.eth_hint_sum)
    TextView tvTokenHint;

    private BigDecimal balance = new BigDecimal(0);

    @Inject
    WalletManager walletManager;

    @Inject
    EthereumNetworkManager networkManager;

    @Inject
    TransactionsManager transactionsManager;

    @Inject
    RawNodeManager tokensManager;

    private String walletNumber;
    private String amountToSend;
    private String tokenCode;
    private boolean isInclude = false;
    private BigInteger gasPrice;
    private BigDecimal currentFeeEth;
    private BigDecimal currentEthBalance;
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
        tokenCode = getIntent().getStringExtra(Extras.TOKEN_CODE_EXTRA);
        tvTokenHint.setText(tokenCode);
        getTokenBalanceBalance();
        getEthereumBalance();
        setDataToView();
        initSendSumField();
        getGasPrice();
        initFeeField();
    }

    private void getEthereumBalance() {
        networkManager.getBalance(walletManager.getWalletFriendlyAddress(), new Callback<BigDecimal>() {
            @Override
            public void onResponse(BigDecimal response) {
                btnConfirm.setEnabled(true);
                currentEthBalance = response;
            }
        });
    }

    private void getTokenBalanceBalance() {
        balance = tokensManager.getTokenByCode(tokenCode).getTokenNum();

        if (balance == null)
            Toast.makeText(this, "Can't get balance, try again later", Toast.LENGTH_SHORT).show();
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
                    } catch (IllegalFormatConversionException e) {
                        btnConfirm.setEnabled(false);
                    }
                    if (feeLessThanEthBalance(newFee)
                            && currentFeeEth.compareTo(BigDecimal.ZERO) > 0) {
                        hideError(etFeeAmount);
                        btnConfirm.setEnabled(true);
                    } else if (currentEthBalance.compareTo(BigDecimal.ZERO) == 0) {
                        btnConfirm.setEnabled(false);
                        String error = getString(R.string.transaction_cant_be_sent)
                                + " " + getString(R.string.app_coin_currency) + " "
                                + getString(R.string.balance);
                        showError(etFeeAmount, error);
                    } else {
                            btnConfirm.setEnabled(false);
                            String error = getString(R.string.et_error_fee_more_than)
                                    + " " + getString(R.string.app_coin_currency) + " "
                                    + getString(R.string.balance);
                            showError(etFeeAmount, error);

                    }
                } else {
                    btnConfirm.setEnabled(false);
                    showError(etFeeAmount, getString(R.string.et_error_fee_is_empty));
                }

            }
        });
    }

    private boolean feeLessThanEthBalance(String newFee) {
        BigDecimal feeDecimal = new BigDecimal(newFee);
        return currentEthBalance.compareTo(feeDecimal) > 0;
    }

    private void updateGasLimitForFeeMode() {
        String newFeeStr = etFeeAmount.getText().toString();
        BigInteger currentFeeWei = Convert.toWei(newFeeStr, Convert.Unit.ETHER).toBigInteger();
        gasLimitForFeeMode = currentFeeWei.divide(gasPrice);
        btnConfirm.setEnabled(true);
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
                    Toast.makeText(SendingTokensActivity.this,
                            "Can't get gas price, try again later",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateFee() {
        BigInteger defaultGasLimit = BigInteger.valueOf(DEFAULT_GAS_LIMIT_FOR_CONTRACT);
        BigInteger fee = gasPrice.multiply(defaultGasLimit);
        currentFeeEth = Convert.fromWei(new BigDecimal(fee), Convert.Unit.ETHER);
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat(ETH_SHOW_PATTERN, symbols);
        String feeStr = decimalFormat.format(currentFeeEth);
        etFeeAmount.setText(feeStr);
    }

    private void setDataToView() {
        etSumSend.setText(amountToSend);
        etWalletAddress.setText(walletNumber);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_sending_tokens;
    }

    private String getToAddress() {
        return tokensManager.getTokeAddressByCode(tokenCode);
    }

    private String getAmountToSend() {
        return etSumSend.getText().toString();
    }

    @OnClick(R.id.btn_confirm)
    public void onConfirmClick(View view) {
        String amount = etSumSend.getText().toString();
        if (!amount.isEmpty()) {
            if (!isAmountMoreBalance(amount)) {

                BigInteger gasLimit;
                BigInteger value;
                String customData;

                gasLimit = gasLimitForFeeMode;
                value = BigInteger.ZERO;
                customData = getContractTransfer();

                networkManager.sendTransaction(getToAddress(), value, gasPrice, gasLimit, customData,
                        new Callback<RawTransactionResponse>() {
                            @Override
                            public void onResponse(RawTransactionResponse response) {
                                closeProgress();
                                if (response != null) {
                                    String hash = response.getHash();
                                    String blockNumber = response.getBlockNumber();

                                    showCongratsActivity();
                                } else {

                                    Toast.makeText(SendingTokensActivity.this,
                                            getString(R.string.transaction_not_sent_fee),
                                            Toast.LENGTH_LONG).show();
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

    private String getContractTransfer() {
        int tokenDecimals = tokensManager.getTokenByCode(tokenCode).getDecimal();
        BigInteger tokenValue = Converter.toDecimals(new BigDecimal(amountToSend), tokenDecimals).toBigInteger();
        List<Type> inputParams = new ArrayList<>();

        Type address = new Address(walletNumber);
        inputParams.add(address);

        Type value = new Uint(tokenValue);
        inputParams.add(value);

        Function function = new Function(
                "transfer",
                inputParams,
                Collections.<TypeReference<?>>emptyList());

        return FunctionEncoder.encode(function);
    }

//    private void sendRawTransactionToHolder(String fromAddress, String toAddress, BigInteger value,
//                                            String hash, long timeStamp, RawTransactionResponse rawResponse,
//                                            String blockNumber) {
//
//        TransactionResponse transaction = new TransactionResponse();
//        transaction.setFrom(fromAddress);
//        transaction.setTo(toAddress);
//        transaction.setHash(hash);
//        transaction.setValue(value.toString());
//        transaction.setTimeStamp(timeStamp / 1000);
//        transaction.setRawResponse(rawResponse);
//        transaction.setBlockNumber(blockNumber);
//        transactionsManager.addPendingTransaction(transaction);
//
//    }

    private boolean isAmountMoreBalance(String amount) {
        BigDecimal amountDecimal = new BigDecimal(amount);
        return balance.compareTo(amountDecimal) < 0;
    }


    private void showCongratsActivity() {
        Intent intent = new Intent(this, CongratsActivity.class);
        intent.putExtra(Extras.CONGRATS_TEXT, getString(R.string.result_transaction_sent));
        intent.putExtra(Extras.COME_FROM, Extras.FROM_WITHDRAW);
        startActivity(intent);
    }

}
