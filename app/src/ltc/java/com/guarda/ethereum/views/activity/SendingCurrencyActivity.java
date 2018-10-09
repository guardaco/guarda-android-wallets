package com.guarda.ethereum.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.BitcoinNodeManager;
import com.guarda.ethereum.managers.EthereumNetworkManager;
import com.guarda.ethereum.managers.TransactionsManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.items.SendRawTxResponse;
import com.guarda.ethereum.models.items.TxFeeResponse;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;
import com.guarda.ethereum.utils.CurrencyUtils;
import com.guarda.ethereum.utils.DigitsInputFilter;
import com.guarda.ethereum.views.activity.base.AToolbarMenuActivity;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.WrongNetworkException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.IllegalFormatConversionException;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

import static com.guarda.ethereum.models.constants.Common.AVG_TX_SIZE_KB;

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
    @BindView(R.id.ll_fee_container)
    LinearLayout feeContainer;

    @Inject
    WalletManager walletManager;

    @Inject
    EthereumNetworkManager networkManager;

    @Inject
    TransactionsManager transactionsManager;

    private String walletNumber;
    private String amountToSend;
    private boolean isInclude = false;
    private long currentFeeEth;
    private String arrivalAmountToSend;

    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        setToolBarTitle(getString(R.string.title_withdraw3));
        etSumSend.setFilters(new InputFilter[]{new DigitsInputFilter(8, 8, Float.POSITIVE_INFINITY)});
        etFeeAmount.setFilters(new InputFilter[]{new DigitsInputFilter(8, 8, Float.POSITIVE_INFINITY)});
        walletNumber = getIntent().getStringExtra(Extras.WALLET_NUMBER);
        amountToSend = getIntent().getStringExtra(Extras.AMOUNT_TO_SEND);
        checkBtnIncludeStatus(isInclude);
        setDataToView();
        initSendSumField();
        initFeeField();
        updateArrivalField();
        updateWarnings();
        updateArrivalField();
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
                updateArrivalField();
                updateWarnings();
                updateArrivalField();
            }
        });
    }

    private void initFeeField() {
        Coin defaultFee = Coin.valueOf(164000);
//        Coin defaultFee = Coin.valueOf(1000);
        currentFeeEth = defaultFee.getValue();
        etFeeAmount.setText(defaultFee.toPlainString());
        updateArrivalField();

        Requestor.getTxFee(new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    HashMap<String, TxFeeResponse> feesMap = (HashMap<String, TxFeeResponse>) response;
                    BigDecimal fee = new BigDecimal(feesMap.get(Common.MAIN_CURRENCY.toLowerCase()).getFee());
                    //fee to fee per Kb (1 Kb is 1000 bytes)
                    fee = fee.divide(AVG_TX_SIZE_KB, BigDecimal.ROUND_HALF_UP);
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
                updateArrivalField();
                updateWarnings();
                updateArrivalField();
            }
        });
    }

    private void updateWarnings() {
        try {
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

            String newFee = etFeeAmount.getText().toString();
            if (newFee.length() > 0) {
                findViewById(R.id.eth_hint_fee).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.eth_hint_fee).setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(newFee)) {
                try {
                    currentFeeEth = Coin.parseCoin(etFeeAmount.getText().toString()).getValue();
                    if (currentFeeEth > 0) {
                        btnConfirm.setEnabled(true);
                    } else {
                        btnConfirm.setEnabled(false);
                        showError(etFeeAmount, getString(R.string.et_error_fee_is_empty));
                    }
                } catch (IllegalFormatConversionException e) {
                    btnConfirm.setEnabled(false);
                }
                if (feeLessThanAmountToSend(newFee)
                        && currentFeeEth > 0) {
                    hideError(etFeeAmount);
                    btnConfirm.setEnabled(true);
                } else if (isInclude) {
                    btnConfirm.setEnabled(false);
                    showError(etFeeAmount, getString(R.string.et_error_fee_more_than_amount));
                }
            } else {
                btnConfirm.setEnabled(false);
                showError(etFeeAmount, getString(R.string.et_error_fee_is_empty));
            }

            long amountSatoshi = Coin.parseCoin(getAmountToSend()).getValue();
            if (amountSatoshi < 0) {
                btnConfirm.setEnabled(false);
                showError(etFeeAmount, getString(R.string.et_error_fee_more_than_amount));
            }
        } catch (Exception e) {
            btnConfirm.setEnabled(false);
            showError(etFeeAmount, getString(R.string.withdraw_amount_can_not_be_empty));
        }
    }

    private void checkFeeLessAmount() {
        if (!feeLessThanAmountToSend(etFeeAmount.getText().toString()) && isInclude) {
            showError(etFeeAmount, getString(R.string.et_error_fee_more_than_amount));
            btnConfirm.setEnabled(false);
        } else {
            hideError(etFeeAmount);
            btnConfirm.setEnabled(true);
        }
    }

    private boolean feeLessThanAmountToSend(String newFee) {
//        BigDecimal feeDecimal = new BigDecimal(newFee);
//        BigDecimal amountDecimal = new BigDecimal(etSumSend.getText().toString());
//        return amountDecimal.compareTo(feeDecimal) > 0;
        return !false;
    }

    private void updateArrivalField() {
        Log.d("flint", "SendingCurrencyActivity.updateArrivalField()...");
        boolean makeSecondCheck = true;
        try {
            currentFeeEth = Coin.parseCoin(etFeeAmount.getText().toString()).getValue();
            long sumSatoshi = Coin.parseCoin(etSumSend.getText().toString()).getValue();
            // divide on 2 - to prevent not_enough_money_error in case of sending all wallet's money include fee
            long totalFee = walletManager.calculateFee(getToAddress(), sumSatoshi / 2, currentFeeEth);
            Log.d("flint", "1 amountToSend=" + sumSatoshi + ", currentFeeEth=" + currentFeeEth + ", totalFee=" + totalFee);
            if (isInclude) {
                if (Coin.valueOf(sumSatoshi - totalFee).isPositive()) {
                    arrivalAmountToSend = Coin.valueOf(sumSatoshi - totalFee).toPlainString();
                } else {
                    arrivalAmountToSend = Coin.ZERO.toPlainString();
                }
            } else {
                arrivalAmountToSend = Coin.valueOf(sumSatoshi).toPlainString();
            }
            etArrivalAmount.setText(arrivalAmountToSend);
        } catch (WrongNetworkException wne){
            Log.e("psd", wne.toString());
            String toastStr = String.format(getString(R.string.send_wrong_address), getString(R.string.app_coin_currency));
            Toast.makeText(this, toastStr, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.d("flint", "1 amountToSend=... exception: " + e.toString());
            btnConfirm.setEnabled(false);
            if (e.getMessage() == "SMALL_SENDING") {
                makeSecondCheck = false;
                showError(etFeeAmount, getString(R.string.small_sum_of_tx));
            } else {
                showError(etFeeAmount, getString(R.string.not_enough_money_to_send));
            }
        }
        // arrivalAmountToSend has changed, recalculate fee for preciseness (totalFee should not change)
        if (makeSecondCheck) {
            try {
                long sumSatoshi = Coin.parseCoin(etSumSend.getText().toString()).getValue();
                long amountSatoshi = Coin.parseCoin(getAmountToSend()).getValue();
                long totalFee = walletManager.calculateFee(getToAddress(), amountSatoshi, currentFeeEth);
                Log.d("flint", "2 amountToSend=" + amountSatoshi + ", currentFeeEth=" + currentFeeEth + ", totalFee=" + totalFee);
                if (isInclude) {
                    if (Coin.valueOf(sumSatoshi - totalFee).isPositive()) {
                        arrivalAmountToSend = Coin.valueOf(sumSatoshi - totalFee).toPlainString();
                    } else {
                        arrivalAmountToSend = Coin.ZERO.toPlainString();
                    }
                } else {
                    arrivalAmountToSend = Coin.valueOf(sumSatoshi).toPlainString();
                }
                etArrivalAmount.setText(arrivalAmountToSend);
            } catch (Exception e) {
                Log.d("flint", "2 amountToSend=... exception: " + e.toString());
                btnConfirm.setEnabled(false);
                if (e.getMessage() == "SMALL_SENDING") {
                    showError(etFeeAmount, getString(R.string.small_sum_of_tx));
                } else {
                    showError(etFeeAmount, getString(R.string.not_enough_money_to_send));
                }
            }
        }
    }

    private void setDataToView() {
        etSumSend.setText(amountToSend);
        etWalletAddress.setText(walletNumber);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_sending_currency;
    }

    @OnClick({R.id.btn_include, R.id.btn_exclude})
    public void sendingCurrencyButtonClick(View view) {
        switch (view.getId()) {
            case R.id.btn_include:
                isInclude = true;
                checkFeeLessAmount();
                checkBtnIncludeStatus(isInclude);
                updateArrivalField();
                break;
            case R.id.btn_exclude:
                isInclude = false;
                checkFeeLessAmount();
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

    private String getToAddress() {
        return etWalletAddress.getText().toString();
    }

    private String getAmountToSend() {
        if (isInclude)
            return etArrivalAmount.getText().toString();
        else
            return etSumSend.getText().toString();
    }

    @OnClick(R.id.btn_confirm)
    public void onConfirmClick(View view) {
        try {
            String amount = etSumSend.getText().toString();
            if (!amount.isEmpty()) {
                if (!isAmountMoreBalance(amount)) {
                    long amountSatoshi = Coin.parseCoin(getAmountToSend()).getValue();

                    Log.d("svcom", "fee = " + currentFeeEth);
                    String hex = walletManager.generateHexTx(getToAddress(), amountSatoshi, currentFeeEth);
                    if (hex.equals(WalletManager.SMALL_SENDING)) {
                        showError(etSumSend, getString(R.string.small_sum_of_tx));
                    } else if (hex.equals(WalletManager.NOT_ENOUGH_MONEY)) {
                        showError(etSumSend, getString(R.string.not_enough_money_to_send));
                    } else {
                        showProgress(getString(R.string.progress_bar_sending_transaction));
                        BitcoinNodeManager.sendTransaction(hex, new ApiMethods.RequestListener() {
                            @Override
                            public void onSuccess(Object response) {
                                SendRawTxResponse res = (SendRawTxResponse) response;
                                Log.d("TX_RES", "res " + res.getHashResult() + " error " + res.getError());
                                closeProgress();
                                showCongratsActivity();
                            }
                            @Override
                            public void onFailure(String msg) {
                                closeProgress();
                                Log.d("svcom", "failure - " + msg);
                                Toast.makeText(SendingCurrencyActivity.this, CurrencyUtils.getBtcLikeError(msg), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    showError(etSumSend, getString(R.string.withdraw_amount_more_than_balance));
                }
            } else {
                showError(etSumSend, getString(R.string.withdraw_amount_can_not_be_empty));
            }
        } catch (WrongNetworkException wne) {
            Log.e("psd", wne.toString());
            String msg = String.format(getString(R.string.send_wrong_address), Common.MAIN_CURRENCY.toUpperCase());
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(SendingCurrencyActivity.this, "Can not send the transaction to the node", Toast.LENGTH_SHORT).show();
            closeProgress();
            e.printStackTrace();
        }
    }

    private boolean isAmountMoreBalance(String amount) {
        return false;
    }

    private void showCongratsActivity() {
        Intent intent = new Intent(this, CongratsActivity.class);
        intent.putExtra(Extras.CONGRATS_TEXT, getString(R.string.result_transaction_sent));
        intent.putExtra(Extras.COME_FROM, Extras.FROM_WITHDRAW);
        startActivity(intent);
    }

}
