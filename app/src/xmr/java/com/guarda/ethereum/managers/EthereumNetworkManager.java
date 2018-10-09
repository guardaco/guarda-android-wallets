package com.guarda.ethereum.managers;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.NetworkOnMainThreadException;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.crash.FirebaseCrash;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.models.items.RawTransactionResponse;

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
    //private Web3j web3jConnection;
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
//            HttpService service = new HttpService(NODE_ADDRESS);
//            web3jConnection = Web3jFactory.build(service);
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

    public void getBalance(final String address, final Callback<BigDecimal> callback) {
    }

    public void getBalance(final String address, final String blockNumber, final Callback<BigDecimal> callback) {
    }

    public void getGasPrice(Callback<BigInteger> callback){
    }


    public void sendTransaction(String toAddress, BigInteger value, BigInteger gasPrice, BigInteger gasLimit, String contractData, Callback<RawTransactionResponse> callback){
    }

    private interface ConnectionCallback {
        void onFinish();
    }


}
