package com.guarda.ethereum.managers;

import android.content.Context;
import android.net.Credentials;
import android.util.Log;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.firebase.crash.FirebaseCrash;
import com.gravilink.zcash.WalletCallback;
import com.gravilink.zcash.ZCashException;
import com.gravilink.zcash.ZCashTransactionOutput;
import com.gravilink.zcash.ZCashTransaction_taddr;
import com.gravilink.zcash.ZCashWalletManager;
import com.gravilink.zcash.crypto.Utils;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.models.items.BtgTxResponse;
import com.guarda.ethereum.models.items.UTXOItem;
import com.guarda.ethereum.models.items.UTXOListResponse;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.RequestorBtc;
import com.guarda.ethereum.utils.Coders;
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
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.UTXO;
import org.bitcoinj.core.UTXOProvider;
import org.bitcoinj.core.UTXOProviderException;
import org.bitcoinj.core.WrongNetworkException;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.wallet.CoinSelection;
import org.bitcoinj.wallet.CoinSelector;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.spongycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.inject.Inject;

import autodagger.AutoInjector;

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

    // if null - standard behaviour of app
    // for debug we can put here any public address, so we will see user's transactions
    private String testReadonlyAddress = null;

    private Wallet wallet;
    private String walletFriendlyAddress;

    @Inject
    SharedManager sharedManager;


    private Coin myBalance;
    private Context context;
    private static NetworkParameters params = new BTGParams();
    private String mnemonicKey;
    private String xprvKey;
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
        restoreFromBlock(Coders.decodeBase64(sharedManager.getLastSyncedBlock()), callback);
    }

    public void createWallet2_wif(String passphrase, Runnable callback) {
        wallet = new Wallet(params);
//        DeterministicSeed seed = wallet.getKeyChainSeed();
//
//        mnemonicKey = Joiner.on(" ").join(seed.getMnemonicCode());
        mnemonicKey = wallet.freshReceiveKey().getPrivateKeyAsWiF(params);
        sharedManager.setLastSyncedBlock(Coders.encodeBase64(mnemonicKey));
        DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, mnemonicKey);
        ECKey key = dumpedPrivateKey.getKey();
        wallet.importKey(key);

        walletFriendlyAddress = wallet.getImportedKeys().get(0).toAddress(params).toString();
        walletFriendlyAddress = testReadonlyAddress == null ? walletFriendlyAddress : testReadonlyAddress;
        callback.run();
    }

    private String getRandomSeed() {
        Set<String> words = new HashSet<>();
        for (int i = 0; i < 12; ++i)
            words.add(getRandomWord());
        return Joiner.on(" ").join(words);
    }

    private String getRandomWord() {
        try {
            int indx = new Random().nextInt(mBip39Words.size());
            int i = 0;
            for (String w : mBip39Words) {
                if (i == indx)
                    return w;
                ++i;
            }
            return mBip39Words.iterator().next();
        } catch (Exception e) {
            return "like";
        }
    }

    public void createWallet2(String passphrase, Runnable callback) {
        String randomSeed = getRandomSeed();
        restoreFromBlock2(randomSeed, callback);
    }

    public void restoreFromBlock(String mnemonicCode, WalletCreationCallback callback) {
        mnemonicCode = mnemonicCode.trim();
        if (mnemonicCode.equals("")) {
            callback.onWalletCreated(null);
            return;
        }

        DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, anyToWif(mnemonicCode));
        ECKey key = dumpedPrivateKey.getKey();
        wallet = new Wallet(params);
        wallet.importKey(key);
        mnemonicKey = mnemonicCode;
