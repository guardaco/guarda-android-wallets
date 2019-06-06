package com.guarda.ethereum.rxcall;

import com.guarda.zcash.sapling.db.DbManager;

import java.util.concurrent.Callable;


public class CallCleanDbLogOut implements Callable<Boolean> {

    private DbManager dbManager;

    public CallCleanDbLogOut(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public Boolean call() throws Exception {
        dbManager.getAppDb().getDetailsTxDao().dropAll();
        dbManager.getAppDb().getReceivedNotesDao().dropAll();
        dbManager.getAppDb().getSaplingWitnessesDao().dropAll();
        return true;
    }

}
