package com.guarda.ethereum.managers;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.NetworkOnMainThreadException;
import android.text.TextUtils;
import android.util.Log;

import com.google.common.io.BaseEncoding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.crash.FirebaseCrash;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.models.items.RawTransactionResponse;

import org.spongycastle.util.encoders.Hex;
import org.spongycastle.util.encoders.HexEncoder;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.RawTransaction;
import org.web3j.protocol.core.methods.request.ShhPost;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.exceptions.MessageDecodingException;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.infura.InfuraHttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import autodagger.AutoInjector;

import static com.guarda.ethereum.models.constants.Common.NODE_ADDRESS;


/**
 * All actions with Ethereum network:
 * - connection to node
 * - balance request
 * - transactions list request
 * - provide transaction request
 * Created by SV on 13.08.2017.
 */

@AutoInjector(GuardaApp.class)
public class EthereumNetworkManager {

    private static final int CONNECTION_RETRY_PAUSE_MILLISECONDS = 1000;
    private WalletManager walletManager;
    private Web3j web3jConnection;
    private boolean connectionAvailable = false;

    public EthereumNetworkManager(WalletManager walletManager) {
        GuardaApp.getAppComponent().inject(this);
        this.walletManager = walletManager;
        provideConnection(null);
    }

    private void provideConnection(ConnectionCallback callback) {
        provideNativeNodeConnection(callback);
//        provideInfuraNodeConnection(callback);
    }

