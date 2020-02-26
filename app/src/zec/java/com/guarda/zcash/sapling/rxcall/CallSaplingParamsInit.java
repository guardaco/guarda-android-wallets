package com.guarda.zcash.sapling.rxcall;

import android.content.Context;

import java.util.concurrent.Callable;

import timber.log.Timber;
import work.samosudov.rustlib.RustAPI;


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
