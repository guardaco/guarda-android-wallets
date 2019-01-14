package com.guarda.ethereum.models.constants;

import java.math.BigDecimal;

public class Common {

    public final static String BLOCK = "passphrase";
    public final static String BTC_NODE_LOGIN = "guarda";
    public final static String BTC_NODE_PASS = "wWACDJg0vUUi4RqdseeMi-3t8n6YOFQmwS5GF31MLH0=";
    public final static String NODE_ADDRESS = "https://kmd.guarda.co";
    public final static String MAIN_CURRENCY = "kmd";
    public final static String MAIN_CURRENCY_NAME = "komodo";
    public final static String ETH_SHOW_PATTERN = "#,##0.#####";

    public final static String TERM_OF_USE_LINK = "https://guarda.co/terms-of-service?isWebView=1";
    public final static String PRIVACY_POLICE_LINK = "https://guarda.co/privacy-policy?isWebView=1";
    public final static String ABOUT_APP_LINK = "https://guarda.co/about";
    public final static String SITE_APP_LINK = "https://guarda.co/";

    /*extras*/
    public final static String EXTRA_TRANSACTION_POSITION = "EXTRA_TRANSACTION_POSITION";

    public final static int DEFAULT_GAS_LIMIT = 21000;
    public final static int DEFAULT_GAS_LIMIT_FOR_CONTRACT = 21000;
    public final static int KMD_MIN_CONFIRM = 1;

    public final static String TOKENS_FILE_NAME = "etc_tokens_request.json";
    public final static String EXTRA_FIELDS = "extra-fields.json";

    public final static String BIP_39_WORDLIST_ASSET = "bip39-wordlist.txt";
    public final static int MNEMONIC_WORDS_COUNT = 12;

    public final static BigDecimal AVG_TX_SIZE_KB = new BigDecimal("0.650");
}
