package com.guarda.ethereum.managers;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.utils.Coders;
import com.guarda.ethereum.utils.KeyStoreUtils;

import javax.inject.Inject;

import autodagger.AutoInjector;
import de.adorsys.android.securestoragelibrary.SecurePreferences;

@AutoInjector(GuardaApp.class)
public class SharedManager {

    public static final String GUARDA_SHAR_PREF = "SecureSharedPrefName";
    public static final String ENCRYPTED_AES = "ENCRYPTED_AES";

    private static final String IS_PIN_CODE_ENABLE = "isPinCodeEnable";
    private static final String IS_SECURE_STORAGE_SUPPORTED = "IS_SECURE_STORAGE_SUPPORTED";
    private static final String ENCRYPTED_LAST_BLOCK = "ENCRYPTED_LAST_BLOCK";
    private static final String PIN_AFTER_RESUME = "PIN_AFTER_RESUME";
    private static final String IS_SHOW_PIN_AFTER_CONGRATS = "IS_SHOW_PIN_AFTER_CONGRATS";
    private static final String HAS_WIF_XPRV_KEYS = "HAS_WIF_XPRV_KEYS";
    private static final String PIN_CODE = "pinCode";
    private static final String USER_EMAIL = "userEmail";
    private static final String CURRENT_CURRENCY = "currentCurrency";
    private static final String LOCAL_CURRENCY = "localCurrency";
    private static final String LAST_SYNCED_BLOCK = "blockChain";
    private static final String WALLET_EMAIL = "walletEmail";
    private static final String WALLET_ID = "grd_walletId";
    private static final String TIME_WRONG_PIN_CODE_CHECKER = "timeWrongPinCodeChecker";
    private static final String PIN_BLOCK_TYPE = "pinBlockType";
    private static final String TOKENS_INFO = "tokenType";
    private static final String LAST_TOKEN_INFO_SYNC = "LAST_TOKEN_INFO_SYNC";
    private static final String IS_ASK_RATE = "askRate";
    private static final String IS_WAIT_FOUR_TR = "IS_WAIT_FOUR_TR";
    private static final String NEXT_TIME_SHOW_RATE = "nextShowRate";
    private static final String IS_SHOW_BACKUP_ALERT = "IS_SHOW_BACKUP_ALERT";
    private static final String LOAD_JSON_EXCEPTION = "LOAD_JSON_EXCEPTION";
    private static final String TXS_CACHE = "TXS_CACHE";
    private static final String MAP_CONTR_TOKENS = "MAP_CONTR_TOKENS";
    private static final String COINIFY_EMAIL = "COINIFY_EMAIL";
    private static final String COINIFY_OFFLINE_TOKEN = "COINIFY_OFFLINE_TOKEN";
    private static final String COINIFY_ACCESS_TOKEN_LIFE_TIME = "COINIFY_ACCESS_TOKEN_LIFE_TIME";
    private static final String COINIFY_ACCESS_TOKEN = "COINIFY_ACCESS_TOKEN";
    private static final String COINIFY_LAST_TRADE = "COINIFY_LAST_TRADE";
    private static final String COINIFY_LAST_STATE = "COINIFY_LAST_STATE";
    private static final String COINIFY_LAST_TRADE_TIME = "COINIFY_LAST_TRADE_TIME";
    private static final String COINIFY_TRADE = "COINIFY_TRADE";
    private static final String CUSTOM_NODE = "CUSTOM_NODE";
    private static final String LAST_CHANGELLY_CURR_LIST = "LAST_CHANGELLY_CURR_LIST";
    private static final String TIME_UPDATE_CURR = "TIME_UPDATE_CURR";
    private static final String LIST_CURRENCIES = "LIST_CURRENCIES";

    public static boolean flag_disable_buy_menu = false;
    public static boolean flag_disable_purchase_menu = false;
    public static boolean flag_create_new_wallet_screen = false;
    public static boolean flag_etc_eth_private_key_showing_fix = false;
//    public static boolean isPinCodeEnable = false;

