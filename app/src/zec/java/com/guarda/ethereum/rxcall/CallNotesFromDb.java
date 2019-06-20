package com.guarda.ethereum.rxcall;

import com.guarda.zcash.sapling.db.DbManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static com.guarda.ethereum.lifecycle.HistoryViewModel.INPUTS_HASHES;
import static com.guarda.ethereum.lifecycle.HistoryViewModel.OUTPUTS_HASHES;


public class CallNotesFromDb implements Callable<List<String>> {

    private DbManager dbManager;

    public CallNotesFromDb(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public List<String> call() throws Exception {
        List<String> inouts = new ArrayList<>();
        inouts.addAll(dbManager.getAppDb().getTxInputDao().getInputTxIds());
        inouts.addAll(dbManager.getAppDb().getTxOutputDao().getOutputTxIds());

        return inouts;
    }

}
