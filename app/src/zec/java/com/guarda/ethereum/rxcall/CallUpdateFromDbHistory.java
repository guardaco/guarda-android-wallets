package com.guarda.ethereum.rxcall;

import com.guarda.ethereum.models.items.TransactionItem;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.db.model.DetailsTxRoom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


public class CallUpdateFromDbHistory implements Callable<List<TransactionItem>> {

    private DbManager dbManager;

    public CallUpdateFromDbHistory(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public List<TransactionItem> call() throws Exception {
        List<TransactionItem> txList = new ArrayList<>();
        List<DetailsTxRoom> dbDetailsList = dbManager.getAppDb().getDetailsTxDao().getTxDetailsListOrdered();
        for (DetailsTxRoom dtx : dbDetailsList) {
            txList.add(new TransactionItem(dtx.getHash(), dtx.getTime(), dtx.getSum(), dtx.getReceived(), dtx.getConfirmations(), dtx.getFrom(), dtx.getTo(), dtx.getOut()));
        }

        return txList;
    }

}
