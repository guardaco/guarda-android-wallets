package com.guarda.zcash.sapling.rxcall;

import com.guarda.zcash.sapling.api.ProtoApi;

import java.util.concurrent.Callable;

public class CallLastBlock implements Callable<Long> {

    @Override
    public Long call() throws Exception {
        return new ProtoApi().getLastBlock();
    }
}
