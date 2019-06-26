package com.guarda.ethereum.models.constants;


public interface ZecExplorer {

    String TRANSACTION_DETAILS = "https://blockchain.info/tx/";
    String BLOCKCHAIN_INFO_KEY = "8e83fce3-96fb-4334-8078-34c45971cf0c";
//    String ZEC_EXPLORER_BASE_URL = "https://zcashnetwork.info/api/";
    String ZEC_EXPLORER_BASE_URL = "https://zec-testnet.guarda.co"; //FIXME: TESTNET
//    String ZEC_EXPLORER_BASE_URL = "https://zecblockexplorer.com";
    String ZEC_EXPLORER_API = ZEC_EXPLORER_BASE_URL + "/api/";
    String ZEC_EXPLORER_TX = ZEC_EXPLORER_BASE_URL + "/tx/";
}
