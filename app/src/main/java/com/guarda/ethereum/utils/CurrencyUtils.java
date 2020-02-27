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

import timber.log.Timber;

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
            Timber.d("getCurrencyNameByCode err=%s", e.getMessage());
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

    private final static String NOT_IN_THE_BLOCK = "The previous transaction is not in the block yet. Kindly wait till it gets confirmed";
    private final static String NOT_SYNCED = "Check if the previous transaction was confirmed and sync your wallet";
    private final static String IN_THE_MEMPOOL = "The previous transaction is in the mempool yet. Kindly wait till it gets confirmed";
    private final static String AMOUTN_SMALL = "Amount too small";
    private final static String FEE_SMALL = "Not enough commission fee";

    public static String getBtcLikeError(String msg) {
        //catch not json message first
        if (msg.contains("bad-txns-sapling-binding-signature-invalid") || msg.contains(". Code:-25"))
            return NOT_IN_THE_BLOCK;

        if (msg.contains("bad-txns-shielded-requirements-not-met"))
            return NOT_SYNCED;

        //try to parse json
        JsonElement je;
        try {
            je = new JsonParser().parse(msg).getAsJsonObject();
        } catch (Exception e) {
            Timber.d("getBtcLikeError() - err=%s", e.getMessage());
            return "Sending error. " + msg;
        }
        if (!je.isJsonObject()) return msg;
        String explain = je.getAsJsonObject().get("error").getAsJsonObject().get("message").getAsString();
        switch (explain) {
            case "66: insufficient priority":
                explain = FEE_SMALL;
                break;
            case "64: dust":
                explain = AMOUTN_SMALL;
                break;
            case "258: txn-mempool-conflict":
                explain = IN_THE_MEMPOOL;
                break;
            case "Missing inputs":
                explain = NOT_IN_THE_BLOCK;
                break;
        }
        return explain;
    }

}
