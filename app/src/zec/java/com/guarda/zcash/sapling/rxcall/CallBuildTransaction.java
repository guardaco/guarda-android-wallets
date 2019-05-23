package com.guarda.zcash.sapling.rxcall;

import com.guarda.zcash.ZCashTransaction_zaddr;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.db.model.ReceivedNotesRoom;
import com.guarda.zcash.sapling.key.SaplingCustomFullKey;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


public class CallBuildTransaction implements Callable<ZCashTransaction_zaddr> {

    private DbManager dbManager;
    private String toAddress;
    private Long value;
    private Long fee;
    private SaplingCustomFullKey key;
    private int expHeight;

    public CallBuildTransaction(DbManager dbManager, String toAddress, Long value, Long fee, SaplingCustomFullKey key, int expHeight) {
        this.dbManager = dbManager;
        this.toAddress = toAddress;
        this.value = value;
        this.fee = fee;
        this.key = key;
        this.expHeight = expHeight;
    }

    @Override
    public ZCashTransaction_zaddr call() throws Exception {
        List<ReceivedNotesRoom> unspents = dbManager.getAppDb().getReceivedNotesDao().getUnspents();

        unspents = chooseUnspents(unspents);

        return new ZCashTransaction_zaddr(key, toAddress, value, fee, expHeight, unspents, dbManager);
    }

    private List<ReceivedNotesRoom> chooseUnspents(List<ReceivedNotesRoom> allUnspents) {
        long realValue = value + fee;
        long sum = 0;
        List<ReceivedNotesRoom> list = new ArrayList<>();

        for (ReceivedNotesRoom r : allUnspents) {
            list.add(r);
            sum += r.getValue();
            if (sum >= realValue) break;
        }

        return list;
    }

}
