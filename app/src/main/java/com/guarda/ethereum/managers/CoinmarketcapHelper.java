package com.guarda.ethereum.managers;

import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;

public class CoinmarketcapHelper {

    public static void getExchange(String from, String to, ApiMethods.RequestListener listener){
        Requestor.getExchangeAmountCmc(from, to, listener);
    }
}
