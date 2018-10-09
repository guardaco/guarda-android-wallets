package com.guarda.ethereum.rest;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.guarda.ethereum.models.items.ResponseCurrencyItem;
import com.guarda.ethereum.models.items.ResponseExchangeAmount;
import com.guarda.ethereum.models.items.TokenBalanceResponse;

import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

interface NodeApi {

    @POST("/")
    Call<List<TokenBalanceResponse>> getTokens(@Body JsonArray object);

}
