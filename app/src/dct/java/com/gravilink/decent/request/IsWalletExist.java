package com.gravilink.decent.request;

import com.gravilink.decent.DecentListener;
import com.gravilink.decent.DecentWallet;
import com.gravilink.decent.DecentWalletManager;

import com.neovisionaries.ws.client.WebSocket;

import org.json.JSONObject;

import java.util.Locale;

public class IsWalletExist extends AbstractDecentRequest {
  private String name;

  public IsWalletExist(DecentWalletManager manager, DecentListener adapter, DecentWallet wallet, String name) {
    super(manager, adapter, wallet);
    this.name = name;
  }

  @Override
  boolean isDatabaseRequired() {
    return false;
  }

  @Override
  boolean isHistoryRequired() {
    return false;
  }

  @Override
  boolean isBroadcastRequired() {
    return false;
  }

  @Override
  boolean onReady(WebSocket socket, int databaseApiIndex, int historyApiIndex, int broadcastApiIndex) {
    socket.sendText(String.format(Locale.ENGLISH, "{\"id\":0, \"method\":\"call\", \"params\":[0, \"get_account_by_name\", [\"%s\"]]}", name));
    return false;
  }

  @Override
  boolean onResponse(WebSocket socket, JSONObject response, int databaseApiIndex, int historyApiIndex, int broadcastApiIndex) {
    Boolean resultIsNull = response.isNull("result");
    adapter.onWalletExistenceChecked(!resultIsNull, name);
    return true;
  }
}
