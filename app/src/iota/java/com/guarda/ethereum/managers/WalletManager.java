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
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.WrongNetworkException;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.Wallet;
import org.greenrobot.eventbus.EventBus;
import org.iota.wallet.api.TaskManager;
import org.iota.wallet.api.requests.GetAccountDataRequest;
import org.iota.wallet.api.requests.GetNewAddressRequest;
import org.iota.wallet.api.requests.SendTransferRequest;
import org.iota.wallet.api.responses.GetAccountDataResponse;
import org.iota.wallet.api.responses.GetNewAddressResponse;
import org.iota.wallet.api.responses.SendTransferResponse;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;

import autodagger.AutoInjector;
import jota.utils.SeedRandomGenerator;
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
    private static NetworkParameters params = MainNetParams.get();
    private String mnemonicKey;
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

        restoreFromBlock(Coders.decodeBase64(sharedManager.getLastSyncedBlock()), callback);

    }

    public void createWallet2(String passphrase, Runnable callback) {
        final String generatedSeed = SeedRandomGenerator.generateNewSeed();
        String addr = generateFirstAddress(generatedSeed);
        sharedManager.setLastSyncedBlock(Coders.encodeBase64(generatedSeed));
        mnemonicKey = generatedSeed;
        walletFriendlyAddress = addr;
        callback.run();
    }

    public void restoreFromBlock(String mnemonicCode, WalletCreationCallback callback) {
        mnemonicCode = mnemonicCode.trim();
        if (mnemonicCode.equals("")) {
            callback.onWalletCreated(null);
            return;
        }

        mnemonicKey = mnemonicCode;
        String addr = generateFirstAddress(mnemonicCode);
        walletFriendlyAddress = addr;
        callback.onWalletCreated(null);
    }

    public void restoreFromBlockByXPRV(String xprv, WalletCreationCallback callback) {
    }

    public void restoreFromBlock0(String mnemonicCode, Runnable callback) {
        if (mnemonicCode.charAt(mnemonicCode.length() - 1) == ' ') {
            mnemonicCode = mnemonicCode.substring(0, mnemonicCode.length() - 1);
        }

        mnemonicKey = mnemonicCode;
        String addr = generateFirstAddress(mnemonicCode);
        walletFriendlyAddress = addr;
        callback.run();
    }


    public void restoreFromBlock2(String mnemonicCode, Runnable callback) {
        if (mnemonicCode.charAt(mnemonicCode.length() - 1) == ' ') {
            mnemonicCode = mnemonicCode.substring(0, mnemonicCode.length() - 1);
        }

        Log.d("flint", "WalletManager.restoreFromBlock2()...");
        mnemonicKey = mnemonicCode;
        String addr = generateFirstAddress(mnemonicCode);
        if (!"".equals(addr))
            sharedManager.setLastSyncedBlock(Coders.encodeBase64(mnemonicCode));
        walletFriendlyAddress = addr;
        callback.run();

    }

    public void restoreFromBlockByXPRV2(String xprv, Runnable callback) {
    }

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
        callback.onResponse(true);
    }

    public boolean isAddressValid(String address) {
        return true;
    }

    public static String SMALL_SENDING = "insufficientMoney";
    public static String NOT_ENOUGH_MONEY = "notEnough";

    public String generateHexTx(String toAddress, long sumIntoSatoshi, long feeIntoSatoshi) {
//        Address RECEIVER = Address.fromBase58(params, toAddress);
//
//        Coin AMOUNT = Coin.valueOf(sumIntoSatoshi);
//        Coin FEE = Coin.valueOf(feeIntoSatoshi);
//
//        Log.d("svcom", "tx - amount = " + AMOUNT.toFriendlyString() + " fee = " + FEE.toFriendlyString());
//        /**
//         * available default fee
//         * Transaction.REFERENCE_DEFAULT_MIN_TX_FEE;
//         * Transaction.DEFAULT_TX_FEE;
//         */
//        SendRequest sendRequest = SendRequest.to(RECEIVER, AMOUNT);
//        sendRequest.changeAddress = wallet.currentReceiveAddress();
//        sendRequest.ensureMinRequiredFee = true;
//        sendRequest.setUseForkId(true);
//        sendRequest.feePerKb = FEE;
//
//        Transaction trx = null;
//        String hex = "";
//        try {
//            //trx = wallet.sendCoinsOffline(sendRequest);
//            wallet.completeTx(sendRequest);
//            trx = sendRequest.tx;
//
////            Log.d("flint", "getInputSum: " + trx.getInputSum().toFriendlyString());
////            Log.d("flint", "getOutputSum: " + trx.getOutputSum().toFriendlyString());
////            Log.d("flint", "getFee: " + trx.getFee().toFriendlyString());
////            Log.d("flint", "getInput.getHash: " + trx.getInput(0).getOutpoint().getHash().toString());
//
//            Log.d("svcom", "size = " + trx.bitcoinSerialize().length);
//            hex = Hex.toHexString(trx.bitcoinSerialize());
//            Log.d("svcom", "hex: " + hex);
//        } catch (InsufficientMoneyException e) {
//            e.printStackTrace();
//            return NOT_ENOUGH_MONEY;
//        } catch (Wallet.DustySendRequested e) {
//            e.printStackTrace();
//            return SMALL_SENDING;
//        }
//        return hex;
        return "";
    }


    public long calculateFee(String toAddress, long sumIntoSatoshi, long feeIntoSatoshi) throws Exception {
//        Address RECEIVER = Address.fromBase58(params, toAddress);
//        Coin AMOUNT = Coin.valueOf(sumIntoSatoshi);
//        Coin FEE = Coin.valueOf(feeIntoSatoshi);
//        SendRequest sendRequest = SendRequest.to(RECEIVER, AMOUNT);
//        sendRequest.changeAddress = wallet.currentReceiveAddress();
//        sendRequest.ensureMinRequiredFee = true;
//        sendRequest.setUseForkId(true);
//        sendRequest.feePerKb = FEE;
//        long result = FEE.getValue();
//        try {
//            Log.d("flint", "WalletManager.calculateFee().... wallet.getBalance(): " + wallet.getBalance());
//            wallet.completeTx(sendRequest);
//            result = sendRequest.tx.getFee().getValue();
//        } catch (InsufficientMoneyException e) {
//            throw new Exception("NOT_ENOUGH_MONEY");
//        } catch (Wallet.DustySendRequested e) {
//            throw new Exception("SMALL_SENDING");
//        } catch (Exception e) {
//            throw new Exception(e.toString());
//        }
//        return result;
        return 777;
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



    private void getAccountData(String seed) {
        TaskManager rt = new TaskManager(context);
        GetAccountDataRequest gna = new GetAccountDataRequest(seed);
        //rt.startNewRequestTask(gna);
    }


    private String generateFirstAddress(String seed) {
        ConcurrentLinkedQueue<GetNewAddressResponse> respContainer = new ConcurrentLinkedQueue<>();
        Thread t1 = new Thread(() -> {
            Thread thisThread = Thread.currentThread();
            Callback<Object> callback = (r) -> {
                if (r instanceof GetNewAddressResponse) {
                    GetNewAddressResponse resp = (GetNewAddressResponse) r;
                    respContainer.add(resp);
                }
                thisThread.interrupt();
            };

            TaskManager rt = new TaskManager(context);
            GetNewAddressRequest gtr = new GetNewAddressRequest();
            gtr.setTotal(1);
            gtr.setSeed(String.valueOf(seed));
            rt.startNewRequestTask(gtr, callback);
            try {Thread.sleep(API_TIMEOUT_MILLIS);}catch (InterruptedException e) {}
        });
        t1.start();

        try {
            t1.join(API_TIMEOUT_MILLIS);
            GetNewAddressResponse resp = respContainer.poll();
            if (resp != null)
                if (resp.getAddresses().size() > 0)
                    return resp.getAddresses().get(0);
        } catch (InterruptedException e) {
            return "";
        }
        return "";
    }



    public void requestBalanceAndTransactions(String seed, Callback<GetAccountDataResponse> onComplete) {
        Callback<Object> callback = (r) -> {
            if (r instanceof GetAccountDataResponse) {
                GetAccountDataResponse resp = (GetAccountDataResponse) r;
                onComplete.onResponse(resp);
            } else {
                onComplete.onResponse(null);
            }
        };

        TaskManager rt = new TaskManager(context);
        GetAccountDataRequest gtr = new GetAccountDataRequest(seed);
        gtr.setSeed(String.valueOf(seed));
        rt.startNewRequestTask(gtr, callback);
    }



    public void sendCoins(String address, long amount, Callback<SendTransferResponse> onComplete) {
        amount = amount / 100;
        String seed = Coders.decodeBase64(sharedManager.getLastSyncedBlock());
        Callback<Object> callback = (r) -> {
            if (r instanceof SendTransferResponse) {
                SendTransferResponse resp = (SendTransferResponse) r;
                onComplete.onResponse(resp);
            } else {
                onComplete.onResponse(null);
            }
        };

        TaskManager rt = new TaskManager(context);
        SendTransferRequest request = new SendTransferRequest(address, Long.valueOf(amount).toString(), "", "", seed);
        rt.startNewRequestTask(request, callback);
    }




    private static int API_TIMEOUT_MILLIS = 8000;

}
