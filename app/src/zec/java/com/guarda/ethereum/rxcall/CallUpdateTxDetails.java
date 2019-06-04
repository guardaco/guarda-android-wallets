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
        dbManager
                .getAppDb()
                .getDetailsTxDao()
                .insertAll(new DetailsTxRoom(
                        tr.getHash(),
                        tr.getTime().longValue(),
                        0L,
                        true,
                        tr.getConfirmations().longValue(),
                        "",
                        "",
                        false));

        return true;
    }

}
