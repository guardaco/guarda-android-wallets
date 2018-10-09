package com.guarda.ethereum.managers;

import android.content.Context;
import android.net.Credentials;
import android.util.Log;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.guarda.ethereum.BuildConfig;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.models.items.ContractUnspentOutput;
import com.guarda.ethereum.models.items.UTXOItem;
import com.guarda.ethereum.models.items.UTXOListResponse;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.RequestorBtc;
import com.guarda.ethereum.utils.Coders;
import com.guarda.ethereum.utils.ContractBuilder;
import com.guarda.ethereum.utils.ContractMethodParameter;
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
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.UTXO;
import org.bitcoinj.core.UTXOProvider;
import org.bitcoinj.core.UTXOProviderException;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.WrongNetworkException;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.QtumMainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import autodagger.AutoInjector;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
    private static NetworkParameters params = QtumMainNetParams.get();
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

    public boolean isCorrectMnemonic(String mnemonic){
        String[] words = mnemonic.split("\\W+");
        for (String word : words) {
            if (!mBip39Words.contains(word)){
                return false;
            }
        }
        return true;
    }

    public void createWallet(String passphrase, WalletCreationCallback callback) {

//        wallet = new Wallet(params);
//        DeterministicSeed seed = wallet.getKeyChainSeed();
//
//        mnemonicKey = Joiner.on(" ").join(seed.getMnemonicCode());
//        //sharedManager.setLastSyncedBlock(Coders.encodeBase64(mnemonicKey));
//
//        updateWalletFriendlyAddress();
//        callback.onWalletCreated(wallet);
        restoreFromBlock(Coders.decodeBase64(sharedManager.getLastSyncedBlock()), callback);

    }

    private void updateWalletFriendlyAddress() {
        walletFriendlyAddress = extractWalletFriendlyAddress().toBase58();
    }

    private Address extractWalletFriendlyAddress() {
//        return wallet.currentReceiveAddress();
        DeterministicKey deterministicKey = extractWalletDeterministicKey();
        String wifwif = deterministicKey.getPrivateKeyAsWiF(params);
        return deterministicKey.toAddress(params);
    }

    private List<ChildNumber> getPathParent() {
        List<ChildNumber> pathParent = new ArrayList<>();
        pathParent.add(new ChildNumber(88, true));
        pathParent.add(new ChildNumber(0, true));
        pathParent.add(new ChildNumber(0, true));
        return pathParent;
    }

    private DeterministicKey extractWalletDeterministicKey () {
        return wallet.getActiveKeyChain().getKeyByPath(getPathParent(), true);
    }

    public void createWallet2(String passphrase, Runnable callback) {

        wallet = new Wallet(params);
        DeterministicSeed seed = wallet.getKeyChainSeed();

        mnemonicKey = Joiner.on(" ").join(seed.getMnemonicCode());
        sharedManager.setLastSyncedBlock(Coders.encodeBase64(mnemonicKey));

        updateWalletFriendlyAddress();

//        ChildNumber childNumber = new ChildNumber(ChildNumber.HARDENED_BIT);
//        DeterministicKey de = wallet.getKeyByPath(ImmutableList.of(childNumber));
//        xprvKey = de.serializePrivB58(params);
//
//        if (BuildConfig.DEBUG) {
//            DeterministicKey dk = extractWalletDeterministicKey();
//            wifKey = dk.getPrivateKeyAsWiF(params);
//        } else {
//            wifKey = wallet.currentReceiveKey().getPrivateKeyAsWiF(params);
//        }

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
        //Log.d("flint", "wallet.toString: " + wallet.toString());
        mnemonicKey = Joiner.on(" ").join(seed.getMnemonicCode());
        //sharedManager.setLastSyncedBlock(Coders.encodeBase64(mnemonicKey));

        updateWalletFriendlyAddress();

//        ChildNumber childNumber = new ChildNumber(ChildNumber.HARDENED_BIT);
//        DeterministicKey de = wallet.getKeyByPath(ImmutableList.of(childNumber));
//        xprvKey = de.serializePrivB58(params);
//
//        if (BuildConfig.DEBUG) {
//            DeterministicKey dk = extractWalletDeterministicKey();
//            wifKey = dk.getPrivateKeyAsWiF(params);
//        } else {
//            wifKey = wallet.currentReceiveKey().getPrivateKeyAsWiF(params);
//        }


        callback.onWalletCreated(wallet);


        RequestorBtc.getUTXOListQtum(walletFriendlyAddress, new ApiMethods.RequestListener() {
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
            updateWalletFriendlyAddress();
            xprvKey = xprv;
        } catch (IllegalArgumentException iae) {
            FirebaseCrash.report(iae);
            Log.e("psd", "restoreFromBlockByXPRV: " + iae.toString());
            callback.onWalletCreated(wallet);
            return;
        }

        callback.onWalletCreated(wallet);

        RequestorBtc.getUTXOListQtum(walletFriendlyAddress, new ApiMethods.RequestListener() {
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

        RequestorBtc.getUTXOListQtum(walletFriendlyAddress, new ApiMethods.RequestListener() {
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

        updateWalletFriendlyAddress();

        callback.run();


        RequestorBtc.getUTXOListQtum(walletFriendlyAddress, new ApiMethods.RequestListener() {
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

        updateWalletFriendlyAddress();
        DeterministicKey deterministicKey = wallet.getActiveKeyChain().getKey(KeyChain.KeyPurpose.RECEIVE_FUNDS);
//        Log.d("flint", "walletAddress: " + deterministicKey.toAddress(params));
//        Log.d("flint", "friendlyWalletAddress: " + deterministicKey.toAddress(params).toBase58());

//        ChildNumber childNumber = new ChildNumber(ChildNumber.HARDENED_BIT);
//        DeterministicKey de = wallet.getKeyByPath(ImmutableList.of(childNumber));
//        xprvKey = de.serializePrivB58(params);
//
//        if (BuildConfig.DEBUG) {
//            DeterministicKey dk = extractWalletDeterministicKey();
//            wifKey = dk.getPrivateKeyAsWiF(params);
//        } else {
//            wifKey = wallet.currentReceiveKey().getPrivateKeyAsWiF(params);
//        }

        callback.run();


        RequestorBtc.getUTXOListQtum(walletFriendlyAddress, new ApiMethods.RequestListener() {
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
            updateWalletFriendlyAddress();
            xprvKey = xprv;
        } catch (IllegalArgumentException iae) {
            FirebaseCrash.report(iae);
            Log.e("psd", "restoreFromBlockByXPRV2: " + iae.toString());
            callback.run();
            return;
        }

        callback.run();

        RequestorBtc.getUTXOListQtum(walletFriendlyAddress, new ApiMethods.RequestListener() {
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

        RequestorBtc.getUTXOListQtum(walletFriendlyAddress, new ApiMethods.RequestListener() {
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
        myBalance = satoshi != null ? Coin.valueOf(satoshi) : Coin.ZERO;
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
//        if (BuildConfig.DEBUG) {
//            return "QcJJEK9Hy31dg9Q4ieb7NzQkTGPVmRYksi";
//            return "QUvjoMYY21pirPovKFmnw78b9xTHhZrCLc";
//            return "QeQbrR7vh9zh9sJ17mqmFVqzt5fWmUQNcx";
//        }
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

    public String getWalletAddressWithoutPrefix() {
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
            return !address.isEmpty()
                    && params.equals(Address.getParametersFromAddress(address));
        } catch (Exception e) {
            return false;
        }
    }

    public static String SMALL_SENDING = "insufficientMoney";
    public static String NOT_ENOUGH_MONEY = "notEnough";

    public String generateHexTx_bak(String toAddress, long sumIntoSatoshi, long feeIntoSatoshi) {
        Address RECEIVER = Address.fromBase58(params, toAddress);

        Coin AMOUNT = Coin.valueOf(sumIntoSatoshi);
        Coin FEE = Coin.valueOf(feeIntoSatoshi);

        Log.d("svcom", "tx - amount = " + AMOUNT.toFriendlyString() + " fee = " + FEE.toFriendlyString());
        /**
         * available default fee
         * Transaction.REFERENCE_DEFAULT_MIN_TX_FEE;
         * Transaction.DEFAULT_TX_FEE;
         */
        SendRequest sendRequest = SendRequest.to(RECEIVER, AMOUNT);
        if (restFromWif) {
            sendRequest.changeAddress = wallet.getImportedKeys().get(0).toAddress(params);
        } else {
            sendRequest.changeAddress = extractWalletFriendlyAddress();
        }
        sendRequest.ensureMinRequiredFee = true;
        //sendRequest.setUseForkId(true);
        sendRequest.feePerKb = FEE;

        Transaction trx = null;
        String hex = "";
        try {
            //trx = wallet.sendCoinsOffline(sendRequest);
            wallet.completeTx(sendRequest);
            trx = sendRequest.tx;

//            Log.d("flint", "getInputSum: " + trx.getInputSum().toFriendlyString());
//            Log.d("flint", "getOutputSum: " + trx.getOutputSum().toFriendlyString());
//            Log.d("flint", "getFee: " + trx.getFee().toFriendlyString());
//            Log.d("flint", "getInput.getHash: " + trx.getInput(0).getOutpoint().getHash().toString());

            Log.d("svcom", "size = " + trx.bitcoinSerialize().length);
            //hex = Hex.toHexString(trx.bitcoinSerialize());
            hex = Hex.toHexString(trx.bitcoinSerialize());
            Log.d("svcom", "hex: " + hex);
        } catch (InsufficientMoneyException e) {
            e.printStackTrace();
            return NOT_ENOUGH_MONEY;
        } catch (Wallet.DustySendRequested e) {
            e.printStackTrace();
            return SMALL_SENDING;
        }
        return hex;
    }

    public String generateHexTx(String toAddress, long sumIntoSatoshi, long feeIntoSatoshi, UnspentOutputList unspentOutputList) {
        Coin AMOUNT = Coin.valueOf(sumIntoSatoshi);
        Coin FEE = Coin.valueOf(feeIntoSatoshi);
        Transaction tx = createTx(toAddress, AMOUNT.toPlainString(), FEE.toPlainString(), unspentOutputList);
        if (tx == null)
            return "error";
        return Hex.toHexString(tx.unsafeBitcoinSerialize());
    }

    public Transaction generateTx(String toAddress, long sumIntoSatoshi, long feeIntoSatoshi, UnspentOutputList unspentOutputList) {
        Coin AMOUNT = Coin.valueOf(sumIntoSatoshi);
        Coin FEE = Coin.valueOf(feeIntoSatoshi);
        Transaction tx = createTx(toAddress, AMOUNT.toPlainString(), FEE.toPlainString(), unspentOutputList);
        return tx;
    }


    public long calculateFee_bak(String toAddress, long sumIntoSatoshi, long feeIntoSatoshi) throws Exception {
        Address RECEIVER = Address.fromBase58(params, toAddress);
        Coin AMOUNT = Coin.valueOf(sumIntoSatoshi);
        Coin FEE = Coin.valueOf(feeIntoSatoshi);
        SendRequest sendRequest = SendRequest.to(RECEIVER, AMOUNT);
        if (restFromWif) {
            sendRequest.changeAddress = wallet.getImportedKeys().get(0).toAddress(params);
        } else {
            sendRequest.changeAddress = extractWalletFriendlyAddress();
        }
        sendRequest.ensureMinRequiredFee = true;
        //sendRequest.setUseForkId(true);
        sendRequest.feePerKb = FEE;
        long result = FEE.getValue();
        try {
            Log.d("flint", "WalletManager.calculateFee().... wallet.getBalance(): " + wallet.getBalance());
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

    public long calculateFee(String toAddress, long sumIntoSatoshi, long feeIntoSatoshi, UnspentOutputList unspentOutputList, long feePerKb) throws Exception {
        Coin AMOUNT = Coin.valueOf(sumIntoSatoshi);
        Coin FEE = Coin.valueOf(feeIntoSatoshi);
        Transaction tx = createTx(toAddress, AMOUNT.toPlainString(), FEE.toPlainString(), unspentOutputList);
        if (tx == null)
            return 0;
        int txSizeInB = (int) Math.ceil(tx.unsafeBitcoinSerialize().length);
        return (long)Math.ceil(feePerKb * txSizeInB / 1024.0);
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


    public void getUnspentOutput(final Callback<UnspentOutputList> callback) {
        getUnspentOutput(walletFriendlyAddress, new Callback<List<UnspentOutput>>() {
            @Override
            public void onResponse(List<UnspentOutput> response) {
                UnspentOutputList list = new UnspentOutputList();
                list.list = response;
                callback.onResponse(list);
            }
        });
    }

    private void getUnspentOutput(final String address, final Callback<List<UnspentOutput>> callback) {
        try {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        OkHttpClient httpClient = new OkHttpClient.Builder()
                                .connectTimeout(3000, TimeUnit.MILLISECONDS)
                                .writeTimeout(3000, TimeUnit.MILLISECONDS)
                                .readTimeout(3000, TimeUnit.MILLISECONDS)
                                .build();
                        String reqUrl = "https://walletapi.qtum.org/outputs/unspent/" + address;
                        Request req = new Request.Builder().url(reqUrl).build();
                        Response resp = httpClient.newCall(req).execute();
                        String respString = resp.body().string();
                        JSONArray respJA = new JSONArray(respString);
                        List<UnspentOutput> respList = new ArrayList<>();
                        for (int i = 0; i < respJA.length(); ++i) {
                            String rowStr = respJA.getString(i);
                            Gson gson = new Gson();
                            respList.add(gson.fromJson(rowStr, UnspentOutput.class));
                        }
                        callback.onResponse(respList);
                    } catch (Exception e) {
                        callback.onResponse(null);
                    }
                }
            };
            final Thread taskThread = new Thread(runnable);
            taskThread.start();
        } catch (Exception e) {
            callback.onResponse(null);
        }
    }



    private Transaction createTx(final String address, final String amountString, final String feeString, UnspentOutputList unspentOutputList) {
        List<UnspentOutput> unspentOutputs = unspentOutputList.list;
        try {
            Transaction transaction = new Transaction(params);
            Address addressToSend;
            BigDecimal bitcoin = new BigDecimal(100000000);
            try {
                addressToSend = Address.fromBase58(params, address);
            } catch (AddressFormatException a) {
                return null;
            }
            ECKey myKey = extractWalletDeterministicKey().decompress();
            BigDecimal amount = new BigDecimal(amountString);
            BigDecimal fee = new BigDecimal(feeString);
            BigDecimal estimateFeePerKb = new BigDecimal(feeString);
            BigDecimal amountFromOutput = new BigDecimal("0.0");
            BigDecimal overFlow = new BigDecimal("0.0");
            transaction.addOutput(Coin.valueOf((long) (amount.multiply(bitcoin).doubleValue())), addressToSend);
            amount = amount.add(fee);

            for (UnspentOutput unspentOutput : unspentOutputs) {
                overFlow = overFlow.add(unspentOutput.getAmount());
                if (overFlow.doubleValue() >= amount.doubleValue()) {
                    break;
                }
            }
            if (overFlow.doubleValue() < amount.doubleValue()) {
                return null;
            }
            BigDecimal delivery = overFlow.subtract(amount);
            if (delivery.doubleValue() != 0.0) {
                Address changeAddress = extractWalletFriendlyAddress();
                transaction.addOutput(Coin.valueOf((long) (delivery.multiply(bitcoin).doubleValue())), changeAddress);
            }
            for (UnspentOutput unspentOutput : unspentOutputs) {
                List<DeterministicKey> keysList = new ArrayList<>();
                keysList.add(extractWalletDeterministicKey());
                for (DeterministicKey deterministicKey : keysList) {
                    if (deterministicKey.toAddress(params).toString().equals(unspentOutput.getAddress())) {
                        Sha256Hash sha256Hash = new Sha256Hash(Utils.parseAsHexOrBase58(unspentOutput.getTxHash()));
                        TransactionOutPoint outPoint = new TransactionOutPoint(params, unspentOutput.getVout(), sha256Hash);
                        Script script = new Script(Utils.parseAsHexOrBase58(unspentOutput.getTxoutScriptPubKey()));
                        transaction.addSignedInput(outPoint, script, deterministicKey, Transaction.SigHash.ALL, true);
                        amountFromOutput = amountFromOutput.add(unspentOutput.getAmount());
                        break;
                    }
                }
                if (amountFromOutput.doubleValue() >= amount.doubleValue()) {
                    break;
                }
            }
            transaction.getConfidence().setSource(TransactionConfidence.Source.SELF);
            transaction.setPurpose(Transaction.Purpose.USER_PAYMENT);
            byte[] bytes = transaction.unsafeBitcoinSerialize();
            int txSizeInkB = (int) Math.ceil(bytes.length / 1024.);
            BigDecimal minimumFee = (estimateFeePerKb.multiply(new BigDecimal(txSizeInkB)));
            if (minimumFee.doubleValue() > fee.doubleValue()) {
                return null;
            }
            String transactionHex = Hex.toHexString(bytes);
            return transaction;
        } catch (Exception e) {
            return null;
        }
    }



    public String createTokenHexTx(String abiParams, String tokenAddress,
                                   String fee, BigDecimal feePerKb, List<ContractUnspentOutput> unspentOutputs,
                                   String description) throws Exception {

        final int gasLimit = 300000;
        final int gasPrice = 40;

        ContractBuilder contractBuilder = new ContractBuilder();
        Script script = contractBuilder.createMethodScript(abiParams, gasLimit, gasPrice, tokenAddress);

        DeterministicKey ownDeterKey = extractWalletDeterministicKey();

        return contractBuilder.createTransactionHash(script, unspentOutputs, gasLimit, gasPrice,
                feePerKb, fee,
                context, params, extractWalletFriendlyAddress(), wallet, description, ownDeterKey);
    }



    public String createAbiMethodParams(String address, String resultAmount) {
        ContractBuilder contractBuilder = new ContractBuilder();
        List<ContractMethodParameter> contractMethodParameterList = new ArrayList<>();
        ContractMethodParameter contractMethodParameterAddress = new ContractMethodParameter("_to", "address", address);

        ContractMethodParameter contractMethodParameterAmount = new ContractMethodParameter("_value", "uint256", resultAmount);
        contractMethodParameterList.add(contractMethodParameterAddress);
        contractMethodParameterList.add(contractMethodParameterAmount);
        return contractBuilder.createAbiMethodParams("transfer", contractMethodParameterList);
    }



    public String getValidatedFee(Double fee) {
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
        formatSymbols.setDecimalSeparator('.');
        String pattern = "##0.00000000";
        DecimalFormat decimalFormat = new DecimalFormat(pattern, formatSymbols);
        return decimalFormat.format(fee);
    }



    public static class UnspentOutputList {
        public List<UnspentOutput> list;
    }

}



class UnspentOutput {
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("amount")
    @Expose
    private BigDecimal amount;
    @SerializedName("vout")
    @Expose
    private Integer vout;
    @SerializedName("txout_scriptPubKey")
    @Expose
    private String txoutScriptPubKey;
    @SerializedName("tx_hash")
    @Expose
    private String txHash;
    @SerializedName("pubkey_hash")
    @Expose
    private String pubkeyHash;
    @SerializedName("confirmations")
    @Expose
    private Integer confirmations;
    @SerializedName("is_stake")
    private boolean isStake;

    public boolean isOutputAvailableToPay() {
        if (isStake) {
            return confirmations > 500;
        }
        return true;
    }

    public UnspentOutput() {
    }

    /**
     * Constructor for unit testing
     */
    public UnspentOutput(Integer confirmations, boolean isStake, BigDecimal amount) {
        this.confirmations = confirmations;
        this.isStake = isStake;
        this.amount = amount;
    }

    public String getAddress() {
        return address;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getVout() {
        return vout;
    }

    public void setVout(Integer vout) {
        this.vout = vout;
    }

    public String getTxoutScriptPubKey() {
        return txoutScriptPubKey;
    }

    public void setTxoutScriptPubKey(String txoutScriptPubKey) {
        this.txoutScriptPubKey = txoutScriptPubKey;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getPubkeyHash() {
        return pubkeyHash;
    }

    public void setPubkeyHash(String pubkeyHash) {
        this.pubkeyHash = pubkeyHash;
    }

    public Integer getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(Integer confirmations) {
        this.confirmations = confirmations;
    }
}