    @Inject
    Context context;
    @Inject
    KeyStoreUtils keyStoreUtils;

    private SharedPreferences settings;

    public SharedManager() {
        GuardaApp.getAppComponent().inject(this);
        settings = context.getSharedPreferences(GUARDA_SHAR_PREF, Context.MODE_PRIVATE);
    }

    public Context getContext() {
        return context;
    }

    public boolean getIsPinCodeEnable() {
        return settings.getBoolean(IS_PIN_CODE_ENABLE, false);
    }

    public void setIsPinCodeEnable(boolean isPinCodeEnable) {
        settings.edit().putBoolean(IS_PIN_CODE_ENABLE, isPinCodeEnable).apply();
    }

    //for devices that are not support SecurePreferences
    public boolean getIsSecureStorageSupported() {
        return settings.getBoolean(IS_SECURE_STORAGE_SUPPORTED, true);
    }

    public void setIsSecureStorageSupported(boolean isSecureStorageSupported) {
        settings.edit().putBoolean(IS_SECURE_STORAGE_SUPPORTED, isSecureStorageSupported).apply();
    }

    public boolean getPinWasCorrect() {
        return settings.getBoolean(PIN_AFTER_RESUME, false);
    }

    public void setPinWasCorrect(boolean pinAfterResume) {
        settings.edit().putBoolean(PIN_AFTER_RESUME, pinAfterResume).apply();
    }

    public boolean getIsShowPinAfterCongrats() {
        return settings.getBoolean(IS_SHOW_PIN_AFTER_CONGRATS, false);
    }

    public void setIsShowPinAfterCongrats(boolean isShowPinAfterCongrats) {
        settings.edit().putBoolean(IS_SHOW_PIN_AFTER_CONGRATS, isShowPinAfterCongrats).apply();
    }

    public String getPinCode() {
        return settings.getString(PIN_CODE, "");
    }

    public void setPinCode(String pinCode) {
        settings.edit().putString(PIN_CODE, pinCode).apply();
    }

    public String getUserEmail() {
        return settings.getString(USER_EMAIL, "");
    }

    public void setUserEmail(String email) {
        settings.edit().putString(USER_EMAIL, email).apply();
    }

    public String getCurrentCurrency() {
        return settings.getString(CURRENT_CURRENCY, Common.MAIN_CURRENCY);
    }

    public void setCurrentCurrency(String currency) {
        settings.edit().putString(CURRENT_CURRENCY, currency).apply();
    }

    public String getLocalCurrency() {
        return settings.getString(LOCAL_CURRENCY, "usd");
    }

    public void setLocalCurrency(String currency) {
        settings.edit().putString(LOCAL_CURRENCY, currency).apply();
    }

    public boolean getHasWifXprvKeys() {
        return settings.getBoolean(HAS_WIF_XPRV_KEYS, false);
    }

    public void setHasWifXprvKeys(boolean isShowPinAfterCongrats) {
        settings.edit().putBoolean(HAS_WIF_XPRV_KEYS, isShowPinAfterCongrats).apply();
    }

