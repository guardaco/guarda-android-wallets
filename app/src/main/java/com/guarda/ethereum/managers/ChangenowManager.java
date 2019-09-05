package com.guarda.ethereum.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChangenowManager {

    public static ChangenowManager getInstance() {
        return instance_s;
    }



    public void updateSupportedCoinsList(final Callback<Boolean> callback) {
        ChangenowApi.getSupportedCoins(new Callback2<String, Map<String, ChangenowApi.SupportedCoinModel>>() {
            @Override
            public void onResponse(String status, Map<String, ChangenowApi.SupportedCoinModel> resp) {
                if ("ok".equals(status)) {
                    supportedCoins.clear();
                    for (String key : resp.keySet()) {
                        ChangenowApi.SupportedCoinModel coin = resp.get(key);
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



    public void getRate(final String fromCoin, final String toCoin, final Callback<ChangenowApi.GetRateRespModel> callback) {
        ChangenowApi.getRate(fromCoin, toCoin, new Callback2<String, ChangenowApi.GetRateRespModel>() {
            @Override
            public void onResponse(String status, ChangenowApi.GetRateRespModel resp) {
                if ("ok".equals(status))
                    callback.onResponse(resp);
                else
                    callback.onResponse(new ChangenowApi.GetRateRespModel());
            }
        });
    }



    public void getMinAmount(final String fromCoin, final String toCoin, final Callback<ChangenowApi.GetRateRespModel> callback) {
        ChangenowApi.getMinAmount(fromCoin, toCoin, new Callback2<String, ChangenowApi.GetRateRespModel>() {
            @Override
            public void onResponse(String status, ChangenowApi.GetRateRespModel resp) {
                if ("ok".equals(status))
                    callback.onResponse(resp);
                else
                    callback.onResponse(new ChangenowApi.GetRateRespModel());
            }
        });
    }



    public List<ChangenowApi.SupportedCoinModel> getSupportedCoins() {
        return supportedCoins;
    }

    private static ChangenowManager instance_s = new ChangenowManager();

    private ArrayList<ChangenowApi.SupportedCoinModel> supportedCoins = new ArrayList<>();

}
