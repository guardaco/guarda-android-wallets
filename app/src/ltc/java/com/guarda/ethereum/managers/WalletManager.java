package com.guarda.ethereum.managers;

import android.content.Context;
import android.net.Credentials;
import android.util.Log;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.firebase.crash.FirebaseCrash;
import com.guarda.ethereum.GuardaApp;
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
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.UTXO;
import org.bitcoinj.core.UTXOProvider;
import org.bitcoinj.core.UTXOProviderException;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.WrongNetworkException;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.LitecoinMainNetParams;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.Protos;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.spongycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import autodagger.AutoInjector;

import static com.google.common.base.Preconditions.checkState;
import static com.guarda.ethereum.models.constants.Common.BIP_39_WORDLIST_ASSET;
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
    private static NetworkParameters params = LitecoinMainNetParams.get();
    private String mnemonicKey;
    private String wifKey;
    private String xprvKey;
    private HashSet<String> mBip39Words;
    private boolean restFromWif = false;
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

        wallet = new Wallet(params);
        DeterministicSeed seed = wallet.getKeyChainSeed();

        mnemonicKey = Joiner.on(" ").join(seed.getMnemonicCode());
        sharedManager.setLastSyncedBlock(Coders.encodeBase64(mnemonicKey));

        walletFriendlyAddress = wallet.currentReceiveAddress().toString();

        ChildNumber childNumber = new ChildNumber(ChildNumber.HARDENED_BIT);
        DeterministicKey de = wallet.getKeyByPath(ImmutableList.of(childNumber));
        xprvKey = de.serializePrivB58(params);

        wifKey = wallet.currentReceiveKey().getPrivateKeyAsWiF(params);

        callback.run();
    }

    public void restoreFromBlock(String mnemonicCode, WalletCreationCallback callback) {
        mnemonicCode = mnemonicCode.trim();
        if (mnemonicCode.equals("")) {
            callback.onWalletCreated(null);
            return;
        }

        DeterministicSeed seed = new DeterministicSeed(Splitter.on(' ').splitToList(mnemonicCode), null, "", 0);

        wallet = Wallet.fromSeed(params, seed);
        mnemonicKey = Joiner.on(" ").join(seed.getMnemonicCode());
        //sharedManager.setLastSyncedBlock(Coders.encodeBase64(mnemonicKey));
        walletFriendlyAddress = wallet.currentReceiveAddress().toString();

        ChildNumber childNumber = new ChildNumber(ChildNumber.HARDENED_BIT);
        DeterministicKey de = wallet.getKeyByPath(ImmutableList.of(childNumber));
        xprvKey = de.serializePrivB58(params);

        wifKey = wallet.currentReceiveKey().getPrivateKeyAsWiF(params);

        callback.onWalletCreated(wallet);

        RequestorBtc.getUTXOListLtcNew(wallet.currentReceiveAddress().toString(), new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                List<UTXOItem> utxos = (List<UTXOItem>)response;
                setUTXO(utxos);
            }

            @Override
            public void onFailure(String msg) {

            }
        });
    }

    public void restoreFromBlockByXPRV(String xprv, WalletCreationCallback callback) {
        xprv = xprv.trim();
        try {
            DeterministicKey dk01 = DeterministicKey.deserializeB58(xprv, params);
            String privhex = dk01.getPrivateKeyAsHex();
            ECKey ecKey001 = ECKey.fromPrivate(Hex.decode(privhex));
            KeyChainGroup kcg = new KeyChainGroup(params, dk01.dropPrivateBytes().dropParent());
            kcg.importKeys(ecKey001);
            wallet = new Wallet(params, kcg);
            walletFriendlyAddress = wallet.currentReceiveAddress().toString();
            xprvKey = xprv;
        } catch (IllegalArgumentException iae) {
            FirebaseCrash.report(iae);
            Log.e("psd", "restoreFromBlockByXPRV: " + iae.toString());
            callback.onWalletCreated(wallet);
            return;
        }

        callback.onWalletCreated(wallet);

        RequestorBtc.getUTXOListLtcNew(wallet.currentReceiveAddress().toString(), new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                List<UTXOItem> utxos = (List<UTXOItem>)response;
                setUTXO(utxos);
            }

            @Override
            public void onFailure(String msg) {

            }
        });
    }

    public void restoreFromBlockByWif(String wif, WalletCreationCallback callback) {
        wif = wif.trim();
        try {
            DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, wif);
            ECKey key = dumpedPrivateKey.getKey();
            wallet = new Wallet(params);
            wallet.importKey(key);
            restFromWif = true;
            walletFriendlyAddress = wallet.getImportedKeys().get(0).toAddress(params).toString();
            wifKey = wif;
        } catch (IllegalArgumentException iae) {
            FirebaseCrash.report(iae);
            Log.e("psd", "restoreFromBlockByWif: " + iae.toString());
            callback.onWalletCreated(wallet);
            return;
        }

        callback.onWalletCreated(wallet);

        RequestorBtc.getUTXOListLtcNew(walletFriendlyAddress, new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                List<UTXOItem> utxos = (List<UTXOItem>)response;
                setUTXO(utxos);
            }

            @Override
            public void onFailure(String msg) {

            }
        });
    }

    public void restoreFromBlock0(String mnemonicCode, Runnable callback) {
        if (mnemonicCode.charAt(mnemonicCode.length() - 1) == ' ') {
            mnemonicCode = mnemonicCode.substring(0, mnemonicCode.length() - 1);
        }

        DeterministicSeed seed = new DeterministicSeed(Splitter.on(' ').splitToList(mnemonicCode), null, "", 0);

        wallet = Wallet.fromSeed(params, seed);
        mnemonicKey = Joiner.on(" ").join(seed.getMnemonicCode());
        //sharedManager.setLastSyncedBlock(Coders.encodeBase64(mnemonicKey));
        walletFriendlyAddress = wallet.currentReceiveAddress().toString();
        callback.run();

        RequestorBtc.getUTXOListLtcNew(wallet.currentReceiveAddress().toString(), new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                List<UTXOItem> utxos = (List<UTXOItem>)response;
                setUTXO(utxos);
            }

            @Override
            public void onFailure(String msg) {

            }
        });
    }

    public void restoreFromBlock2(String mnemonicCode, Runnable callback) {
        if (mnemonicCode.charAt(mnemonicCode.length() - 1) == ' ') {
            mnemonicCode = mnemonicCode.substring(0, mnemonicCode.length() - 1);
        }

        DeterministicSeed seed = new DeterministicSeed(Splitter.on(' ').splitToList(mnemonicCode), null, "", 0);

        wallet = Wallet.fromSeed(params, seed);
        mnemonicKey = Joiner.on(" ").join(seed.getMnemonicCode());
        sharedManager.setLastSyncedBlock(Coders.encodeBase64(mnemonicKey));
        walletFriendlyAddress = wallet.currentReceiveAddress().toString();

        ChildNumber childNumber = new ChildNumber(ChildNumber.HARDENED_BIT);
        DeterministicKey de = wallet.getKeyByPath(ImmutableList.of(childNumber));
        xprvKey = de.serializePrivB58(params);

        wifKey = wallet.currentReceiveKey().getPrivateKeyAsWiF(params);

        callback.run();

        RequestorBtc.getUTXOListLtcNew(wallet.currentReceiveAddress().toString(), new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                List<UTXOItem> utxos = (List<UTXOItem>)response;
                setUTXO(utxos);
            }

            @Override
            public void onFailure(String msg) {

            }
        });
    }

    public void restoreFromBlockByXPRV2(String xprv, Runnable callback) {
        xprv = xprv.trim();
        try {
            DeterministicKey dk01 = DeterministicKey.deserializeB58(xprv, params);
            String privhex = dk01.getPrivateKeyAsHex();
            ECKey ecKey001 = ECKey.fromPrivate(Hex.decode(privhex));
            KeyChainGroup kcg = new KeyChainGroup(params, dk01.dropPrivateBytes().dropParent());
            kcg.importKeys(ecKey001);
            wallet = new Wallet(params, kcg);
            sharedManager.setLastSyncedBlock(Coders.encodeBase64(xprv));
            walletFriendlyAddress = wallet.currentReceiveAddress().toString();
            xprvKey = xprv;
        } catch (IllegalArgumentException iae) {
            FirebaseCrash.report(iae);
            Log.e("psd", "restoreFromBlockByXPRV2: " + iae.toString());
            callback.run();
            return;
        }

        callback.run();

        RequestorBtc.getUTXOListLtcNew(wallet.currentReceiveAddress().toString(), new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                List<UTXOItem> utxos = (List<UTXOItem>)response;
                setUTXO(utxos);
            }

            @Override
            public void onFailure(String msg) {

            }
        });
    }

    public void restoreFromBlockByWif2(String wif, Runnable callback) {
        wif = wif.trim();
        try {
            DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, wif);
            ECKey key = dumpedPrivateKey.getKey();
            wallet = new Wallet(params);
            wallet.importKey(key);
            restFromWif = true;
            sharedManager.setLastSyncedBlock(Coders.encodeBase64(wif));
            walletFriendlyAddress = wallet.getImportedKeys().get(0).toAddress(params).toString();
            wifKey = wif;
        } catch (WrongNetworkException wne) {
            Log.e("psd", "restoreFromBlockByWif2: " + wne.toString());
            callback.run();
            return;
        } catch (IllegalArgumentException iae) {
            FirebaseCrash.report(iae);
            Log.e("psd", "restoreFromBlockByWif2: " + iae.toString());
            callback.run();
            return;
        }

        callback.run();

        RequestorBtc.getUTXOListLtcNew(walletFriendlyAddress, new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                List<UTXOItem> utxos = (List<UTXOItem>)response;
                setUTXO(utxos);
            }

            @Override
            public void onFailure(String msg) {

            }
        });
    }

    private void setUTXO(List<UTXOItem> utxoList) {
        if (wallet == null) return;

        Address a;
        if (restFromWif) {
            a = wallet.getImportedKeys().get(0).toAddress(params);
        } else {
            a = wallet.currentReceiveAddress();
        }

        final List<UTXO> utxos = new ArrayList<>();

        for (UTXOItem utxo : utxoList) {
            Sha256Hash hash = Sha256Hash.wrap(utxo.getTxHash());
            utxos.add(new UTXO(hash, utxo.getTxOutputN(), Coin.valueOf(utxo.getSatoshiValue()),
                    0, false, ScriptBuilder.createOutputScript(a)));
        }

        UTXOProvider utxoProvider = new UTXOProvider() {
            @Override
            public List<UTXO> getOpenTransactionOutputs(List<Address> addresses) throws UTXOProviderException {
                return utxos;
            }

            @Override
            public int getChainHeadHeight() throws UTXOProviderException {
                return Integer.MAX_VALUE;
            }

            @Override
            public NetworkParameters getParams() {
                return wallet.getParams();
            }
        };
        wallet.setUTXOProvider(utxoProvider);
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

    public void setBalance(Long balance) {
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
        return xprvKey;
    }

    public String getWifKey() {
        if (wallet != null) {
            return wifKey;
        } else {
            return "";
        }
    }

    public String getWalletFriendlyAddress() {
        return walletFriendlyAddress;
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
        wallet = null;
        walletFriendlyAddress = null;
        mnemonicKey = "";
        myBalance = Coin.ZERO;
        xprvKey = "";
        wifKey = "";
        restFromWif = false;
    }

    public boolean isValidPrivateKey(String key) {
        String[] words = key.split("\\W+");

        if (words.length != MNEMONIC_WORDS_COUNT){
            return false;
        }
        for (String word : words) {
            if (!mBip39Words.contains(word)){
                return false;
            }
        }
        return true;
    }

    public boolean isSimilarToAddress(String text) {
        return isAddressValid(text);
    }

    public void isAddressValid(String address, final Callback<Boolean> callback) {
        try {
            Address.fromBase58(params, address);
            callback.onResponse(true);
        } catch (WrongNetworkException wne) {
            Log.e("psd", "isAddressValid: " + wne.toString());
            callback.onResponse(false);
        } catch (AddressFormatException afe) {
            Log.e("psd", "isAddressValid: " + afe.toString());
            callback.onResponse(false);
        } catch (Exception e) {
            e.printStackTrace();
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
        if (restFromWif) {
            sendRequest.changeAddress = wallet.getImportedKeys().get(0).toAddress(params);
        } else {
            sendRequest.changeAddress = wallet.currentReceiveAddress();
        }
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
        if (restFromWif) {
            sendRequest.changeAddress = wallet.getImportedKeys().get(0).toAddress(params);
        } else {
            sendRequest.changeAddress = wallet.currentReceiveAddress();
        }
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

}
