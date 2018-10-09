package com.bitshares.bitshareswallet.wallet.exception;

import java.io.IOException;
import java.net.UnknownHostException;



public class NetworkStatusException extends Exception{
    public NetworkStatusException(String strMessage) {
        super(strMessage);
    }

    public NetworkStatusException(Throwable throwable) {
        super(throwable);
    }
}
