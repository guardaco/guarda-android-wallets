package com.guarda.ethereum.managers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

public class ChangenowApi {

    public static void getSupportedCoins(final Callback2<String, Map<String, ChangenowApi.SupportedCoinModel>> onComplete) {
        makeGetQuery("/api/v1/currencies?active=true", new Callback2<String, String>() {
            @Override
            public void onResponse(String status, String resp) {
                try {
                    if ("ok".equals(status)) {
                        HashMap<String, ChangenowApi.SupportedCoinModel> res = new HashMap<>();
                        JSONArray arr = new JSONArray(resp);
                        for (int i = 0; i < arr.length(); ++i) {
                            JSONObject coinObj = arr.getJSONObject(i);
                            boolean isFiat = false;
                            try {isFiat = coinObj.getBoolean("isFiat");}catch (Exception e) {isFiat = false;}
                            if (!isFiat) {
                                ChangenowApi.SupportedCoinModel coinModel = new ChangenowApi.SupportedCoinModel();
                                coinModel.symbol = coinObj.getString("ticker");
                                try {
                                    coinModel.name = coinObj.getString("name");
                                } catch (Exception e) {
                                    coinModel.name = coinModel.symbol;
                                }
                                coinModel.imageUrl = coinObj.getString("image");
                                coinModel.isAbailable = true;
                                res.put(coinModel.symbol, coinModel);
                            }
                        }
                        if (onComplete != null)
                            onComplete.onResponse(status, res);
                    } else {
                        if (onComplete != null)
                            onComplete.onResponse(status, null);
                    }
                } catch (Exception e) {
                    Log.e("flint", "ChangenowApi... getSupportedCoins: " + e.getMessage());
                    if (onComplete != null)
                        onComplete.onResponse("error: " + e.toString(), null);
                }
            }
        });
    }

    public static void getRate(final String fromCoin, final String toCoin, final Callback2<String, ChangenowApi.GetRateRespModel> onComplete) {
        final String pair = fromCoin + "_" + toCoin;
        makeGetQuery("/api/v1/exchange-amount/10000/" + pair, new Callback2<String, String>() {
            @Override
            public void onResponse(String status, String resp) {
                try {
                    Timber.d("getRate pair=%s resp=%s status=%s", pair, resp, status);
                    if ("ok".equals(status)) {
                        HashMap<String, ChangenowApi.SupportedCoinModel> res = new HashMap<>();
                        JSONObject obj = new JSONObject(resp);
                        ChangenowApi.GetRateRespModel respModel = new ChangenowApi.GetRateRespModel();
                        respModel.rate = BigDecimal.valueOf(obj.getDouble("estimatedAmount"));
                        respModel.minimum = BigDecimal.valueOf(0);
                        if (onComplete != null)
                            onComplete.onResponse(status, respModel);
                    } else {
                        if (onComplete != null)
                            onComplete.onResponse(status, null);
                    }
                } catch (Exception e) {
                    Log.e("flint", "ChangenowApi... getRate: " + e.getMessage());
                    if (onComplete != null)
                        onComplete.onResponse("error: " + e.toString(), null);
                }
            }
        });
    }

    public static void getMinAmount(final String fromCoin, final String toCoin, final Callback2<String, ChangenowApi.GetRateRespModel> onComplete) {
        final String pair = fromCoin + "_" + toCoin;
        makeGetQuery("/api/v1/min-amount/" + pair, new Callback2<String, String>() {
            @Override
            public void onResponse(String status, String resp) {
                try {
                    Timber.d("getMinAmount pair=%s resp=%s status=%s", pair, resp, status);
                    if ("ok".equals(status)) {
                        HashMap<String, ChangenowApi.SupportedCoinModel> res = new HashMap<>();
                        JSONObject obj = new JSONObject(resp);
                        ChangenowApi.GetRateRespModel respModel = new ChangenowApi.GetRateRespModel();
                        respModel.minimum = BigDecimal.valueOf(obj.getDouble("minAmount"));
                        if (onComplete != null)
                            onComplete.onResponse(status, respModel);
                    } else {
                        if (onComplete != null)
                            onComplete.onResponse(status, null);
                    }
                } catch (Exception e) {
                    Log.e("flint", "ChangenowApi... getMinAmount: " + e.getMessage());
                    if (onComplete != null)
                        onComplete.onResponse("error: " + e.toString(), null);
                }
            }
        });
    }

    public static void generateAddress(final String fromCoin, final String toCoin, final String toAddress, final String extraId, final Callback2<String, ChangenowApi.GenerateAddressRespModel> onComplete) {
        Log.d("flint", "ChangenowApi.generateAddress...");
        HashMap<String, String> params = new HashMap<>();
        params.put("from", fromCoin);
        params.put("to", toCoin);
        params.put("address", toAddress);
        params.put("amount", "12");
        if (extraId != null)
            params.put("extraId", extraId);
        makePostQuery("/api/v1/transactions/"+PUBLIC_API_KEY, params, new Callback2<String, String>() {
            @Override
            public void onResponse(String status, String resp) {
                try {
                    Log.d("flint", "ChangenowApi.generateAddress... resp: " + resp);
                    if ("ok".equals(status)) {
                        HashMap<String, ShapeshiftApi.SupportedCoinModel> res = new HashMap<>();
                        JSONObject obj = new JSONObject(resp);
                        ChangenowApi.GenerateAddressRespModel respModel = new ChangenowApi.GenerateAddressRespModel();
                        respModel.depositAddress = obj.getString("payinAddress");
                        if (onComplete != null)
                            onComplete.onResponse(status, respModel);
                    } else {
                        Log.e("flint", "ChangenowApi.generateAddress... returns error: status="+status+", resp="+resp);
                        if (onComplete != null)
                            onComplete.onResponse(status, null);
                    }
                } catch (Exception e) {
                    Log.e("flint", "ChangenowApi... generateAddress: " + e.getMessage());
                    if (onComplete != null)
                        onComplete.onResponse("error: " + e.toString(), null);
                }
            }
        });
    }

    private static void makeGetQuery(final String subUrl, final Callback2<String, String> callback) {
        makeGetQueryEx(API_URL, subUrl, callback);
    }

    private static void makeGetQueryEx(final String apiUrl, final String subUrl, final Callback2<String, String> callback) {
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
                        String reqUrl = apiUrl + subUrl;
                        Request req = new Request.Builder().url(reqUrl).build();
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

    private static void makePostQuery(final String subUrl, final Map<String,String> params, final Callback2<String, String> callback) {
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
                        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                        String paramsJsonString = new JSONObject(params).toString();
                        RequestBody requestBody = RequestBody.create(JSON, paramsJsonString);
                        String reqUrl = API_URL + subUrl;
                        Request req = new Request.Builder().url(reqUrl).post(requestBody).build();
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

    public static class SupportedCoinModel {
        public String name = "";
        public String symbol = "";
        public String imageUrl = "";
        public boolean isAbailable = false;
    }

    public static class GetRateRespModel {
        public BigDecimal rate = new BigDecimal(0.0);
        public BigDecimal minimum = new BigDecimal(0.0);
    }

    public static class GenerateAddressRespModel {
        public String depositAddress = "";
    }

    private static final String API_URL = "https://changenow.io";
    private static final int TIMEOUT_CONNECT = 8000;
    private static final int TIMEOUT_WRITE = 8000;
    private static final int TIMEOUT_READ = 8000;
    private static final String PUBLIC_API_KEY = "90499effcbdabdf3e63dd9e3df8d0fec3f54318dd58eecf29f8767187d80e1e9";

}
