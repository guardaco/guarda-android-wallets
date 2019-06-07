package com.guarda.ethereum.rxcall;

import com.guarda.zcash.sapling.db.DbManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static com.guarda.ethereum.lifecycle.HistoryViewModel.INPUTS_HASHES;
import static com.guarda.ethereum.lifecycle.HistoryViewModel.OUTPUTS_HASHES;


public class CallNotesFromDb implements Callable<List<String>> {

    private DbManager dbManager;
    private int flag;

    public CallNotesFromDb(DbManager dbManager, int flag) {
        this.dbManager = dbManager;
        this.flag = flag;
    }

    @Override
    public List<String> call() throws Exception {
        if (flag == INPUTS_HASHES) return dbManager.getAppDb().getTxInputDao().getInputTxIds();
        if (flag == OUTPUTS_HASHES) return dbManager.getAppDb().getTxOutputDao().getOutputTxIds();
        return new ArrayList<>();
    }

}
