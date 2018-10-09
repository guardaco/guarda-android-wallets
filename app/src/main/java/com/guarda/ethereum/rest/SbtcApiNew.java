package com.guarda.ethereum.rest;


import com.guarda.ethereum.models.items.BlockHeightResponse;
import com.guarda.ethereum.models.items.BtgBalanceResponse;
import com.guarda.ethereum.models.items.BtgTxListResponse;
import com.guarda.ethereum.models.items.UTXOItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

interface SbtcApiNew {
    @GET("addr/{address}?noTxList=1")
    Call<BtgBalanceResponse> getBalance(@Path("address") String address);

    @GET("addrs/{address}/utxo")
    Call<List<UTXOItem>> getUTXOByAddress(@Path("address") String address);

    @GET("/blocks/{timeMillis}?format=json")
    Call<BlockHeightResponse> getCurrentBlockHeight(@Path("timeMillis") String time);

    @GET("txs")
    Call<BtgTxListResponse> getTransactions(@Query("address") String address);
}
