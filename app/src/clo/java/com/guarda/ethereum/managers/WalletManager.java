package com.guarda.ethereum.managers;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.common.StringUtils;
import com.guarda.ethereum.BuildConfig;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.utils.Coders;
import com.guarda.ethereum.utils.DebugHelper;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
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

    private WalletFile currentWallet;
    private String walletFriendlyAddress;
    private String walletAddress;
    private String key;
    private Credentials credentials;
    private BigDecimal balance = BigDecimal.ZERO;

    private Context context;


    public WalletManager(Context context) {
        GuardaApp.getAppComponent().inject(this);
        this.context = context;
    }

    public void createWallet(String passphrase, WalletCreationCallback callback) {
//        ECKeyPair keyPair = null;
//        try {
//            keyPair = Keys.createEcKeyPair();
//        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
//            e.printStackTrace();
//        }
//        WalletCreationTask task = new WalletCreationTask(callback, keyPair);
//        task.execute(passphrase);
        restoreFromBlock(Coders.decodeBase64(sharedManager.getLastSyncedBlock()), callback);
    }

    public void createWallet2(String passphrase, final Runnable callback) {
        ECKeyPair keyPair = null;
        try {
            keyPair = Keys.createEcKeyPair();
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        WalletCreationTask2 task = new WalletCreationTask2(new WalletCreationCallback() {
            @Override
            public void onWalletCreated(WalletFile walletFile) {
                callback.run();
            }
        }, keyPair);
        task.execute(passphrase);
    }

    private void restoreWalletFromFile(String walletPath, String pass, WalletCreationCallback callback) {
        Credentials credentials = null;
        try {
            credentials = WalletUtils.loadCredentials(pass, walletPath);
        } catch (IOException | CipherException e) {
            e.printStackTrace();
        }

        if (credentials != null) {
            ECKeyPair keyPair = credentials.getEcKeyPair();
            WalletCreationTask task = new WalletCreationTask(callback, keyPair);
            task.execute(pass);
        }
    }

    public void restoreFromBlock(String privateKey, WalletCreationCallback callback) {
        try {
            credentials = Credentials.create(privateKey);
            WalletCreationTask task = new WalletCreationTask(callback, credentials.getEcKeyPair());
            task.execute(BLOCK);
        } catch (NumberFormatException nfe) {
            Log.e("psd", "restoreFromBlock: " + nfe.toString());
            DebugHelper.logIventFirebase("restoreFromBlock", nfe.toString(), context);
        }
    }

    public void restoreFromBlock0(String privateKey, final Runnable callback) {
        credentials = Credentials.create(privateKey);
        WalletCreationTask task = new WalletCreationTask(new WalletCreationCallback() {
            @Override
            public void onWalletCreated(WalletFile walletFile) {
                callback.run();
            }
        }, credentials.getEcKeyPair());
        task.execute(BLOCK);
    }

    public void restoreFromBlock2(String privateKey, final Runnable callback) {
        credentials = Credentials.create(privateKey);
        WalletCreationTask2 task = new WalletCreationTask2(new WalletCreationCallback() {
            @Override
            public void onWalletCreated(WalletFile walletFile) {
                callback.run();
            }
        }, credentials.getEcKeyPair());
        task.execute(BLOCK);
    }

    public void saveJsonLight() {
        try {
            BigInteger b = new BigInteger(Coders.decodeBase64(sharedManager.getLastSyncedBlock()), 16);
            String filename1 = WalletUtils.generateWalletFile("", ECKeyPair.create(b), com.google.common.io.Files.createTempDir(), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void restoreFromBlock2Json(Uri uri, String pwd, final Runnable callback) {
        loadFromJson(uri, pwd);
        if (credentials == null) {
            callback.run();
            return;
        }
        WalletCreationTask2 task = new WalletCreationTask2(new WalletCreationCallback() {
            @Override
            public void onWalletCreated(WalletFile walletFile) {
                callback.run();
            }
        }, credentials.getEcKeyPair());
        task.execute(BLOCK);
    }

    private void loadFromJson(Uri uri, String pwd) {
        sharedManager.setJsonExcep("");
        try {
            ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
            objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            InputStream is = context.getContentResolver().openInputStream(uri);
            WalletFile walletFile = objectMapper.readValue(is, WalletFile.class);
            credentials = Credentials.create(Wallet.decrypt(pwd, walletFile));
        } catch (JsonParseException jpe) {
            sharedManager.setJsonExcep("JsonParseException");
            jpe.printStackTrace();
        } catch (CipherException ce) {
            if (ce.getMessage().equals("Invalid password provided")) {
                sharedManager.setJsonExcep("WrongPassword");
            } else {
                sharedManager.setJsonExcep("CipherException");
            }
            ce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class WalletCreationTask extends AsyncTask<String, Void, WalletFile> {

        private WalletCreationCallback callback;
        private ECKeyPair keyPair;

        WalletCreationTask(WalletCreationCallback callback, ECKeyPair keyPair) {
            this.callback = callback;
            this.keyPair = keyPair;
        }

        @Override
        protected WalletFile doInBackground(String... params) {

                /* any key in friendly format*/
            key = keyPair.getPrivateKey().toString(16);
            //sharedManager.setLastSyncedBlock(Coders.encodeBase64(key));
            return generateWallet(keyPair, params[0]);
        }

        private WalletFile generateWallet(@NonNull ECKeyPair keyPair, String password) {
            WalletFile wallet = null;
            try {
                wallet = Wallet.createLight(password, keyPair);
                walletFriendlyAddress = context.getString(R.string.hex_marker) + wallet.getAddress();
                walletAddress = wallet.getAddress();
            } catch (CipherException e) {
                e.printStackTrace();
            }

            return wallet;
        }

        @Override
        protected void onPostExecute(WalletFile walletFile) {
            currentWallet = walletFile;
            if (callback != null) {
                callback.onWalletCreated(walletFile);
            }
        }

    }

    private class WalletCreationTask2 extends AsyncTask<String, Void, WalletFile> {

        private WalletCreationCallback callback;
        private ECKeyPair keyPair;

        WalletCreationTask2(WalletCreationCallback callback, ECKeyPair keyPair) {
            this.callback = callback;
            this.keyPair = keyPair;
        }

        @Override
        protected WalletFile doInBackground(String... params) {

                /* any key in friendly format*/
            key = keyPair.getPrivateKey().toString(16);
            sharedManager.setLastSyncedBlock(Coders.encodeBase64(key));
            return generateWallet(keyPair, params[0]);
        }

        private WalletFile generateWallet(@NonNull ECKeyPair keyPair, String password) {
            WalletFile wallet = null;
            try {
                wallet = Wallet.createLight(password, keyPair);
                walletFriendlyAddress = context.getString(R.string.hex_marker) + wallet.getAddress();
                walletAddress = wallet.getAddress();
            } catch (CipherException e) {
                e.printStackTrace();
            }

            return wallet;
        }

        @Override
        protected void onPostExecute(WalletFile walletFile) {
            currentWallet = walletFile;
            if (callback != null) {
                callback.onWalletCreated(walletFile);
            }
        }

    }

    public WalletFile getCurrentWallet() {
        if (currentWallet == null) {
            restoreWalletFromFile(null, null, null);
        }
        return currentWallet;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getBalance() {
        return this.balance;
    }

    public String getWalletFriendlyAddress() {
        return walletFriendlyAddress;
    }

    public String getWalletAddressForDeposit() {
        return walletFriendlyAddress;
    }

    public String getWalletAddressWithoutPrefix() {
        return walletAddress;
    }

    public String getPrivateKey() {
        return key;
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
        currentWallet = null;
        walletFriendlyAddress = null;
    }

    Credentials getCredentials() {
        return credentials;
    }

    public boolean isValidPublicAddress(String address) {
        return  WalletUtils.isValidAddress(address);
    }

    public boolean isValidPrivateKey(String privateKey) {
        if ((Numeric.cleanHexPrefix(privateKey).matches("[abcdefx1234567890]+")) && WalletUtils.isValidPrivateKey(privateKey.toLowerCase())) {
            try {
                Credentials.create(privateKey);
                return true;
            } catch (NumberFormatException e) {
                return false;
            } catch (IllegalFormatCodePointException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isSimilarToAddress(String text) {
        String hexMarker = context.getString(R.string.hex_marker);
        boolean isNormalLength =  (text.length() == FULL_ETH_WALLET_LENGTH && text.contains(hexMarker) )
                || (text.length() == SHORT_ETH_WALLET_LENGTH && !text.contains(hexMarker));
        boolean isAlphanumeric = isAlphaNumeric(text);
        boolean isValidAddress = isValidPublicAddress(text);
        return isNormalLength && isAlphanumeric && isValidAddress;
    }

    public void isAddressValid(String address, final Callback<Boolean> callback) {
        String hexMarker = context.getString(R.string.hex_marker);
        if (address.length() == SHORT_ETH_WALLET_LENGTH && !address.substring(0, 2).equals(hexMarker)) {
            address = hexMarker + address;
//            etSendCoinsAddress.setText(address);
        }
        Boolean res = address.length() == FULL_ETH_WALLET_LENGTH && isAlphaNumeric(address) && isValidPublicAddress(address);
        callback.onResponse(res);
    }

    public boolean isAlphaNumeric(String s) {
        String pattern = "^[a-zA-Z0-9]*$";
        return s.matches(pattern);
    }



    public static void cashAddressToLegacy(final String cashAddress, final Callback<String> callback) {
        callback.onResponse(cashAddress);
    }



    public static CharSequence isCharSequenceValidForAddress(CharSequence charSequence) {
        final String validChars = "abcdefABCDEF01234567890xX";
        if (charSequence.length() > 10) // for qr-scanner
            return null;
        String res = "";
        for (int i = 0; i < charSequence.length(); ++i) {
            char c = charSequence.charAt(i);
            if (validChars.indexOf(c) >= 0)
                res += c;
        }
        return res;
    }

}
