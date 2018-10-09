package com.guarda.ethereum.rest;

import com.guarda.ethereum.models.items.KmdTxListResponse;
import com.guarda.ethereum.models.items.QtumTxListResponse;
import com.guarda.ethereum.models.items.UTXOItem;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface KmdApi {

//    @GET("api/addr/{address}/balance")
//    Call<ResponseBody> getBalance(@Path("address") String address);
//    @GET("api/addrs/{address}/txs?from=0&to=50")
//    Call<KmdTxListResponse> getTransactions(@Path("address") String address);
//    @GET("api/addr/{address}/utxo")
//    Call<List<UTXOItem>> getUTXOByAddress(@Path("address") String address);

    @GET("insight-api-komodo/addr/{address}/balance")
    Call<ResponseBody> getBalance(@Path("address") String address);
    @GET("insight-api-komodo/addrs/{address}/txs")
    Call<KmdTxListResponse> getTransactions(@Path("address") String address, @Query("from") int from, @Query("to") int to);
    @GET("insight-api-komodo/addr/{address}/utxo")
    Call<List<UTXOItem>> getUTXOByAddress(@Path("address") String address);


}
