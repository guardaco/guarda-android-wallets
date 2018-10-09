package com.gravilink.decent.request;

import com.gravilink.decent.DecentListener;
import com.gravilink.decent.DecentWallet;
import com.gravilink.decent.DecentWalletManager;
import com.neovisionaries.ws.client.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

public class GetTransactionFee extends AbstractDecentRequest {
  public GetTransactionFee(DecentWalletManager manager, DecentListener adapter, DecentWallet wallet) {
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
    socket.sendText("{\"id\":0,\"method\":\"call\",\"params\":[0, \"get_required_fees\",[[[0,{}]],\"1.3.0\"]]}");
    return false;
  }

  @Override
  boolean onResponse(WebSocket socket, JSONObject response, int databaseApiIndex, int historyApiIndex, int broadcastApiIndex) {
    try {
      adapter.onFeeGot(response.getJSONArray("result").getJSONObject(0).getLong("amount"));
    } catch (JSONException e) {
      onError(e);
    }
    return true;
  }
}
