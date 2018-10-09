package com.guarda.ethereum.views.activity;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.guarda.ethereum.R;
import com.guarda.ethereum.customviews.GuardaInputLayout;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.utils.KeyboardManager;
import com.guarda.ethereum.views.activity.base.AToolbarActivity;
import com.hbb20.CountryCodePicker;

import butterknife.BindView;
import butterknife.OnClick;

public class EnterPhoneToPurchaseActivity extends AToolbarActivity {

    private String phoneCode;

    @BindView(R.id.et_phone_number)
    EditText etPhoneNumber;
    @BindView(R.id.gi_country_phone)
    GuardaInputLayout inputLayoutPhone;
    @BindView(R.id.btn_next)
    Button btNext;
    @BindView(R.id.ccp_phone_code)
    CountryCodePicker cppCountry;


    @Override
    protected void init(Bundle savedInstanceState) {
        setToolBarTitle(getString(R.string.toolbar_title_enter_phone));

        KeyboardManager.disableKeyboardByClickView(etPhoneNumber);

        etPhoneNumber.requestFocus();
        etPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etPhoneNumber.setSelection(count);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

//        String countryISOCode = "";
//
//        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//        if (tm != null) {
//            if (tm.getNetworkCountryIso() != null && !tm.getNetworkCountryIso().isEmpty()) {
//                countryISOCode = tm.getNetworkCountryIso();
//            } else if (tm.getSimCountryIso() != null && !tm.getSimCountryIso().isEmpty()) {
//                countryISOCode = tm.getSimCountryIso();
//            } else{
//                countryISOCode = this.getResources().getConfiguration().locale.getCountry().toLowerCase();
//            }
//        }

////        countryISOCode = "ru";
//        cppCountry.setCountryForNameCode(countryISOCode);

        phoneCode = cppCountry.getSelectedCountryCode();

        cppCountry.registerCarrierNumberEditText(etPhoneNumber);
        inputLayoutPhone.setInputListener(new GuardaInputLayout.onGuardaInputLayoutListener() {
            @Override
            public void onTextChanged(String inputText) {
                etPhoneNumber.setText(inputText);
            }
        });

        cppCountry.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                phoneCode = cppCountry.getSelectedCountryCode();
            }
        });

    }

    @Override
    protected int getLayout() {
        return R.layout.activity_enter_phone_to_purchase;
    }

    @OnClick(R.id.btn_next)
    public void onNextBtnClick() {
        String phoneNumber = etPhoneNumber.getText().toString();

        if (cppCountry.isValidFullNumber()) {
            Intent intent = new Intent(this, PurchaseWemovecoinsActivity.class);
            intent.putExtra(Extras.PURCHASE_SERVICE, getIntent().getStringExtra(Extras.PURCHASE_SERVICE));
            intent.putExtra(Extras.PURCHASE_COINS, getIntent().getStringExtra(Extras.PURCHASE_COINS));
            intent.putExtra(Extras.USER_FULL_NAME, getIntent().getStringExtra(Extras.USER_FULL_NAME));
            intent.putExtra(Extras.PURCHASE_CURR, getIntent().getStringExtra(Extras.PURCHASE_CURR));
            intent.putExtra(Extras.USER_EMAIL, getIntent().getStringExtra(Extras.USER_EMAIL));
            intent.putExtra(Extras.COUNTRY_PHONE_CODE, phoneCode);
            intent.putExtra(Extras.USER_PHONE, phoneNumber);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.no_slide);
        } else {
            Toast.makeText(this, R.string.phone_is_not_valid, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_slide, R.anim.slide_in_right);
    }
}
