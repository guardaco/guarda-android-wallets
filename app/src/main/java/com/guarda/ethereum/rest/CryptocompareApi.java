package com.guarda.ethereum.rest;


import com.guarda.ethereum.models.items.RespExch;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

interface CryptocompareApi {

    @GET("/data/price")
    Call<HashMap<String, BigDecimal>> getExchange(@Query("fsym") String from, @Query("tsyms") String to);
}
