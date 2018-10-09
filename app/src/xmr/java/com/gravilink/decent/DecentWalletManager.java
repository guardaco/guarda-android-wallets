package com.gravilink.decent;

import com.gravilink.decent.request.AbstractDecentRequest;
import com.gravilink.decent.request.CreateTransaction;
import com.gravilink.decent.request.DecentRequest;
import com.gravilink.decent.request.GetBalance;
import com.gravilink.decent.request.GetTransactionFee;
import com.gravilink.decent.request.GetTransactions;
import com.gravilink.decent.request.GetWalletInfo;
import com.gravilink.decent.request.PushTransaction;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DecentWalletManager {
  private String endpoint;
  private WebSocketFactory wsFactory;

  private DecentListener adapter;
  private DecentWallet wallet;

  private final Lock lock = new ReentrantLock();
  private boolean hasActiveRequest = false;

  public DecentWalletManager(String endpoint, DecentListener adapter) {
    this.endpoint = endpoint;
    this.adapter = adapter;
    this.wsFactory = new WebSocketFactory();
    this.wallet = new DecentWallet();
    if (wallet.isInitialized()) {
      adapter.onWalletRestored();
    } else {
      adapter.onImportRequired();
    }
  }

  public void importWallet(String name, String privateKey) {
    try {
      wallet.setPrivateKey(privateKey);
    } catch (Exception e) {
      adapter.onError(e);
      return;
    }

    wallet.setName(name);
    createRequest(new GetWalletInfo(this, adapter, wallet));
  }

  public String getAddress() {
    return wallet.getPublicKey();
  }

  public String getKeys() {
    return wallet.getId();
  }

  public void getBalance() {
    createRequest(new GetBalance(this, adapter, wallet));
  }

  public void getTransactionFee() {
    createRequest(new GetTransactionFee(this, adapter, wallet));
  }

  public void getTransactions(int limit, String startId) {
    new DecentRequest(wsFactory, endpoint, new GetTransactions(this, adapter, wallet, limit, startId));
  }

  public void getTransactions(int limit) {
    getTransactions(limit, null);
  }

  public void createTransaction(DecentAccount to, long amount, long fee) {
    new DecentRequest(wsFactory, endpoint, new CreateTransaction(this, adapter, wallet, to, amount, fee));
  }

  public void createTransactionFromName(String to, long amount, long fee) {
    DecentAccount toAccount = new DecentAccount();
    toAccount.setName(to);
    createTransaction(toAccount, amount, fee);
  }

  public void createTransactionFromPublicKey(String to, long amount, long fee) {
    DecentAccount account = new DecentAccount();
    account.setPublicKey(to);
    createTransaction(account, amount, fee);
  }

  public void pushTransaction(DecentTransaction decentTransaction) {
    new DecentRequest(wsFactory, endpoint, new PushTransaction(this, adapter, decentTransaction, wallet));
  }

  public boolean isWalletRestored() {
    return wallet.isInitialized();
  }

  public void clearWallet() {
    wallet.clear();
  }

  public void finishRequest() {
    lock.lock();
    hasActiveRequest = false;
    lock.unlock();
  }

  private void createRequest(AbstractDecentRequest request) {
    lock.lock();
    if (!hasActiveRequest) {
      hasActiveRequest = true;
      new DecentRequest(wsFactory, endpoint, request);
    } else {
      adapter.onError(new IOException("Previous request is not finished."));
    }
    lock.unlock();
  }

}
