package com.guarda.ethereum.views.activity;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.guarda.ethereum.R;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.utils.KeyboardManager;
import com.guarda.ethereum.views.activity.base.AToolbarActivity;
import com.guarda.ethereum.views.fragments.PurchaseCoinsFragment;

import java.util.regex.Matcher;

import butterknife.BindView;
import butterknife.OnClick;

public class EnterEmailToPurchaseActivity extends AToolbarActivity {

    @BindView(R.id.et_email)
    EditText etEmail;
    @BindView(R.id.btn_next)
    Button btNext;

    private final int MIN_NAME_LENGTH = 5;

    @Override
    protected void init(Bundle savedInstanceState) {
        setToolBarTitle(getString(R.string.toolbar_title_enter_email));

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
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_enter_email_to_purchase;
    }

    @Override
    protected void onResume() {
        super.onResume();
        KeyboardManager.setFocusAndOpenKeyboard(this, etEmail);
    }

    @OnClick(R.id.btn_next)
    public void nextClick() {
        String email = etEmail.getText().toString();
        if (!email.isEmpty() && validateEmail(email)) {
            if ("wemovecoins".equals(getIntent().getStringExtra(Extras.PURCHASE_SERVICE))) {
                Intent intent = new Intent(this, EnterPhoneToPurchaseActivity.class);
                intent.putExtra(Extras.PURCHASE_SERVICE, getIntent().getStringExtra(Extras.PURCHASE_SERVICE));
                intent.putExtra(Extras.PURCHASE_COINS, getIntent().getStringExtra(Extras.PURCHASE_COINS));
                intent.putExtra(Extras.USER_FULL_NAME, getIntent().getStringExtra(Extras.USER_FULL_NAME));
                intent.putExtra(Extras.PURCHASE_CURR, getIntent().getStringExtra(Extras.PURCHASE_CURR));
                intent.putExtra(Extras.USER_EMAIL, email);
                startActivity(intent);
            } else {
                PurchaseCoinsFragment startFragment = new PurchaseCoinsFragment();
                Bundle b = new Bundle();
                b.putString(Extras.USER_EMAIL, email);
                b.putString(Extras.PURCHASE_SERVICE, getIntent().getStringExtra(Extras.PURCHASE_SERVICE));
//                startFragment.setPrevFragment(thisFragment);
                navigateToFragment(startFragment);
            }
            overridePendingTransition(R.anim.slide_in_left, R.anim.no_slide);
        } else {
            showError(etEmail, getString(R.string.email_is_not_valid));
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_slide, R.anim.slide_in_right);
    }

    private boolean validateEmail(String text) {
        Matcher matcher = Patterns.EMAIL_ADDRESS.matcher(text);
        return matcher.matches();
    }

    protected void navigateToFragment(Fragment fragment) {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_up, R.anim.fade_out_animation, R.anim.fade_out_animation, R.anim.slide_down);
        fragmentTransaction.add(fragment, PurchaseCoinsFragment.class.getSimpleName());
        fragmentTransaction.commit();
    }
}
