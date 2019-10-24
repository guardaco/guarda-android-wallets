package com.guarda.ethereum.managers;


import android.content.Context;
import android.util.Log;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.items.CryptoItem;
import com.guarda.ethereum.models.items.ResponseCurrencyItem;
import com.guarda.ethereum.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import autodagger.AutoInjector;

import static com.guarda.ethereum.models.constants.Common.MAIN_CURRENCY;

@AutoInjector(GuardaApp.class)
public class CurrencyListHolder {

    @Inject
    SharedManager sharedManager;

    private List<CryptoItem> listOfCurrencies;

    public List<CryptoItem> getListOfCurrencies() {
        return listOfCurrencies;
    }

    private void setListOfCurrencies(List<CryptoItem> listOfCurrencies) {
        this.listOfCurrencies = prepareListCurrencies(listOfCurrencies);
    }

    private List<CryptoItem> prepareListCurrencies(List<CryptoItem> listOfCurrencies) {
        List<CryptoItem> list = new ArrayList<>();
        for (CryptoItem item : listOfCurrencies) {
            if (item.getName().charAt(0) == ' ') {
                item.setName(item.getName().replaceFirst(" ", ""));
            }

            if (!item.getCode().equalsIgnoreCase(MAIN_CURRENCY)) {
                list.add(item);
            }
        }
        sortListByAlphabetic(list);
        return list;
    }

    public List<CryptoItem> castResponseCurrencyToCryptoItem(ResponseCurrencyItem responseCurrency, Context context) {
        List<CryptoItem> items = new ArrayList<>();
        HashMap<String, String> map = getMapOfCurrencies(context);
        for (String currencyCode : responseCurrency.getResult()) {
            CryptoItem crypto = new CryptoItem();
            currencyCode = currencyCode.toUpperCase();
            crypto.setCode(currencyCode);
            crypto.setName(map.containsKey(currencyCode) ? map.get(currencyCode) : currencyCode);

            items.add(crypto);
        }

        sortListByAlphabetic(items);
        setListOfCurrencies(items);

        Boolean btcCurrencyFound = false;
        Boolean ourCurrencyFound = false;
        String btcCurrency = "BTC";
        String mainCurrency = String.valueOf(Common.MAIN_CURRENCY).toUpperCase();
        for (CryptoItem itm : items) {
            String itemCurrency = itm.getCode().toUpperCase();
            if (btcCurrency.equals(itemCurrency))
                btcCurrencyFound = true;
            if (mainCurrency.equals(itemCurrency))
                ourCurrencyFound = true;
        }
        Log.d("flint", "btcCurrencyFound: " + btcCurrencyFound + ", ourCurrencyFound: " + ourCurrencyFound);
        //SharedManager.flag_disable_buy_menu = true; // !!! since we have 3 exchange services, make buy menu initially enabled
        SharedManager.flag_disable_purchase_menu = true;
        if (ourCurrencyFound && btcCurrencyFound) {
            //SharedManager.flag_disable_buy_menu = false;
            SharedManager.flag_disable_purchase_menu = false;
        } else if (ourCurrencyFound) {
            SharedManager.flag_disable_purchase_menu = false;
        }
        SharedManager.flag_disable_purchase_menu = false; // !!! since we have two exchange services, changelly's check for exchange activity was disabled

        return items;
    }

    private HashMap<String, String> getMapOfCurrencies(Context context) {
        return CurrencyUtils.getCurrencyNameByCode(context);
    }

    private List<CryptoItem> sortListByAlphabetic(List<CryptoItem> list) {
        Collections.sort(list, new Comparator<CryptoItem>() {
            @Override
            public int compare(CryptoItem s1, CryptoItem s2) {
                String firstName = (s1.getName().equalsIgnoreCase(s1.getCode())? s1.getCode():s1.getName());
                String secondName = (s2.getName().equalsIgnoreCase(s2.getCode())? s2.getCode():s2.getName());
                return firstName.compareToIgnoreCase(secondName);
            }
        });
        return list;
    }
}
