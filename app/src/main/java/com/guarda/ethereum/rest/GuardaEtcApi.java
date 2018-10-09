package com.guarda.ethereum.rest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

interface GuardaEtcApi {

    @GET("/tokens.json")
    Call<ResponseBody> getTokensList();

}
