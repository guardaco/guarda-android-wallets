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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.EthereumNetworkManager;
import com.guarda.ethereum.managers.TransactionsManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.items.SendRawTxResponse;
import com.guarda.ethereum.models.items.TxFeeResponse;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;
import com.guarda.ethereum.rest.RequestorBtc;
import com.guarda.ethereum.rxcall.CallUpdateTxDetails;
import com.guarda.ethereum.utils.CurrencyUtils;
import com.guarda.ethereum.utils.DigitsInputFilter;
import com.guarda.ethereum.views.activity.base.AToolbarMenuActivity;
import com.guarda.zcash.ZCashException;
import com.guarda.zcash.ZCashTransaction_zaddr;
import com.guarda.zcash.ZCashTransaction_ztot;
import com.guarda.zcash.ZCashWalletManager;
import com.guarda.zcash.crypto.Utils;
import com.guarda.zcash.sapling.db.DbManager;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.WrongNetworkException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IllegalFormatConversionException;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

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
    @Inject
    DbManager dbManager;

    private String walletNumber;
    private String amountToSend;
    private boolean isSaplingAddress;
    private String saplingBalance;

    private boolean isInclude = false;
    private long currentFeeEth;
    private String arrivalAmountToSend;
    private Coin defaultFee = Coin.valueOf(558);

    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        setToolBarTitle(getString(R.string.title_withdraw3));
        etSumSend.setFilters(new InputFilter[]{new DigitsInputFilter(8, 8, Float.POSITIVE_INFINITY)});
        etFeeAmount.setFilters(new InputFilter[]{new DigitsInputFilter(8, 8, Float.POSITIVE_INFINITY)});
        walletNumber = getIntent().getStringExtra(Extras.WALLET_NUMBER);
        amountToSend = getIntent().getStringExtra(Extras.AMOUNT_TO_SEND);
        isSaplingAddress = getIntent().getBooleanExtra(Extras.IS_SAPLING_ADDRESS, false);
        saplingBalance = getIntent().getStringExtra(Extras.SAPLING_BALANCE_STRING);
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
        if (isSaplingAddress) defaultFee = Coin.valueOf(10000);
        currentFeeEth = defaultFee.getValue();
        etFeeAmount.setText(defaultFee.toPlainString());
        updateArrivalField();

        Requestor.getTxFee(new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    HashMap<String, TxFeeResponse> feesMap = (HashMap<String, TxFeeResponse>) response;
                    BigDecimal fee = new BigDecimal(feesMap.get(Common.MAIN_CURRENCY.toLowerCase()).getFee());
                    fee = fee.setScale(8, BigDecimal.ROUND_HALF_UP).stripTrailingZeros();
                    etFeeAmount.setText(fee.toPlainString());

                    updateArrivalField();
                } catch (Exception e) {
                    e.printStackTrace();
                    Timber.d("getTxFee - onSuccess: %s", e.getMessage());
                }
            }

            @Override
            public void onFailure(String msg) {
                Timber.d("getTxFee - onFailure: %s", msg);
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
                if (!isValueMoreBalance(newAmount)) {
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
                if (currentFeeEth > 0) {
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

    private void updateArrivalField() {
        Timber.d("SendingCurrencyActivity.updateArrivalField()...");
        boolean makeSecondCheck = true;
        try {
            currentFeeEth = Coin.parseCoin(etFeeAmount.getText().toString()).getValue();
            long sumSatoshi = Coin.parseCoin(etSumSend.getText().toString()).getValue();
            long totalFee = currentFeeEth;
            Timber.d("1 amountToSend=" + sumSatoshi + ", currentFeeEth=" + currentFeeEth + ", totalFee=" + totalFee);
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
            Coin tzBalance = isSaplingAddress ? Coin.parseCoin(saplingBalance) : walletManager.getMyBalance();
            if (!isInclude && Coin.valueOf(currentFeeEth).plus(Coin.parseCoin(amountToSend)).compareTo(tzBalance) > 0) {
                showError(etArrivalAmount, getString(R.string.not_enough_money_to_send));
            } else {
                hideError(etArrivalAmount);
            }
        } catch (WrongNetworkException wne) {
            Timber.e("updateArrivalField wne=%s", wne.toString());
            String toastStr = String.format(getString(R.string.send_wrong_address), getString(R.string.app_coin_currency));
            Toast.makeText(this, toastStr, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Timber.d("1 amountToSend=... exception: %s", e.toString());
            btnConfirm.setEnabled(false);
            if (e.getMessage().equalsIgnoreCase("SMALL_SENDING")) {
                makeSecondCheck = false;
                showError(etFeeAmount, getString(R.string.small_sum_of_tx));
            } else if (e.getMessage().equalsIgnoreCase("java.lang.ArithmeticException: Rounding necessary")) {
                makeSecondCheck = false;
                showError(etFeeAmount, "Fee is too small");
            } else {
                showError(etFeeAmount, getString(R.string.not_enough_money_to_send));
            }
        }
        // arrivalAmountToSend has changed, recalculate fee for preciseness (totalFee should not change)
        if (makeSecondCheck) {
            try {
                long sumSatoshi = Coin.parseCoin(etSumSend.getText().toString()).getValue();
                long amountSatoshi = Coin.parseCoin(getAmountToSend()).getValue();
                long totalFee = currentFeeEth;
                Timber.d("2 amountToSend=" + amountSatoshi + ", currentFeeEth=" + currentFeeEth + ", totalFee=" + totalFee);
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
                Timber.d("2 amountToSend=... exception: %s", e.toString());
                btnConfirm.setEnabled(false);
                if (e.getMessage().equals("SMALL_SENDING")) {
                    showError(etFeeAmount, getString(R.string.small_sum_of_tx));
                } else if (e.getMessage().equalsIgnoreCase("java.lang.ArithmeticException: Rounding necessary")) {
                    showError(etFeeAmount, "Fee is too small");
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
                updateArrivalField();
                checkBtnIncludeStatus(isInclude);
                break;
            case R.id.btn_exclude:
                isInclude = false;
                updateArrivalField();
                checkBtnIncludeStatus(isInclude);
                break;
        }
    }

    private void checkBtnIncludeStatus(boolean isInclude) {
        if (isInclude) {
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
                if (!isValueMoreBalance(amount)) {
                    showProgress();
                    if (isSaplingAddress) {
                        sendFromShielded();
                    } else {
                        sendFromTransparent();
                    }
                } else {
                    showError(etSumSend, getString(R.string.withdraw_amount_more_than_balance));
                }
            } else {
                showError(etSumSend, getString(R.string.withdraw_amount_can_not_be_empty));
            }
        } catch (WrongNetworkException wne) {
            Timber.e("onConfirmClick wne=%s", wne.toString());
            Toast.makeText(this, R.string.send_wrong_address, Toast.LENGTH_SHORT).show();
        } catch (ZCashException e) {
            doToast("Can not send the transaction: " + e.getMessage());
            Timber.e("createTx ZCashException=%s", e.getMessage());
        } catch (Exception e) {
            Toast.makeText(SendingCurrencyActivity.this, "Error of sending", Toast.LENGTH_SHORT).show();
            closeProgress();
            e.printStackTrace();
        }
    }

    private void sendFromTransparent() throws ZCashException {
        String addr = getToAddress();
        if (addr.substring(0, 1).equalsIgnoreCase("t")) {
            sendTransparentToTransparent();
        } else if (addr.substring(0, 1).equalsIgnoreCase("z")) {
            sendTransparentToSapling();
        }
    }

    // T to T
    private void sendTransparentToTransparent() throws ZCashException {
        Timber.d("sendTransparentToTransparent");
        long amountSatoshi = Coin.parseCoin(getAmountToSend()).getValue();
        Timber.d("amount=%d fee=%d", amountSatoshi, currentFeeEth);
        ZCashWalletManager.getInstance().createTransaction_taddr(walletManager.getWalletFriendlyAddress(),
                getToAddress(),
                amountSatoshi,
                currentFeeEth,
                walletManager.getPrivateKey(),
                Common.ZCASH_MIN_CONFIRM, (r1, r2) -> {
                        Timber.i("RESPONSE CODE %s", r1);
                        if (r1.equals("ok")) {
                            try {
                                String lastTxhex = Utils.bytesToHex(r2.getBytes());
                                Timber.d("lastTxhex %s", lastTxhex);

                                sendTxHashAndUpdateDb(lastTxhex);
                            } catch (ZCashException e) {
                                closeProgress();
                                doToast("Sending error: " + e.getMessage());
                                Timber.d("Cannot sign transaction");
                            }
                        } else {
                            closeProgress();
                            doToast("Sending error: " + r1);
                            Timber.d("createTransaction_taddr: RESPONSE CODE is not ok");
                        }
                });
    }

    // T to Z
    private void sendTransparentToSapling() throws ZCashException {
        Timber.d("sendTransparentToSapling");
        long amountSatoshi = Coin.parseCoin(getAmountToSend()).getValue();
        Timber.d("amount=%d fee=%d", amountSatoshi, currentFeeEth);
        ZCashWalletManager.getInstance().createTransaction_ttoz(walletManager.getWalletFriendlyAddress(),
                getToAddress(),
                amountSatoshi,
                currentFeeEth,
                walletManager.getPrivateKey(),
                walletManager.getSaplingCustomFullKey(),
                Common.ZCASH_MIN_CONFIRM, (r1, r2) -> {
                        Timber.i("sendTransparentToSapling RESPONSE CODE %s", r1);
                        if (r1.equals("ok")) {
                            try {
                                String lastTxhex = Utils.bytesToHex(r2.getBytes());
                                Timber.d("sendTransparentToSapling lastTxhex %s", lastTxhex);

                                sendTxHashAndUpdateDb(lastTxhex);
                            } catch (ZCashException e) {
                                closeProgress();
                                doToast("Sending error: " + e.getMessage());
                                Timber.d("sendTransparentToSapling Cannot sign transaction e=%s", e.getMessage());
                            }
                        } else {
                            closeProgress();
                            doToast("Sending error: " + r1);
                            Timber.d("sendTransparentToSapling: RESPONSE CODE is not ok");
                        }
                });
    }

    private void sendFromShielded() throws ZCashException {
        String addr = getToAddress();
        if (addr.substring(0, 1).equalsIgnoreCase("z")) {
            sendShieldedToShielded();
        } else if (addr.substring(0, 1).equalsIgnoreCase("t")) {
            sendShieldedToTransparent();
        }
    }

    // Z to Z
    private void sendShieldedToShielded() throws ZCashException {
        Timber.d("sendShieldedToShielded");
        long amountSatoshi = Coin.parseCoin(getAmountToSend()).getValue();
        Timber.d("amount=" + amountSatoshi + " fee=" + currentFeeEth);

        ZCashWalletManager.getInstance().createTransaction_zaddr(walletManager.getSaplingAddress(),
                getToAddress(),
                amountSatoshi,
                currentFeeEth,
                walletManager.getSaplingCustomFullKey(),
                1,
                dbManager,
                (r1, r2) -> {
                    Timber.d("z to z - onResponse %s", r1);
                    if (r1.equals("ok")) {
                        ZCashTransaction_zaddr tx = (ZCashTransaction_zaddr) r2;
                        byte[] bytes = tx.getBytes();
                        Timber.d("z to z - bytes=%s %d", Arrays.toString(bytes), bytes.length);
                        String lastTxhex = Utils.bytesToHex(bytes);
                        Timber.d("z to z - lastTxhex=%s", lastTxhex);

                        sendTxHashAndUpdateDb(lastTxhex);
                    } else {
                        closeProgress();
                        doToast("Sending error: " + r1);
                        Timber.d("z to z - err=%s", r1);
                    }
                });
    }

    // Z to T
    private void sendShieldedToTransparent() throws ZCashException {
        Timber.d("z to t - started");
        long amountSatoshi = Coin.parseCoin(getAmountToSend()).getValue();
        Timber.d("z to t - amount=" + amountSatoshi + " fee=" + currentFeeEth);

        ZCashWalletManager.getInstance().createTransaction_ztot(walletManager.getWalletFriendlyAddress(),
                getToAddress(),
                amountSatoshi,
                currentFeeEth,
                walletManager.getSaplingCustomFullKey(),
                dbManager,
                (r1, r2) -> {
                    Timber.i("z to t - RESPONSE CODE %s", r1);
                    if (r1.equals("ok")) {
                        ZCashTransaction_ztot tx = (ZCashTransaction_ztot) r2;
                        String lastTxhex = Utils.bytesToHex(tx.getBytes());
                        Timber.d("z to t - lastTxhex %s", lastTxhex);

                        sendTxHashAndUpdateDb(lastTxhex);
                    } else {
                        closeProgress();
                        doToast("Sending error: " + r1);
                        Timber.d("z to t - RESPONSE CODE is not ok");
                    }
                });
    }

    private void sendTxHashAndUpdateDb(String lastTxhex) {
        RequestorBtc.broadcastRawTxZexNew(lastTxhex, new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                SendRawTxResponse res = (SendRawTxResponse) response;
                Timber.d("broadcastRawTxZexNew txid=%s", res.getTxid());
                updateFromInsight(res.getTxid());
            }

            @Override
            public void onFailure(String msg) {
                closeProgress();
                doToast(CurrencyUtils.getBtcLikeError(msg));
                Timber.d("broadcastRawTxZexNew e=%s", msg);
            }
        });
    }

    private void updateFromInsight(String hash) {
        compositeDisposable.add(
                RequestorBtc
                        .getOneTx(hash)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                tx -> {
                                    if (tx == null) {
                                        finishSending();
                                        Timber.e("getOneTx tx == null");
                                        return;
                                    }
                                    compositeDisposable.add(Observable
                                            .fromCallable(new CallUpdateTxDetails(
                                                    dbManager,
                                                    tx,
                                                    walletManager.getWalletFriendlyAddress()
                                            ))
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe((value) -> {
                                                finishSending();
                                                Timber.d("CallDbFillHistory value=%s", value);
                                            }));
                                },
                                e -> {
                                    finishSending();
                                    Timber.e("getOneTx e=%s", e.getMessage());
                                }
                        )
        );
    }

    private void finishSending() {
        closeProgress();
        showCongratsActivity();
    }

    private void doToast(final String text) {
        runOnUiThread(() -> Toast.makeText(SendingCurrencyActivity.this, text, Toast.LENGTH_SHORT).show());
    }

    private boolean isValueMoreBalance(String amount) {
        if (isSaplingAddress) {
            return Coin.parseCoin(amount).compareTo(Coin.parseCoin(saplingBalance)) > 0;
        } else {
            return Coin.parseCoin(amount).compareTo(walletManager.getMyBalance()) > 0;
        }
    }

    private void showCongratsActivity() {
        Intent intent = new Intent(this, CongratsActivity.class);
        intent.putExtra(Extras.CONGRATS_TEXT, getString(R.string.result_transaction_sent));
        intent.putExtra(Extras.COME_FROM, Extras.FROM_WITHDRAW);
        startActivity(intent);
    }

}
