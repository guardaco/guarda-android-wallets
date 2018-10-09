package com.guarda.ethereum.rest;

import com.google.gson.JsonObject;
import com.guarda.ethereum.models.items.CoinifyAuthResponse;
import com.guarda.ethereum.models.items.CoinifyBankAcc;
import com.guarda.ethereum.models.items.CoinifyKYCResponse;
import com.guarda.ethereum.models.items.CoinifyPaysResponse;
import com.guarda.ethereum.models.items.CoinifyQuoteResponse;
import com.guarda.ethereum.models.items.CoinifySignUpResponse;
import com.guarda.ethereum.models.items.CoinifyTradeRespForList;
import com.guarda.ethereum.models.items.CoinifyTradeRespSell;
import com.guarda.ethereum.models.items.CoinifyTradeResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

interface CoinifyApi {

    @POST("/signup/trader")
    Call<CoinifySignUpResponse> signUp(@Body JsonObject signUp);

    @POST("/auth")
    Call<CoinifyAuthResponse> auth(@Body JsonObject auth);

    @POST("/trades/quote")
    Call<CoinifyQuoteResponse> quote(@Body JsonObject quote);

    @GET("/trades/payment-methods")
    Call<List<CoinifyPaysResponse>> pays();

    @POST("/trades")
    Call<CoinifyTradeResponse> trades(@Body JsonObject trade);

    @GET("/trades")
    Call<List<CoinifyTradeRespForList>> tradesList();

    @PATCH("/trades/{tradeId}/cancel")
    Call<ResponseBody> cancelTrade(@Path("tradeId") int tradeId);

    @GET("/kyc")
    Call<List<CoinifyKYCResponse>> kycList();

    @POST("/traders/me/kyc")
    Call<CoinifyKYCResponse> postKyc(@Body JsonObject trade);

    @GET("/bank-accounts")
    Call<List<CoinifyBankAcc>> bankAccounts();

    @POST("/bank-accounts")
    Call<CoinifyBankAcc> postBankAccounts(@Body JsonObject bankAcc);

    @POST("/trades")
    Call<CoinifyTradeRespSell> tradesSell(@Body JsonObject trade);

    @GET("/trades/{tradeId}")
    Call<CoinifyTradeResponse> getTrade(@Path("tradeId") int tradeId);
}
