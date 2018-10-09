package com.guarda.ethereum.views.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.constants.RequestCode;
import com.guarda.ethereum.utils.Coders;
import com.guarda.ethereum.utils.KeyUtils;
import com.guarda.ethereum.views.activity.base.AToolbarActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

import static com.guarda.ethereum.models.constants.Extras.DISABLE_CHECK;

@AutoInjector(GuardaApp.class)
public class RestoreFromBackupActivity extends AToolbarActivity {

    @BindView(R.id.restore_title)
    TextView restoreTitle;
    @BindView(R.id.spinnerKey)
    Spinner spinnerKey;
    @BindView(R.id.rl_backup_phrase)
    RelativeLayout rlBackupPhrase;
    @BindView(R.id.et_backup_phrase)
    EditText etBackupPhrase;
    @BindView(R.id.imageViewScanQr)
    ImageView imageViewScanQr;
    @BindView(R.id.btn_restore)
    Button btnRestore;
    @BindView(R.id.rl_json_up)
    RelativeLayout rlJsonUp;
    @BindView(R.id.btn_upload_json)
    Button btnUploadJson;
    @BindView(R.id.rl_json_path)
    RelativeLayout rlJsonPath;
    @BindView(R.id.tv_json_path)
    TextView tvJsonPath;
    @BindView(R.id.tl_json_pwd)
    TextInputLayout tlJsonPwd;
    @BindView(R.id.et_json_pwd)
    EditText etJsonPwd;

    @Inject
    WalletManager walletManager;

    @Inject
    SharedManager sharedManager;

    Uri selectedFilePath;

    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        setFocusToPassPhrase();

        setToolBarTitle(R.string.title_restore_backup);
        restoreTitle.setVisibility(View.GONE);
        spinnerKey.setVisibility(View.VISIBLE);
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

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.restore_way, R.layout.spinner_restore_item);
        adapter.setDropDownViewResource(R.layout.spinner_restore_item);
        spinnerKey.setAdapter(adapter);
        spinnerKey.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        rlJsonUp.setVisibility(View.GONE);
                        rlJsonPath.setVisibility(View.GONE);
                        tlJsonPwd.setVisibility(View.GONE);
                        rlBackupPhrase.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        rlBackupPhrase.setVisibility(View.GONE);
                        rlJsonUp.setVisibility(View.VISIBLE);
                        tlJsonPwd.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_restore_from_backup;
    }

    @Override
    protected void onPause() {
        etBackupPhrase.getText().clear();
        etJsonPwd.getText().clear();
        super.onPause();
    }

    @OnClick(R.id.btn_upload_json)
    public void uploadJson(View v) {
        Intent intent = new Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select a .json file"), RequestCode.SELECT_JSON_FILE);
    }

    @OnClick(R.id.btn_restore)
    public void restore(View btn) {
        switch (spinnerKey.getSelectedItemPosition()) {
            case 0:
                if (!TextUtils.isEmpty(etBackupPhrase.getText().toString().trim())) {
                    if (KeyUtils.isValidPrivateKey(etBackupPhrase.getText().toString().trim())) {
                        btnRestore.setEnabled(false);
                        showProgress();
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                walletManager.restoreFromBlock2(etBackupPhrase.getText().toString().trim(), new Runnable() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (!"".equals(Coders.decodeBase64(sharedManager.getLastSyncedBlock()))) {
                                                    goToMainActivity();
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
                } else {
                    showError(etBackupPhrase, getString(R.string.et_error_private_key_is_empty));
                }
                break;
            case 1:
                if (selectedFilePath != null) {
                    if (!etJsonPwd.getText().toString().isEmpty()) {
                        btnRestore.setEnabled(false);
                        showProgress();
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                walletManager.restoreFromBlock2Json(selectedFilePath, etJsonPwd.getText().toString(), new Runnable() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if ("JsonParseException".equals(sharedManager.getJsonExcep())) {
                                                    showCustomToast(getString(R.string.restore_backup_wrong_rest_file), R.drawable.err_json_doc);
                                                    btnRestore.setEnabled(true);
                                                } else if ("CipherException".equals(sharedManager.getJsonExcep())) {
                                                    showCustomToast(getString(R.string.restore_backup_wrong_rest_file), R.drawable.err_json_doc);
                                                    btnRestore.setEnabled(true);
                                                } else if ("WrongPassword".equals(sharedManager.getJsonExcep())) {
                                                    showCustomToast(getString(R.string.restore_backup_wrong_json_password), R.drawable.err_json_doc);
                                                    btnRestore.setEnabled(true);
                                                } else if (!"".equals(Coders.decodeBase64(sharedManager.getLastSyncedBlock()))) {
                                                    goToMainActivity();
                                                    btnRestore.setEnabled(false);
                                                }
                                                closeProgress();
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    } else {
                        showCustomToast(getString(R.string.restore_backup_json_password_empty), R.drawable.err_json_doc);
                    }
                }
                break;
        }
    }

    @OnClick(R.id.btn_json_clear)
    public void clearJsonPath(View v) {
        selectedFilePath = null;
        tvJsonPath.setText("");
        rlJsonPath.setVisibility(View.GONE);
        tlJsonPwd.setVisibility(View.GONE);
        rlJsonUp.setVisibility(View.VISIBLE);
    }

    public void setFocusToPassPhrase() {
        if (etBackupPhrase.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    public void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Extras.FIRST_ACTION_MAIN_ACTIVITY, Extras.RESTORE_WALLET);
        intent.putExtra(Extras.KEY, etBackupPhrase.getText().toString());
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
                case RequestCode.SELECT_JSON_FILE:
                    selectedFilePath = data.getData();
                    rlJsonUp.setVisibility(View.GONE);
                    rlJsonPath.setVisibility(View.VISIBLE);
                    tlJsonPwd.setVisibility(View.VISIBLE);
                    if (selectedFilePath != null) {
                        btnRestore.setEnabled(true);
                        String fileName = URLUtil.guessFileName(selectedFilePath.getLastPathSegment(), null, null);
                        tvJsonPath.setText(fileName);
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.restore_backup_wrong_file_name), Toast.LENGTH_LONG).show();
                    }
                    break;
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
