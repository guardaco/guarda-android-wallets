package com.guarda.ethereum.rxcall;

import com.guarda.ethereum.models.items.TransactionItem;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.db.model.TxDetailsRoom;

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
        List<TxDetailsRoom> dbDetailsList = dbManager.getAppDb().getTxDetailsDao().getTxDetailsListOrdered();
        for (TxDetailsRoom dtx : dbDetailsList) {
            txList.add(new TransactionItem(dtx.getHash(), dtx.getTime(), dtx.getSum(), true, dtx.getConfirmations(), dtx.getFromAddress(), dtx.getToAddress(), dtx.getOut()));
        }

        return txList;
    }

}
