package com.guarda.ethereum.rxcall;

import com.guarda.ethereum.models.items.ZecTxResponse;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.db.model.DetailsTxRoom;
import com.guarda.zcash.sapling.db.model.ReceivedNotesRoom;

import java.util.List;
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
        boolean isOut = false;
        Long value = 0L;

        List<String> cmuList = dbManager.getAppDb().getTxOutputDao().getCmByHash(tr.getHash());
        if (cmuList != null) {
            for (String cmu : cmuList) {
                ReceivedNotesRoom note = dbManager.getAppDb().getReceivedNotesDao().getNoteByCm(cmu);
                if (note != null) {
                    value = note.getValue();
                    isOut = false;
                }
            }
        }

        String nf = dbManager.getAppDb().getTxInputDao().getNfByHash(tr.getHash());
        if (nf != null) {
            ReceivedNotesRoom note = dbManager.getAppDb().getReceivedNotesDao().getNoteByNf(nf);
            if (note != null) {
                value = note.getValue();
                isOut = true;
            }
        }

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
                        isOut));

        return true;
    }

}
