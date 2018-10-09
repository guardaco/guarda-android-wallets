package com.guarda.ethereum.rest;


import com.guarda.ethereum.models.items.TransactionsListResponse;


import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

interface CallistoExplorerApi {

    @POST("addr")
    @FormUrlEncoded
    Call<TransactionsListResponse> getTransactions(@Field("addr") String address,
                                                   @Field("start") String start,
                                                   @Field("length") String length);
}
