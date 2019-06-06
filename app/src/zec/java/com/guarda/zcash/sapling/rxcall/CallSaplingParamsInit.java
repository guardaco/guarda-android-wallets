package com.guarda.zcash.sapling.rxcall;

import android.content.Context;

import com.guarda.zcash.RustAPI;

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
