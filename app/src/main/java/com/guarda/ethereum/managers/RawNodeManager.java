package com.guarda.ethereum.managers;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.models.items.TokenBalanceResponse;
import com.guarda.ethereum.models.items.TokenBodyItem;
import com.guarda.ethereum.rest.ApiMethods;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import autodagger.AutoInjector;

/**
 * Created by SV on 03.10.2017.
 */

@AutoInjector(GuardaApp.class)
public class RawNodeManager {

    private static final String BALANCE_OF_PREFIX = "0x70a08231000000000000000000000000";

    @Inject
    Context mContext;

    @Inject
    WalletManager walletManager;

    @Inject
    SharedManager sharedManager;

    private JsonArray requestsArray;
    private List<String> tokensList;
    private Map<String, Integer> tokensDecimals = new HashMap<>();
    private Map<String, String> tokensAddresses = new HashMap<>();
    private List<TokenBodyItem> walletTokensList;
    private List<String> walletTokensCodes;

    public RawNodeManager() {
        GuardaApp.getAppComponent().inject(this);
    }

    private void fillTokensList() {
        tokensList = new ArrayList<>();
        String jsonStr = getJsonFromPrefs();

        JsonParser parser = new JsonParser();
        requestsArray = (JsonArray) parser.parse(jsonStr);
        for (JsonElement jsonElement : requestsArray) {
            String tokenName = jsonElement.getAsJsonObject().get("name").getAsString();
            Integer tokenDecimal = jsonElement.getAsJsonObject().get("decimal").getAsInt();
            tokensDecimals.put(tokenName, tokenDecimal);
            tokensList.add(tokenName);
            JsonElement paramElement = jsonElement.getAsJsonObject().get("params");
            paramElement.getAsJsonArray().get(0).getAsJsonObject().addProperty("data",
                    BALANCE_OF_PREFIX + walletManager.getWalletAddressWithoutPrefix());
            String tokenAddress = paramElement.getAsJsonArray().get(0).getAsJsonObject().get("to").getAsString();
            tokensAddresses.put(tokenName, tokenAddress);
        }
    }


    public void getTokens(final Callback<List<TokenBodyItem>> callback) {
        fillTokensList();
        NodeHelper.getTokens(requestsArray, new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                List<TokenBodyItem> tokenList = new ArrayList<>();
                walletTokensCodes = new ArrayList<String>();
                walletTokensCodes.add(sharedManager.getCurrentCurrency().toUpperCase());
                List<TokenBalanceResponse> tokens = (List<TokenBalanceResponse>) response;
                for (int i = 0; i < tokens.size(); i++) {
                    String valueWithoutPrefix = "...";
                    try {
                        valueWithoutPrefix = tokens.get(i).getResult().substring(2);
                    } catch (Exception e) {
                        Log.e("psd", "RawNodeManager - tokens.get(i).getResult() = " + tokens.get(i).getResult() + " e = " + e.toString());
                        e.printStackTrace();
                    }
                    try {
                        BigInteger value = new BigInteger(valueWithoutPrefix, 16);
                        BigDecimal decimal = new BigDecimal(value);
                        BigDecimal formatted = Converter.fromDecimals(decimal, tokensDecimals.get(tokensList.get(i)));
                        if (decimal.compareTo(new BigDecimal(0)) > 0) {
                            tokenList.add(new TokenBodyItem(tokensList.get(i), formatted, "", tokensDecimals.get(tokensList.get(i))));
                            walletTokensCodes.add(tokensList.get(i));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                walletTokensList = tokenList;
                callback.onResponse(tokenList);
            }

            @Override
            public void onFailure(String msg) {
                Log.d("psd", "NodeHelper.getTokens - " + msg);
            }
        });
    }

    public List<TokenBodyItem> getWalletTokensList() {
        return walletTokensList;
    }

    public void clearTokensList() {
        if (walletTokensList == null)
            walletTokensList = new ArrayList<>();
        walletTokensList.clear();
    }

    public void addTokenToList(TokenBodyItem token) {
        if (walletTokensList == null)
            walletTokensList = new ArrayList<>();
        walletTokensList.add(token);
    }

    public void addTokensList(List<TokenBodyItem> tokens) {
        if (walletTokensList == null)
            walletTokensList = new ArrayList<>();
        walletTokensList.addAll(tokens);
    }

    public void updateTokensCodes() {
        if (walletTokensCodes == null)
            walletTokensCodes = new ArrayList<>();
        walletTokensCodes.clear();
        walletTokensCodes.add(sharedManager.getCurrentCurrency().toUpperCase());
        for (TokenBodyItem tokenBodyItem : walletTokensList) {
            walletTokensCodes.add(tokenBodyItem.getTokenName());
        }
    }

    public List<String> getWalletTokensCodes() {
        return walletTokensCodes;
    }

    public TokenBodyItem getTokenByCode(String code) {
        if (walletTokensCodes == null) return null;

        for (TokenBodyItem tokenItem : walletTokensList) {
            if (tokenItem.getTokenName().equals(code)) {
                return tokenItem;
            }
        }
        return null;
    }

    public String getTokeAddressByCode(String code) {
        return tokensAddresses.get(code);
    }

    private String getJsonFromPrefs() {

        return sharedManager.getTokensInfo();

    }

}

