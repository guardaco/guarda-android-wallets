package com.guarda.ethereum.views.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.Callback;
import com.guarda.ethereum.managers.EthereumNetworkManager;
import com.guarda.ethereum.managers.TransactionsManager;
import com.guarda.ethereum.managers.WalletAPI;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.items.TransactionResponse;
import com.guarda.ethereum.views.activity.base.AToolbarMenuActivity;
import com.guarda.ethereum.utils.ClipboardUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

import static com.guarda.ethereum.models.constants.Common.ETH_SHOW_PATTERN;
import static com.guarda.ethereum.models.constants.Common.EXTRA_TRANSACTION_POSITION;

@AutoInjector(GuardaApp.class)
public class TransactionDetailsActivity extends AToolbarMenuActivity {

    @BindView(R.id.et_trans_details_sum)
    EditText etTrValue;

    @BindView(R.id.et_trans_details_date)
    EditText etDate;

    @BindView(R.id.et_trans_details_time)
    EditText etTime;

    @BindView(R.id.et_trans_details_hash)
    EditText etHash;

    @BindView(R.id.et_trans_details_balance_after)
    EditText etBalanceAfter;

    @BindView(R.id.et_trans_details_balance_before)
    EditText etBalanceBefore;

    @BindView(R.id.btn_copy)
    Button btnCopy;

    @BindView(R.id.btn_repeat)
    Button btnRepeat;

    @BindView(R.id.tv_confirmations)
    TextView etConfirmations;

    @Inject
    TransactionsManager transactionsManager;

    @Inject
    WalletManager walletManager;

    @Inject
    EthereumNetworkManager networkManager;

