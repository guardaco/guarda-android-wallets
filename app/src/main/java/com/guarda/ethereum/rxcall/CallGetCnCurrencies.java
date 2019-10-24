package com.guarda.ethereum.rxcall;

import com.guarda.ethereum.managers.Callback2;
import com.guarda.ethereum.managers.ChangenowApi;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.utils.GsonUtils;

import java.util.Map;
import java.util.concurrent.Callable;

import timber.log.Timber;


public class CallGetCnCurrencies implements Callable<Map<String, ChangenowApi.SupportedCoinModel>> {

    private SharedManager sharedManager;
    private GsonUtils gsonUtils;
    private Callback2 callCurrencies;

    public CallGetCnCurrencies(SharedManager sharedManager, GsonUtils gsonUtils) {
        this.sharedManager = sharedManager;
        this.gsonUtils = gsonUtils;
    }

    @Override
    public Map<String, ChangenowApi.SupportedCoinModel> call() throws Exception {


        return null;
    }



}
