package com.guarda.ethereum.managers;

import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;


/**
 * Created by SV on 18.08.2017.
 */

public class CallistoExplorerHelper {

    public static void getTransactions(String address, ApiMethods.RequestListener listener){
        Requestor.getTransactions(address, listener);
    }
}
