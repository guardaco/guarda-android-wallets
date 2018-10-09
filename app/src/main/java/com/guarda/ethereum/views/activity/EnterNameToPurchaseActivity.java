package com.guarda.ethereum.views.activity;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import com.guarda.ethereum.R;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.utils.KeyboardManager;
import com.guarda.ethereum.views.activity.base.AToolbarActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class EnterNameToPurchaseActivity extends AToolbarActivity {

    @BindView(R.id.et_name)
    EditText etName;
    @BindView(R.id.btn_next)
    Button btNext;


    @Override
    protected void init(Bundle savedInstanceState) {
        setToolBarTitle(getString(R.string.toolbar_title_enter_full_name));

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideError(etName);

                if (isNameValid(s.toString())) {
                    etName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_confirm, 0);
                } else {
                    etName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_enter_name_to_purchase;
    }

    @Override
    protected void onResume() {
        super.onResume();
        KeyboardManager.setFocusAndOpenKeyboard(this, etName);
    }

    @OnClick(R.id.btn_next)
    public void nextClick() {
        String name = etName.getText().toString();
        if (isNameValid(name)) {
            Intent intent = new Intent(this, EnterEmailToPurchaseActivity.class);
            intent.putExtra(Extras.PURCHASE_SERVICE, getIntent().getStringExtra(Extras.PURCHASE_SERVICE));
            intent.putExtra(Extras.PURCHASE_COINS, getIntent().getStringExtra(Extras.PURCHASE_COINS));
            intent.putExtra(Extras.PURCHASE_CURR, getIntent().getStringExtra(Extras.PURCHASE_CURR));
            intent.putExtra(Extras.USER_FULL_NAME, name);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.no_slide);

        } else {
            showError(etName, getString(R.string.et_warning_name_is_empty));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_slide, R.anim.slide_in_right);
    }

    /**
     * valid name should contains two words (first name and second name)
     * and each words should contains min 2 character
     *
     * @param name
     * @return
     */
    private boolean isNameValid(String name) {
        if (name.contains(" ")) {
            String[] names = name.split(" ");
            if (names.length > 1) {
                if (names[0].length() > 1 && names[1].length() > 1) {
                    return true;
                }
            }
        }
        return false;
    }
}
