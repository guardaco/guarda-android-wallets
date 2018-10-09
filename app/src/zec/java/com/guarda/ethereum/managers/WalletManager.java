package com.guarda.ethereum.managers;

import android.content.Context;
import android.net.Credentials;
import android.util.Log;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.firebase.crash.FirebaseCrash;
import com.gravilink.zcash.WalletCallback;
import com.gravilink.zcash.ZCashException;
import com.gravilink.zcash.ZCashWalletManager;
import com.gravilink.zcash.crypto.BrainKeyDict;
import com.guarda.ethereum.BuildConfig;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.items.UTXOItem;
import com.guarda.ethereum.models.items.UTXOListResponse;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.RequestorBtc;
import com.guarda.ethereum.utils.Coders;
import com.guarda.ethereum.utils.DebugHelper;
import com.guarda.ethereum.utils.FileUtils;


import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.WrongNetworkException;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import autodagger.AutoInjector;

import static com.google.common.base.Preconditions.checkState;
import static com.guarda.ethereum.models.constants.Common.BIP_39_WORDLIST_ASSET;
import static com.guarda.ethereum.models.constants.Common.BTC_NODE_PASS;
import static com.guarda.ethereum.models.constants.Common.MNEMONIC_WORDS_COUNT;


/**
 * Provide all actions with Bitcoin wallet
 * <p>
 * NOTE: not recommend use native function of bitcoinj library, like wallet.getBalance()
 * because, this wallet restoring using list of utxo and this mean that bitcoinj library is not approve
 * method getBalance, because it will try to check is utxo is valid
 * 09.10.2017.
 */

@AutoInjector(GuardaApp.class)
public class WalletManager {

    private Wallet wallet;
    private String walletFriendlyAddress;

    @Inject
    SharedManager sharedManager;


    private Coin myBalance;
    private Context context;
//    private static NetworkParameters params = LitecoinNetParameters.get();
//    private static NetworkParameters params = MainNetParams.get();
    private static NetworkParameters params = new ZecParams();
    private String mnemonicKey;
    private HashSet<String> mBip39Words;
    private BigDecimal balance = BigDecimal.ZERO;


    public WalletManager(Context context) {
        GuardaApp.getAppComponent().inject(this);
        this.context = context;
        mBip39Words = FileUtils.readToSet(context, BIP_39_WORDLIST_ASSET);
    }

    public void createWallet(String passphrase, WalletCreationCallback callback) {

//        wallet = new Wallet(params);
//        DeterministicSeed seed = wallet.getKeyChainSeed();
//
//        mnemonicKey = Joiner.on(" ").join(seed.getMnemonicCode());
//        sharedManager.setLastSyncedBlock(Coders.encodeBase64(mnemonicKey));
//
//        walletFriendlyAddress = wallet.currentReceiveAddress().toString();
//        callback.onWalletCreated(wallet);
        DebugHelper.checkEmptyLastSyncedBlock(sharedManager.getLastSyncedBlock(), context);
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
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (ZCashException zce) {
                    zce.printStackTrace();
                }
                sharedManager.setLastSyncedBlock(Coders.encodeBase64(mnemonicKey));

                callback.run();
            }
        }.start();

//        try {
//            Thread.sleep(10000);
//        } catch (Exception e){}


    }

    public void restoreFromBlock(String mnemonicCode, final WalletCreationCallback callback) {
        try {
            ZCashWalletManager.getInstance().importWallet_taddr(mnemonicCode,
                    ZCashWalletManager.UpdateRequirement.NO_UPDATE,
                    new WalletCallback<String, Void>() {
                        @Override
                        public void onResponse(String r1, Void r2) {
                            callback.onWalletCreated();
                            Log.i("RESPONSE CODE", r1);
                        }
                    });
            mnemonicKey = mnemonicCode;
            walletFriendlyAddress = ZCashWalletManager.publicKeyFromPrivateKey_taddr(mnemonicKey);
        } catch (IllegalArgumentException iae) {
            callback.onWalletCreated();
            iae.printStackTrace();
        } catch (ZCashException zce) {
            callback.onWalletCreated();
            zce.printStackTrace();
        }
//        sharedManager.setLastSyncedBlock(Coders.encodeBase64(mnemonicKey));

        callback.onWalletCreated();
    }

