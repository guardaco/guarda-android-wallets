package com.gravilink.decent;

import android.util.Log;

import com.google.common.primitives.Bytes;
import com.gravilink.decent.crypto.Base58;
import com.gravilink.decent.crypto.BrainKeyDict;
import com.gravilink.decent.crypto.DumpedPrivateKey;
import com.gravilink.decent.crypto.ECKey;
import com.gravilink.decent.crypto.SecureRandomGenerator;
import com.gravilink.decent.request.AbstractDecentRequest;
import com.gravilink.decent.request.CreateTransaction;
import com.gravilink.decent.request.DecentRequest;
import com.gravilink.decent.request.GetBalance;
import com.gravilink.decent.request.GetTransactionFee;
import com.gravilink.decent.request.GetTransactions;
import com.gravilink.decent.request.GetWalletInfo;
import com.gravilink.decent.request.IsWalletExist;
import com.gravilink.decent.request.PushTransaction;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.spongycastle.crypto.digests.RIPEMD160Digest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.gravilink.decent.crypto.BrainKeyDict.BRAINKEY_WORD_COUNT;
import static com.gravilink.decent.crypto.BrainKeyDict.DICT_WORD_COUNT;

public class DecentWalletManager {
  private String endpoint;
  private WebSocketFactory wsFactory;

  private DecentListener adapter;
  private DecentWallet wallet;

  private final Lock lock = new ReentrantLock();
  private boolean hasActiveRequest = false;

  public DecentWalletManager(String endpoint, DecentListener adapter, String words) {
    this.endpoint = endpoint;
    this.adapter = adapter;
    BrainKeyDict.words = words;
    this.wsFactory = new WebSocketFactory();
    this.wallet = new DecentWallet();
    if (wallet.isInitialized()) {
      adapter.onWalletRestored();
    } else {
      adapter.onImportRequired();
    }
  }

  public static String suggestBrainKey() {
    String[] wordArray = BrainKeyDict.words.split(",");
    ArrayList<String> suggestedBrainKey = new ArrayList<String>();
    assert (wordArray.length == DICT_WORD_COUNT);
    SecureRandom secureRandom = SecureRandomGenerator.getSecureRandom();
    int index;
    for (int i = 0; i < BRAINKEY_WORD_COUNT; i++) {
      index = secureRandom.nextInt(DICT_WORD_COUNT - 1);
      suggestedBrainKey.add(wordArray[index].toUpperCase());
    }
    StringBuilder stringBuilder = new StringBuilder();
    for(String word : suggestedBrainKey){
      stringBuilder.append(word);
      stringBuilder.append(" ");
    }

    return stringBuilder.toString().trim();
  }

  public static String privateKeyFromBrainKey(String brainKey) {
    String fullkey = brainKey + " 0"; //Append default sequence number
    String privateKey = null;
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-512");
      byte[] bytes = md.digest(fullkey.getBytes("UTF-8"));
      MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
      byte[] result = sha256.digest(bytes);
      privateKey = ECKey.fromPrivate(result).decompress().getPrivateKeyEncoded().toBase58();
    } catch (NoSuchAlgorithmException e) {
      System.out.println("NoSuchAlgotithmException. Msg: " + e.getMessage());
    } catch (UnsupportedEncodingException e) {
      System.out.println("UnsupportedEncodingException. Msg: " + e.getMessage());
    }
    return privateKey;
  }

  public static String publicKeyFromPrivateKey(String privateKey) {
    byte[] pubKey = null;
    try {
      pubKey = DumpedPrivateKey.fromBase58(privateKey).getPubKeyPoint().getEncoded(true);
    } catch (Exception e) {
      return null;
    }
    byte[] checksum = new byte[160 / 8];
    RIPEMD160Digest ripemd160Digest = new RIPEMD160Digest();
    ripemd160Digest.update(pubKey, 0, pubKey.length);
    ripemd160Digest.doFinal(checksum, 0);
    checksum =  Arrays.copyOfRange(checksum, 0, 4);
    byte[] pubKeyChecksummed = Bytes.concat(pubKey, checksum);
    return "DCT" + Base58.encode(pubKeyChecksummed);
  }

  public void isWalletExist(String name) {
    createRequest(new IsWalletExist(this, adapter, wallet, name));
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
    return wallet.getName();
  }

  public String getKeys() {
    return wallet.getId();
  }

  public String getPublicKey() {
    return wallet.getPublicKey();
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
