package com.guarda.ethereum.rest;


import com.google.gson.JsonObject;
import com.guarda.ethereum.models.items.SendRawTxResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface BitcoinNodeApi {

    @POST("/")
    Call<SendRawTxResponse> sendRawTransaction(@Body JsonObject object);

}

