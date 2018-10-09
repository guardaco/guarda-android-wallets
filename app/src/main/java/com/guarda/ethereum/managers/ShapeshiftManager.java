package com.guarda.ethereum.managers;

import android.util.Log;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShapeshiftManager {

    private ShapeshiftManager() {
    }



    public static ShapeshiftManager getInstance() {
        return instance_s;
    }



    public void updateSupportedCoinsList(final Callback<Boolean> callback) {
        ShapeshiftApi.getSupportedCoins(new Callback2<String, Map<String, ShapeshiftApi.SupportedCoinModel>>() {
            @Override
            public void onResponse(String status, Map<String, ShapeshiftApi.SupportedCoinModel> resp) {
                if ("ok".equals(status)) {
                    supportedCoins.clear();
                    for (String key : resp.keySet()) {
                        ShapeshiftApi.SupportedCoinModel coin = resp.get(key);
                        if (coin.isAbailable)
                            supportedCoins.add(resp.get(key));
                    }
                    if (callback != null)
                        callback.onResponse(true);
                } else {
                    if (callback != null)
                        callback.onResponse(false);
                }
            }
        });
    }



    public void getRate(final String fromCoin, final String toCoin, final Callback<ShapeshiftApi.GetRateRespModel> callback) {
        ShapeshiftApi.getRate(fromCoin, toCoin, new Callback2<String, ShapeshiftApi.GetRateRespModel>() {
            @Override
            public void onResponse(String status, ShapeshiftApi.GetRateRespModel resp) {
                if ("ok".equals(status))
                    callback.onResponse(resp);
                else
                    callback.onResponse(new ShapeshiftApi.GetRateRespModel());
            }
        });
    }



    public List<ShapeshiftApi.SupportedCoinModel> getSupportedCoins() {
        return supportedCoins;
    }






    private static ShapeshiftManager instance_s = new ShapeshiftManager();

    private ArrayList<ShapeshiftApi.SupportedCoinModel> supportedCoins = new ArrayList<>();

}
