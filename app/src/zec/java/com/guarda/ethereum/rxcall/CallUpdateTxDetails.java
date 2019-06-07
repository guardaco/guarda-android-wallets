package com.guarda.ethereum.rxcall;

import com.guarda.ethereum.models.items.ZecTxResponse;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.db.model.DetailsTxRoom;

import java.util.concurrent.Callable;


public class CallUpdateTxDetails implements Callable<Boolean> {

    private DbManager dbManager;
    private ZecTxResponse tr;

    public CallUpdateTxDetails(DbManager dbManager, ZecTxResponse tr) {
        this.dbManager = dbManager;
        this.tr = tr;
    }

    @Override
    public Boolean call() throws Exception {
        Long value = dbManager.getAppDb().getReceivedNotesDao().getValueByTxHashOuts(tr.getHash());
        if (value == null) {
            value = dbManager.getAppDb().getReceivedNotesDao().getValueByTxHashInputs(tr.getHash());
        }
        if (value == null) value = 0L;

        dbManager
                .getAppDb()
                .getDetailsTxDao()
                .insertAll(new DetailsTxRoom(
                        tr.getHash(),
                        tr.getTime().longValue(),
                        value,
                        true,
                        tr.getConfirmations().longValue(),
                        "",
                        "",
                        false));

        return true;
    }

}
