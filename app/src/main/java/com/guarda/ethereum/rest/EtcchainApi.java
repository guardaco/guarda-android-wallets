package com.guarda.ethereum.rest;


import com.guarda.ethereum.models.items.TransactionResponse;
import com.guarda.ethereum.models.items.TransactionsListResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

interface EtcchainApi {
    @GET("getTransactionsByAddress?offset=999&sort=desc")
    Call<List<TransactionResponse>> getTransactions(@Query("address") String address);
}
