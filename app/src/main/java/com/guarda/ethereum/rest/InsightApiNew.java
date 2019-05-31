package com.guarda.ethereum.rest;


import android.telecom.CallScreeningService;

import com.guarda.ethereum.models.items.BtgBalanceResponse;
import com.guarda.ethereum.models.items.BlockHeightResponse;
import com.guarda.ethereum.models.items.BtgTxListResponse;
import com.guarda.ethereum.models.items.SendRawTxResponse;
import com.guarda.ethereum.models.items.UTXOItem;
import com.guarda.ethereum.models.items.UTXOItemDgb;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

interface InsightApiNew {
    @GET("addr/{address}?noTxList=1")
    Call<BtgBalanceResponse> getBalance(@Path("address") String address);

    @GET("addrs/{address}/utxo")
    Call<List<UTXOItemDgb>> getUTXOByAddress(@Path("address") String address);

    @GET("/blocks/{timeMillis}?format=json")
    Call<BlockHeightResponse> getCurrentBlockHeight(@Path("timeMillis") String time);

    @GET("txs")
    Call<BtgTxListResponse> getTransactions(@Query("address") String address);

    @POST("tx/send")
    @FormUrlEncoded
    Call<SendRawTxResponse> broadcastRawTx(@Field("rawtx") String rawtx);
}
