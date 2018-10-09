package com.guarda.ethereum.managers;

import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;

/**
 * Created by flint on 03.04.2018.
 */

public class GastrackerHelper {

    public static void getTransactions(String address, ApiMethods.RequestListener listener){
        Requestor.getTransactionsGastracker(address, listener);
    }

}