    private void provideNativeNodeConnection(final ConnectionCallback callback) {
        try {
            HttpService service = new HttpService(NODE_ADDRESS);
            web3jConnection = Web3jFactory.build(service);
            connectionAvailable = true;
        } catch (NetworkOnMainThreadException e) {
            e.printStackTrace();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onFinish();
                }
            }
        }, CONNECTION_RETRY_PAUSE_MILLISECONDS);
    }

    private void provideInfuraNodeConnection(final ConnectionCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InfuraHttpService infuraHttpService = new InfuraHttpService("https://mainnet.infura.io/cTd3cl3I5sP56Sic315h");
                    web3jConnection = Web3jFactory.build(infuraHttpService);
                    connectionAvailable = true;
                } catch (NetworkOnMainThreadException e) {
                    e.printStackTrace();
                }
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onFinish();
                        }
                    }
                }, CONNECTION_RETRY_PAUSE_MILLISECONDS);
            }
        }).start();
    }

    public void getBalance(final String address, final Callback<BigDecimal> callback) {
        if (web3jConnection != null && connectionAvailable) {
            BalanceRequestTask task = new BalanceRequestTask(address, DefaultBlockParameterName.LATEST, callback);
            task.execute();
        } else {
            provideConnection(new ConnectionCallback() {
                @Override
                public void onFinish() {
                    getBalance(address, callback);
                }
            });
        }
    }

    public void getBalance(final String address, final String blockNumber, final Callback<BigDecimal> callback) {
        if (web3jConnection != null && connectionAvailable) {
            BalanceRequestTask task = new BalanceRequestTask(address,
                    new DefaultBlockParameterNumber(new BigInteger(blockNumber)),
                    callback);
            task.execute();
        } else {
            provideConnection(new ConnectionCallback() {
                @Override
                public void onFinish() {
                    getBalance(address, blockNumber, callback);
                }
            });
        }

    }

    public void getGasPrice(Callback<BigInteger> callback){
        if (web3jConnection != null) {
            GasPriceTask task = new GasPriceTask(callback);
            task.execute();
        }
    }

    public void getTxReceipt(final String txHash, final Callback<TransactionReceipt> callback) {
        if (web3jConnection != null && connectionAvailable) {
            TxReceiptRequestTask task = new TxReceiptRequestTask(txHash, callback);
            task.execute();
        } else {
            provideConnection(new ConnectionCallback() {
                @Override
                public void onFinish() {
                    getTxReceipt(txHash, callback);
                }
            });
        }
    }


    private class BalanceRequestTask extends AsyncTask<Void, Void, BigInteger> {
        private String mAddress;
        private Callback<BigDecimal> mCallback;
        private DefaultBlockParameter mBlockParameter;

        BalanceRequestTask(String mAddress, DefaultBlockParameter blockParameter,
                           Callback<BigDecimal> mCallback) {
            this.mAddress = mAddress;
            this.mCallback = mCallback;
            this.mBlockParameter = blockParameter;
        }

        @Override
        protected BigInteger doInBackground(Void... params) {
            try {
                EthGetBalance getBalance = null;
                try {
                    getBalance = web3jConnection.ethGetBalance(mAddress, mBlockParameter).send();
                } catch (IOException e) {
//                    FirebaseCrash.logcat(Log.ERROR, "getBalance", e.toString());
//                    FirebaseCrash.report(e);
                    Log.e("EthereumNetworkManager", "doInBackground()... exception1: " + e.toString());
                }

                if (getBalance != null) {
                    try {
                        return getBalance.getBalance();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (MessageDecodingException mde) {
                        mde.printStackTrace();
                    }
                }
            } catch (Exception e) {
                Log.e("EthereumNetworkManager", "doInBackground()... exception2: " + e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(BigInteger balance) {
            super.onPostExecute(balance);
            if (mCallback != null) {
                if (balance != null) {
                    BigDecimal ethBalance = new BigDecimal(balance);
                    mCallback.onResponse(Convert.fromWei(ethBalance, Convert.Unit.ETHER));
                } else {
                    mCallback.onResponse(null);
                }
            }
        }
    }

    private class NodeVersionTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            Web3ClientVersion web3ClientVersion = null;
            try {
                web3ClientVersion = web3jConnection.web3ClientVersion().send();
                String clientVersion = web3ClientVersion.getWeb3ClientVersion();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }


    public void sendTransaction(String toAddress, BigInteger value, BigInteger gasPrice,
                                BigInteger gasLimit, String contractData, Callback<RawTransactionResponse> callback){
        if (web3jConnection != null) {
            SendTransactionTask task = new SendTransactionTask(toAddress, value, gasPrice, gasLimit,
                    contractData, callback);
            task.execute();
        }
    }


    private class SendTransactionTask extends AsyncTask<Void, Void, RawTransactionResponse> {
        private String toAddress;
        private BigInteger value;
        private BigInteger gasPrice;
        private BigInteger gasLimit;
        private String contractData;
        private Callback<RawTransactionResponse> callback;

        SendTransactionTask(String toAddress, BigInteger value, BigInteger gasPrice,
                            BigInteger gasLimit, String contractData,
                            Callback<RawTransactionResponse> callback) {
            this.toAddress = toAddress;
            this.value = value; //in wei
            this.gasPrice = gasPrice;
            this.gasLimit = gasLimit;
            this.contractData = contractData;
            this.callback = callback;
        }

        @Override
        protected RawTransactionResponse doInBackground(Void... params) {
            BigInteger nonce = getNonce();
            if (nonce == null) return null;
            RawTransaction rawTransaction;
            String blockNumber = getBlocNumber();
            if (TextUtils.isEmpty(contractData)) {
                rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, toAddress, value);
            } else {
                rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, toAddress, new String(Hex.encode(contractData.getBytes())));
            }

            byte[] signedMessage;
            if (walletManager.getCredentials() != null && rawTransaction != null) {
                signedMessage = TransactionEncoder.signMessage(rawTransaction, walletManager.getCredentials());
            } else {
                return null;
            }

            String hexValue = Numeric.toHexString(signedMessage);
            EthSendTransaction ethSendTransaction = null;


                try {
                    ethSendTransaction = web3jConnection.ethSendRawTransaction(hexValue).send();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            if (ethSendTransaction != null && ethSendTransaction.getTransactionHash() != null) {
                return new RawTransactionResponse(ethSendTransaction.getTransactionHash(),
                        hexValue, blockNumber);
            } else {
                return null;
            }
        }

        private String getBlocNumber() {
            if (web3jConnection != null) {
                try {
                    EthBlockNumber blockNumber = web3jConnection.ethBlockNumber().send();
                    return blockNumber.getBlockNumber().toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return null;
        }


        private BigInteger getNonce() {
            BigInteger nonce = null;
            try {
                EthGetTransactionCount txCount = web3jConnection.ethGetTransactionCount(walletManager.getWalletFriendlyAddress(),
                        DefaultBlockParameterName.LATEST).send();
                nonce = txCount.getTransactionCount();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return nonce;
        }

        @Override
        protected void onPostExecute(RawTransactionResponse response) {
            super.onPostExecute(response);
            callback.onResponse(response);
        }
   }

   private class GasPriceTask extends AsyncTask<Void, Void, BigInteger>{

        Callback<BigInteger> callback;

       GasPriceTask(Callback<BigInteger> callback) {
           this.callback = callback;
       }

       @Override
       protected BigInteger doInBackground(Void... params) {
           BigInteger gasPrice = null;
           try {
               EthGasPrice price = web3jConnection.ethGasPrice().send();
               gasPrice = price.getGasPrice();
           } catch (IOException e) {
               e.printStackTrace();
           }
           return gasPrice;
       }

       @Override
       protected void onPostExecute(BigInteger gasPrice) {
           super.onPostExecute(gasPrice);
           callback.onResponse(gasPrice);
       }
   }

    private class TxReceiptRequestTask extends AsyncTask<Void, Void, TransactionReceipt> {
        private String txHash;
        private Callback<TransactionReceipt> callback;

        TxReceiptRequestTask(String txHash,
                           Callback<TransactionReceipt> callback) {
            this.txHash = txHash;
            this.callback = callback;
        }

        @Override
        protected TransactionReceipt doInBackground(Void... params) {
            try {
                EthGetTransactionReceipt getBalance = null;
                try {
                    getBalance = web3jConnection.ethGetTransactionReceipt(txHash).send();
                } catch (IOException e) {
                    Log.e("TxReceiptRequestTask", "doInBackground()... exception1: " + e.toString());
                }

                if (getBalance != null) {
                    try {
                        return getBalance.getTransactionReceipt();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (MessageDecodingException mde) {
                        mde.printStackTrace();
                    }
                }
            } catch (Exception e) {
                Log.e("TxReceiptRequestTask", "doInBackground()... exception2: " + e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(TransactionReceipt txReceipt) {
            super.onPostExecute(txReceipt);
            if (txReceipt != null) {
                if (callback != null) {
                    callback.onResponse(txReceipt);
                }
            }
        }
    }

    private interface ConnectionCallback {
        void onFinish();
    }

    private class BlockRequestTask extends AsyncTask<Void, Void, BigInteger> {
        private Callback<BigInteger> mCallback;

        BlockRequestTask(Callback<BigInteger> mCallback) {
            this.mCallback = mCallback;
        }

        @Override
        protected BigInteger doInBackground(Void... params) {
            if (web3jConnection != null) {
                try {
                    EthBlockNumber blockNumber = web3jConnection.ethBlockNumber().send();
                    return blockNumber.getBlockNumber();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(BigInteger block) {
            super.onPostExecute(block);
            if (mCallback != null) {
                if (block != null) {
                    mCallback.onResponse(block);
                } else {
                    mCallback.onResponse(null);
                }
            }
        }
    }

    public void getBlocNumber(Callback<BigInteger> callback) {
        if (web3jConnection != null) {
            BlockRequestTask task = new BlockRequestTask(callback);
            task.execute();
        }
    }

}
