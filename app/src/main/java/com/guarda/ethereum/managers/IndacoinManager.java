package com.guarda.ethereum.managers;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class IndacoinManager {

    public static void createTransaction(final String amount, final String currencyTo, final String addressTo, final String email, final Callback2<String, CreateTransactionRespModel> onComplete) {
        HashMap<String, String> params = new HashMap<>();
        params.put("amount", amount);
        params.put("currencyTo", currencyTo);
        params.put("addressTo", addressTo);
        params.put("email", email);
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("apiKey", INDACOIN_API_KEY);
        makePostQuery("/api/v1/buy/create-transaction", params, queryParams, new Callback2<String, String>() {
            @Override
            public void onResponse(String status, String resp) {
                try {
                    if ("ok".equals(status)) {
//                        HashMap<String, ShapeshiftApi.SupportedCoinModel> res = new HashMap<>();
                        JSONObject obj = new JSONObject(resp);
                        CreateTransactionRespModel respModel = new CreateTransactionRespModel();
                        String respStatus = obj.getString("status");
                        if ("success".equals(respStatus)) {
                            respModel.paymentUrl = obj.getString("paymentUrl");
                            respModel.transactionId = obj.getString("transactionId");
                            if (onComplete != null)
                                onComplete.onResponse(status, respModel);
                        } else {
                            if (onComplete != null)
                                onComplete.onResponse(respStatus, null);
                        }
                    } else {
                        Log.e("flint", "IndacoinManager.createTransaction... returns error: status="+status+", resp="+resp);
                        if (onComplete != null)
                            onComplete.onResponse(status, null);
                    }
                } catch (Exception e) {
                    if (onComplete != null)
                        onComplete.onResponse("error: " + e.toString(), null);
                }
            }
        });
    }



    public static void getEstimate(final String amount, final String currencyTo, final Callback2<String, GetEstimateRespModel> onComplete) {
        HashMap<String, String> params = new HashMap<>();
        params.put("amount", amount);
        params.put("currencyTo", currencyTo);
        params.put("apiKey", PUBLIC_API_KEY);
        HashMap<String, String> queryParams = new HashMap<>();
        makeGetQuery("/api/v1/buy/get-estimate", params, new Callback2<String, String>() {
            @Override
            public void onResponse(String status, String resp) {
                try {
                    if ("ok".equals(status)) {
                        JSONObject obj = new JSONObject(resp);
                        GetEstimateRespModel respModel = new GetEstimateRespModel();
                        respModel.estimate = obj.getString("estimate");
                        if (onComplete != null)
                            onComplete.onResponse(status, respModel);
                    } else {
                        Log.e("flint", "IndacoinManager.createTransaction... returns error: status="+status+", resp="+resp);
                        if (onComplete != null)
                            onComplete.onResponse(status, null);
                    }
                } catch (Exception e) {
                    if (onComplete != null)
                        onComplete.onResponse("error: " + e.toString(), null);
                }
            }
        });
    }


    public static void getLimits(final String email, final String currencyTo, final Callback2<String, GetLimitsRespModel> onComplete) {
        HashMap<String, String> params = new HashMap<>();
        params.put("currencyTo", currencyTo);
        params.put("email", email);
        params.put("apiKey", PUBLIC_API_KEY);
        HashMap<String, String> queryParams = new HashMap<>();
        makePostQuery("/api/v1/buy/get-limits", params, queryParams, new Callback2<String, String>() {
            @Override
            public void onResponse(String status, String resp) {
                try {
                    if ("ok".equals(status)) {
//                        HashMap<String, ShapeshiftApi.SupportedCoinModel> res = new HashMap<>();
                        JSONObject obj = new JSONObject(resp);
                        GetLimitsRespModel respModel = new GetLimitsRespModel();
                        JSONObject respLimit = obj.getJSONObject("limits");
                        respModel.max = respLimit.getString("max");
                        respModel.min = respLimit.getString("min");
                        if (onComplete != null)
                            onComplete.onResponse(status, respModel);
                    } else {
                        Log.e("flint", "IndacoinManager.createTransaction... returns error: status="+status+", resp="+resp);
                        if (onComplete != null)
                            onComplete.onResponse(status, null);
                    }
                } catch (Exception e) {
                    if (onComplete != null)
                        onComplete.onResponse("error: " + e.toString(), null);
                }
            }
        });
    }



    private static void makePostQuery(final String subUrl, final Map<String,String> params, final Map<String,String> queryParams, final Callback2<String, String> callback) {
        try {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        OkHttpClient httpClient = new OkHttpClient.Builder()
                                .connectTimeout(TIMEOUT_CONNECT, TimeUnit.MILLISECONDS)
                                .writeTimeout(TIMEOUT_WRITE, TimeUnit.MILLISECONDS)
                                .readTimeout(TIMEOUT_READ, TimeUnit.MILLISECONDS)
                                .build();
                        MediaType JSON = MediaType.parse("application/json");
                        String paramsJsonString = new JSONObject(params).toString();
                        RequestBody requestBody = RequestBody.create(JSON, paramsJsonString);
                        String reqUrl = API_URL + subUrl + mapToQueryParameters(queryParams);
                        Request req = new Request.Builder()
                                .url(reqUrl)
                                .post(requestBody)
                                .addHeader("GuardaApiKey", PUBLIC_API_KEY)
                                .build();
                        Response resp = httpClient.newCall(req).execute();
                        String respString = resp.body().string();
                        callback.onResponse("ok", respString);
                    } catch (Exception e) {
                        callback.onResponse("error from OkHttpClient: " + e.toString(), "");
                    }
                }
            };
            final Thread taskThread = new Thread(runnable);
            taskThread.start();
        } catch (Exception e) {
            callback.onResponse("error: " + e.toString(), "");
        }
    }

    private static void makeGetQuery(final String subUrl, final Map<String,String> params, final Callback2<String, String> callback) {
        try {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        OkHttpClient httpClient = new OkHttpClient.Builder()
                                .connectTimeout(TIMEOUT_CONNECT, TimeUnit.MILLISECONDS)
                                .writeTimeout(TIMEOUT_WRITE, TimeUnit.MILLISECONDS)
                                .readTimeout(TIMEOUT_READ, TimeUnit.MILLISECONDS)
                                .build();
                        String reqUrl = API_URL + subUrl + mapToQueryParameters(params);
                        Request req = new Request.Builder()
                                .url(reqUrl)
                                .get()
                                .addHeader("GuardaApiKey", PUBLIC_API_KEY)
                                .build();
                        Response resp = httpClient.newCall(req).execute();
                        String respString = resp.body().string();
                        callback.onResponse("ok", respString);
                    } catch (Exception e) {
                        callback.onResponse("error from OkHttpClient: " + e.toString(), "");
                    }
                }
            };
            final Thread taskThread = new Thread(runnable);
            taskThread.start();
        } catch (Exception e) {
            callback.onResponse("error: " + e.toString(), "");
        }
    }

    public static String mapToQueryParameters(Map<String, String> params) {
        ArrayList<String> parr = new ArrayList<>();
        for (String key : params.keySet()) {
            String s = key + "=" + params.get(key);
            parr.add(s);
        }
        return "?" + TextUtils.join("&", parr);
    }






    public static class CreateTransactionRespModel {
        public String paymentUrl = "";
        public String transactionId = "";
    }

    public static class GetEstimateRespModel {
        public String estimate = "0";
    }

    public static class GetLimitsRespModel {
        public String max = "0";
        public String min = "0";
    }



    private static final String API_URL = "https://guarda.co";
    private static final int TIMEOUT_CONNECT = 8000;
    private static final int TIMEOUT_WRITE = 8000;
    private static final int TIMEOUT_READ = 8000;
    private static final String PUBLIC_API_KEY = "d8bfa71d3a06ca958868e11160b6b6c9f216052c26a98915f421dad0a73c0b82";
    private static final String INDACOIN_API_KEY = "Lv5cx35Oy8ZW0jmQ";

}
