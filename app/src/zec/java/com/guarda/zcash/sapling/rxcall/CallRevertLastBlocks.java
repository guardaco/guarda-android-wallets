package com.guarda.zcash.sapling.rxcall;

import com.guarda.zcash.sapling.db.DbManager;

import java.util.concurrent.Callable;

import timber.log.Timber;


public class CallRevertLastBlocks implements Callable<Boolean> {

    private static final int DROP_LAST_BLOCK_NUMBER = 10;

    private DbManager dbManager;

    public CallRevertLastBlocks(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public Boolean call() throws Exception {
        Timber.d("started");
        dbManager.getAppDb().getBlockDao().dropLastNumber(DROP_LAST_BLOCK_NUMBER);
        Timber.d("completed");
        return true;
    }

}
