package com.guarda.ethereum.request;

import android.net.Uri;
import android.util.Log;

import com.guarda.ethereum.JSONParser;
import com.guarda.ethereum.WalletCallback;
import com.guarda.ethereum.ZCashException;
import com.guarda.ethereum.ZCashTransactionDetails_taddr;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;

public class UpdateTransactionCache_taddr extends AbstractZCashRequest implements Runnable {
  Vector<ZCashTransactionDetails_taddr> cache;
  boolean rescan;
  String pubKey;
  WalletCallback<String, Void> callback;
  final static Object requestExistsMutex = new Object();
  static boolean requestExist;

  static {
    requestExist = false;
  }

  public UpdateTransactionCache_taddr(Vector<ZCashTransactionDetails_taddr> cache, boolean rescan, String pubKey, WalletCallback<String, Void> callback) {
    this.cache = cache;
    this.rescan = rescan;
    this.pubKey = pubKey;
    this.callback = callback;
  }

  public static List<ZCashTransactionDetails_taddr> getReceivedTransactions(String pubKey, int limit, int offset) throws ZCashException {
    String uri = Uri.parse(explorerAddress).buildUpon()
            .appendEncodedPath("accounts")
            .appendEncodedPath(pubKey)
            .appendEncodedPath("recv")
            .appendQueryParameter("limit", String.valueOf(limit))
            .appendQueryParameter("offset", String.valueOf(offset))
            .build().toString();

    HttpsURLConnection conn = queryExplorerForConnection(uri);
    try {
      return JSONParser.parseTxArray(conn.getInputStream());
    } catch (IOException e) {
      throw new ZCashException("Cannot parse response from explorer.", e);
    }
  }

  public static List<ZCashTransactionDetails_taddr> getSentTransactions(String pubKey, int limit, int offset) throws ZCashException {
    String uri = Uri.parse(explorerAddress).buildUpon()
            .appendEncodedPath("accounts")
            .appendEncodedPath(pubKey)
            .appendEncodedPath("sent")
            .appendQueryParameter("limit", String.valueOf(limit))
            .appendQueryParameter("offset", String.valueOf(offset))
            .build().toString();
    HttpsURLConnection conn = queryExplorerForConnection(uri);
    try {
      return JSONParser.parseTxArray(conn.getInputStream());
    } catch (IOException e) {
      throw new ZCashException("Cannot parse response from explorer.", e);
    }
  }

  @Override
  public void run() {
    synchronized (requestExistsMutex) {
      if (requestExist) {
        callback.onResponse("Another UpdateTransacionCache_taddr request exists.", null);
        return;
      } else {
        requestExist = true;
      }
    }

    Vector<ZCashTransactionDetails_taddr> transactions = cache;
    if (transactions == null) {
      synchronized (requestExistsMutex) {
        requestExist = false;
      }

      callback.onResponse("Wallet is not imported.", null);
      return;
    }


    SortedSet<ZCashTransactionDetails_taddr> uniqueTransactions = new TreeSet<>();
    long lastBlock = 0;

    if (transactions.isEmpty()) {
      rescan = true;
    } else {
      lastBlock = transactions.lastElement().blockHeight;
    }

    if (rescan) {
      synchronized (transactions) {
        transactions.clear();
      }
    }

    try {
      getAllRecv(20, 0, rescan, lastBlock, uniqueTransactions);
      getAllSent(20, 0, rescan, lastBlock, uniqueTransactions);
    } catch (ZCashException e) {
      synchronized (requestExistsMutex) {
        requestExist = false;
      }

      callback.onResponse(e.getMessage(), null);
      return;
    }

    boolean initialized = ZCashTransactionDetails_taddr.prepareAfterParsing(uniqueTransactions);
    if (initialized) {
      transactions.addAll(uniqueTransactions);
    } else {
      synchronized (requestExistsMutex) {
        requestExist = false;
      }

      return;
    }

    synchronized (requestExistsMutex) {
      requestExist = false;
    }


    callback.onResponse("ok", null);
  }


  private void getAllSent(int limit, int offset, boolean rescan, long lastBlock, SortedSet<ZCashTransactionDetails_taddr> uniqueTransactions) throws ZCashException {
    List<ZCashTransactionDetails_taddr> nextPack;
    do {
      List<ZCashTransactionDetails_taddr> uncachedTransactions = new LinkedList<>();
      nextPack = getSentTransactions(pubKey, limit, offset);
      //Log.i("UPDATE CACHE:", String.format("Downloaded %d transactions", nextPack.size()));
      if (!rescan) {
        boolean cachedAll = true;
        for (ZCashTransactionDetails_taddr details : nextPack) {
          boolean cached = details.blockHeight <= lastBlock;
          if (!cached) {
            uncachedTransactions.add(details);
          }

          cachedAll &= details.blockHeight <= lastBlock;
        }

        if (cachedAll) {
          break;
        }
      } else {
        uncachedTransactions = nextPack;
      }

      uniqueTransactions.addAll(uncachedTransactions);
      Log.i("UPDATE CACHE:", String.format("Downloaded %d transactions", uniqueTransactions.size()));
      offset += limit;
    } while (nextPack.size() == limit);
  }

  private void getAllRecv(int limit, int offset, boolean rescan, long lastBlock, SortedSet<ZCashTransactionDetails_taddr> uniqueTransactions) throws ZCashException {
    List<ZCashTransactionDetails_taddr> nextPack;
    do {
      List<ZCashTransactionDetails_taddr> uncachedTransactions = new LinkedList<>();
      nextPack = getReceivedTransactions(pubKey, limit, offset);
      //Log.i("UPDATE CACHE:", String.format("Downloaded %d transactions", nextPack.size()));
      if (!rescan) {
        boolean cachedAll = true;
        for (ZCashTransactionDetails_taddr details : nextPack) {
          boolean cached = details.blockHeight <= lastBlock;
          if (!cached) {
            uncachedTransactions.add(details);
          }

          cachedAll &= details.blockHeight <= lastBlock;
        }

        if (cachedAll) {
          break;
        }
      } else {
        uncachedTransactions = nextPack;
      }

      uniqueTransactions.addAll(uncachedTransactions);
      Log.i("UPDATE CACHE:", String.format("Downloaded %d transactions", uniqueTransactions.size()));
      offset += limit;
    } while (nextPack.size() == limit);
  }

}
