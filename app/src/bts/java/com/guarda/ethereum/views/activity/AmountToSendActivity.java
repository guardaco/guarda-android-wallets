package com.guarda.ethereum.views.activity;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.customviews.GuardaInputLayout;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.utils.DigitsInputFilter;
import com.guarda.ethereum.utils.KeyboardManager;
import com.guarda.ethereum.views.activity.base.AToolbarMenuActivity;

import java.math.BigDecimal;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

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
    @BindView(R.id.btn_max)
    Button btnMax;

    private String walletNumber;
    private BigDecimal balance;
    private BigDecimal minAmount;

    @Inject
    WalletManager walletManager;

    @Inject
    SharedManager sharedManager;

    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        setToolBarTitle(getString(R.string.title_withdraw));
        etAmountToSend.setFilters(new InputFilter[]{new DigitsInputFilter(8, 8, Float.POSITIVE_INFINITY)});
        KeyboardManager.disableKeyboardByClickView(etAmountToSend);
        walletNumber = getIntent().getStringExtra(Extras.WALLET_NUMBER);
        minAmount = (BigDecimal) getIntent().getSerializableExtra(Extras.EXCHANGE_MINAMOUNT);

        inputLayout.setInputListener(new GuardaInputLayout.onGuardaInputLayoutListener() {
            @Override
            public void onTextChanged(String inputText) {
                etAmountToSend.setText(inputText);
                etAmountToSend.setSelection(etAmountToSend.getText().length());
            }
        });
        setCurrentBalance("00.00", sharedManager.getCurrentCurrency());
        setCurrentBalance(WalletManager.getFriendlyBalance(walletManager.getMyBalance()), sharedManager.getCurrentCurrency().toUpperCase());

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
            if (Float.parseFloat(amount) > 0) {
                if (!isAmountMoreBalance(amount)) {
                    if (isAmountMoreMin(amount)) {
                        openSendingActivity();
                    } else {
                        showError(etAmountToSend, getString(R.string.coinify_min_amount));
                    }
                } else {
                    showError(etAmountToSend, getString(R.string.withdraw_amount_more_than_balance));
                }
            }
        } else {
            showError(etAmountToSend, getString(R.string.withdraw_amount_can_not_be_empty));
        }
    }

    @OnClick(R.id.btn_max)
    public void maxAmount(View view) {
        if (!walletManager.getMyBalance().isZero()) {
            etAmountToSend.setText(WalletManager.getFriendlyBalance(walletManager.getMyBalance()));
            inputLayout.setCurrentText(WalletManager.getFriendlyBalance(walletManager.getMyBalance()));
            etAmountToSend.setSelection(etAmountToSend.getText().length());
        }
    }

    private boolean isAmountMoreBalance(String amount) {
        return Float.valueOf(amount) >
                Float.valueOf(WalletManager.getFriendlyBalance(walletManager.getMyBalance()));
    }

    private boolean isAmountMoreMin(String amount) {
        if (minAmount == null) {
            return true;
        }
        return Float.valueOf(amount) >= minAmount.floatValue();
    }

    private void openSendingActivity() {
        Intent intent = new Intent(this, SendingCurrencyActivity.class);
        intent.putExtra(Extras.WALLET_NUMBER, walletNumber);
        intent.putExtra(Extras.AMOUNT_TO_SEND, etAmountToSend.getText().toString());
        startActivity(intent);
    }

}
