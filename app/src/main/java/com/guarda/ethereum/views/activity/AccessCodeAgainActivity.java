package com.guarda.ethereum.views.activity;

import android.content.Intent;
import android.os.Bundle;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.customviews.GuardaPinCodeLayout;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.views.activity.base.APinCodeActivity;
import com.guarda.ethereum.utils.Coders;
import com.guarda.ethereum.managers.SharedManager;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;

@AutoInjector(GuardaApp.class)
public class AccessCodeAgainActivity extends APinCodeActivity {

    @BindView(R.id.gpl_pin_code)
    GuardaPinCodeLayout ilInputLayout;

    @Inject
    SharedManager sharedManager;

    private String firstPinCode;
    private String pinCode;

    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        setToolBarTitle("");

        firstPinCode = getIntent().getStringExtra(Extras.PIN_CODE);

        ilInputLayout.setInputListener(new GuardaPinCodeLayout.OnPinCodeListener() {
            @Override
            public void onTextChanged(String text) {
                pinCode = text;
                checkPinCode(pinCode);
            }
        });
    }

    private void checkPinCode(String pinCode) {
        if (pinCode.length() == 4) {
            if (firstPinCode.equals(pinCode)) {
                sharedManager.setIsPinCodeEnable(true);
                pinCode = Coders.getSha1Hex(pinCode);
                sharedManager.setPinCode(pinCode);

                sharedManager.setIsShowPinAfterCongrats(false);
                Intent intent = new Intent(this, CongratsActivity.class);
                intent.putExtra(Extras.CONGRATS_TEXT, getString(R.string.result_code_created));
                intent.putExtra(Extras.COME_FROM, Extras.FROM_PIN_CODE);
                startActivity(intent);
            } else {
                ilInputLayout.callError();
            }
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_access_code_again;
    }

}
