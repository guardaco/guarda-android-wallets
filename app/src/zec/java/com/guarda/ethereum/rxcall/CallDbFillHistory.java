package com.guarda.ethereum.rxcall;

import com.guarda.ethereum.BuildConfig;
import com.guarda.ethereum.managers.TransactionsManager;
import com.guarda.ethereum.models.items.TransactionItem;
import com.guarda.ethereum.models.items.ZecTxResponse;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.db.model.TxDetailsRoom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import timber.log.Timber;

import static com.guarda.ethereum.lifecycle.HistoryViewModel.T_TX_KEY_PREFIX;


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

            //test data
            if (BuildConfig.DEBUG) {
                txItems.clear();
                txItems.add(new TransactionItem("8cf74fb1295b93510a913d11f000835367b64092b58be711703167dd95e12c4a", 1565956571, 1196158, true, 14995, "t1Lb7pwVomWgXjfJuAGZMaouifcWymXs6Du", "t1gynHkGVxofEmfZk7ywxM7w36TcqoHyWwP", true));
                txItems.add(new TransactionItem("fde325313ef5e21a5fb3ea48cc23e919d9c01e2e91ef093cf93a7732f554a0bb", 1565697321, 999322, true, 16711, "t1W75CYYjaFC8SmuM3s2WqsEb4RQXEzYaGK", "t1Lb7pwVomWgXjfJuAGZMaouifcWymXs6Du", false));
                txItems.add(new TransactionItem("ca63fea4c37525a7a1b0e766f00564277246981db9f22196ff09b41cbee5085b", 1561378710, 8663715, true, 3, "t1Lb7pwVomWgXjfJuAGZMaouifcWymXs6Du", "", true));
                txItems.add(new TransactionItem("f5c05e95c06e0771cb8305a011976c8d3a7fe66209ca6888fc099e52c65d9d31", 1568127838, 45678, true, 4, "", "t1Lb7pwVomWgXjfJuAGZMaouifcWymXs6Du", false));
            }

            //update list in manager
            transactionsManager.setTransactionsList(txItems);

            List<TxDetailsRoom> details = new ArrayList<>();
            //fill transpatent transactions
            for (TransactionItem tx : txItems) {
                details.add(new TxDetailsRoom(
                        T_TX_KEY_PREFIX + tx.getHash(),
                        tx.getHash(),
                        tx.getTime(),
                        tx.getSum(),
                        tx.getConfirmations(),
                        tx.getFrom(),
                        tx.getTo(),
                        tx.isOut()));
            }
            //update list in DB
            dbManager.getAppDb().getTxDetailsDao().insertList(details);
        } catch (Exception e) {
            e.printStackTrace();
            Timber.e("loading txs e=%s", e.getMessage());
            return false;
        }
        return true;
    }

}
