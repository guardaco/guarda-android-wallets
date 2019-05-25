package com.guarda.zcash.sapling.rxcall;

import android.content.Context;

import com.guarda.ethereum.managers.WalletManager;
import com.guarda.zcash.RustAPI;
import com.guarda.zcash.ZCashTransaction_zaddr;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.db.model.ReceivedNotesRoom;
import com.guarda.zcash.sapling.key.SaplingCustomFullKey;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import timber.log.Timber;


public class CallSaplingParamsInit implements Callable<Boolean> {

    private Context context;
    private WalletManager walletManager;

    public CallSaplingParamsInit(Context context, WalletManager walletManager) {
        this.context = context;
        this.walletManager = walletManager;
    }

    @Override
    public Boolean call() throws Exception {
        Timber.d("started");
        RustAPI.checkInit(context);
        walletManager.setSaplingCustomFullKey(new SaplingCustomFullKey(RustAPI.dPart(walletManager.getPrivateKey().getBytes())));
        return true;
    }

}
