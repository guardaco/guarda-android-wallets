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
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.customviews.GuardaInputLayout;
import com.guarda.ethereum.managers.Callback;
import com.guarda.ethereum.managers.EthereumNetworkManager;
import com.guarda.ethereum.managers.RawNodeManager;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.utils.DigitsInputFilter;
import com.guarda.ethereum.utils.KeyboardManager;
import com.guarda.ethereum.views.activity.base.AToolbarMenuActivity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

import static com.guarda.ethereum.models.constants.Common.ETH_SHOW_PATTERN;

@AutoInjector(GuardaApp.class)
public class AmountToSendActivity extends AToolbarMenuActivity {

    @BindView(R.id.et_amount_to_send)
    EditText etAmountToSend;
    @BindView(R.id.tv_current_balance_title)
    TextView tvCurrentBalanceTitle;
    @BindView(R.id.tv_current_balance)
    TextView tvCurrentBalance;
    @BindView(R.id.btn_send)
    Button btnSend;
    @BindView(R.id.gi_input_layout)
    GuardaInputLayout inputLayout;
    @BindView(R.id.eth_hint)
    TextView tvHint;
    @BindView(R.id.btn_max)
    Button btnMax;

    private String walletNumber;
    private BigDecimal balance;
    private BigDecimal minAmount;
    private String tokenCode;

    @Inject
    WalletManager walletManager;

    @Inject
    EthereumNetworkManager mNetwonetworkManagerkManager;

    @Inject
    SharedManager sharedManager;

    @Inject
    RawNodeManager tokenManager;

    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        setToolBarTitle(getString(R.string.title_withdraw));
        etAmountToSend.setFilters(new InputFilter[]{new DigitsInputFilter(8, 4, Float.POSITIVE_INFINITY)});
        KeyboardManager.disableKeyboardByClickView(etAmountToSend);
        walletNumber = getIntent().getStringExtra(Extras.WALLET_NUMBER);
        tokenCode = getIntent().getStringExtra(Extras.TOKEN_CODE_EXTRA);
        minAmount = (BigDecimal) getIntent().getSerializableExtra(Extras.EXCHANGE_MINAMOUNT);

        inputLayout.setInputListener(new GuardaInputLayout.onGuardaInputLayoutListener() {
            @Override
            public void onTextChanged(String inputText) {
                etAmountToSend.setText(inputText);
                etAmountToSend.setSelection(etAmountToSend.getText().length());
            }
        });
        if (!tokenAvailable()) {
            setCurrentBalance("00.00", sharedManager.getCurrentCurrency());
            updateEthBalance();
        } else {
            tvHint.setText(tokenCode.toUpperCase());
            setCurrentBalance("00.00", tokenCode);
            updateTokenBalance();
        }

