package com.guarda.ethereum.rxcall;

import com.guarda.ethereum.sapling.db.DbManager;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;


public class CallNotesFromDb implements Callable<Set<String>> {

    private DbManager dbManager;

    public CallNotesFromDb(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public Set<String> call() {
        Set<String> inouts = new HashSet<>();
        inouts.addAll(dbManager.getAppDb().getTxInputDao().getInputTxIds());
        inouts.addAll(dbManager.getAppDb().getTxOutputDao().getOutputTxIds());

        return inouts;
    }

}
