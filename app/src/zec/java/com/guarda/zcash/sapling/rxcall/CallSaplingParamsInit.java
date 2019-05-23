package com.guarda.zcash.sapling.rxcall;

import android.content.Context;

import com.guarda.zcash.RustAPI;
import com.guarda.zcash.ZCashTransaction_zaddr;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.db.model.ReceivedNotesRoom;
import com.guarda.zcash.sapling.key.SaplingCustomFullKey;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import timber.log.Timber;


public class CallSaplingParamsInit implements Callable<Boolean> {

    private Context context;

    public CallSaplingParamsInit(Context context) {
        this.context = context;
    }

    @Override
    public Boolean call() throws Exception {
        Timber.d("started");
        RustAPI.checkInit(context);
        return true;
    }

}
