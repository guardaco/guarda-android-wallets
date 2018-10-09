package com.guarda.ethereum.rest;


import com.guarda.ethereum.models.items.RespExch;
import com.guarda.ethereum.models.items.ResponseExchangeAmount;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

interface CoinmarketcapApi {

    @GET("/v1/ticker/{from}")
    Call<List<RespExch>> getExchange(@Path("from") String from, @Query("convert") String to);
}
