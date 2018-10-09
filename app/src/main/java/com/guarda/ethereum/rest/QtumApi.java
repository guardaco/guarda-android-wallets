package com.guarda.ethereum.rest;

import com.guarda.ethereum.models.items.BlockHeightResponse;
import com.guarda.ethereum.models.items.BtgBalanceResponse;
import com.guarda.ethereum.models.items.BtgTxListResponse;
import com.guarda.ethereum.models.items.ContractUnspentOutput;
import com.guarda.ethereum.models.items.QtumTxListResponse;
import com.guarda.ethereum.models.items.SendTxResponse;
import com.guarda.ethereum.models.items.TransactionsInsightListResponse;
import com.guarda.ethereum.models.items.UTXOItem;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface QtumApi {
    @GET("insight-api/addr/{address}/balance")
    Call<ResponseBody> getBalance(@Path("address") String address);

    @GET("insight-api/addr/{address}/utxo")
    Call<List<UTXOItem>> getUTXOByAddress(@Path("address") String address);

    @GET("insight-api/addr/{address}/utxo")
    Call<List<ContractUnspentOutput>> getUTXOByAddressForToken(@Path("address") String address);

    @GET("/api/blocks/?format=json")
    Call<BlockHeightResponse> getCurrentBlockHeight();

    @GET("insight-api/addrs/{address}/txs?from=0&to=50")
    Call<QtumTxListResponse> getTransactions(@Path("address") String address);

    @GET("/insight-api/tokens/{token}/addresses/{address}/balance")
    Call <ResponseBody> getTokenBalance(@Path("token") String token, @Path("address") String address);

    @GET("/insight-api/tokens/{token}/transactions")
    Call <TransactionsInsightListResponse> getTokenTransactions(@Path("token") String token, @Query("addresses") String... addresses);

    @POST("/insight-api/tx/send")
    @FormUrlEncoded
    Call<SendTxResponse> sendRawTx(@Field("rawtx") String rawTx);
}
