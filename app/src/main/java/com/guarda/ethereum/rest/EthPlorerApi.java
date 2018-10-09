package com.guarda.ethereum.rest;


import com.guarda.ethereum.models.items.AddressInfoResp;
import com.guarda.ethereum.models.items.TokensTxListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

interface EthPlorerApi {

    @GET("service.php?page=pageSize%3D100")
    Call<TokensTxListResponse> getTransactions(@Query("data") String address);

    @GET("/getAddressInfo/{address}")
    Call<AddressInfoResp> getTokensBalances(@Path("address") String address, @Query("apiKey") String apikey);
}
