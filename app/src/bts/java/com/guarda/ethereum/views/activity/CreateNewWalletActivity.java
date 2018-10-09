package com.guarda.ethereum.views.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.bitshares.bitshareswallet.wallet.common.ErrorCode;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.BtsManager;
import com.guarda.ethereum.managers.Callback;
import com.guarda.ethereum.managers.Callback2;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.utils.Coders;
import com.guarda.ethereum.views.activity.base.AToolbarActivity;
import com.mrd.bitlib.crypto.PublicKey;
import com.bitshares.bitshareswallet.wallet.private_key;

import java.security.SecureRandom;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

import static com.guarda.ethereum.models.constants.Extras.DISABLE_CHECK;

@AutoInjector(GuardaApp.class)
public class CreateNewWalletActivity extends AToolbarActivity {

    @BindView(R.id.et_wallet_email)
    EditText etWalletEmail;

    @BindView(R.id.btn_create)
    Button btnCreate;

    @Inject
    WalletManager walletManager;

    @Inject
    SharedManager sharedManager;

    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        setFocusToEmail();

        setToolBarTitle(R.string.start_create_wallet);
        etWalletEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideError(etWalletEmail);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_create_new_wallet;
    }

    @OnClick(R.id.btn_create)
    public void create(View btn) {
        try {
            final String name = etWalletEmail.getText().toString();
            final String pswd = BtsManager.getInstance().generateRandomPassword();
            BtsManager.getInstance().createNewWallet(name, pswd, new Callback<Integer>() {
                @Override
                public void onResponse(final Integer res) {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (res == 0) {
                                    walletManager.restoreFromBlock2(name, pswd, new Runnable() {
                                        @Override
                                        public void run() {
                                            goToMainActivity();
                                        }
                                    });
                                } else if (res == ErrorCode.ERROR_ACCOUNT_OBJECT_EXIST) {
                                    showError(etWalletEmail, "account " + name + " is already registered");
                                } else {
                                    showError(etWalletEmail, "unable to create new bts wallet");
                                    Log.e("flint", "unable to create new bts wallet, error: " + res);
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            showError(etWalletEmail, "unable to create new bts wallet");
            e.printStackTrace();
            closeProgress();
        }
    }

    public void setFocusToEmail() {
        if (etWalletEmail.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    public void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Extras.FIRST_ACTION_MAIN_ACTIVITY, Extras.RESTORE_WALLET);
        intent.putExtra(DISABLE_CHECK, true);
        startActivity(intent);
    }

}
