package com.guarda.ethereum.rest;


import com.guarda.ethereum.models.items.TransactionsListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

interface EtherScanApi {
    @POST("api?module=account&action=txlist&startblock=0&endblock=99999999&page=1&offset=100&sort=desc")
    Call<TransactionsListResponse> getTransactions(@Query("address") String address,
                                                   @Query("apikey") String apikey);
}
