package com.guarda.ethereum.screens.exchange.first;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.guarda.ethereum.managers.ChangenowApi;
import com.guarda.ethereum.managers.ChangenowManager;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.utils.GsonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.guarda.ethereum.models.constants.Const.UPD_CURRENCIES_DELAY;

public class ExchangeViewModel extends ViewModel {

    private final SharedManager sharedManager;
    private final GsonUtils gsonUtils;
    private String forCoin;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    public MutableLiveData<Map<String, ChangenowApi.SupportedCoinModel>> currencies = new MutableLiveData<>();
    public MutableLiveData<SortedMap<String, ArrayList<String>>> forCoinMap = new MutableLiveData<>();

    public ExchangeViewModel(SharedManager sharedManager,
                             GsonUtils gsonUtils,
                             String forCoin) {
        this.sharedManager = sharedManager;
        this.gsonUtils = gsonUtils;
        this.forCoin = forCoin;
        initState();
    }

    private void initState() {
        Timber.d("initState()");
        //get local list of carrencies or call it from changenow API
        String map = sharedManager.getListCurrencies();
        if (!map.isEmpty()) {
            Map<String, ChangenowApi.SupportedCoinModel> mapCoins = gsonUtils.fromGsonCurrencies(map);
            currencies.setValue(mapCoins);

            //if it's time to update list of currencies
            long s = System.currentTimeMillis();
            if (s > sharedManager.getTimeUpdateCurr() + UPD_CURRENCIES_DELAY)
                getCurrencies(false);
        } else {
            getCurrencies(true);
        }
    }

    private void getCurrencies(boolean isReturnMap) {
        ChangenowApi.getSupportedCoins((String status, Map<String, ChangenowApi.SupportedCoinModel> resp) -> {
            if ("ok".equals(status)) {
                sharedManager.setListCurrencies(gsonUtils.toGsonCurrencies(resp));
                if (isReturnMap) currencies.postValue(resp);
            } else {
                Timber.e("ChangenowApi.getSupportedCoins is NOT ok");
            }
        });
    }

    public void getPairs() {
        compositeDisposable.add(ChangenowManager.changeNowApi().pairsObs()
                .flatMap(pairs -> {
                    SortedMap<String, ArrayList<String>> sortedMap = strToPairs(pairs, forCoin);
                    return Observable.just(sortedMap);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (res) -> {
                            forCoinMap.setValue(res);
                            Timber.d("getPairs res=%s", res);
                        },
                        (e) -> Timber.e("getPairs error=%s", e.getMessage()))
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }

    private static SortedMap<String, ArrayList<String>> strToPairs(ArrayList<String> strPairs, String forCoin) {
        SortedMap<String, ArrayList<String>> pairs = new TreeMap<>();
        ArrayList<String> fromCoins = new ArrayList<>();
        ArrayList<String> toCoins = new ArrayList<>();
        String current = "zec";
        String filerCoin = forCoin.toLowerCase();
        for (String p : strPairs) {
            if (!p.contains(filerCoin)) continue;
            int index = p.indexOf("_");
            if (p.substring(index + 1).equalsIgnoreCase(current)) { fromCoins.add(p.substring(0, index)); }
            if (p.substring(0, index).equalsIgnoreCase(current)) { toCoins.add(p.substring(index + 1)); }
        }

        Collections.sort(fromCoins);
        Collections.sort(toCoins);

        pairs.put("from", fromCoins);
        pairs.put("to", toCoins);

        return pairs;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        private final SharedManager sharedManager;
        private final GsonUtils gsonUtils;
        private final String forCoin;

        public Factory(SharedManager sharedManager,
                       GsonUtils gsonUtils,
                       String forCoin) {
            this.sharedManager = sharedManager;
            this.gsonUtils = gsonUtils;
            this.forCoin = forCoin;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new ExchangeViewModel(
                    this.sharedManager,
                    this.gsonUtils,
                    this.forCoin);
        }

    }

}
