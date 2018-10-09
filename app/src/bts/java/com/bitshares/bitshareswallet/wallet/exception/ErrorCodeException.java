package com.bitshares.bitshareswallet.wallet.exception;

/**
 * Created by lorne on 01/11/2017.
 */

public class ErrorCodeException extends Exception {
    private int nErrorCode;
    public ErrorCodeException(int nErrorCode, String strMessage) {
        super(strMessage);
        this.nErrorCode = nErrorCode;
    }

    public ErrorCodeException(int nErrorCode, Throwable throwable) {
        super(throwable);
        this.nErrorCode = nErrorCode;
    }

    public int getErrorCode() {
        return nErrorCode;
    }
}
