package com.guarda.ethereum.managers;


import androidx.annotation.Nullable;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;

public class BitcoinNodeManager {

    private static String TAG = "BitcoinNodeManager";


    BitcoinNodeManager() throws Exception {

    }


    public static void sendTransaction(String rawTx, ApiMethods.RequestListener listener) {
//        TODO: check json body
        JsonArray array = new JsonArray();
        array.add(rawTx);

        JsonObject json = getMainJsonParam("sendrawtransaction");
        json.add("params", array);
        Log.d("svcom", "request json: " + json.toString());
        Requestor.sendRawTransaction(json, listener);
    }

    private static JsonObject getMainJsonParam(String method) {
        JsonObject json = new JsonObject();
        json.addProperty("jsonrpc", "2.0");
        json.addProperty("method", method);
        json.addProperty("id", 1);

        return json;
    }

}
