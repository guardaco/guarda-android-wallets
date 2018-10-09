package com.gravilink.decent.request;

import com.gravilink.decent.DecentListener;
import com.gravilink.decent.DecentWallet;
import com.gravilink.decent.DecentWalletManager;
import com.neovisionaries.ws.client.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class GetWalletInfo extends AbstractDecentRequest {
  public GetWalletInfo(DecentWalletManager manager, DecentListener adapter, DecentWallet wallet) {
    super(manager, adapter, wallet);
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
    socket.sendText(String.format(Locale.ENGLISH, "{\"id\":0, \"method\":\"call\", \"params\":[0, \"get_account_by_name\", [\"%s\"]]}",
            super.wallet.getName()));
    return false;
  }

  @Override
  boolean onResponse(WebSocket socket, JSONObject response, int databaseApiIndex, int historyApiIndex, int broadcastApiIndex) {
    try {
      JSONObject result = response.getJSONObject("result");
      String id = result.getString("id");
      String publicKey = result.getJSONObject("active").getJSONArray("key_auths").getJSONArray(0).getString(0);
      wallet.setId(id);
      wallet.setPublicKey(publicKey);
      adapter.onWalletImported();
    } catch (JSONException e) {
      onError(e);
    }
    return true;
  }
}
