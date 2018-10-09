package com.guarda.ethereum.managers;

import org.web3j.crypto.WalletFile;

/**
 *
 * Created by SV on 02.08.2017.
 */

public interface WalletCreationCallback {
    void onWalletCreated(WalletFile walletFile);
}
