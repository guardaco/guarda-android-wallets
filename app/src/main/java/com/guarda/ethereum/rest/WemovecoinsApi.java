package com.guarda.ethereum.rest;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface WemovecoinsApi {

    @GET("/LatestBTCRate")
    Call<ResponseBody> getBtcRate(@Query("currency") String currency);
}