    public String getLastSyncedBlock() {
        if (getIsSecureStorageSupported()) {
            String legasyStorage = SecurePreferences.getStringValue(LAST_SYNCED_BLOCK, "");
            //set to the new storage and disable legacy storage
            if (legasyStorage != null && !legasyStorage.isEmpty()) {
//                Log.d("psd", "getLastSyncedBlock legasyStorage = " + legasyStorage);
                setLastSyncedBlock(legasyStorage);
                setIsSecureStorageSupported(false);

                if (getHasWifXprvKeys()) {
                    //because previously version cointains setters for xprv and wif without decodeBase64()
                    if (legasyStorage.substring(0, 4).equalsIgnoreCase("xprv") ||
                            ((legasyStorage.length() == 51 || legasyStorage.length() == 52) && !legasyStorage.trim().contains(" "))) {
                        return Coders.encodeBase64(legasyStorage);
                    } else {
                        return legasyStorage;
                    }
                } else {
                    return legasyStorage;
                }
            }
//            Log.d("psd", "getLastSyncedBlock SecureStorageSupported legasyStorage = " + legasyStorage);

            return "";
        } else {
            String encrypted = settings.getString(ENCRYPTED_LAST_BLOCK, "");
//            Log.d("psd", "getLastSyncedBlock no SecureStorageSupported encrypted = " + encrypted);
            if (!encrypted.isEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    Log.d("psd", "getLastSyncedBlock no SecureStorageSupported encrypted = " + encrypted);
                    return Coders.encodeBase64(keyStoreUtils.decryptData(encrypted));
                } else {
                    try {
                        return Coders.encodeBase64(keyStoreUtils.decryptOld(encrypted));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "";
                    }
                }
            } else {
                return "";
            }
        }
    }

    public void setLastSyncedBlock(String wallet) {
        String encrypted = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Log.d("psd", "setLastSyncedBlock() wallet = " + wallet);
            encrypted = keyStoreUtils.encryptData(Coders.decodeBase64(wallet));
        } else {
            try {
                encrypted = keyStoreUtils.encryptOld(Coders.decodeBase64(wallet));
            } catch (Exception e) {
                e.printStackTrace();
//                Log.d("psd", "setLastSyncedBlock() for unsupported old version: " + e.getMessage());
            }
        }
