/*
 * Copyright (C) 2017 IOTA Foundation
 *
 * Authors: pinpong, adrianziser, saschan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.iota.wallet.helper;

public class Constants {

    // global constants

    //Preferences
    public static final String PREFERENCE_NODE_PROTOCOL = "preference_node_protocol";
    public static final String PREFERENCE_NODE_IP = "preference_node_ip";
    public static final String PREFERENCE_NODE_PORT = "preference_node_port";
    public static final String PREFERENCE_ENC_SEED = "preference_enc_seed";
    public static final String PREFERENCE_WALLET_VALUE_CURRENCY = "preference_wallet_currency";
    public static final String PREFERENCE_RUN_WITH_ROOT = "preference_run_with_root";
    public static final String PREFERENCES_CURRENT_IOTA_BALANCE = "preference_current_iota_balance";
    public static final String PREFERENCES_LOCAL_POW = "preference_local_pow";
    public static final String PREFERENCE_ISSUE_REPORTER = "preference_issue_reporter";
    public static final String PRICE_STORAGE_PREFIX = "exchange_rate_storage";

    //Preferences defaults
    public static final String PREFERENCE_NODE_DEFAULT_PROTOCOL = "http";
    public static final String PREFERENCE_NODE_DEFAULT_IP = "node.iotawallet.info";
    public static final String PREFERENCE_NODE_DEFAULT_PORT = "14265";

    public static final int REQUEST_CODE_LOGIN = 101;

    public static final int REQUEST_STORAGE_PERMISSION = 1;
    public static final int REQUEST_CAMERA_PERMISSION = 12;

    public static final String QRCODE = "qrcode";
    public static final String UDP = "udp://";
    public static final String NEW_ADDRESS_TAG = "NEW9ADDRESS9999999999999999";
    public static final String NEW_TRANSFER_TAG = "ANDROID9WALLET9TRANSFER9999";
    public static final String TANGLE_EXPLORER_SEARCH_ITEM = "searchItem";

    //Intent actions
    public static final String ACTION_MAIN = "ACTION_MAIN";
    public static final String ACTION_SEND_TRANSFER = "sendTransfer";
    public static final String ACTION_GENERATE_QR_CODE = "generateQrCode";

}
