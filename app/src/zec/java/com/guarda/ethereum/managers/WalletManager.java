package com.guarda.ethereum.managers;

import android.content.Context;
import android.net.Credentials;
import android.util.Log;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.utils.Coders;
import com.guarda.zcash.ZCashException;
import com.guarda.zcash.ZCashWalletManager;
import com.guarda.zcash.crypto.BrainKeyDict;
import com.guarda.zcash.sapling.key.SaplingCustomFullKey;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigDecimal;

import javax.inject.Inject;

import timber.log.Timber;
import work.samosudov.rustlib.RustAPI;


/**
 * Provide all actions with Bitcoin wallet
 * <p>
 * NOTE: not recommend use native function of bitcoinj library, like wallet.getBalance()
 * because, this wallet restoring using list of utxo and this mean that bitcoinj library is not approve
 * method getBalance, because it will try to check is utxo is valid
 * 09.10.2017.
 */

public class WalletManager {

    private Wallet wallet;
    private String walletFriendlyAddress;
    private String saplingAddress;

    @Inject
    SharedManager sharedManager;

    private Coin myBalance;
    private Context context;
    private static NetworkParameters params = new ZecParams();
    private String mnemonicKey;
    private BigDecimal balance = BigDecimal.ZERO;
    private SaplingCustomFullKey saplingCustomFullKey;

    public WalletManager(Context context) {
        GuardaApp.getAppComponent().inject(this);
        this.context = context;
    }

    public void createWallet(String passphrase, WalletCreationCallback callback) {
        restoreFromBlock(Coders.decodeBase64(sharedManager.getLastSyncedBlock()), callback);
    }