//        Log.d("psd", "setLastSyncedBlock - encrypted = " + encrypted);
        settings.edit().putString(ENCRYPTED_LAST_BLOCK, encrypted).apply();
        setIsSecureStorageSupported(false);
    }

    public String getWalletEmail() {
        return SecurePreferences.getStringValue(WALLET_EMAIL, "");
    }

    public void setWalletEmail(String value) {
        try {
            SecurePreferences.setValue(WALLET_EMAIL, value);
        } catch (Exception e) {
            //Toast.makeText(context, context.getText(R.string.can_t_save_your_private_key), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public String getWalletId() {
        return SecurePreferences.getStringValue(WALLET_ID, "");
    }

    public void setWalletId(String value) {
        try {
            SecurePreferences.setValue(WALLET_ID, value);
        } catch (Exception e) {
            //Toast.makeText(context, context.getText(R.string.can_t_save_your_private_key), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public long getLastWrongPinCodeTime() {
        return settings.getLong(TIME_WRONG_PIN_CODE_CHECKER, 0);
    }

    public void setLastWrongPinCodeTime(long time) {
        settings.edit().putLong(TIME_WRONG_PIN_CODE_CHECKER, time).apply();
    }

    public int getPinBlockType() {
        return settings.getInt(PIN_BLOCK_TYPE, 0);
    }

    public void setPinBlockType(int type) {
        settings.edit().putInt(PIN_BLOCK_TYPE, type).apply();
    }

    public void setTokensInfo(String tokens) {
        settings.edit().putString(TOKENS_INFO, tokens).apply();
    }

    public String getTokensInfo() {
        return settings.getString(TOKENS_INFO, "");
    }

    public void setTokenUpdateDate(long date) {
        settings.edit().putLong(LAST_TOKEN_INFO_SYNC, date).apply();
    }

    public Long getTokenUpdateDate() {
        return settings.getLong(LAST_TOKEN_INFO_SYNC, 0);
    }

    public boolean getIsAskRate(){
        return settings.getBoolean(IS_ASK_RATE, true);
    }

    public void setIsAskRate(boolean b){
        settings.edit().putBoolean(IS_ASK_RATE, b).apply();
    }

    public int getNextShowRate(){
        return settings.getInt(NEXT_TIME_SHOW_RATE, 0);
    }

    public void setNextShowRate(int count) {
        settings.edit().putInt(NEXT_TIME_SHOW_RATE, count).apply();
    }

    public boolean getIsWaitFourTr(){
        return settings.getBoolean(IS_WAIT_FOUR_TR, true);
    }

    public void setIsWaitFourTr(boolean b){
        settings.edit().putBoolean(IS_WAIT_FOUR_TR, b).apply();
    }

    public void setJsonExcep(String ex) {
        settings.edit().putString(LOAD_JSON_EXCEPTION, ex).apply();
    }

    public String getJsonExcep() {
        return settings.getString(LOAD_JSON_EXCEPTION, "");
    }


    public void setTxsCache(String txsCache) {
        settings.edit().putString(TXS_CACHE, txsCache).apply();
    }

    public String getTxsCache() {
        return settings.getString(TXS_CACHE, "");
    }

    public void setMapTokens(String mapTokens) {
        settings.edit().putString(MAP_CONTR_TOKENS, mapTokens).apply();
    }

    public String getMapTokens() {
        return settings.getString(MAP_CONTR_TOKENS, "");
    }

    public void setCoinifyEmail(String email) {
        settings.edit().putString(COINIFY_EMAIL, email).apply();
    }

    public String getCoinifyEmail() {
        return settings.getString(COINIFY_EMAIL, "");
    }

    public void setCoinifyOfflineToken(String email) {
        settings.edit().putString(COINIFY_OFFLINE_TOKEN, email).apply();
    }

    public String getCoinifyOfflineToken() {
        return settings.getString(COINIFY_OFFLINE_TOKEN, "");
    }

    public void setCoinifyAccessTokenLifeTime(long lastTime) {
        settings.edit().putLong(COINIFY_ACCESS_TOKEN_LIFE_TIME, lastTime).apply();
    }

    public long getCoinifyAccessTokenLifeTime() {
        return settings.getLong(COINIFY_ACCESS_TOKEN_LIFE_TIME, 0);
    }

    public void setCoinifyAccessToken(String lastTime) {
        settings.edit().putString(COINIFY_ACCESS_TOKEN, lastTime).apply();
    }

    public String getCoinifyAccessToken() {
        return settings.getString(COINIFY_ACCESS_TOKEN, "");
    }

    public void setCoinifyLastTradeId(int lastTrade) {
        settings.edit().putInt(COINIFY_LAST_TRADE, lastTrade).apply();
    }

    public int getCoinifyLastTradeId() {
        return settings.getInt(COINIFY_LAST_TRADE, 0);
    }

    public void setCoinifyLastTradeState(String lastState) {
        settings.edit().putString(COINIFY_LAST_STATE, lastState).apply();
    }

    public String getCoinifyLastTradeUptime() {
        return settings.getString(COINIFY_LAST_TRADE_TIME, "");
    }

    public void setCoinifyLastTradeUptime(String uptime) {
        settings.edit().putString(COINIFY_LAST_TRADE_TIME, uptime).apply();
    }

    public String getCoinifyLastTradeState() {
        return settings.getString(COINIFY_LAST_STATE, "");
    }

    public void setCoinifyBankTrade(String coinifyTrade) {
        settings.edit().putString(COINIFY_TRADE, coinifyTrade).apply();
    }

    public String getCoinifyBankTrade() {
        return settings.getString(COINIFY_TRADE, "");
    }

    public void setCustomNode(String customNode) {
        settings.edit().putString(CUSTOM_NODE, customNode).apply();
    }

    public String getCustomNode() {
        return settings.getString(CUSTOM_NODE, "");
    }

    public long getLastChangelliCurList() {
        return settings.getLong(LAST_CHANGELLY_CURR_LIST, 0);
    }

    public void setLastChangelliCurList(long time) {
        settings.edit().putLong(LAST_CHANGELLY_CURR_LIST, time).apply();
    }

    public long getTimeUpdateCurr() {
        return settings.getLong(TIME_UPDATE_CURR, 0);
    }

    public void setTimeUpdateCurr(long timeUpdateCurr) {
        settings.edit().putLong(TIME_UPDATE_CURR, timeUpdateCurr).apply();
    }

    public String getListCurrencies() {
        return settings.getString(LIST_CURRENCIES, "");
    }

    public void setListCurrencies(String listCurrencies) {
        settings.edit().putString(LIST_CURRENCIES, listCurrencies).apply();
    }
}
