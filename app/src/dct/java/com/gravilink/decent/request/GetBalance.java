package com.gravilink.decent.request;

import com.gravilink.decent.DecentListener;
import com.gravilink.decent.DecentWallet;
import com.gravilink.decent.DecentWalletManager;
import com.neovisionaries.ws.client.WebSocket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.Locale;

public class GetBalance extends AbstractDecentRequest {

  public GetBalance(DecentWalletManager manager, DecentListener adapter, DecentWallet wallet) {
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
    socket.sendText(String.format(Locale.ENGLISH, "{\"id\":0, \"method\":\"call\", \"params\":[0, \"get_account_balances\", [\"%s\", [\"1.3.0\"]]]}", wallet.getId()));
    return false;
  }

  @Override
  boolean onResponse(WebSocket socket, JSONObject response, int databaseApiIndex, int historyApiIndex, int broadcastApiIndex) {
    try {
      JSONArray result = response.getJSONArray("result");
      BigInteger amount = new BigInteger("0");
      for (int i = 0; i < result.length(); i++) {
        if (result.getJSONObject(i).getString("asset_id").equals("1.3.0")) {
          amount = new BigInteger(result.getJSONObject(i).getString("amount"));
          break;
        }
      }

      adapter.onBalanceGot(amount);

    } catch (JSONException e) {
      onError(e);
    }
    return true;
  }
}
