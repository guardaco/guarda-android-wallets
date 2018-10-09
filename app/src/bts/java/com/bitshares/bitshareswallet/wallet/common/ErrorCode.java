package com.bitshares.bitshareswallet.wallet.common;

public class ErrorCode {
    public static final int ERROR_UNKNOWN = -1;
    public static final int ERROR_FILE_NOT_FOUND = -2;
    public static final int ERROR_FILE_READ_FAIL = -3;
    public static final int ERROR_NO_ACCOUNT_OBJECT = -4;

    public static final int ERROR_IMPORT_NOT_MATCH_PRIVATE_KEY = -5;

    public static final int ERROR_PASSWORD_INVALID = -6;
    public static final int ERROR_NETWORK_FAIL = -7;
    public static final int ERROR_FILE_BIN_PASSWORD_INVALID = -8;

    public static final int ERROR_CONNECT_SERVER_FAILD = -9;
    public static final int ERROR_ACCOUNT_OBJECT_EXIST = -10;

    public static final int ERROR_SERVER_RESPONSE_FAIL = -11;

    public static final int ERROR_SERVER_CREATE_ACCOUNT_FAIL = -12; // server response the error on createing account.

    public static final int ERROR_PASSWORD_CONFIRM_FAIL = -13;
}
