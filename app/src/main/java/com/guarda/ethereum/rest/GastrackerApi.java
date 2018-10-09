package com.guarda.ethereum.rest;

import com.guarda.ethereum.models.items.TransactionResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

interface GastrackerApi {
    @GET("/v1/addr/{address}/transactions")
    //Call<List<TransactionResponse>> getTransactions(@Path("address") String address);
    Call<Object> getTransactions(@Path("address") String address);
}
