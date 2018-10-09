package com.guarda.ethereum.managers;

import com.google.gson.JsonArray;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;


/**
 * Created by SV on 18.08.2017.
 */

public class NodeHelper {

    public static void getTokens(JsonArray param, ApiMethods.RequestListener listener){
        Requestor.getTokens(param, listener);
    }
}
