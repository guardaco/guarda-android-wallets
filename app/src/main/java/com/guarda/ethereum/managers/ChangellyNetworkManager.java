package com.guarda.ethereum.managers;


import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;
import com.guarda.ethereum.models.constants.Changelly;
import com.guarda.ethereum.utils.Coders;

import java.util.HashMap;

public class ChangellyNetworkManager {

    private static String TAG = "ChangellyNetworkManager";


    ChangellyNetworkManager() throws Exception {

    }

    public static void getExchangeStatus(String id, ApiMethods.RequestListener listener) {
        JsonObject param = new JsonObject();
        param.addProperty("id", id);

        JsonObject json = getMainJsonParam("getStatus");
        json.add("params", param);

        String sign = Coders.hmacDigest(json.toString(), Changelly.API_SECRET);
        HashMap<String, String> header = getHeader(sign);

    }

    public static void getTransactions(@Nullable String currency, @Nullable String address,
                                       @Nullable String extraId, int limit, int offset,
                                       ApiMethods.RequestListener listener) {

        JsonObject param = new JsonObject();
        param.addProperty("currency", currency);
        param.addProperty("address", address);
        param.addProperty("extraId", extraId);
        param.addProperty("limit", limit);
        param.addProperty("offset", offset);

        JsonObject json = getMainJsonParam("getTransactions");
        json.add("params", param);

        String sign = Coders.hmacDigest(json.toString(), Changelly.API_SECRET);
        HashMap<String, String> header = getHeader(sign);

    }

    public static void getExchangeAmount(String from, String to, String amount, ApiMethods.RequestListener listener) {
        JsonObject param = new JsonObject();
        param.addProperty("from", from);
        param.addProperty("to", to);
        param.addProperty("amount", amount);

        JsonObject json = getMainJsonParam("getExchangeAmount");
        json.add("params", param);

        Log.d("ExchangeAmount", json.toString());
        String sign = Coders.hmacDigest(json.toString(), Changelly.API_SECRET);
        HashMap<String, String> header = getHeader(sign);

        Requestor.getExchangeChangellyAmount(header, json, listener);
    }

    public static void getMinAmount(String from, String to, ApiMethods.RequestListener listener) {
        JsonObject param = new JsonObject();
        param.addProperty("from", from);
        param.addProperty("to", to);

        JsonObject json = getMainJsonParam("getMinAmount");
        json.add("params", param);

        String sign = Coders.hmacDigest(json.toString(), Changelly.API_SECRET);
        HashMap<String, String> header = getHeader(sign);
        Requestor.getMinAmount(header, json, listener);
    }

    public static void generateAddress(String from, String to, String address, @Nullable String extraId, ApiMethods.RequestListener listener) {
        JsonObject param = new JsonObject();
        param.addProperty("from", from);
        param.addProperty("to", to);
        param.addProperty("address", address);

        JsonObject json = getMainJsonParam("generateAddress");
        json.add("params", param);

        String sign = Coders.hmacDigest(json.toString(), Changelly.API_SECRET);
        HashMap<String, String> header = getHeader(sign);

        Requestor.getAddress(header, json, listener);
    }

    public static void getCurrencies(ApiMethods.RequestListener listener) {
        JsonObject json = getMainJsonParam("getCurrencies");
        json.add("params", new JsonObject());

        String sign = Coders.hmacDigest(json.toString(), Changelly.API_SECRET);
        HashMap<String, String> header = getHeader(sign);

        Requestor.getAvailableCurrencies(header, json, listener);
    }

    private static HashMap<String, String> getHeader(String sign) {
        HashMap<String, String> header = new HashMap<>();
        header.put("api-key", Changelly.API_KEY);
        header.put("sign", sign);
        Log.d(TAG, "Header: " + header);
        return header;
    }

    private static JsonObject getMainJsonParam(String method) {
        JsonObject json = new JsonObject();
        json.addProperty("jsonrpc", "2.0");
        json.addProperty("method", method);
        json.addProperty("id", 1);

        return json;
    }

}
