package com.guarda.ethereum.rest;


import com.guarda.ethereum.models.items.IconItemResponse;

import java.util.HashMap;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

interface GuardaEthApi {

    @GET("/tokens.json")
    Call<ResponseBody> getTokensList();

    @GET("/CryptoCurrencies.json")
    Call<HashMap<String, IconItemResponse>> getIconsUrls();
}
