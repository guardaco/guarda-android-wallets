package com.guarda.ethereum.rest;


import androidx.annotation.Nullable;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;

import retrofit2.Call;

public class Requestor {

    public static void getAvailableCurrencies(HashMap headerParam, JsonObject param, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createLightWalletApi(headerParam).getListCurrencies(param);
        ApiMethods.makeRequest(call, listener);
    }

    public static void getMinAmount(HashMap headerParam, JsonObject param, ApiMethods.RequestListener listener){
        Call call = ApiMethods.createLightWalletApi(headerParam).getMinAmount(param);
        ApiMethods.makeRequest(call, listener);
    }

    public static void getExchangeChangellyAmount(HashMap headerParam, JsonObject param, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createLightWalletApi(headerParam).getExchangeAmount(param);
        ApiMethods.makeRequest(call, listener);
    }

    public static void sendRawTransaction(JsonObject param, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createBitcoinNodeApi().sendRawTransaction(param);
        ApiMethods.makeRequest(call, listener);
    }

    public static void getAddress(HashMap headerParam, JsonObject param, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createLightWalletApi(headerParam).generateAddress(param);
        ApiMethods.makeRequest(call, listener);
    }

    public static void getTokens(JsonArray param, ApiMethods.RequestListener listener){
        Call call = ApiMethods.createNodeApi().getTokens(param);
        ApiMethods.makeRequest(call, listener);
    }

    public static void getExchangeAmountCc(String from, String to, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createCryptocompareApi().getExchange(from, to);
        Log.d("Request", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getExchangeAmountCmc(String from, String to, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createCoinmarketcapApi().getExchange(from, to);
        Log.d("Request", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getBtcRate(String currency, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createWemovecoinApi().getBtcRate(currency);
        ApiMethods.makeRequest(call, listener);
    }

    public static void coinifySignUp(JsonObject signUp, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createCoinifyApi().signUp(signUp);
        ApiMethods.makeRequest(call, listener);
    }

    public static void coinifyAuth(JsonObject jsonObject, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createCoinifyApi().auth(jsonObject);
        ApiMethods.makeRequest(call, listener);
    }

    public static void coinifyQuote(String accessToken, JsonObject jsonObject, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createCoinifyApiAuth(accessToken).quote(jsonObject);
        ApiMethods.makeRequest(call, listener);
    }

    public static void coinifyPays(String accessToken, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createCoinifyApiAuth(accessToken).pays();
        ApiMethods.makeRequest(call, listener);
    }

    public static void coinifyTrade(String accessToken, JsonObject trade, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createCoinifyApiAuth(accessToken).trades(trade);
        ApiMethods.makeRequest(call, listener);
    }

    public static void coinifyTradesList(String accessToken, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createCoinifyApiAuth(accessToken).tradesList();
        ApiMethods.makeRequest(call, listener);
    }

    public static void coinifyCancelTrade(String accessToken, int tradeId, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createCoinifyApiAuth(accessToken).cancelTrade(tradeId);
        ApiMethods.makeRequest(call, listener);
    }

    public static void coinifyKYCList(String accessToken, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createCoinifyApiAuth(accessToken).kycList();
        ApiMethods.makeRequest(call, listener);
    }

    public static void coinifyPostKYC(String accessToken, JsonObject jsonObject, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createCoinifyApiAuth(accessToken).postKyc(jsonObject);
        ApiMethods.makeRequest(call, listener);
    }

    public static void coinifyBankAccounts(String accessToken, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createCoinifyApiAuth(accessToken).bankAccounts();
        ApiMethods.makeRequest(call, listener);
    }

    public static void coinifyPostBankAccounts(String accessToken, JsonObject jsonObject, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createCoinifyApiAuth(accessToken).postBankAccounts(jsonObject);
        ApiMethods.makeRequest(call, listener);
    }

    public static void coinifyTradeSell(String accessToken, JsonObject trade, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createCoinifyApiAuth(accessToken).tradesSell(trade);
        ApiMethods.makeRequest(call, listener);
    }

    public static void coinifyGetTrade(String accessToken, int tradeId, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createCoinifyApiAuth(accessToken).getTrade(tradeId);
        ApiMethods.makeRequest(call, listener);
    }

    public static void getIconsUrls(ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createGuardaEthApi().getIconsUrls();
        Log.d("Request", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getTxFee(ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createGuardaCoApi().getFeeList();
        Log.d("Request", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

}
