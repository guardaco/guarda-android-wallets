package com.guarda.ethereum.views.activity;

import android.os.Bundle;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.views.activity.base.AToolbarActivity;

import javax.inject.Inject;

import autodagger.AutoInjector;

@AutoInjector(GuardaApp.class)
public class CreateNewWalletActivity extends AToolbarActivity {

    @Inject
    WalletManager walletManager;

    @Inject
    SharedManager sharedManager;

    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        setToolBarTitle(R.string.title_restore_backup);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_restore_from_backup;
    }
}