//        sharedManager.setLastSyncedBlock(Coders.encodeBase64(mnemonicKey));

        walletFriendlyAddress = wallet.getImportedKeys().get(0).toAddress(params).toBase58();
        walletFriendlyAddress = testReadonlyAddress == null ? walletFriendlyAddress : testReadonlyAddress;

        callback.onWalletCreated(wallet);

        RequestorBtc.getUTXOListKmdNew(walletFriendlyAddress, new ApiMethods.RequestListener() {
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

    private String anyToWif(String val) {
        if (val == null)
            return null;
        if (val.indexOf(" ") == -1)
            return val;
        String res = seedToWif(val);
        if (res == null)
            res = val;
        return res;
    }

    private String seedToWif(String seed) {
        try {
            byte[] bytes = Sha256Hash.hash(seed.getBytes());
            bytes[0] &= 248;
            bytes[31] &= 127;
            bytes[31] |= 64;
            BigInteger bi = new BigInteger(Hex.toHexString(bytes), 16);
            ECKey testKey = ECKey.fromPrivate(bi);
            return testKey.getPrivateKeyAsWiF(params);
        } catch (Exception e) {
            Log.e("flintd", "seedToWif... exception: " + e.toString());
        }
        return null;
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
            walletFriendlyAddress = testReadonlyAddress == null ? walletFriendlyAddress : testReadonlyAddress;
            xprvKey = xprv;
        } catch (IllegalArgumentException iae) {
            FirebaseCrash.report(iae);
            Log.e("psd", "restoreFromBlockByXPRV: " + iae.toString());
            callback.onWalletCreated(wallet);
            return;
        }

        callback.onWalletCreated(wallet);

        RequestorBtc.getUTXOListKmdNew(walletFriendlyAddress, new ApiMethods.RequestListener() {
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

        DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, anyToWif(mnemonicCode));
        ECKey key = dumpedPrivateKey.getKey();
        wallet = new Wallet(params);
        wallet.importKey(key);
        mnemonicKey = mnemonicCode;
//        sharedManager.setLastSyncedBlock(Coders.encodeBase64(mnemonicKey));

        walletFriendlyAddress = wallet.getImportedKeys().get(0).toAddress(params).toBase58();
        walletFriendlyAddress = testReadonlyAddress == null ? walletFriendlyAddress : testReadonlyAddress;

        callback.run();

        RequestorBtc.getUTXOListKmdNew(walletFriendlyAddress, new ApiMethods.RequestListener() {
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

        DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, anyToWif(mnemonicCode));
        ECKey key = dumpedPrivateKey.getKey();
        wallet = new Wallet(params);
        wallet.importKey(key);
        mnemonicKey = mnemonicCode;
        sharedManager.setLastSyncedBlock(Coders.encodeBase64(mnemonicKey));

        walletFriendlyAddress = wallet.getImportedKeys().get(0).toAddress(params).toBase58();
        walletFriendlyAddress = testReadonlyAddress == null ? walletFriendlyAddress : testReadonlyAddress;

        callback.run();

        RequestorBtc.getUTXOListKmdNew(walletFriendlyAddress, new ApiMethods.RequestListener() {
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
            walletFriendlyAddress = testReadonlyAddress == null ? walletFriendlyAddress : testReadonlyAddress;
            xprvKey = xprv;
        } catch (IllegalArgumentException iae) {
            FirebaseCrash.report(iae);
            Log.e("psd", "restoreFromBlockByXPRV2: " + iae.toString());
            callback.run();
            return;
        }

        callback.run();

        RequestorBtc.getUTXOListKmdNew(walletFriendlyAddress, new ApiMethods.RequestListener() {
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
        if (wallet == null) {
            return;
        }

        Address a = wallet.getImportedKeys().get(0).toAddress(params);
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
    }

    public boolean isValidPrivateKey(String key) {
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
        sendRequest.changeAddress = wallet.getImportedKeys().get(0).toAddress(params);
        sendRequest.ensureMinRequiredFee = true;
        //sendRequest.setUseForkId(true);
        sendRequest.feePerKb = FEE;
        Transaction trx = null;
        String hex = "";
        try {
            sendRequest.tx.setLockTime(new Date().getTime()/1000);
            wallet.completeTx(sendRequest);
            trx = sendRequest.tx;
            hex = Hex.toHexString(trx.bitcoinSerialize());
        } catch (InsufficientMoneyException e) {
            e.printStackTrace();
            return NOT_ENOUGH_MONEY;
        } catch (Wallet.DustySendRequested e) {
            e.printStackTrace();
            return SMALL_SENDING;
        }
        return hex;
    }

    public String generateClaimHexTx(long interest) {
        Address RECEIVER = Address.fromBase58(params, walletFriendlyAddress);

        Coin AMOUNT = Coin.valueOf(getMyBalance().getValue());
        Coin FEE = Coin.valueOf(0);

        SendRequest sendRequest = SendRequest.to(RECEIVER, AMOUNT);
        sendRequest.changeAddress = wallet.getImportedKeys().get(0).toAddress(params);
        sendRequest.ensureMinRequiredFee = false;
        //sendRequest.setUseForkId(true);
        sendRequest.feePerKb = FEE;
        Transaction trx = null;
        String hex = "";
        try {
            sendRequest.tx.setLockTime(new Date().getTime()/1000);
            sendRequest.emptyWallet = true;
            completeClaimTx(sendRequest, interest);
            trx = sendRequest.tx;
            hex = Hex.toHexString(trx.bitcoinSerialize());
        } catch (InsufficientMoneyException e) {
            e.printStackTrace();
            return NOT_ENOUGH_MONEY;
        } catch (Wallet.DustySendRequested e) {
            e.printStackTrace();
            return SMALL_SENDING;
        }
        return hex;
    }

    public void getEstimateInterest(List<BtgTxResponse> txList, Callback<Coin> callback) {
        RequestorBtc.getUTXOListKmdNew(walletFriendlyAddress, new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                List<UTXOItem> utxos = (List<UTXOItem>)response;
                setUTXO(utxos);

                long resp = 0;
                for (UTXOItem utxo : utxos) {
                    for (BtgTxResponse tx : txList) {
                        if (tx.getHash().equalsIgnoreCase(utxo.getTxHash())) {
                            resp += kmdCaldInterest(tx.getLocktime(), utxo.getSatoshiValue());
                            break;
                        }
                    }
                }

                callback.onResponse(Coin.valueOf(resp));
            }

            @Override
            public void onFailure(String msg) {
                callback.onResponse(Coin.valueOf(0));
            }
        });
    }

    private long kmdCaldInterest(long locktime, long value) {
        long unixNow = System.currentTimeMillis() / 1000L;
        long timestampDiff = unixNow - locktime - 777;
        double timestampDiffMinutes = (double)timestampDiff / 60.0;
        long interest = 0;
        if (locktime > 0) {
            if (value >= 1000000000) {
                if (timestampDiffMinutes >= 60.0) {
                    timestampDiffMinutes = Math.min(timestampDiffMinutes, 365 * 24 * 60);
                    timestampDiffMinutes -= 59.0;
                    interest = Math.round(((double) value / 10512000.0) * timestampDiffMinutes);
                }
            }
        }
        return interest;
    }

    private void completeClaimTx(SendRequest req, long interest) throws InsufficientMoneyException {
        try {
//            if(req.getUseForkId()) {
                req.tx.setVersion(2);
//            }

            Coin value = Coin.ZERO;

            TransactionOutput output;
            for(Iterator var3 = req.tx.getOutputs().iterator(); var3.hasNext(); value = value.add(output.getValue())) {
                output = (TransactionOutput)var3.next();
            }

            Coin totalInput = Coin.ZERO;
            Iterator var15 = req.tx.getInputs().iterator();

            TransactionInput candidates;
            while(var15.hasNext()) {
                candidates = (TransactionInput)var15.next();
                if(candidates.getConnectedOutput() != null) {
                    totalInput = totalInput.add(candidates.getConnectedOutput().getValue());
                } else {
                }
            }

            value = value.subtract(totalInput);
            List<TransactionInput> originalInputs = new ArrayList(req.tx.getInputs());
            TransactionOutput bestChangeOutput;
            if(req.ensureMinRequiredFee && !req.emptyWallet) {
                int opReturnCount = 0;
                Iterator var6 = req.tx.getOutputs().iterator();

                while(var6.hasNext()) {
                    bestChangeOutput = (TransactionOutput)var6.next();
                    if(bestChangeOutput.isDust()) {
                        throw new Wallet.DustySendRequested();
                    }

                    if(bestChangeOutput.getScriptPubKey().isOpReturn()) {
                        ++opReturnCount;
                    }
                }

                if(opReturnCount > 1) {
                    throw new Wallet.MultipleOpReturnRequested();
                }
            }

            List<TransactionOutput> candidates2 = wallet.calculateAllSpendCandidates(true, req.missingSigsMode == Wallet.MissingSigsMode.THROW);
            bestChangeOutput = null;
            List<Coin> updatedOutputValues = null;
            CoinSelection bestCoinSelection;
            if(!req.emptyWallet) {
//                Wallet.FeeCalculation feeCalculation = this.calculateFee(req, value, originalInputs, req.ensureMinRequiredFee, candidates);
//                bestCoinSelection = feeCalculation.bestCoinSelection;
//                bestChangeOutput = feeCalculation.bestChangeOutput;
//                updatedOutputValues = feeCalculation.updatedOutputValues;
                bestCoinSelection = null;
            } else {
                Preconditions.checkState(req.tx.getOutputs().size() == 1, "Empty wallet TX must have a single output only.");
                //CoinSelector selector = req.coinSelector == null?this.coinSelector:req.coinSelector;
                CoinSelector selector = wallet.getCoinSelector();
                bestCoinSelection = selector.select(this.params.getMaxMoney(), candidates2);
                candidates2 = null;
                req.tx.getOutput(0L).setValue(bestCoinSelection.valueGathered);
            }

            Iterator var21 = bestCoinSelection.gathered.iterator();

            while(var21.hasNext()) {
                TransactionOutput output2 = (TransactionOutput)var21.next();
                req.tx.addInput(output2);
            }

//            if(req.emptyWallet) {
//                Coin feePerKb = req.feePerKb == null?Coin.ZERO:req.feePerKb;
//                if(!this.adjustOutputDownwardsForFee(req.tx, bestCoinSelection, feePerKb, req.ensureMinRequiredFee)) {
//                    throw new Wallet.CouldNotAdjustDownwards();
//                }
//            }

            int size;
            if(updatedOutputValues != null) {
                for(size = 0; size < updatedOutputValues.size(); ++size) {
                    req.tx.getOutput((long)size).setValue((Coin)updatedOutputValues.get(size));
                }
            }

            if(bestChangeOutput != null) {
                req.tx.addOutput(bestChangeOutput);
            }

            TransactionOutput claimOutput = new TransactionOutput(params, req.tx, Coin.valueOf(interest), req.changeAddress);
            req.tx.addOutput(claimOutput);

            if(req.shuffleOutputs) {
                req.tx.shuffleOutputs();
            }

            if(req.signInputs) {
                wallet.signTransaction(req);
            }

            size = req.tx.unsafeBitcoinSerialize().length;
            if(size > 100000) {
                throw new Wallet.ExceededMaxTransactionSize();
            }

            req.tx.getConfidence().setSource(TransactionConfidence.Source.SELF);
            req.tx.setPurpose(Transaction.Purpose.USER_PAYMENT);
            req.tx.setExchangeRate(req.exchangeRate);
            req.tx.setMemo(req.memo);
            //req.completed = true;
        } finally {
        }

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
        sendRequest.changeAddress = wallet.getImportedKeys().get(0).toAddress(params);
        sendRequest.ensureMinRequiredFee = true;
//        sendRequest.setUseForkId(true);
        sendRequest.tx.setVersion(4);
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

    private static class BTGParams extends MainNetParams{
        BTGParams(){
            super();
            addressHeader = 0x3c;
            p2shHeader = 0x55;
            dumpedPrivateKeyHeader = 0xbc;
            acceptableAddressCodes = new int[] {addressHeader, p2shHeader};
            id = "org.bitcoingold.production";
        }
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
