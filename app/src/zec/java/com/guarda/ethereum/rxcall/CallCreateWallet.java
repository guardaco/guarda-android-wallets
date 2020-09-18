package com.guarda.ethereum.rxcall;

import com.guarda.ethereum.managers.WalletManager;
import com.guarda.zcash.sapling.api.ProtoApi;

import java.util.concurrent.Callable;


public class CallCreateWallet implements Callable<Boolean> {

    private WalletManager walletManager;
    private ProtoApi protoApi;

    public CallCreateWallet(WalletManager walletManager, ProtoApi protoApi) {
        this.walletManager = walletManager;
        this.protoApi = protoApi;
    }

    @Override
    public Boolean call() {
        long lastBlock = protoApi.getLastBlock();
        if (lastBlock != 0) {
            // save block height when wallet created
            walletManager.setCreateHeight(lastBlock);
            return walletManager.createWallet();
        } else {
            return false;
        }
    }

}
