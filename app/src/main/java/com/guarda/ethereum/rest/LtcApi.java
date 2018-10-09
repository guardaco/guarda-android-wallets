package com.guarda.ethereum.rest;


import com.guarda.ethereum.models.items.BalanceAndTxResponse;
import com.guarda.ethereum.models.items.BlockHeightResponse;
import com.guarda.ethereum.models.items.UTXOListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

interface LtcApi {
    @GET("address/{address}")
    Call<BalanceAndTxResponse> getBalance(@Path("address") String address);

    @GET("unspent")
    Call<UTXOListResponse> getUTXOByAddress(@Query("active") String address);

    @GET("/blocks/{timeMillis}?format=json")
    Call<BlockHeightResponse> getCurrentBlockHeight(@Path("timeMillis") String time);
}
