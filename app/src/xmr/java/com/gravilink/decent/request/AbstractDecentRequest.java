package com.gravilink.decent.request;

import com.gravilink.decent.DecentListener;
import com.gravilink.decent.DecentWallet;
import com.gravilink.decent.DecentWalletManager;
import com.neovisionaries.ws.client.WebSocket;

import org.json.JSONObject;

public abstract class AbstractDecentRequest {
  DecentWalletManager manager;
  DecentListener adapter;
  DecentWallet wallet;

  abstract boolean isDatabaseRequired();

  abstract boolean isHistoryRequired();

  abstract boolean isBroadcastRequired();

  abstract boolean onReady(WebSocket socket, int databaseApiIndex, int historyApiIndex, int broadcastApiIndex);

  abstract boolean onResponse(WebSocket socket, JSONObject response, int databaseApiIndex, int historyApiIndex, int broadcastApiIndex);

  AbstractDecentRequest(DecentWalletManager manager, DecentListener adapter, DecentWallet wallet) {
    this.manager = manager;
    this.adapter = adapter;
    this.wallet = wallet;
  }

  void onError(Exception e) {
    adapter.onError(e);
  }

}
