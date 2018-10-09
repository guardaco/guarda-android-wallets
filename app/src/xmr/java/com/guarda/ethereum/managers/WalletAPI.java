package com.guarda.ethereum.managers;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.gravilink.decent.DecentAdapter;
import com.gravilink.decent.DecentTransaction;
import com.gravilink.decent.DecentWalletManager;
import com.guarda.ethereum.models.constants.Common;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class WalletAPI {


    public static void restoreWallet(final String name, final String key, final Callback2<String, String> onComplete) {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            private DecentWalletManager decent_ = null;
            private Boolean status_ = false;
            @Override
            protected Void doInBackground(Void... voids) {
                final Semaphore waiter = new Semaphore(1);
                waiter.acquireUninterruptibly();
                decent_ = new DecentWalletManager(CONNECTION_ADDRESS, new DecentAdapter() {
                    @Override public void onWalletImported() {status_ = true;decent_.finishRequest();waiter.release();Log.d("flint", "restoreWallet.onWalletImported");}
                    @Override public void onError(Exception e) {waiter.release();Log.d("flint", "restoreWallet.onError: " + e.toString());}
                });
                Log.d("flint", "restoreWallet.importWallet...");
                decent_.importWallet(name, key);
                try {waiter.tryAcquire(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);} catch (InterruptedException e) {}
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                if (onComplete != null)
                    onComplete.onResponse(status_?decent_.getAddress():null, decent_.getKeys());
            }
        };

        task.execute();
    }



    public static void getBalance(final String name, final String key, final Callback<Long> onComplete) {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            private DecentWalletManager decent_ = null;
            private BigInteger res_ = BigInteger.valueOf(0);
            @Override
            protected Void doInBackground(Void... voids) {
                final Semaphore waiter = new Semaphore(1);
                waiter.acquireUninterruptibly();
                res_ = BigInteger.valueOf(0);
                decent_ = new DecentWalletManager(CONNECTION_ADDRESS, new DecentAdapter() {
                    @Override public void onWalletImported() {decent_.finishRequest();decent_.getBalance();Log.d("flint", "getBalance.onWalletImported");}
                    @Override public void onBalanceGot(BigInteger balance) {res_ = balance;decent_.finishRequest();waiter.release();Log.d("flint", "getBalance.onBalanceGot " + res_);}
                    @Override public void onError(Exception e) {waiter.release();Log.d("flint", "getBalance.onError: " + e.toString());}
                });
                Log.d("flint", "getBalance.importWallet... ");
                decent_.importWallet(name, key);
                try {waiter.tryAcquire(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);} catch (InterruptedException e) {}
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                if (onComplete != null)
                    onComplete.onResponse(res_.longValue());
            }
        };

        task.execute();
    }



    public static void getTransactionList(final String name, final String key, final Callback<Vector<DecentTransaction>> onComplete) {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            private DecentWalletManager decent_ = null;
            private Vector<DecentTransaction> transactionsRes_ = null;
            @Override
            protected Void doInBackground(Void... voids) {
                final Semaphore waiter = new Semaphore(1);
                waiter.acquireUninterruptibly();
                transactionsRes_ = new Vector<>();
                decent_ = new DecentWalletManager(CONNECTION_ADDRESS, new DecentAdapter() {
                    @Override public void onWalletImported() {decent_.finishRequest();decent_.getTransactions(200);Log.d("flint", "getTransactionList.onWalletImported");}
                    @Override public void onTransactionsGot(Vector<DecentTransaction> transactions) {transactionsRes_ = transactions;decent_.finishRequest();waiter.release();Log.d("flint", "getTransactionList.onTransactionsGot");}
                    @Override public void onError(Exception e) {waiter.release();Log.d("flint", "getTransactionList.onError: " + e.toString());}
                });
                Log.d("flint", "getTransactionList.importWallet... ");
                decent_.importWallet(name, key);
                try {waiter.tryAcquire(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);} catch (InterruptedException e) {}
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                Log.d("flint", "getTransactionList done, transactions count: " + transactionsRes_.size());
                if (onComplete != null)
                    onComplete.onResponse(transactionsRes_);
            }
        };

        task.execute();
    }



    public static void sendTransactionFromName(final String name, final String key, final String to, final long amount, final long fee, final Callback<Boolean> onComplete) {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            private DecentWalletManager decent_ = null;
            private Boolean status_ = false;
            @Override
            protected Void doInBackground(Void... voids) {
                Log.d("flint", "createTransactionFromName... to="+to + ", amount="+amount + ", fee="+fee);
                final Semaphore waiter = new Semaphore(1);
                waiter.acquireUninterruptibly();
                decent_ = new DecentWalletManager(CONNECTION_ADDRESS, new DecentAdapter() {
                    @Override public void onWalletImported() {decent_.finishRequest();decent_.createTransactionFromName(to, amount, fee);Log.d("flint", "sendTransactionFromName.onWalletImported");}
                    @Override public void onTransactionCreated(DecentTransaction transaction) {decent_.finishRequest();decent_.pushTransaction(transaction);Log.d("flint", "sendTransactionFromName.onTransactionCreated");}
                    @Override public void onTransactionPushed(DecentTransaction transaction) {status_=true;decent_.finishRequest();waiter.release();Log.d("flint", "sendTransactionFromNamesendTransactionFromName.onTransactionPushed");}
                    @Override public void onError(Exception e) {waiter.release();Log.d("flint", "createTransactionFromName.onError: " + e.toString());}
                });
                Log.d("flint", "sendTransactionFromName.importWallet... ");
                decent_.importWallet(name, key);
                try {waiter.tryAcquire(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);} catch (InterruptedException e) {}
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                Log.d("flint", "createTransactionFromName done");
                if (onComplete != null)
                    onComplete.onResponse(status_);
            }
        };

        task.execute();
    }



    public static void requestMarketPrice(final String target, final Callback<Double> callback) {
        final Semaphore waiter = new Semaphore(1);
        waiter.acquireUninterruptibly();

        class MyAsyncTask implements Runnable {
            public Double result = null;
            @Override
            public void run() {
                try {
                    OkHttpClient httpClient = new OkHttpClient();
                    String reqUrl = "https://api.coinmarketcap.com/v1/ticker/decent/?convert="+target;
                    Request req = new Request.Builder().url(reqUrl).build();
                    Response resp = httpClient.newCall(req).execute();
                    JSONArray jsonArr = new JSONArray(resp.body().string());
                    JSONObject obj = jsonArr.getJSONObject(0);
                    String resultText = obj.getString("price_"+target.toLowerCase());
                    result = Double.valueOf(resultText);
                } catch (Exception e) {
                    Log.d("flint", "WalletAPI.requestMarketPrice... exeption: " + e.toString());
                }
                waiter.release();
            }
        };
        final MyAsyncTask task = new MyAsyncTask();
        final Thread taskThread = new Thread(task);

        Runnable taskWaiter = new Runnable() {
            @Override
            public void run() {
                try {waiter.tryAcquire(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);} catch (InterruptedException e) {}
                taskThread.interrupt();
                callback.onResponse(task.result);
            }
        };
        Thread taskWaiterThread = new Thread(taskWaiter);
        taskWaiterThread.start();
        taskThread.start();
    }



    public static void requestActualHeight(final Callback<Long> callback) {
        final Semaphore waiter = new Semaphore(1);
        waiter.acquireUninterruptibly();

        class MyThread implements Runnable {
            public Long result = null;
            @Override
            public void run() {
                try {
                    OkHttpClient httpClient = new OkHttpClient();
                    String reqUrl = "https://decent-db.com/";
                    Request req = new Request.Builder().url(reqUrl).build();
                    Response resp = httpClient.newCall(req).execute();
                    String strResp = resp.body().string();
                    String pattern = "<span class=\"ui horizontal blue basic label\" data-props=\"head_block_number\">";
                    int indx1 = strResp.indexOf(pattern);
                    if (indx1 >= 0) {
                        int indx2 = strResp.indexOf("<", indx1+pattern.length());
                        if (indx2 >= 0) {
                            String strHeight = strResp.substring(indx1+pattern.length(),indx2);
                            strHeight = strHeight.replaceAll(" ", "").replaceAll("\n", "").replaceAll("\r", "");
                            result = Long.valueOf(strHeight);
                        }
                    }
                } catch (Exception e) {
                    Log.d("flint", "WalletAPI.requestActualHeight... exeption: " + e.toString());
                }
                waiter.release();
            }
        };
        final MyThread task = new MyThread();
        final Thread taskThread = new Thread(task);

        Runnable taskWaiter = new Runnable() {
            @Override
            public void run() {
                try {waiter.tryAcquire(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);} catch (InterruptedException e) {}
                taskThread.interrupt();
                callback.onResponse(task.result);
            }
        };
        Thread taskWaiterThread = new Thread(taskWaiter);
        taskWaiterThread.start();
        taskThread.start();
    }




    public static double satoshiToCoinsDouble(long amount) {
        return (double)amount/100000000.0;
    }

    public static String satoshiToCoinsString(long amount) {
        return String.format("%.8f", satoshiToCoinsDouble(amount)).replaceAll(",", ".");
    }

    public static String satoshiToCoinsString(BigDecimal amount) {
        return satoshiToCoinsString(amount.toBigInteger().longValue());
    }

    public static long coinsToSatoshiLong(double amount) {
        return (long)(amount * 100000000.0);
    }

    public static long coinsToSatoshiLong(String amount) {
        return coinsToSatoshiLong(Double.valueOf(amount).doubleValue());
    }



    private static final String CONNECTION_ADDRESS = "ws://dct.guarda.co:8091";
    private static final int TIMEOUT_MILLIS = 5000;

}
