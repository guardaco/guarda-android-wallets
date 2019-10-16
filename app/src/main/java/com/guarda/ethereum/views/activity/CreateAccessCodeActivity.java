package com.guarda.ethereum.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.guarda.ethereum.R;
import com.guarda.ethereum.customviews.GuardaPinCodeLayout;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.views.activity.base.APinCodeActivity;

import butterknife.BindView;
import butterknife.OnClick;


public class CreateAccessCodeActivity extends APinCodeActivity {

    @BindView(R.id.gpl_pin_code)
    GuardaPinCodeLayout ilInputLayout;

    @Override
    protected void init(Bundle savedInstanceState) {
        setToolBarTitle("");

        ilInputLayout.setInputListener((String text) -> checkPinCode(text));
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_create_access_code;
    }

    void checkPinCode(String pinCode) {
        if (pinCode != null && pinCode.length() == 4) {
            Intent intent = new Intent(CreateAccessCodeActivity.this, AccessCodeAgainActivity.class);
            intent.putExtra(Extras.PIN_CODE, pinCode);
            startActivity(intent);
        }
    }
}
