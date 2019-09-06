package com.guarda.zcash.sapling.rxcall;

import com.guarda.zcash.sapling.db.DbManager;
import java.util.concurrent.Callable;
import timber.log.Timber;


public class CallDropLastBlockRange implements Callable<Boolean> {

    private DbManager dbManager;

    public CallDropLastBlockRange(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public Boolean call() throws Exception {
        Timber.d("started");

        int lastCount = 100;
        dbManager.getAppDb().getBlockDao().dropLastNumber(lastCount);
        Timber.d("zecResync last %d dropped", lastCount);

        return true;
    }

}
