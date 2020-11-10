package com.guarda.ethereum.sapling.rxcall;

import com.guarda.ethereum.sapling.db.DbManager;

import java.util.concurrent.Callable;

import timber.log.Timber;


public class CallRevertLastBlock implements Callable<Boolean> {

    private DbManager dbManager;

    public CallRevertLastBlock(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public Boolean call() throws Exception {
        Timber.d("started");
        dbManager.getAppDb().getBlockDao().deleteLastWithThree();
        return true;
    }

}