    private TransactionResponse transaction;

    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        setToolBarTitle(getString(R.string.title_transaction_detail));
        int transactionPosition = getIntent().getIntExtra(EXTRA_TRANSACTION_POSITION, 0);
        transaction = transactionsManager.getTxByPosition(transactionPosition);
        etBalanceAfter.setVisibility(View.GONE);
        etBalanceBefore.setVisibility(View.GONE);
        updateViews();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_transaction_details;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_slide, R.anim.slide_in_right);
    }

    private void updateViews() {
        if (transaction != null) {
            setValue(transaction.getValue());
            setDateAndTime();
            setHash(transaction.getHash());
            setConfirmations(transaction.getConfirmations());
            setBalanceDetails();
            initRepeatButton();
        }
    }

    private void initRepeatButton() {
        if (isDebit(walletManager.getWalletFriendlyAddress(), transaction.getTo())){
            btnRepeat.setVisibility(View.GONE);
        } else {
            btnRepeat.setVisibility(View.VISIBLE);
        }
    }

    private boolean isPending() {
        return transaction.haveRawResponse();
    }

    private void setBalanceDetails() {
        if (transaction.getBlockNumber() != null) {
            networkManager.getBalance(walletManager.getWalletFriendlyAddress(),
                    transaction.getBlockNumber(), new Callback<BigDecimal>() {
                        @Override
                        public void onResponse(BigDecimal response) {
                            if (response != null) {
                                if (!isPending()) {
                                    BigDecimal value = new BigDecimal(transaction.getValue());
                                    BigDecimal formatedValue = new BigDecimal(WalletAPI.satoshiToCoinsString(value));
                                    BigDecimal balanceBefore;
                                    if (isDebit(walletManager.getWalletFriendlyAddress(), transaction.getTo())) {
                                        balanceBefore = response.subtract(formatedValue);
                                    } else {
                                        balanceBefore = response.add(formatedValue);
                                    }
                                    etBalanceAfter.setText(balanceToString(response));
                                    etBalanceBefore.setText(balanceToString(balanceBefore));
                                } else {
                                    BigDecimal balanceBefore = (BigDecimal) response;
                                    BigDecimal value = new BigDecimal(transaction.getValue());
                                    BigDecimal formatedValue = new BigDecimal(WalletAPI.satoshiToCoinsString(value));
                                    BigDecimal balanceAfter;
                                    balanceAfter = balanceBefore.subtract(formatedValue);
                                    etBalanceBefore.setText(String.format("%s %s", balanceToString(balanceBefore), Common.MAIN_CURRENCY.toUpperCase()));
                                    etBalanceAfter.setText(String.format("%s %s", balanceToString(balanceAfter), Common.MAIN_CURRENCY.toUpperCase()));
                                }
                            }
                        }
                    });
        } else {
            networkManager.getBalance(walletManager.getWalletFriendlyAddress(),
                    new Callback<BigDecimal>() {
                        @Override
                        public void onResponse(BigDecimal balanceAfter) {
                            BigDecimal value = new BigDecimal(transaction.getValue());
                            BigDecimal formatedValue = new BigDecimal(WalletAPI.satoshiToCoinsString(value));
                            BigDecimal balanceBefore;
                            if (isDebit(walletManager.getWalletFriendlyAddress(), transaction.getTo())) {
                                balanceBefore = balanceAfter.subtract(formatedValue);
                            } else {
                                balanceBefore = balanceAfter.add(formatedValue);
                            }
                            etBalanceBefore.setText(String.format("%s %s", balanceToString(balanceBefore), Common.MAIN_CURRENCY.toUpperCase()));
                            etBalanceAfter.setText(String.format("%s %s", balanceToString(balanceAfter), Common.MAIN_CURRENCY.toUpperCase()));
                        }
                    });
        }

    }

    private String balanceToString(BigDecimal balance) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat(ETH_SHOW_PATTERN, symbols);
        decimalFormat.setRoundingMode(RoundingMode.DOWN);
        if (!balance.equals(new BigDecimal(0))) {
            return decimalFormat.format(balance);

        } else {
            return "00.00";
        }
    }

    private void setConfirmations(String confirmations) {
        if (confirmations != null) {
            etConfirmations.setText(confirmations);
        } else {
            etConfirmations.setText(String.format(Locale.US, "%d", 0));
        }
    }

    private void setHash(String hash) {
        etHash.setText(hash);
    }

    private void setDateAndTime() {
        Date date = new Date(transaction.getTimeStamp() * 1000);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        etDate.setText(dateFormat.format(date));
        etTime.setText(timeFormat.format(date));
    }

    private void setValue(String respValue) {
        BigInteger value = new BigInteger(respValue);
        BigDecimal decimal = new BigDecimal(value);
        BigDecimal formatted = new BigDecimal(WalletAPI.satoshiToCoinsString(decimal));
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat(ETH_SHOW_PATTERN, symbols);
        decimalFormat.setRoundingMode(RoundingMode.DOWN);

        String valueStr = (isDebit(walletManager.getWalletFriendlyAddress(), transaction.getTo()) ? "+" : "-")
                + " " + decimalFormat.format(formatted);
        etTrValue.setText(String.format("%s %s", valueStr, Common.MAIN_CURRENCY.toUpperCase()));
    }

    private boolean isDebit(String ourAddress, String toAddress) {
        try {
            return ourAddress.equals(toAddress);
        } catch (Exception e) {
            return false;
        }
    }

    @OnClick(R.id.btn_copy)
    public void onCopyClick() {
        ClipboardUtils.copyToClipBoard(this, transaction.getHash());
    }

    @OnClick(R.id.btn_repeat)
    public void onRepeatTransactionClick(){
        String transValue = "";
        BigInteger value = new BigInteger(transaction.getValue());
        BigDecimal decimal = new BigDecimal(value);
        BigDecimal formatted = new BigDecimal(WalletAPI.satoshiToCoinsString(decimal));
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.#####", symbols);
        transValue = decimalFormat.format(formatted);

        Intent intent = new Intent(this, SendingCurrencyActivity.class);
        intent.putExtra(Extras.WALLET_NUMBER, transaction.getTo());
        intent.putExtra(Extras.AMOUNT_TO_SEND, transValue);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.no_slide);

    }

    @OnClick({R.id.til_trans_details_hash, R.id.et_trans_details_hash})
    public void onTransHashClick(){
        String hashDetailUrl = "https://explorer.decent.ch/block/" + etHash.getText().toString();
        openWebURL(hashDetailUrl);
    }

    public void openWebURL(String inURL) {
        Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(inURL));
        startActivity(browse);
    }

}
