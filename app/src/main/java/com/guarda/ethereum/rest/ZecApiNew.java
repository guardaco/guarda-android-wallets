package com.guarda.ethereum.rest;


import com.guarda.ethereum.models.constants.items.RawBlockResponse;
import com.guarda.ethereum.models.constants.items.ZecTxListResponse;
import com.guarda.ethereum.models.constants.items.ZecTxResponse;
import com.guarda.ethereum.models.items.BlockHeightResponse;
import com.guarda.ethereum.models.items.BtgBalanceResponse;
import com.guarda.ethereum.models.items.UTXOItem;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

interface ZecApiNew {
    @GET("addr/{address}")
    Call<BtgBalanceResponse> getBalance(@Path("address") String address);

    @GET("addr/{address}/utxo")
    Call<List<UTXOItem>> getUTXOByAddress(@Path("address") String address);

    @GET("blocks/{timeMillis}?format=json")
    Call<BlockHeightResponse> getCurrentBlockHeight(@Path("timeMillis") String time);

    @GET("txs")
    Call<ZecTxListResponse> getTransactions(@Query("address") String address);

    @GET("tx/{hash}")
    Observable<ZecTxResponse> getOneTx(@Path("hash") String hash);

    @GET("rawblock/{hash}")
    Observable<RawBlockResponse> getRawBlockByHash(@Path("hash") String hash);
}
