package com.guarda.ethereum.rest;


import com.guarda.ethereum.models.items.RespExch;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface CryptocompareApi {

    @GET("/data/price")
    Call<RespExch> getExchange(@Query("fsym") String from,
                               @Query("tsyms") String to,
                               @Query("api_key") String apiKey);

}
