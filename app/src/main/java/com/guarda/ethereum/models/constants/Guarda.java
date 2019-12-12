package com.guarda.ethereum.models.constants;

/**
 *
 * Created by SV on 18.08.2017.
 */

public interface Guarda {

    String GUARDA_CO_URL = "https://guarda.co";
    String GUARDA_ETC_URL = "https://etc.guarda.co";
    String GUARDA_ETH_URL = "https://eth.guarda.co";
    String GUARDA_LOGGING = "https://guarda.co";
    String GUARDA_INRETNAL = "/api/v2/internal/";

    //Logger error types
    String ERROR_SENDING_WRONG_NETWORK = "SHIELDED_SEND_TX_WNE";
    String ERROR_SENDING_COMMON_ZCASHEXCEPTION = "ERROR_SENDING_COMMON_ZCASHEXCEPTION";
    String ERROR_SENDING_COMMON_EXCEPTION = "ERROR_SENDING_COMMON_EXCEPTION";
    String ERROR_SENDING_Z_ADDRESS_SYNCING = "ERROR_SENDING_Z_ADDRESS_SYNCING";
    String ERROR_SENDING_BUILDING_Z_Z = "ERROR_SENDING_BUILDING_Z_Z";
    String ERROR_SENDING_BUILDING_Z_T = "ERROR_SENDING_BUILDING_Z_T";
    String ERROR_SENDING_BUILDING_T_Z = "ERROR_SENDING_BUILDING_T_Z";
    String ERROR_SENDING_BUILDING_T_T = "ERROR_SENDING_BUILDING_T_T";
    String ERROR_SENDING_NODE_RESPONSE = "ERROR_SENDING_NODE_RESPONSE";

}
