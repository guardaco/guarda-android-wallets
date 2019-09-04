package com.guarda.ethereum.managers;

import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;

public class CryptocompareHelper {

    private final static String CRYPTOCOMPARE_API_KEY = "c442a0179f0855b688fbc9788475ff9a72f526e31b18e86e41b8c206bbfb7cc8";

    public static void getExchange(String from, String to, ApiMethods.RequestListener listener) {
        Requestor.getExchangeAmountCc(from, to, CRYPTOCOMPARE_API_KEY, listener);
    }

}
