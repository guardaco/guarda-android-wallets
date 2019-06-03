package com.guarda.ethereum.rxcall;

import com.guarda.ethereum.managers.TransactionsManager;
import com.guarda.ethereum.models.items.TransactionItem;
import com.guarda.ethereum.models.items.ZecTxResponse;
import com.guarda.zcash.sapling.api.ProtoApi;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.db.model.BlockRoom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import timber.log.Timber;


public class CallFillHistory implements Callable<Boolean> {

    private TransactionsManager transactionsManager;
    private List<ZecTxResponse> txList;
    private String transparentAddr;

    public CallFillHistory(TransactionsManager transactionsManager, List<ZecTxResponse> txList, String transparentAddr) {
        this.transactionsManager = transactionsManager;
        this.txList = txList;
        this.transparentAddr = transparentAddr;
    }

    @Override
    public Boolean call() throws Exception {
        List<TransactionItem> txItems = new ArrayList<>();
        try {
            txItems = transactionsManager.transformTxToFriendlyNew(txList, transparentAddr);
        } catch (Exception e) {
            e.printStackTrace();
            Timber.d("loading txs e=%s", e.getMessage());
        }
        transactionsManager.setTransactionsList(txItems);
        return true;
    }

}
