package com.guarda.ethereum.rest;


import android.util.Log;

import retrofit2.Call;

public class RequestorBtc {

    public static void getBalanceAndTx(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createBlockChainInfoApi().getBalance(address);
        Log.d("Request", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getBalanceAndTxBtg(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createBtgApi().getBalance(address);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getBalanceAndTxDgb(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createDgbApiNew().getBalance(address);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getBalanceAndTxBch(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createBchApi().getBalance(address);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getBalanceAndTxSbtc(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createSbtcApi().getBalance(address);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getBalanceAndTxLtc(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createLtcApi().getBalance(address);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getBalanceBtgNew(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createBtgApiNew().getBalance(address);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getBalanceDgbNew(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createDgbApiNew().getBalance(address);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getBalanceBchNew(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createBchApi().getBalance(address);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getBalanceQtumNew(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createQtumApi().getBalance(address);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getBalanceKmdNew(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createKmdApi().getBalance(address);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void SendRawTxQtum(String txHex, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createQtumApi().sendRawTx(txHex);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getBalanceSbtcNew(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createSbtcApiNew().getBalance(address);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getBalanceLtcNew(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createLtcApiNew().getBalance(address);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getBalanceZecNew(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createZecApiNew().getBalance(address);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getTransactionsBtgNew(String address, ApiMethods.RequestListener listener){
        Call call = ApiMethods.createBtgApiNew().getTransactions(address);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getTransactionsDgbNew(String address, ApiMethods.RequestListener listener){
        Call call = ApiMethods.createDgbApiNew().getTransactions(address);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getTransactionsBchNew(String address, ApiMethods.RequestListener listener){
        Call call = ApiMethods.createBchApi().getTransactions(address);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getTransactionsQtumNew(String address, ApiMethods.RequestListener listener){
        Call call = ApiMethods.createQtumApi().getTransactions(address);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getTransactionsKmdNew(String address, int from, int to, ApiMethods.RequestListener listener){
        Call call = ApiMethods.createKmdApi().getTransactions(address, from, to);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getTokenBalanceQtumNew(String token, String address, ApiMethods.RequestListener listener){
        Call call = ApiMethods.createQtumApi().getTokenBalance(token, address);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getTokenTransactionsQtumNew(String token, String address, ApiMethods.RequestListener listener){
        Call call = ApiMethods.createQtumApi().getTokenTransactions(token, address);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getTransactionsSbtcNew(String address, ApiMethods.RequestListener listener){
        Call call = ApiMethods.createSbtcApiNew().getTransactions(address);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getTransactionsLtcNew(String address, ApiMethods.RequestListener listener){
        Call call = ApiMethods.createLtcApiNew().getTransactions(address);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getTransactionsZecNew(String address, ApiMethods.RequestListener listener){
        Call call = ApiMethods.createZecApiNew().getTransactions(address);
        Log.d("svcom", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getUTXOList(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createBlockChainInfoApi().getUTXOByAddress(address);
        Log.d("Request", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getUTXOListBtg(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createBtgApi().getUTXOByAddress(address);
        Log.d("Request", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getUTXOListBch(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createBchApi().getUTXOByAddress(address);
        Log.d("Request", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getUTXOListQtum(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createQtumApi().getUTXOByAddress(address);
        Log.d("Request", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getUTXOListBtgNew(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createBtgApiNew().getUTXOByAddress(address);
        Log.d("Request", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getUTXOListDgbNew(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createDgbApiNew().getUTXOByAddress(address);
        Log.d("Request", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void broadcastRawTxDgbNew(String hexTx, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createDgbApiNew().broadcastRawTx(hexTx);
        Log.d("Request", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getUTXOListKmdNew(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createKmdApi().getUTXOByAddress(address);
        Log.d("Request", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getUTXOListSbtcNew(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createSbtcApiNew().getUTXOByAddress(address);
        Log.d("Request", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getUTXOListLtcNew(String address, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createLtcApiNew().getUTXOByAddress(address);
        Log.d("Request", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getCurrentBlockHeight(String timeMillis, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createBlockChainInfoApi().getCurrentBlockHeight(timeMillis);
        Log.d("Request", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

    public static void getCurrentBlockHeightBch(String timeMillis, ApiMethods.RequestListener listener) {
        Call call = ApiMethods.createBchApi().getCurrentBlockHeight();
        Log.d("Request", "path " + call.request());
        ApiMethods.makeRequest(call, listener);
    }

}
