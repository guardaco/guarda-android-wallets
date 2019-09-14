package com.guarda.ethereum.rxcall;

import com.guarda.ethereum.managers.WalletManager;

import java.util.concurrent.Callable;


public class CallCreateWallet implements Callable<Boolean> {

    private WalletManager walletManager;

    public CallCreateWallet(WalletManager walletManager) {
        this.walletManager = walletManager;
    }

    @Override
    public Boolean call() throws Exception {
        walletManager.createWallet();
        return true;
    }

}
