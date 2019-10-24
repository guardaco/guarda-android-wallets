package com.guarda.ethereum.views.activity;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.guarda.ethereum.managers.ChangenowApi;

import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;

import io.reactivex.disposables.CompositeDisposable;

public class SharedViewModel extends ViewModel {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    public MutableLiveData<Map<String, ChangenowApi.SupportedCoinModel>> currencies = new MutableLiveData<>();

    public MutableLiveData<String> selectedExchange = new MutableLiveData<>();
    public MutableLiveData<ChangenowApi.SupportedCoinModel> selectedFrom = new MutableLiveData<>();
    public MutableLiveData<ChangenowApi.SupportedCoinModel> selectedTo = new MutableLiveData<>();


    public SharedViewModel() {
        initState();
    }

    private void initState() {

    }

    public void selectExchange(String exchange) {
        selectedExchange.setValue(exchange);
    }

    public void selectFrom(ChangenowApi.SupportedCoinModel from) {
        selectedFrom.setValue(from);
    }

    public void selectTo(ChangenowApi.SupportedCoinModel to) {
        selectedTo.setValue(to);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }

}
