package com.guarda.ethereum.views.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.customviews.RootDialog;
import com.guarda.ethereum.managers.EthereumNetworkManager;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.utils.Coders;
import com.guarda.ethereum.views.activity.base.SimpleTrackOnStopActivity;
import com.guarda.zcash.RustAPI;
import com.guarda.zcash.sapling.rxcall.CallLastBlock;
import com.guarda.zcash.sapling.rxcall.CallSaplingParamsInit;
import com.scottyab.rootbeer.RootBeer;

import java.util.Arrays;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.guarda.ethereum.models.constants.Extras.CREATE_WALLET;
import static com.guarda.ethereum.models.constants.Extras.DISABLE_CHECK;
import static com.guarda.ethereum.models.constants.Extras.FIRST_ACTION_MAIN_ACTIVITY;

@AutoInjector(GuardaApp.class)
public class AuthorizationTypeActivity extends SimpleTrackOnStopActivity {

    @Inject
    EthereumNetworkManager networkManager;
    @Inject
    WalletManager walletManager;
    @Inject
    SharedManager sharedManager;

    DialogFragment rootDialog;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);

        walletManager.clearWallet();
        sharedManager.setIsShowBackupAlert(true);

        saplingParamsInit();
    }

    @Override
    protected void onResume() {
        isRootDevice();

        Log.d("psd", "AuthorizationTypeActivity onResume");
        String block = sharedManager.getLastSyncedBlock();
        if (!block.isEmpty()) {
            Log.d("psd", "goToMainActivity(Coders.decodeBase64(block));");
            goToMainActivity(Coders.decodeBase64(block));
        }
        isUnblocked = false;
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    protected boolean isShouldToBlockScreen() {
        Log.d("psd", "isShouldToBlockScreen AuthorizationTypeActivity");
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
                createWallet(Common.BLOCK);
                break;
            case R.id.btn_login_from_backup:
                goToLoginFromBackup();
                break;
        }
    }

    private void goToCreateWallet() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(FIRST_ACTION_MAIN_ACTIVITY, CREATE_WALLET);
        intent.putExtra(DISABLE_CHECK, true);
        startActivity(intent);
    }

    private void goToLoginFromBackup() {
        Intent intent = new Intent(this, RestoreFromBackupActivity.class);
        intent.putExtra(FIRST_ACTION_MAIN_ACTIVITY, CREATE_WALLET);
        intent.putExtra(DISABLE_CHECK, true);
        startActivity(intent);
    }

    public void goToMainActivity(String backUpPhrase) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Extras.FIRST_ACTION_MAIN_ACTIVITY, Extras.RESTORE_WALLET);
        intent.putExtra(Extras.KEY, backUpPhrase);
        intent.putExtra(DISABLE_CHECK, true);
        startActivity(intent);
    }

    private void createWallet(String passphrase) {
        if (SharedManager.flag_create_new_wallet_screen) {
            Intent intent = new Intent(this, CreateNewWalletActivity.class);
            intent.putExtra(DISABLE_CHECK, true);
            startActivity(intent);
        } else {
            showProgress(getString(R.string.generating_wallet));
            walletManager.createWallet2(passphrase, new Runnable() {
                @Override
                public void run() {
                    closeProgress();
                    goToCreateWallet();
                }
            });
        }
    }

    private void saplingParamsInit() {
        compositeDisposable.add(Observable
                .fromCallable(new CallSaplingParamsInit(this))
                .subscribeOn(Schedulers.io())
                .subscribe((latest) -> Timber.d("CallSaplingParamsInit latest=%s", latest)));
    }

    private void isRootDevice() {
        try {
            RootBeer rb = new RootBeer(this);
            if (rb.detectRootCloakingApps()
                    || rb.checkForSuBinary()
                    || rb.checkForDangerousProps()
                    || rb.checkForRWPaths()
                    || rb.checkSuExists()
                    || rb.checkForRootNative()) {
                showRootDialog();
            }
        } catch (Exception e) {
            showRootDialog();
            e.printStackTrace();
        }
    }

    private void showRootDialog() {
        if (getSupportFragmentManager().findFragmentByTag(RootDialog.TAG) == null) {
            rootDialog = new RootDialog();
            rootDialog.show(getSupportFragmentManager(), RootDialog.TAG);
        }
    }
}
