package com.guarda.ethereum.rxcall;

import com.guarda.ethereum.managers.TransactionsManager;
import com.guarda.ethereum.models.items.TransactionItem;
import com.guarda.ethereum.models.items.ZecTxResponse;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.db.model.DetailsTxRoom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import timber.log.Timber;


public class CallDbFillHistory implements Callable<Boolean> {

    private TransactionsManager transactionsManager;
    private List<ZecTxResponse> txList;
    private String transparentAddr;
    private DbManager dbManager;

    public CallDbFillHistory(TransactionsManager transactionsManager,
                             List<ZecTxResponse> txList,
                             String transparentAddr,
                             DbManager dbManager) {
        this.transactionsManager = transactionsManager;
        this.txList = txList;
        this.transparentAddr = transparentAddr;
        this.dbManager = dbManager;
    }

    @Override
    public Boolean call() throws Exception {
        List<TransactionItem> txItems;
        try {
            txItems = transactionsManager.transformTxToFriendlyNew(txList, transparentAddr);
            //update list in manager
            transactionsManager.setTransactionsList(txItems);

            List<DetailsTxRoom> details = new ArrayList<>();
            //fill transpatent transactions
            for (TransactionItem tx : txItems) {
                details.add(new DetailsTxRoom(tx.getHash(), tx.getTime(), tx.getSum(), tx.isReceived(), tx.getConfirmations(), tx.getFrom(), tx.getTo(), tx.isOut()));
            }
            //update list in DB
            dbManager.getAppDb().getDetailsTxDao().insertList(details);
        } catch (Exception e) {
            e.printStackTrace();
            Timber.e("loading txs e=%s", e.getMessage());
            return false;
        }
        return true;
    }

}
