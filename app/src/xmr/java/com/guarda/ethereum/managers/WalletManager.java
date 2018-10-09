package com.guarda.ethereum.managers;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.utils.Coders;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.IllegalFormatCodePointException;

import javax.inject.Inject;

import autodagger.AutoInjector;

import static com.guarda.ethereum.models.constants.Common.BLOCK;


/**
 * Provide all actions with Ethereum wallet
 * Created by SV on 01.08.2017.
 */

@AutoInjector(GuardaApp.class)
public class WalletManager {
    @Inject
    SharedManager sharedManager;

    private final int FULL_ETH_WALLET_LENGTH = 42;
    private final int SHORT_ETH_WALLET_LENGTH = 40;

    //private WalletFile currentWallet;
    private String walletFriendlyAddress;
    private String walletAddress;
    private String key;
    //private Credentials credentials;
    private BigDecimal balance = BigDecimal.ZERO;

    private Context context;


    public WalletManager(Context context) {
        GuardaApp.getAppComponent().inject(this);
        this.context = context;
    }

    public void createWallet(String passphrase, WalletCreationCallback callback) {
        //restoreFromBlock(Coders.decodeBase64(sharedManager.getLastSyncedBlock()), callback);
        callback.onWalletCreated();
    }

    public void createWallet2(String passphrase, final Runnable callback) {
        callback.run();
    }

    private void restoreWalletFromFile(String walletPath, String pass, WalletCreationCallback callback) {
    }

    public void restoreFromBlock(String privateKey, final WalletCreationCallback callback){
        String email = sharedManager.getWalletEmail();
        String name = "u"+Coders.md5(email);
        WalletAPI.restoreWallet(name, privateKey, new Callback2<String, String>() {
            @Override
            public void onResponse(String result, String walletId) {
                if (result != null) {
                    walletAddress = result;
                    walletFriendlyAddress = result;
                }
                callback.onWalletCreated();
            }
        });
    }

    public void restoreFromBlock0(String privateKey, final Runnable callback){
        String email = sharedManager.getWalletEmail();
        String name = "u"+Coders.md5(email);
        WalletAPI.restoreWallet(name, privateKey, new Callback2<String, String>() {
            @Override
            public void onResponse(String result, String walletId) {
                if (result != null) {
                    walletAddress = result;
                    walletFriendlyAddress = result;
                }
                callback.run();
            }
        });
    }

    public void restoreFromBlock2(final String email, final String privateKey, final Runnable callback){
        Log.d("flint", "WalletManager.restoreFromBlock2...");
        String name = "u"+Coders.md5(email);
        WalletAPI.restoreWallet(name, privateKey, new Callback2<String, String>() {
            @Override
            public void onResponse(String result, String walletId) {
                if (result != null) {
                    sharedManager.setLastSyncedBlock(Coders.encodeBase64(privateKey));
                    sharedManager.setWalletEmail(email);
                    sharedManager.setWalletId(walletId);
                    walletAddress = result;
                    walletFriendlyAddress = result;
                }
                callback.run();
            }
        });
    }

    public String getWalletFriendlyAddress() {
        return walletFriendlyAddress;
    }

    public String getWalletAddressForDeposit() {
        return sharedManager.getWalletEmail();
    }

    public String getWalletAddressWithoutPrefix(){
        return walletAddress;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getBalance() {
        return this.balance;
    }

    public String getPrivateKey() {
        return Coders.decodeBase64(sharedManager.getLastSyncedBlock());
    }

    public String getXPRV() {
        if (currentWallet != null) {
            return "NOT_IMPLEMENTED";
        } else {
            return "";
        }
//        ChildNumber childNumber = new ChildNumber(ChildNumber.HARDENED_BIT);
//        DeterministicKey de = wallet.getKeyByPath(ImmutableList.of(childNumber));
//        return de.serializePrivB58(params);
    }

    public String getWifKey() {
        if (currentWallet != null) {
            return "NOT_IMPLEMENTED";
        } else {
            return "";
        }
    }

    public void clearWallet() {
        walletFriendlyAddress = null;
    }

//    Credentials getCredentials(){
//        return credentials;
//    }

    public boolean isValidPublicAddress(String address){
        return false;
    }

    public boolean isValidPrivateKey(String privateKey){
        return true;
    }

    public boolean isSimilarToAddress(String text){
        String hexMarker = context.getString(R.string.hex_marker);
        boolean isNormalLength =  (text.length() == FULL_ETH_WALLET_LENGTH && text.contains(hexMarker) )
                || (text.length() == SHORT_ETH_WALLET_LENGTH && !text.contains(hexMarker));
        boolean isAlphanumeric = isAlphaNumeric(text);
        boolean isValidAddress = isValidPublicAddress(text);
        return isNormalLength && isAlphanumeric && isValidAddress;
    }

    public void isAddressValid(String address, final Callback<Boolean> callback) {
        callback.onResponse(true);
    }


    public boolean isAlphaNumeric(String s) {
        String pattern = "^[a-zA-Z0-9]*$";
        return s.matches(pattern);
    }



    public static void cashAddressToLegacy(final String cashAddress, final Callback<String> callback) {
        callback.onResponse(cashAddress);
    }



    public static CharSequence isCharSequenceValidForAddress(CharSequence charSequence) {
        return null;
    }

}
