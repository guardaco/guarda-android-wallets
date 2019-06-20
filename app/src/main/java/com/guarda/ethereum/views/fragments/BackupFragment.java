package com.guarda.ethereum.views.fragments;


import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.guarda.ethereum.BuildConfig;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.KeysSpinnerRowModel;
import com.guarda.ethereum.utils.ClipboardUtils;
import com.guarda.ethereum.views.adapters.KeysSpinnerAdapter;
import com.guarda.ethereum.views.fragments.base.BaseFragment;
import com.guarda.zcash.RustAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

@AutoInjector(GuardaApp.class)
public class BackupFragment extends BaseFragment {

    @BindView(R.id.btn_show_pass_phrase)
    Button btnShowPassPhrase;
    @BindView(R.id.btn_copy_pass_phrase)
    Button btnCopyPassPhrase;
    @BindView(R.id.tv_pass_phrase)
    TextView tvPassPhrase;
    @BindView(R.id.spinnerKeys)
    Spinner spinnerKeys;


    @Inject
    WalletManager walletManager;

    public BackupFragment() {
        GuardaApp.getAppComponent().inject(this);
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_backup;
    }

    @Override
    protected void init() {
        spinnerKeys.setAdapter(new KeysSpinnerAdapter(this.getContext(), createKeysSpinnerRows()));
        if (spinnerKeys.getCount() == 1) {
            spinnerKeys.setEnabled(false);
        }
        spinnerKeys.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String keyName = ((KeysSpinnerRowModel) spinnerKeys.getItemAtPosition(spinnerKeys.getSelectedItemPosition())).name;
                setKeys(keyName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void setKeys(String name) {
        switch (name) {
            case "main":
                tvPassPhrase.setText(getPrivateKeyForUser());
                break;
            case "xprv":
                tvPassPhrase.setText(walletManager.getXPRV());
                break;
            case "wif":
                tvPassPhrase.setText(walletManager.getWifKey());
                break;
            case "zec":
                String spendingKey = RustAPI.getExtsk(walletManager.getPrivateKey().getBytes());
                tvPassPhrase.setText(spendingKey);
                break;
        }
    }

    private String getPrivateKeyForUser() {
        try {
            String privKey = walletManager.getPrivateKey();
            if (SharedManager.flag_etc_eth_private_key_showing_fix) {
                while (privKey.length() < 64) {
                    privKey = "0" + privKey;
                }
            }
            return privKey;
            } catch (Exception e) {
                return "";
            }
    }

    private void generatePassPhrase() {
        setPassPhrase(getPrivateKeyForUser());
    }

    private void setPassPhrase(String passPhrase) {
        if (passPhrase == null)
            return;

        if (!passPhrase.isEmpty()) {
            tvPassPhrase.setText(passPhrase);
        } else if (passPhrase.isEmpty() && !walletManager.getXPRV().isEmpty()) {
            tvPassPhrase.setTextSize(10);
            tvPassPhrase.setText(walletManager.getXPRV());
        } else if (passPhrase.isEmpty() && !walletManager.getWifKey().isEmpty()) {
            tvPassPhrase.setText(walletManager.getWifKey());
        }
    }

    @OnClick(R.id.btn_show_pass_phrase)
    public void showPassPhrase(View view) {
        if (btnCopyPassPhrase.getVisibility() == View.VISIBLE) {
            btnCopyPassPhrase.setVisibility(View.GONE);
            tvPassPhrase.setVisibility(View.GONE);
            btnShowPassPhrase.setText(R.string.app_show);
        } else {
            btnCopyPassPhrase.setVisibility(View.VISIBLE);
            tvPassPhrase.setVisibility(View.VISIBLE);
            btnShowPassPhrase.setText(R.string.app_hide);
        }

    }

    @OnClick(R.id.btn_copy_pass_phrase)
    public void copyPassPhrase(View view) {
        ClipboardUtils.copyToClipBoard(getContext(), tvPassPhrase.getText().toString());
    }

    private List<KeysSpinnerRowModel> createKeysSpinnerRows() {
        List<KeysSpinnerRowModel> listTitles = new ArrayList<>();
        String prKey = getPrivateKeyForUser();
        String xprv = walletManager.getXPRV();
        String wif = walletManager.getWifKey();

        if (prKey != null) {
            if (!prKey.isEmpty()) {
                if (prKey.contains(" ")) {
                    listTitles.add(new KeysSpinnerRowModel("main", "Mnemonic phrase"));
                } else {
                    listTitles.add(new KeysSpinnerRowModel("main", "Private key"));
                }
            }
        }

        if (xprv != null) {
            if (!xprv.isEmpty() && !xprv.equals("NOT_IMPLEMENTED")) {
                listTitles.add(new KeysSpinnerRowModel("xprv", "BIP32 Ext. Private Key (m/0')"));
            }
        }

        if (xprv != null) {
            if (!wif.isEmpty() && !wif.equals("NOT_IMPLEMENTED")) {
                listTitles.add(new KeysSpinnerRowModel("wif", "WIF"));
            }
        }

        if (BuildConfig.FLAVOR == "zec") {
            listTitles.add(new KeysSpinnerRowModel("zec", "Spending key"));
        }

        return listTitles;
    }
}