//    public void restoreFromBlockByXPRV(String xprv, WalletCreationCallback callback) {
//        xprv = xprv.trim();
//        try {
//            DeterministicKey dk01 = DeterministicKey.deserializeB58(xprv, params);
//            String privhex = dk01.getPrivateKeyAsHex();
//            ECKey ecKey001 = ECKey.fromPrivate(Hex.decode(privhex));
//            KeyChainGroup kcg = new KeyChainGroup(params, dk01.dropPrivateBytes().dropParent());
//            kcg.importKeys(ecKey001);
//            wallet = new Wallet(params, kcg);
//            walletFriendlyAddress = wallet.currentReceiveAddress().toString();
//            xprvKey = xprv;
//        } catch (IllegalArgumentException iae) {
//            FirebaseCrash.report(iae);
//            Log.e("psd", "restoreFromBlockByXPRV: " + iae.toString());
//            callback.onWalletCreated(wallet);
//            return;
//        }
//
//        callback.onWalletCreated(wallet);
//
//        RequestorBtc.getUTXOListLtcNew(wallet.currentReceiveAddress().toString(), new ApiMethods.RequestListener() {
//            @Override
//            public void onSuccess(Object response) {
//                List<UTXOItem> utxos = (List<UTXOItem>)response;
//                setUTXO(utxos);
//            }
//
//            @Override
//            public void onFailure(String msg) {
//
//            }
//        });
//    }

    public void restoreFromBlock0(String mnemonicCode, Runnable callback) {
        try {
            BrainKeyDict.init(context.getAssets());
            mnemonicKey = ZCashWalletManager.generateNewPrivateKey_taddr();
            walletFriendlyAddress = ZCashWalletManager.publicKeyFromPrivateKey_taddr(mnemonicKey);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ZCashException zce) {
            zce.printStackTrace();
        }
//        sharedManager.setLastSyncedBlock(Coders.encodeBase64(mnemonicKey));

        callback.run();
    }

    public void restoreFromBlock2(String mnemonicCode, final Runnable callback) {
        try {
            ZCashWalletManager.getInstance().importWallet_taddr(mnemonicCode,
                    ZCashWalletManager.UpdateRequirement.NO_UPDATE,
                    new WalletCallback<String, Void>() {
                        @Override
                        public void onResponse(String r1, Void r2) {
                            try {
                                mnemonicKey = mnemonicCode;
                                walletFriendlyAddress = ZCashWalletManager.publicKeyFromPrivateKey_taddr(mnemonicKey);
                                sharedManager.setLastSyncedBlock(Coders.encodeBase64(mnemonicKey));
                                callback.run();
                                Log.i("RESPONSE CODE", r1);
                            } catch (IllegalArgumentException iae) {
                                iae.printStackTrace();
                                callback.run();
                            } catch (ZCashException zce) {
                                zce.printStackTrace();
                                callback.run();
                            }
                        }
                    });
        } catch (IllegalArgumentException iae) {
            callback.run();
            iae.printStackTrace();
        } catch (ZCashException zce) {
            zce.printStackTrace();
            callback.run();
        }

//        callback.run();
    }

