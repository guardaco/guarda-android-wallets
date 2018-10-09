package com.guarda.ethereum.views.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.constants.RequestCode;
import com.guarda.ethereum.utils.Coders;
import com.guarda.ethereum.views.activity.base.AToolbarActivity;

import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

import static com.guarda.ethereum.models.constants.Extras.DISABLE_CHECK;

@AutoInjector(GuardaApp.class)
public class RestoreFromBackupActivity extends AToolbarActivity {

    @BindView(R.id.et_backup_phrase)
    EditText etBackupPhrase;
    @BindView(R.id.btn_restore)
    Button btnRestore;
    @BindView(R.id.imageViewScanQr)
    ImageView imageViewScanQr;

    @Inject
    WalletManager walletManager;

    @Inject
    SharedManager sharedManager;

    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        setFocusToPassPhrase();

        setToolBarTitle(R.string.title_restore_backup);
        etBackupPhrase.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideError(etBackupPhrase);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        imageViewScanQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanQr_onClick();
            }
        });
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_restore_from_backup;
    }

    @OnClick(R.id.btn_restore)
    public void restore(View btn) {
        if (!TextUtils.isEmpty(etBackupPhrase.getText().toString().trim())) {
            String clearKey = etBackupPhrase.getText().toString().trim();
            if (clearKey.length() > 4 && clearKey.substring(0, 4).equalsIgnoreCase("xprv")) {
                btnRestore.setEnabled(false);
                showProgress();
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        walletManager.restoreFromBlockByXPRV2(etBackupPhrase.getText().toString(), new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!"".equals(Coders.decodeBase64(sharedManager.getLastSyncedBlock()))) {
                                            goToMainActivity(etBackupPhrase.getText().toString());
                                            btnRestore.setEnabled(false);
                                        } else {
                                            showError(etBackupPhrase, getString(R.string.et_error_wrong_private_key));
                                            btnRestore.setEnabled(true);
                                        }
                                        closeProgress();
                                    }
                                });
                            }
                        });
                    }
                });
            } else if ((etBackupPhrase.getText().toString().trim().length() == 51 ||
                    etBackupPhrase.getText().toString().trim().length() == 52) &&
                    !etBackupPhrase.getText().toString().trim().contains(" ")) {
                btnRestore.setEnabled(false);
                showProgress();
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        walletManager.restoreFromBlockByWif2(etBackupPhrase.getText().toString(), new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!"".equals(Coders.decodeBase64(sharedManager.getLastSyncedBlock()))) {
                                            goToMainActivity(etBackupPhrase.getText().toString());
                                            btnRestore.setEnabled(false);
                                        } else {
                                            showError(etBackupPhrase, getString(R.string.et_error_wrong_private_key));
                                            btnRestore.setEnabled(true);
                                        }
                                        closeProgress();
                                    }
                                });
                            }
                        });
                    }
                });
            } else {
                if (walletManager.isValidPrivateKey(etBackupPhrase.getText().toString())) {
                    btnRestore.setEnabled(false);
                    showProgress();
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            walletManager.restoreFromBlock2(etBackupPhrase.getText().toString(), new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!"".equals(Coders.decodeBase64(sharedManager.getLastSyncedBlock()))) {
                                                goToMainActivity(etBackupPhrase.getText().toString());
                                                btnRestore.setEnabled(false);
                                            } else {
                                                showError(etBackupPhrase, getString(R.string.et_error_wrong_private_key));
                                                btnRestore.setEnabled(true);
                                            }
                                            closeProgress();
                                        }
                                    });
                                }
                            });
                        }
                    });
                } else {
                    showError(etBackupPhrase, getString(R.string.et_error_wrong_private_key));
                }
            }

        } else {
            showError(etBackupPhrase, getString(R.string.et_error_private_key_is_empty));
        }
    }

    public void setFocusToPassPhrase() {
        if (etBackupPhrase.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    public void goToMainActivity(String key) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Extras.FIRST_ACTION_MAIN_ACTIVITY, Extras.RESTORE_WALLET);
        intent.putExtra(Extras.KEY, key);
        intent.putExtra(DISABLE_CHECK, true);
        startActivity(intent);
        finish();
    }

    private void scanQr_onClick() {
        Intent intent = new Intent(this, ScanQrCodeActivity.class);
        startActivityForResult(intent, RequestCode.QR_CODE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RequestCode.QR_CODE_REQUEST_CODE:
                    String result = data.getStringExtra(Extras.QR_CODE_RESULT);
                    if (!result.isEmpty()) {
                        String address = filterAddress(result);
                        etBackupPhrase.setText(address);
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String filterAddress(String address) {
        Pattern pattern = Pattern.compile("\\w+:w+");
        Matcher matcher = pattern.matcher(address);
        while (matcher.find()) {
            String candidate = matcher.group();
            if (walletManager.isSimilarToAddress(candidate)){
                return candidate;
            }
        }
        return address;
    }

}
