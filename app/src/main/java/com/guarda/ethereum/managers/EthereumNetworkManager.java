package com.guarda.ethereum.managers;

public class EthereumNetworkManager {

    public EthereumNetworkManager(WalletManager walletManager) {

    }

    private interface ConnectionCallback {
        void onFinish();
    }


}