//    public void restoreFromBlockByXPRV2(String xprv, Runnable callback) {
//        xprv = xprv.trim();
//        try {
//            DeterministicKey dk01 = DeterministicKey.deserializeB58(xprv, params);
//            String privhex = dk01.getPrivateKeyAsHex();
//            ECKey ecKey001 = ECKey.fromPrivate(Hex.decode(privhex));
//            KeyChainGroup kcg = new KeyChainGroup(params, dk01.dropPrivateBytes().dropParent());
//            kcg.importKeys(ecKey001);
//            wallet = new Wallet(params, kcg);
//            sharedManager.setLastSyncedBlock(xprv);
//            walletFriendlyAddress = wallet.currentReceiveAddress().toString();
//            xprvKey = xprv;
//        } catch (IllegalArgumentException iae) {
//            FirebaseCrash.report(iae);
//            Log.e("psd", "restoreFromBlockByXPRV2: " + iae.toString());
//            callback.run();
//            return;
//        }
//
//        callback.run();
//
//        RequestorBtc.getUTXOListLtcNew(wallet.currentReceiveAddress().toString(), new ApiMethods.RequestListener() {
//            @Override
//            public void onSuccess(Object response) {
//                List<UTXOItem> utxos = (List<UTXOItem>)response;
//                setUTXO(utxos);
//            }
//
//            @Override
//            public void onFailure(String msg) {
//
//            }
//        });
//    }

//    private void setUTXO(List<UTXOItem> utxoList) {
//
//        Address a = wallet.currentReceiveAddress();
//        final List<UTXO> utxos = new ArrayList<>();
//
//        for (UTXOItem utxo : utxoList) {
//            Sha256Hash hash = Sha256Hash.wrap(utxo.getTxHash());
//            utxos.add(new UTXO(hash, utxo.getTxOutputN(), Coin.valueOf(utxo.getSatoshiValue()),
//                    0, false, ScriptBuilder.createOutputScript(a)));
//        }
//
//        UTXOProvider utxoProvider = new UTXOProvider() {
//            @Override
//            public List<UTXO> getOpenTransactionOutputs(List<Address> addresses) throws UTXOProviderException {
//                return utxos;
//            }
//
//            @Override
//            public int getChainHeadHeight() throws UTXOProviderException {
//                return Integer.MAX_VALUE;
//            }
//
//            @Override
//            public NetworkParameters getParams() {
//                return wallet.getParams();
//            }
//        };
//        wallet.setUTXOProvider(utxoProvider);
//    }

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
//        ChildNumber childNumber = new ChildNumber(ChildNumber.HARDENED_BIT);
//        DeterministicKey de = wallet.getKeyByPath(ImmutableList.of(childNumber));
//        return de.serializePrivB58(params);
    }

    public String getWifKey() {
        if (wallet != null) {
            return "NOT_IMPLEMENTED";
        } else {
            return "";
        }
    }

    public String getWalletFriendlyAddress() {
        return walletFriendlyAddress;
//            return "t1VpYecBW4UudbGcy4ufh61eWxQCoFaUrPs"; //to z-address
//            return "t1fDUDwxfte5M4CYCFUzejVAXr2Pc31vLq8"; //from z-address
    }

    public String getWalletAddressForDeposit() {
        return walletFriendlyAddress;
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
    }

    public boolean isValidPrivateKey(String key) {
//        String[] words = key.split("\\W+");
//
//        if (words.length != MNEMONIC_WORDS_COUNT){
//            return false;
//        }
//        for (String word : words) {
//            if (!mBip39Words.contains(word)){
//                return false;
//            }
//        }
        return true;
    }

    public boolean isSimilarToAddress(String text) {
        return isAddressValid(text);
    }

    public void isAddressValid(String address, final Callback<Boolean> callback) {
        if (address.length() == 35) {
            callback.onResponse(address.substring(0, 1).equalsIgnoreCase("t"));
        } else {
            callback.onResponse(false);
        }

//        try {
//            Address.fromBase58(params, address);
//            callback.onResponse(true);
//        } catch (WrongNetworkException wne) {
//            Log.e("psd", "isAddressValid: " + wne.toString());
//            callback.onResponse(false);
//        } catch (AddressFormatException afe) {
//            Log.e("psd", "isAddressValid: " + afe.toString());
//            callback.onResponse(false);
//        } catch (Exception e) {
//            e.printStackTrace();
//            callback.onResponse(false);
//        }
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

    private static class ZecParams extends MainNetParams{
        ZecParams() {
            super();

            addressHeader = 0;
            p2shHeader = 28;
            acceptableAddressCodes = new int[] {addressHeader, p2shHeader};
        }
    }

}
