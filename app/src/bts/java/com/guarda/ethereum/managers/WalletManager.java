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
import com.guarda.ethereum.utils.FileUtils;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.UTXO;
import org.bitcoinj.core.UTXOProvider;
import org.bitcoinj.core.UTXOProviderException;
import org.bitcoinj.core.WrongNetworkException;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;
import com.bitshares.bitshareswallet.wallet.account_object;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import autodagger.AutoInjector;
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
    private String walletSystemAddress;

    @Inject
    SharedManager sharedManager;


    private Coin myBalance;
    private Context context;
    private static NetworkParameters params = MainNetParams.get();
    private String mnemonicKey;
    private String xprvKey;
    private HashSet<String> mBip39Words;
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
//        walletFriendlyAddress = wallet.currentReceiveAddress().toString();
//        callback.onWalletCreated(wallet);
        restoreFromBlock(Coders.decodeBase64(sharedManager.getLastSyncedBlock()), callback);
    }

    public void createWallet2(String passphrase, Runnable callback) {

        wallet = new Wallet(params);
        DeterministicSeed seed = wallet.getKeyChainSeed();

        mnemonicKey = Joiner.on(" ").join(seed.getMnemonicCode());
        sharedManager.setLastSyncedBlock(Coders.encodeBase64(mnemonicKey));

        walletFriendlyAddress = wallet.currentReceiveAddress().toString();
        callback.run();
    }

    public void restoreFromBlock(String privateKey, WalletCreationCallback callback) {
        String name = sharedManager.getWalletEmail();
        String accId = BtsManager.getInstance().importAccountPassword(name, privateKey);
        if (accId != null) {
            walletFriendlyAddress = name;
            walletSystemAddress = accId;
        }
        callback.onWalletCreated(null);
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

        RequestorBtc.getUTXOListBch(wallet.currentReceiveAddress().toString(), new ApiMethods.RequestListener() {
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

    public void restoreFromBlock0(String privateKey, Runnable callback) {
        String name = sharedManager.getWalletEmail();
        String accId = BtsManager.getInstance().importAccountPassword(name, privateKey);
        if (accId != null) {
            walletFriendlyAddress = name;
            walletSystemAddress = accId;
        }
        callback.run();
    }


    public void restoreFromBlock2(String name, String privateKey, Runnable callback) {
        String accId = BtsManager.getInstance().importAccountPassword(name, privateKey);
        if (accId != null) {
            sharedManager.setLastSyncedBlock(Coders.encodeBase64(privateKey));
            sharedManager.setWalletEmail(name);
            walletFriendlyAddress = name;
            walletSystemAddress = accId;
        }
        callback.run();
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

        RequestorBtc.getUTXOListBch(wallet.currentReceiveAddress().toString(), new ApiMethods.RequestListener() {
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

        Address a = wallet.currentReceiveAddress();
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

    public void setBalance(long balance) {
        Coin coin = balance != 0 ? Coin.valueOf(balance) : Coin.ZERO;
        this.balance = new BigDecimal(coin.toPlainString());
    }

    public BigDecimal getBalance() {
        return this.balance;
    }

    public String getPrivateKey() {
        if ("".equals(mnemonicKey))
            mnemonicKey = Coders.decodeBase64(sharedManager.getLastSyncedBlock());
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

    public String getWalletFriendlyAddress() {
        return walletFriendlyAddress;
    }

    public String getWalletSystemAddress() {
        return walletSystemAddress;
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
    }

    public boolean isValidPrivateKey(String key) {
        return true;
    }

    public boolean isSimilarToAddress(String text) {
        return isAddressValid(text);
    }

    public void isAddressValid(String address, final Callback<Boolean> callback) {
        account_object accountObject =BtsManager.getInstance().isNameRegistered(address);
        callback.onResponse(accountObject != null);
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

    public String generateHexTx(String toAddress, long sumIntoSatoshi, long feeIntoSatoshi) {
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
        sendRequest.changeAddress = wallet.currentReceiveAddress();
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


    public long calculateFee(String toAddress, long sumIntoSatoshi, long feeIntoSatoshi) throws Exception {
        Address RECEIVER = Address.fromBase58(params, toAddress);
        Coin AMOUNT = Coin.valueOf(sumIntoSatoshi);
        Coin FEE = Coin.valueOf(feeIntoSatoshi);
        SendRequest sendRequest = SendRequest.to(RECEIVER, AMOUNT);
        sendRequest.changeAddress = wallet.currentReceiveAddress();
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



    public static void cashAddressToLegacy(final String cashAddress, final Callback<String> callback) {
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
                        String reqUrl = "http://cashaddr.bitcoincash.org/convert?address=" + cashAddress;
                        Request req = new Request.Builder().url(reqUrl).build();
                        Response resp = httpClient.newCall(req).execute();
                        String respString = resp.body().string();
                        JSONObject respJO = new JSONObject(respString);
                        callback.onResponse(respJO.getString("legacy"));
                    } catch (Exception e) {
                        callback.onResponse(cashAddress);
                    }
                }
            };
            final Thread taskThread = new Thread(runnable);
            taskThread.start();
        } catch (Exception e) {
            callback.onResponse(cashAddress);
        }
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