        etAmountToSend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideError(etAmountToSend);
                if (s.length() > 0) {
                    findViewById(R.id.eth_hint).setVisibility(View.VISIBLE);
//                    findViewById(R.id.btn_max).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.eth_hint).setVisibility(View.GONE);
//                    findViewById(R.id.btn_max).setVisibility(View.VISIBLE);
                }
                inputLayout.setCurrentText(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private boolean tokenAvailable(){
        return tokenCode != null && !TextUtils.isEmpty(tokenCode);
    }

    private void updateEthBalance() {
        btnSend.setEnabled(false);
        mNetwonetworkManagerkManager.getBalance(walletManager.getWalletFriendlyAddress(), new Callback<BigDecimal>() {
            @Override
            public void onResponse(BigDecimal response) {
                btnSend.setEnabled(true);
                balance = response;
                DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
                symbols.setDecimalSeparator('.');
                symbols.setGroupingSeparator(',');
                DecimalFormat decimalFormat = new DecimalFormat(ETH_SHOW_PATTERN, symbols);
                decimalFormat.setRoundingMode(RoundingMode.DOWN);
                if (balance != null && !balance.equals(new BigDecimal(0))) {
                    setCurrentBalance(decimalFormat.format(balance), sharedManager.getCurrentCurrency().toUpperCase());
                }
            }
        });
    }

    private void updateTokenBalance(){
        balance = tokenManager.getTokenByCode(tokenCode).getTokenNum();
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');
        DecimalFormat decimalFormat = new DecimalFormat(ETH_SHOW_PATTERN, symbols);
        decimalFormat.setRoundingMode(RoundingMode.DOWN);
        if (balance != null && !balance.equals(new BigDecimal(0))) {
            setCurrentBalance(decimalFormat.format(balance), tokenCode.toUpperCase());
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_amount_to_send;
    }

    private void setCurrentBalance(String balance, String currency) {
        tvCurrentBalanceTitle.setText(getString(R.string.withdraw_your_current_balance));
        tvCurrentBalance.setText(String.format("%s %s", balance, currency));
    }

    @OnClick(R.id.btn_send)
    public void sendClick(View view) {
        String amount = etAmountToSend.getText().toString();
        if (!amount.isEmpty()) {
                if (!isAmountMoreBalance(amount)) {
                    if (isAmountMoreMin(amount)) {
                        if (amount.equalsIgnoreCase("0.")) {
                            etAmountToSend.getText().append("0");
                        }
                        if (!tokenAvailable()) {
                            openSendingActivity();
                        } else {
                            openSendingTokensActivity();
                        }
                    } else {
                        showError(etAmountToSend, getString(R.string.coinify_min_amount));
                    }
                } else {
                    showError(etAmountToSend, getString(R.string.withdraw_amount_more_than_balance));
                }
        } else {
            showError(etAmountToSend, getString(R.string.withdraw_amount_can_not_be_empty));
        }
    }

    @OnClick(R.id.btn_max)
    public void maxAmount(View view) {
        if (balance != null && !balance.equals(new BigDecimal(0))) {
            DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
            symbols.setDecimalSeparator('.');
            symbols.setGroupingSeparator(',');
            DecimalFormat decimalFormat = new DecimalFormat(ETH_SHOW_PATTERN, symbols);
            decimalFormat.setRoundingMode(RoundingMode.FLOOR);
            String clearAmount = decimalFormat.format(balance).replace(",","");
            etAmountToSend.setText(clearAmount);
            inputLayout.setCurrentText(clearAmount);
            etAmountToSend.setSelection(etAmountToSend.getText().length());
        }
    }

    private void openSendingTokensActivity() {
        Intent intent = new Intent(this, SendingTokensActivity.class);
        intent.putExtra(Extras.WALLET_NUMBER, walletNumber);
        intent.putExtra(Extras.AMOUNT_TO_SEND, etAmountToSend.getText().toString());
        intent.putExtra(Extras.TOKEN_CODE_EXTRA, tokenCode);
        startActivity(intent);
    }

    private boolean isAmountMoreBalance(String amount) {
        amount = amount.replace(",","");
        BigDecimal amountDecimal = BigDecimal.ZERO;
        try {
            amountDecimal = new BigDecimal(amount);
        } catch (NumberFormatException nfe) {
            Crashlytics.log("Amount: " + amount + ", Balance: " + balance + ", NumberFormatException: " + nfe.getMessage());
            nfe.printStackTrace();
            Log.d("psd", "isAmountMoreBalance - " + nfe.getMessage());
        }
        return balance.compareTo(amountDecimal) < 0;
    }

    private boolean isAmountMoreMin(String amount) {
        if (minAmount == null) {
            return true;
        }
        amount = amount.replace(",","");
        BigDecimal amountDecimal = BigDecimal.ZERO;
        try {
            amountDecimal = new BigDecimal(amount);
        } catch (NumberFormatException nfe) {
            Crashlytics.log("Amount: " + amount + ", Balance: " + balance + ", NumberFormatException: " + nfe.getMessage());
            nfe.printStackTrace();
            Log.d("psd", "isAmountMoreMin - " + nfe.getMessage());
        }
        return minAmount.compareTo(amountDecimal) <= 0;
    }

    private void openSendingActivity() {
        Intent intent = new Intent(this, SendingCurrencyActivity.class);
        intent.putExtra(Extras.WALLET_NUMBER, walletNumber);
        intent.putExtra(Extras.AMOUNT_TO_SEND, etAmountToSend.getText().toString());
        startActivity(intent);
    }

}
