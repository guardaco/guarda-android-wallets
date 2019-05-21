package com.guarda.zcash.sapling.rxcall;

import com.guarda.zcash.sapling.db.DbManager;

import java.util.concurrent.Callable;

import timber.log.Timber;

public class CallSaplingBalance implements Callable<Long> {

    private DbManager dbManager;

    public CallSaplingBalance(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public Long call() throws Exception {
        Long saplingBalance = dbManager.getAppDb().getReceivedNotesDao().getBalance();
        Timber.d("saplingBalance = %d", saplingBalance);
        return saplingBalance;
    }
}
