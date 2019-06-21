package com.guarda.zcash.sapling.rxcall;

import com.guarda.zcash.globals.Optional;
import com.guarda.zcash.sapling.db.DbManager;

import java.util.concurrent.Callable;

import timber.log.Timber;


public class CallGetMemo implements Callable<Optional<String>> {

    private String hash;
    private DbManager dbManager;

    public CallGetMemo(String hash, DbManager dbManager) {
        this.hash = hash;
        this.dbManager = dbManager;
    }

    @Override
    public Optional<String> call() throws Exception {
        Timber.d("started");
        String memo = dbManager.getAppDb().getReceivedNotesDao().getMemoByHash(hash);
        Timber.d("memo=%s", memo);

        return new Optional<>(memo);
    }

}
