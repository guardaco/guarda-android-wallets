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
//        dbManager.getAppDb().getDetailsTxDao().insertList(new DetailsTxRoom(tr.getHash(), tr.getTime(), 0L, tr.getConfirmations()));
//        DetailsTxRoom(@NonNull String hash, Long time, Long sum, Boolean isReceived, Long confirmations, String from, String to, Boolean isOut) {

        return true;
    }

}
