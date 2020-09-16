package com.guarda.ethereum.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.managers.ChangenowApi;
import com.guarda.ethereum.models.items.SaplingTreeModel;

import java.lang.reflect.Type;
import java.util.Map;

import javax.inject.Inject;


/**
 * Created by samosudovd on 08/06/2018.
 */
public class GsonUtils {

    private Gson gson;

    @Inject
    public GsonUtils(Gson gson) {
        GuardaApp.getAppComponent().inject(this);
        this.gson = gson;
    }

    public String toGsonCurrencies(Map<String, ChangenowApi.SupportedCoinModel> currencyRespArrayList) {
        return gson.toJson(currencyRespArrayList);
    }

    public Map<String, ChangenowApi.SupportedCoinModel> fromGsonCurrencies(String curGson) {
        Type txRespType = new TypeToken<Map<String, ChangenowApi.SupportedCoinModel>>(){}.getType();
        return gson.fromJson(curGson, txRespType);
    }

    public SaplingTreeModel saplingTreeModel(String stringSaplingTreeModel) {
        Type type = new TypeToken<SaplingTreeModel>(){}.getType();
        return gson.fromJson(stringSaplingTreeModel, type);
    }
}
