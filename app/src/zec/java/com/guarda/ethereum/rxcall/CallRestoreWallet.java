package com.guarda.ethereum.rxcall;

import com.guarda.ethereum.managers.WalletManager;
import com.guarda.zcash.sapling.db.DbManager;

import java.util.concurrent.Callable;

import timber.log.Timber;


public class CallRestoreWallet implements Callable<Boolean> {

    private WalletManager walletManager;
    private String key;

    public CallRestoreWallet(WalletManager walletManager, String key) {
        this.walletManager = walletManager;
        this.key = key;
    }

    @Override
    public Boolean call() throws Exception {

        walletManager.restoreFromBlock(key, () -> Timber.d("empty callback"));

        return true;
    }

}
