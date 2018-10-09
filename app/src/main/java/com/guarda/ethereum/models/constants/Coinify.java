package com.guarda.ethereum.models.constants;


public interface Coinify {

    String COINIFY_BASE_URL = "https://app-api.coinify.com";
//    String SANDBOX_COINIFY_BASE_URL = "https://app-api.sandbox.coinify.com";
    String SANDBOX_COINIFY_BASE_URL = "https://app-api.coinify.com";

    String COINIFY_GRANT_TYPE_OFFLINE_TOKEN = "offline_token";
    String COINIFY_GRANT_TYPE_EMAIL_PASS = "password";
    long DELTA_ACCESS_TOKEN_LIFE_TIME = 5 * 60 * 1000;
}
