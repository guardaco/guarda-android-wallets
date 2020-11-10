package com.guarda.ethereum.views.activity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProviders;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.lifecycle.AuthorizationViewModel;
import com.guarda.ethereum.managers.EthereumNetworkManager;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.sapling.SyncManager;
import com.guarda.ethereum.sapling.api.ProtoApi;
import com.guarda.ethereum.utils.Coders;
import com.guarda.ethereum.views.activity.base.SimpleTrackOnStopActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

import static com.guarda.ethereum.models.constants.Extras.CREATE_WALLET;
import static com.guarda.ethereum.models.constants.Extras.DISABLE_CHECK;
import static com.guarda.ethereum.models.constants.Extras.FIRST_ACTION_MAIN_ACTIVITY;

public class AuthorizationTypeActivity extends SimpleTrackOnStopActivity {

    @BindView(R.id.btn_create_wallet) Button btn_create_wallet;

    @Inject
    EthereumNetworkManager networkManager;
    @Inject
    WalletManager walletManager;
    @Inject
    ProtoApi protoApi;
    @Inject
    SharedManager sharedManager;
    @Inject
    SyncManager syncManager;

    private AuthorizationViewModel authorizationViewModel;

    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);

        AuthorizationViewModel.Factory factory = new AuthorizationViewModel.Factory(walletManager, protoApi);
        authorizationViewModel = ViewModelProviders.of(this, factory).get(AuthorizationViewModel.class);

        walletManager.clearWallet();

        initSubscriptions();
    }

    @Override
    protected void onResume() {
        String block = sharedManager.getLastSyncedBlock();
        if (!block.isEmpty()) {
            toMainActivity(Coders.decodeBase64(block));
        }
        isUnblocked = false;
        super.onResume();
    }

    protected boolean isShouldToBlockScreen() {
        if (!sharedManager.getLastSyncedBlock().isEmpty()) {
            if (sharedManager.getIsPinCodeEnable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_authorization_type;
    }

    @OnClick({R.id.btn_create_wallet, R.id.btn_login_from_backup})
    public void typeAuthClick(View view) {
        switch (view.getId()) {
            case R.id.btn_create_wallet:
                createWallet();
                break;
            case R.id.btn_login_from_backup:
                toLoginFromBackup();
                break;
        }
    }

    private void toCreateWallet() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(FIRST_ACTION_MAIN_ACTIVITY, CREATE_WALLET);
        intent.putExtra(DISABLE_CHECK, true);
        startActivity(intent);
    }

    private void toLoginFromBackup() {
        Intent intent = new Intent(this, RestoreFromBackupActivity.class);
        intent.putExtra(FIRST_ACTION_MAIN_ACTIVITY, CREATE_WALLET);
        intent.putExtra(DISABLE_CHECK, true);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.no_slide);
    }

    public void toMainActivity(String backUpPhrase) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Extras.FIRST_ACTION_MAIN_ACTIVITY, Extras.RESTORE_WALLET);
        intent.putExtra(Extras.KEY, backUpPhrase);
        intent.putExtra(DISABLE_CHECK, true);
        startActivity(intent);
    }

    private void createWallet() {
        btn_create_wallet.setEnabled(false);
        showProgress(getString(R.string.generating_wallet));
        authorizationViewModel.createWallet();
    }

    private void initSubscriptions() {
        authorizationViewModel.getIsCreated().observe(this, isCreated -> {
            Timber.d("authorizationViewModel.getIsCreated() isCreated=%s", isCreated);
            closeProgress();
            btn_create_wallet.setEnabled(true);

            if (isCreated) {
                toCreateWallet();
            } else {
                Toast.makeText(this, "Can't create new wallet", Toast.LENGTH_LONG).show();
            }
        });
    }

}
