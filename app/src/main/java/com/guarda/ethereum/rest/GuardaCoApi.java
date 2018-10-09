package com.guarda.ethereum.rest;


import com.guarda.ethereum.models.items.TokenListItem;
import com.guarda.ethereum.models.items.TxFeeResponse;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

interface GuardaCoApi {

    @GET("/fee.json")
    Call<HashMap<String, TxFeeResponse>> getFeeList();

    @GET("/tokens.json")
    Call<List<TokenListItem>> getTokensList();
}
