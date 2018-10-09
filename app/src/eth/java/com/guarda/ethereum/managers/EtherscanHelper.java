package com.guarda.ethereum.managers;

import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;

import static com.guarda.ethereum.models.constants.Etherscan.ETHERSCAN_API_KEY;

/**
 * Created by SV on 18.08.2017.
 */

public class EtherscanHelper {

    public static void getTransactions(String address, ApiMethods.RequestListener listener){
        Requestor.getTransactions(address, ETHERSCAN_API_KEY, listener);
    }

}
