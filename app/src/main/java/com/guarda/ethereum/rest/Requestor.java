package com.guarda.ethereum.rest;


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

    public static void getExchangeAmountCc(String from, String to, String apiKey, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createCryptocompareApi().getExchange(from, to, apiKey);
        Log.d("Request", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getExchangeAmountCmc(String from, String to, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createCoinmarketcapApi().getExchange(from, to);
        Log.d("Request", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

}
