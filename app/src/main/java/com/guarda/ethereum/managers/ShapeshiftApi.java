package com.guarda.ethereum.managers;

import android.util.Log;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShapeshiftApi {

    public static void getSupportedCoins(final Callback2<String, Map<String, SupportedCoinModel>> onComplete) {
        makeGetQuery("/getcoins", new Callback2<String, String>() {
            @Override
            public void onResponse(String status, String resp) {
                try {
                    if ("ok".equals(status)) {
                        HashMap<String, SupportedCoinModel> res = new HashMap<>();
                        JSONObject obj = new JSONObject(resp);
                        Iterator<?> keys = obj.keys();
                        while(keys.hasNext()) {
                            String key = (String)keys.next();
                            JSONObject coinObj = obj.getJSONObject(key);
                            SupportedCoinModel coinModel = new SupportedCoinModel();
                            coinModel.name = coinObj.getString("name");
                            coinModel.symbol = coinObj.getString("symbol");
                            coinModel.imageUrl = coinObj.getString("image");
                            coinModel.isAbailable = "available".equals(coinObj.getString("status"));
                            res.put(coinModel.symbol, coinModel);
                        }
                        if (onComplete != null)
                            onComplete.onResponse(status, res);
                    } else {
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



    public static void getRate(final String fromCoin, final String toCoin, final Callback2<String, GetRateRespModel> onComplete) {
        final String pair = fromCoin + "_" + toCoin;
        makeGetQuery("/marketinfo/" + pair, new Callback2<String, String>() {
            @Override
            public void onResponse(String status, String resp) {
                try {
                    Log.d("flint", "resp ("+pair+"): " + resp);
                    if ("ok".equals(status)) {
                        HashMap<String, SupportedCoinModel> res = new HashMap<>();
                        JSONObject obj = new JSONObject(resp);
                        GetRateRespModel respModel = new GetRateRespModel();
                        respModel.rate = BigDecimal.valueOf(obj.getDouble("rate"));
                        respModel.minimum = BigDecimal.valueOf(obj.getDouble("minimum"));
                        if (onComplete != null)
                            onComplete.onResponse(status, respModel);
                    } else {
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



    public static void generateAddress(final String fromCoin, final String toCoin, final String toAddress, final String returnAddress, final Callback2<String, GenerateAddressRespModel> onComplete) {
        HashMap<String, String> params = new HashMap<>();
        params.put("pair", fromCoin + "_" + toCoin);
        params.put("withdrawal", toAddress);
        params.put("apiKey", PUBLIC_API_KEY);
        params.put("returnAddress", returnAddress);
        makePostQuery("/shift", params, new Callback2<String, String>() {
            @Override
            public void onResponse(String status, String resp) {
                try {
                    if ("ok".equals(status)) {
                        HashMap<String, SupportedCoinModel> res = new HashMap<>();
                        JSONObject obj = new JSONObject(resp);
                        GenerateAddressRespModel respModel = new GenerateAddressRespModel();
                        respModel.depositAddress = obj.getString("deposit");
                        if (onComplete != null)
                            onComplete.onResponse(status, respModel);
                    } else {
                        Log.e("flint", "ShapeshiftApi.generateAddress... returns error: status="+status+", resp="+resp);
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




    // it is not shapeshift's api, it's file from our server
    public static void getCoinsExternalInfo(final Callback2<String, Map<String, CoinExternalInfoModel>> onComplete) {
        makeGetQueryEx("https://eth.guarda.co/CryptoCurrencies.json", "", new Callback2<String, String>() {
            @Override
            public void onResponse(String status, String resp) {
                try {
                    JSONObject obj = new JSONObject(resp);
                    HashMap<String, CoinExternalInfoModel> respMap = new HashMap<>();
                    Iterator<?> keys = obj.keys();
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        JSONObject coinObj = obj.getJSONObject(key);
                        CoinExternalInfoModel coin = new CoinExternalInfoModel();
                        coin.name = coinObj.getString("name");
                        coin.code = coinObj.getString("code");
                        try {coin.iconURL = coinObj.getString("iconURL");} catch (Exception e) {}
                        try {coin.memo = coinObj.getString("memo");} catch (Exception e) {coin.memo = null;}
                        respMap.put(coin.code, coin);
                    }
                    if (onComplete != null)
                        onComplete.onResponse(status, respMap);
                } catch (Exception e) {
                    Log.e("flint", "ShapeshiftApi.getCoinsExternalInfo... returns error: status="+status+", resp="+resp);
                    if (onComplete != null)
                        onComplete.onResponse(status, null);
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



    public static class GenerateAddressRespModel {
        public String depositAddress = "";
    }



    public static class GetRateRespModel {
        public BigDecimal rate = new BigDecimal(0.0);
        public BigDecimal minimum = new BigDecimal(0.0);
    }



    public static class CoinExternalInfoModel {
        public String name = "";
        public String code = "";
        public String iconURL = "";
        public String memo = null;
    }



    private static final String API_URL = "https://shapeshift.io";
    private static final int TIMEOUT_CONNECT = 10000;
    private static final int TIMEOUT_WRITE = 10000;
    private static final int TIMEOUT_READ = 10000;
    private static final String PUBLIC_API_KEY = "59e9738f9726d5db4b3196d675e5527c63caad5e5cbddf1dc2c1c166077dfd487eb03d5cdad59ca04aaa749ab5698581afd04ce4b8f7800056b457fba944e2cd";

}
