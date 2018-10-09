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

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.Callback;
import com.guarda.ethereum.managers.Callback2;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.WalletAPI;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.utils.Coders;
import com.guarda.ethereum.views.activity.base.AToolbarActivity;

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
            long passedSecs = WalletAPI.getSecsPassedAfterPrevWalletCreation();
            if ((passedSecs >= 5*60) || (passedSecs <= -60)) {
                showProgress();
                final String email = etWalletEmail.getText().toString();
                final String name = "u" + Coders.md5(email);
                final String newPrivateKey = WalletAPI.generateNewPrivateKey();
                final String newPublicKey = WalletAPI.publicKeyFromPrivateKey(newPrivateKey);
                Log.d("flint", "newEmail: " + email);
                Log.d("flint", "newName: " + name);
                //Log.d("flint", "newPrivateKey: " + newPrivateKey);
                Log.d("flint", "newPublicKey: " + newPublicKey);
                Log.d("flint", "isWalletExist(" + email + ")...");
                hideError(etWalletEmail);
                WalletAPI.isWalletExist(name, new Callback2<Boolean, Boolean>() {
                    @Override
                    public void onResponse(Boolean result, Boolean status) {
                        Log.d("flint", "isWalletExist... done! result=" + result + ", status=" + status);
                        if (status) {
                            if (result) {
                                showError(etWalletEmail, "Email is already in use");
                                closeProgress();
                            } else {
                                WalletAPI.registerAccount(name, newPublicKey, new Callback<Boolean>() {
                                    @Override
                                    public void onResponse(final Boolean response) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (response) {
                                                    walletManager.restoreFromBlock2(email, newPrivateKey, new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    if (!"".equals(Coders.decodeBase64(sharedManager.getLastSyncedBlock()))) {
                                                                        WalletAPI.saveNewWalletCreationLocalTime();
                                                                        goToMainActivity();
                                                                    } else {
                                                                        showError(etWalletEmail, "unable to login to new decent wallet");
                                                                    }
                                                                    closeProgress();
                                                                }
                                                            });
                                                        }
                                                    });
                                                } else {
                                                    showError(etWalletEmail, "unable to register new decent wallet");
                                                    closeProgress();
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        } else {
                            showError(etWalletEmail, "check your internet connection");
                            closeProgress();
                        }
                    }
                });
            } else {
                showError(etWalletEmail, "You has just created another wallet. Please wait.");
                Log.d("flint", "You has just created another wallet. Please wait. Passed " + WalletAPI.getSecsPassedAfterPrevWalletCreation() + " sec");
            }
        } catch (Exception e) {
            showError(etWalletEmail, "unable to create new decent wallet");
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
        intent.putExtra(Extras.FIRST_ACTION_MAIN_ACTIVITY, Extras.CREATE_WALLET);
        intent.putExtra(DISABLE_CHECK, true);
        startActivity(intent);
    }

}
