package com.guarda.ethereum.utils;


import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public final class CurrencyUtils {

    private CurrencyUtils() {
    }

    public static HashMap<String, String> getCurrencyNameByCode(Context context) {
        HashMap<String, String> map = new HashMap<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("ListOfCurrencies.txt"), "UTF-8"));

            String mLine;
            while ((mLine = reader.readLine()) != null) {

                String[] splited = mLine.split("\\s+");

                String currency = "";
                for (int i = 1; i < splited.length; i++) {
                    currency += " " + splited[i];
                }

                map.put(splited[0].toUpperCase(), currency);

            }
        } catch (IOException e) {
            Log.d("CurrencyUtils", e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }

    public static String getBtcLikeError(String msg) {
        JsonElement je;
        try {
            je = new JsonParser().parse(msg).getAsJsonObject();
        } catch (Exception e) {
            Log.d("psd", "CurrencyUtils.getBtcLikeError() - " + e.getMessage());
            return "Sending error.";
        }
        if (!je.isJsonObject()) return "Sending error";
        String explain = je.getAsJsonObject().get("error").getAsJsonObject().get("message").getAsString();
        switch (explain) {
            case "66: insufficient priority":
                explain = "Not enough commission fee";
                break;
            case "64: dust":
                explain = "Amount too small";
                break;
            case "258: txn-mempool-conflict":
                explain = "The previous transaction is in the mempool. Wait, please";
                break;
            case "Missing inputs":
                explain = "The previous transaction is not in the block. Wait, please";
                break;
        }
        return explain;
    }

}
