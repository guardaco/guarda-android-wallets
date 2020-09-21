package com.guarda.ethereum.rxcall;

import com.guarda.zcash.sapling.db.DbManager;

import java.util.concurrent.Callable;


public class CallCleanDbLogOut implements Callable<Boolean> {

    private DbManager dbManager;
    private boolean isDropBlocks;

    public CallCleanDbLogOut(DbManager dbManager, boolean isDropBlocks) {
        this.dbManager = dbManager;
        this.isDropBlocks = isDropBlocks;
    }

    @Override
    public Boolean call() {
        dbManager.getAppDb().getDetailsTxDao().dropAll();
        dbManager.getAppDb().getTxDetailsDao().dropAll();
        dbManager.getAppDb().getReceivedNotesDao().dropAll();
        dbManager.getAppDb().getSaplingWitnessesDao().dropAll();
        if (isDropBlocks) {
            dbManager.getAppDb().getBlockDao().dropAll();
        } else {
            dbManager.getAppDb().getBlockDao().dropAllTrees();
        }
        return true;
    }

}
