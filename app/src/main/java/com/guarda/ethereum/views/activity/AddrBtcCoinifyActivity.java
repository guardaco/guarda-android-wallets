package com.guarda.ethereum.views.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.customviews.RobotoLightEditText;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.constants.RequestCode;
import com.guarda.ethereum.views.activity.base.AToolbarActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

@AutoInjector(GuardaApp.class)
public class AddrBtcCoinifyActivity extends AToolbarActivity {

    @BindView(R.id.et_send_coins_address)
    RobotoLightEditText etSendCoinsAddress;
    @BindView(R.id.btn_scan_qr)
    ImageButton btScanQr;
    @BindView(R.id.btn_next)
    Button btNext;

    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        setToolBarTitle(getString(R.string.withdraw_address_send));

        initAddressField();
    }

    private void initAddressField() {
        etSendCoinsAddress.setOnPasteListener(new RobotoLightEditText.OnPasteTextListener() {
            @Override
            public void onPasteText(String text) {
                etSendCoinsAddress.setText(filterAddress(text));
            }
        });
    }

    private String filterAddress(String address) {
        return address.trim();
    }

    @OnClick({R.id.btn_scan_qr, R.id.btn_next})
    public void withdrawButtonsClick(View view) {
        switch (view.getId()) {
            case R.id.btn_scan_qr:
                Intent intent = new Intent(this, ScanQrCodeActivity.class);
                startActivityForResult(intent, RequestCode.QR_CODE_REQUEST_CODE);
                break;
            case R.id.btn_next:
                String address = etSendCoinsAddress.getText().toString();
                address = filterAddress(address);
                etSendCoinsAddress.setText(address);

                goNext(address);
                break;
        }
    }

    private void goNext(String address) {
        if (!address.isEmpty()) {
            if (isAlphaNumeric(address)) {
                toConfirmCoinifyActivity(address);
            } else {
                showError(etSendCoinsAddress, getString(R.string.withdraw_the_withdrawal_address_is_incorrect));
            }
        } else {
            showError(etSendCoinsAddress, getString(R.string.withdraw_address_not_valid));
        }
    }

    public boolean isAlphaNumeric(String s) {
        String pattern = "^[a-zA-Z0-9]*$";
        return s.matches(pattern);
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_coinify_btcaddr;
    }

    private void toConfirmCoinifyActivity(String address) {
        Intent intent = new Intent(this, ConfirmCoinifyActivity.class);
        intent.putExtra(Extras.COINIFY_IN_AMOUNT, getIntent().getFloatExtra(Extras.COINIFY_IN_AMOUNT, 0.0f));
        intent.putExtra(Extras.COINIFY_IN_AMOUNT_CUR, getIntent().getStringExtra(Extras.COINIFY_IN_AMOUNT_CUR));
        intent.putExtra(Extras.COINIFY_OUT_AMOUNT, getIntent().getStringExtra(Extras.COINIFY_OUT_AMOUNT));
        intent.putExtra(Extras.COINIFY_AMOUNT_FEE, getIntent().getStringExtra(Extras.COINIFY_AMOUNT_FEE));
        intent.putExtra(Extras.COINIFY_QUOTE_ID, getIntent().getIntExtra(Extras.COINIFY_QUOTE_ID, 0));
        intent.putExtra(Extras.COINIFY_PAY_METHOD, getIntent().getStringExtra(Extras.COINIFY_PAY_METHOD));
        intent.putExtra(Extras.COINIFY_BTC_ADDRESS, address);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCode.QR_CODE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra(Extras.QR_CODE_RESULT);
                if (!result.isEmpty()) {
                    String address = filterAddress(result);
                    etSendCoinsAddress.setText(address);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_slide, R.anim.slide_in_right);
    }

}