    public void createWallet2(String passphrase, Runnable callback) {
        new Thread() {
            @Override
            public void run() {
                try {
                    BrainKeyDict.init(context.getAssets());
                    mnemonicKey = ZCashWalletManager.generateNewPrivateKey_taddr();
                    walletFriendlyAddress = ZCashWalletManager.publicKeyFromPrivateKey_taddr(mnemonicKey);
                    saplingAddress = RustAPI.zAddrFromWif(mnemonicKey.getBytes());
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (ZCashException zce) {
                    zce.printStackTrace();
                }
                sharedManager.setLastSyncedBlock(Coders.encodeBase64(mnemonicKey));

                callback.run();
            }
        }.start();
    }

    public void createWallet() {
        try {
            BrainKeyDict.init(context.getAssets());
            mnemonicKey = ZCashWalletManager.generateNewPrivateKey_taddr();
            walletFriendlyAddress = ZCashWalletManager.publicKeyFromPrivateKey_taddr(mnemonicKey);
            saplingAddress = RustAPI.zAddrFromWif(mnemonicKey.getBytes());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ZCashException zce) {
            zce.printStackTrace();
        }
        sharedManager.setLastSyncedBlock(Coders.encodeBase64(mnemonicKey));
    }

    public void restoreFromBlock(String mnemonicCode, final WalletCreationCallback callback) {
        try {
            mnemonicKey = mnemonicCode;
            walletFriendlyAddress = ZCashWalletManager.publicKeyFromPrivateKey_taddr(mnemonicKey);
            saplingAddress = RustAPI.zAddrFromWif(mnemonicKey.getBytes());
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
            Timber.e("restoreFromBlock IllegalArgumentException e=%s", iae.getMessage());
        } catch (ZCashException zce) {
            zce.printStackTrace();
            Timber.e("restoreFromBlock ZCashException e=%s", zce.getMessage());
        }

        callback.onWalletCreated();
    }

    public void restoreFromBlock0(String mnemonicCode, Runnable callback) {
        try {
            BrainKeyDict.init(context.getAssets());
            mnemonicKey = ZCashWalletManager.generateNewPrivateKey_taddr();
            walletFriendlyAddress = ZCashWalletManager.publicKeyFromPrivateKey_taddr(mnemonicKey);
            callback.run();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ZCashException zce) {
            zce.printStackTrace();
        }
    }

    public void restoreFromBlock2(String mnemonicCode, final Runnable callback) {
        try {
            ZCashWalletManager.getInstance().importWallet_taddr(mnemonicCode,
                    ZCashWalletManager.UpdateRequirement.NO_UPDATE,
                    (r1, r2) -> {
                        try {
                            mnemonicKey = mnemonicCode;
                            walletFriendlyAddress = ZCashWalletManager.publicKeyFromPrivateKey_taddr(mnemonicKey);
                            saplingAddress = RustAPI.zAddrFromWif(mnemonicKey.getBytes());
                            sharedManager.setLastSyncedBlock(Coders.encodeBase64(mnemonicKey));
                            callback.run();
                            Timber.d("RESPONSE CODE %s", r1);
                        } catch (IllegalArgumentException iae) {
                            iae.printStackTrace();
                        } catch (ZCashException zce) {
                            zce.printStackTrace();
                        }
                    });
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        } catch (ZCashException zce) {
            zce.printStackTrace();
        }
    }

    public static String getFriendlyBalance(Coin coin) {
        String[] arr = coin.toFriendlyString().split(" ");
        return arr[0];
    }

    public Coin getMyBalance() {
        return myBalance != null ? myBalance : Coin.ZERO;
    }

    public void setMyBalance(Long satoshi) {
        myBalance = satoshi != 0 ? Coin.valueOf(satoshi) : Coin.ZERO;
    }

    public void setBalance(long balance) {
        Coin coin = balance != 0 ? Coin.valueOf(balance) : Coin.ZERO;
        this.balance = new BigDecimal(coin.toPlainString());
    }

    public BigDecimal getBalance() {
        return this.balance;
    }

    public String getPrivateKey() {
        return mnemonicKey;
    }

    public String getXPRV() {
        if (wallet != null) {
            return "NOT_IMPLEMENTED";
        } else {
            return "";
        }
    }

    public String getWifKey() {
        if (wallet != null) {
            return "NOT_IMPLEMENTED";
        } else {
            return "";
        }
    }

    public SaplingCustomFullKey getSaplingCustomFullKey() {
        return saplingCustomFullKey;
    }

    public void setSaplingCustomFullKey(SaplingCustomFullKey saplingCustomFullKey) {
        this.saplingCustomFullKey = saplingCustomFullKey;
    }

    public String getWalletFriendlyAddress() {
        return walletFriendlyAddress;
    }

    public String getWalletAddressForDeposit() {
        return walletFriendlyAddress;
    }

    public String getSaplingAddress() {
        return saplingAddress;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public Credentials getCredentials() {
        return null;
    }

    String getWalletAddressWithoutPrefix() {
        return walletFriendlyAddress;
    }

    public void clearWallet() {
        walletFriendlyAddress = null;
        mnemonicKey = "";
        myBalance = Coin.ZERO;
        saplingAddress = null;
        saplingCustomFullKey = null;
    }

    public boolean isValidPrivateKey(String key) {
        return true;
    }

    public boolean isSimilarToAddress(String text) {
        return isAddressValid(text);
    }

    public void isAddressValid(String address, final Callback<Boolean> callback) {
        int l = address.length();
        // 35 - t address
        // 78 - z address
        // 88 - z testnet address
        if (l != 35 && l != 78 && l != 88) {
            callback.onResponse(false);
            return;
        }
        if (address.contains("ztestsapling1") || address.contains("zs1")) {
            callback.onResponse(true);
            return;
        }
        if (address.length() == 35) {
            callback.onResponse(address.substring(0, 1).equalsIgnoreCase("t"));
        } else {
            callback.onResponse(false);
        }
    }

    public boolean isAddressValid(String address) {
        try {
            return !address.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String SMALL_SENDING = "insufficientMoney";
    public static String NOT_ENOUGH_MONEY = "notEnough";

    public String generateHexTx(String toAddress, long sumIntoSatoshi, long feeIntoSatoshi) {
        Address RECEIVER = Address.fromBase58(params, toAddress);

        Coin AMOUNT = Coin.valueOf(sumIntoSatoshi);
        Coin FEE = Coin.valueOf(feeIntoSatoshi);

        /**
         * available default fee
         * Transaction.REFERENCE_DEFAULT_MIN_TX_FEE;
         * Transaction.DEFAULT_TX_FEE;
         */
        SendRequest sendRequest = SendRequest.to(RECEIVER, AMOUNT);
        sendRequest.changeAddress = wallet.currentReceiveAddress();
        sendRequest.ensureMinRequiredFee = true;
//        sendRequest.setUseForkId(true);
        sendRequest.feePerKb = FEE;
        Transaction trx = null;
        String hex = "";
        try {
            wallet.completeTx(sendRequest);
            trx = sendRequest.tx;
            hex = Hex.toHexString (trx.bitcoinSerialize());
        } catch (InsufficientMoneyException e) {
            e.printStackTrace();
            return NOT_ENOUGH_MONEY;
        } catch (Wallet.DustySendRequested e) {
            e.printStackTrace();
            return SMALL_SENDING;
        }
        return hex;
    }

    public long calculateFee(String toAddress, long sumIntoSatoshi, long feeIntoSatoshi) throws Exception {
        Address RECEIVER = Address.fromBase58(params, toAddress);

        Coin AMOUNT = Coin.valueOf(sumIntoSatoshi);
        Coin FEE = Coin.valueOf(feeIntoSatoshi);

        /**
         * available default fee
         * Transaction.REFERENCE_DEFAULT_MIN_TX_FEE;
         * Transaction.DEFAULT_TX_FEE;
         */
        SendRequest sendRequest = SendRequest.to(RECEIVER, AMOUNT);
        sendRequest.changeAddress = wallet.currentReceiveAddress();
        sendRequest.ensureMinRequiredFee = true;
//        sendRequest.setUseForkId(true);
        sendRequest.feePerKb = FEE;
        long result = FEE.getValue();
        try {
            Log.d("flint", "WalletManager.calculateFee()... wallet.getBalance(): " + wallet.getBalance());
            wallet.completeTx(sendRequest);
            result = sendRequest.tx.getFee().getValue();
        } catch (InsufficientMoneyException e) {
            throw new Exception("NOT_ENOUGH_MONEY");
        } catch (Wallet.DustySendRequested e) {
            throw new Exception("SMALL_SENDING");
        } catch (Exception e) {
            throw new Exception(e.toString());
        }
        return result;
    }

    public static void cashAddressToLegacy(final String cashAddress, final Callback<String> callback) {
        callback.onResponse(cashAddress);
    }

    public static CharSequence isCharSequenceValidForAddress(CharSequence charSequence) {
        final String validChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890";
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

    private static class ZecParams extends MainNetParams {
        ZecParams() {
            super();

            addressHeader = 0;
            p2shHeader = 28;
            acceptableAddressCodes = new int[] {addressHeader, p2shHeader};
        }
    }

}
