package com.guarda.ethereum.models.constants;


public interface ZecExplorer {

    String TRANSACTION_DETAILS = "https://blockchain.info/tx/";
    String BLOCKCHAIN_INFO_KEY = "8e83fce3-96fb-4334-8078-34c45971cf0c";
//    String ZEC_EXPLORER_BASE_URL = "https://zcashnetwork.info/api/";
    /**
     * Mainnet
     */
    String ZEC_EXPLORER_BASE_URL = "https://zecblockexplorer.com";
    String ZEC_EXPLORER_API = ZEC_EXPLORER_BASE_URL + "/api/";
    String ZEC_EXPLORER_TX = ZEC_EXPLORER_BASE_URL + "/tx/";

    /**
     * Testnet
     */
    String ZEC_EXPLORER_BASE_URL_TESTNET = "https://explorer.testnet.z.cash";
    String ZEC_EXPLORER_API_TESTNET = ZEC_EXPLORER_BASE_URL_TESTNET + "/api/";
    String ZEC_EXPLORER_TX_TESTNET = ZEC_EXPLORER_BASE_URL_TESTNET + "/tx/";

    /**
     * Mainnet
     */
    String ZEC_BOOK_BASE_URL = "https://zecbook.guarda.co";
    String ZEC_BOOK_API = ZEC_BOOK_BASE_URL + "/api/v2/";
    String ZEC_BOOK_TX = ZEC_BOOK_BASE_URL + "/tx/";

    /**
     * Testnet
     */
    String ZEC_BOOK_BASE_URL_TESTNET = "https://zecbook-testnet.guarda.co";
    String ZEC_BOOK_API_TESTNET = ZEC_BOOK_BASE_URL + "/api/v2/";
    String ZEC_BOOK_TX_TESTNET = ZEC_BOOK_BASE_URL + "/tx/";
}
