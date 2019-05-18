package com.guarda.zcash.sapling.rxcall;

import com.guarda.zcash.sapling.api.ProtoApi;

import java.util.concurrent.Callable;

public class CallBlockRange implements Callable<Boolean> {

    private ProtoApi protoApi;
    private long end;

    private static final long BLOCK_RANGE_STEP = 1000;

    public CallBlockRange(ProtoApi protoApi, long end) {
        this.protoApi = protoApi;
        this.end = end;
    }

    @Override
    public Boolean call() throws Exception {
        long endLocal = protoApi.pageNum + BLOCK_RANGE_STEP;
        if (endLocal >= end)
            endLocal = end;

        protoApi.gB(protoApi.pageNum, endLocal);

        return true;
    }
}
