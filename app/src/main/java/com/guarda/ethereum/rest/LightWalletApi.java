package com.guarda.ethereum.rest;


import com.google.gson.JsonObject;
import com.guarda.ethereum.models.items.ResponseChangellyAmount;
import com.guarda.ethereum.models.items.ResponseChangellyMinAmount;
import com.guarda.ethereum.models.items.ResponseCurrencyItem;
import com.guarda.ethereum.models.items.ResponseGenerateAddress;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LightWalletApi {

    @POST("/")
    Call<ResponseCurrencyItem> getListCurrencies(@Body JsonObject object);

    @POST("/")
    Call<ResponseGenerateAddress> generateAddress(@Body JsonObject object);

    @POST("/")
    Call<ResponseChangellyAmount> getExchangeAmount(@Body JsonObject object);

    @POST("/")
    Call<ResponseChangellyMinAmount> getMinAmount(@Body JsonObject object);

}

