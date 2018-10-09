package com.guarda.ethereum.views.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.utils.KeyboardManager;
import com.guarda.ethereum.views.activity.EnterPhoneToPurchaseActivity;
import com.guarda.ethereum.views.activity.base.AToolbarActivity;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import java.util.regex.Matcher;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

@AutoInjector(GuardaApp.class)
public class EnterIndacoinEmailFragment extends BaseFragment {

    @BindView(R.id.et_email)
    EditText etEmail;
    @BindView(R.id.btn_next)
    Button btNext;

    private final int MIN_NAME_LENGTH = 5;

    private PurchaseServiceFragment prevFragment = null;
    public String selectedService = "wemovecoins";

    public void setPrevFragment(PurchaseServiceFragment prevFragment) {
        this.prevFragment = prevFragment;
    }

    @Override
    protected void init() {
        setToolbarTitle(getString(R.string.toolbar_title_enter_email));

        if (prevFragment == null) return;

        selectedService = prevFragment.selectedService;
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideError(etEmail);
                if (validateEmail(s.toString())) {
                    etEmail.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_confirm, 0);
                } else {
                    etEmail.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        initBackButton();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_enter_email_to_purchase;
    }

    @OnClick(R.id.btn_next)
    public void nextClick() {
        String email = etEmail.getText().toString();
        if (!email.isEmpty() && validateEmail(email)) {
            final EnterIndacoinEmailFragment thisFragment = this;
            PurchaseCoinsFragment startFragment = new PurchaseCoinsFragment();
            startFragment.setIndaPrevFragment(thisFragment);
            Bundle bundle = new Bundle();
            bundle.putString(Extras.USER_EMAIL, email);
            startFragment.setArguments(bundle);
//            b.putString(Extras.PURCHASE_SERVICE, prevFragment.selectedService);
//                startFragment.setPrevFragment(thisFragment);
            navigateToFragment(startFragment);
        } else {
            showError(etEmail, getString(R.string.email_is_not_valid));
        }
    }


    @Override
    public boolean onBackPressed() {
        if (prevFragment != null) {
            navigateToFragment(prevFragment);
            return true;
        }
        return false;
    }

    @Override
    public boolean onHomePressed() {
        return onBackPressed();
    }

    private boolean validateEmail(String text) {
        Matcher matcher = Patterns.EMAIL_ADDRESS.matcher(text);
        return matcher.matches();
    }
}
