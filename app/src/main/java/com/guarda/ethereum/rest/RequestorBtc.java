package com.guarda.ethereum.rest;


import com.guarda.ethereum.models.constants.items.RawBlockResponse;
import com.guarda.ethereum.models.constants.items.ZecTxResponse;
import com.guarda.ethereum.models.items.BlockBookBlock;

import io.reactivex.Observable;
import retrofit2.Call;

public class RequestorBtc {

    // BlockBook
    public static Observable<BlockBookBlock> getBlockBookBlock(String blockHeight) {
        return ApiMethods.createZecBookApi().block(blockHeight);
    }

    // Insight
    public static void broadcastRawTxZexNew(String hexTx, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createZecInsightApiNew().broadcastRawTx(hexTx);
        ApiMethods.makeRequest(call, listener);
    }


    public static void getBalanceZecNew(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createZecApiNew().getBalance(address);
        ApiMethods.makeRequest(call, listener);
    }

    public static void getTransactionsZecNew(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createZecApiNew().getTransactions(address);
        ApiMethods.makeRequest(call, listener);
    }

    public static Observable<ZecTxResponse> getOneTx(String hash) {
        return ApiMethods.createZecApiNew().getOneTx(hash);
    }

    public static Observable<RawBlockResponse> getRawBlockByHash(String hash) {
        return ApiMethods.createZecApiNew().getRawBlockByHash(hash);
    }

}
