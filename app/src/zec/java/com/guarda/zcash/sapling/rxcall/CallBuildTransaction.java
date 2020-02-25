package com.guarda.zcash.sapling.rxcall;

import com.guarda.zcash.ZCashTransaction_zaddr;
import com.guarda.zcash.ZCashTransaction_ztot;
import com.guarda.zcash.ZcashTransaction;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.db.model.ReceivedNotesRoom;
import com.guarda.zcash.sapling.key.SaplingCustomFullKey;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import timber.log.Timber;


public class CallBuildTransaction implements Callable<ZcashTransaction> {

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
    public ZcashTransaction call() throws Exception {
        List<ReceivedNotesRoom> unspents = dbManager.getAppDb().getReceivedNotesDao().getUnspents();

        unspents = chooseUnspents(unspents);
        int us = unspents.size();
        if (us == 0) Timber.e("unspents.size() == 0");
        Timber.d("unspents.size() = %d", us);

        if (toAddress.substring(0, 1).equalsIgnoreCase("z")) {
            //from z to z
            return new ZCashTransaction_zaddr(key, toAddress, value, fee, expHeight, unspents, dbManager);
        } else if (toAddress.substring(0, 1).equalsIgnoreCase("t")) {
            //from z to t
            return new ZCashTransaction_ztot(key, toAddress, value, fee, expHeight, unspents, dbManager);
        }

        return new ZCashTransaction_ztot(key, toAddress, value, fee, expHeight, unspents, dbManager);
    }

    private List<ReceivedNotesRoom> chooseUnspents(List<ReceivedNotesRoom> allUnspents) {
        long realValue = value + fee;
        long sum = 0;
        List<ReceivedNotesRoom> list = new ArrayList<>();

        for (ReceivedNotesRoom r : allUnspents) {
            list.add(r);
            sum += r.getValue();
            Timber.d("chooseUnspents ReceivedNotesRoom: r=%s", r.toString());
            if (sum >= realValue) break;
        }

        return list;
    }

}
