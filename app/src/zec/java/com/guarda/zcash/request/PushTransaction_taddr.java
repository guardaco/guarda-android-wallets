package com.guarda.zcash.request;

import com.guarda.zcash.WalletCallback;
import com.guarda.zcash.ZCashException;
import com.guarda.zcash.ZCashTransaction_taddr;
import com.guarda.zcash.crypto.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PushTransaction_taddr extends AbstractZCashRequest implements Runnable {
  private WalletCallback<String, Void> callback;
  private ZCashTransaction_taddr transaction;

  public PushTransaction_taddr(ZCashTransaction_taddr transaction, WalletCallback<String, Void> callback) {
    this.callback = callback;
    this.transaction = transaction;
  }

  public void pushTransaction() throws ZCashException {
    JSONObject query = new JSONObject();
    JSONArray params = new JSONArray();
    try {
      params.put(Utils.bytesToHex(transaction.getBytes()));
      query.put("method", "sendrawtransaction")
              .put("params", params)
              .put("id", "test");
    } catch (JSONException e) {
      throw new ZCashException("Cannot create JSON query in PushTransaction_taddr.pushTransaction", e);
    }

    JSONObject response = queryNode(query);
    if (!response.isNull("error")) {
      throw new ZCashException("Node retured an error to PushTransaction_taddr.pushTransaction");
    }

  }

  @Override
  public void run() {
    try {
      pushTransaction();
      callback.onResponse("ok", null);
    } catch (ZCashException e) {
      callback.onResponse(e.getMessage(), null);
    }
  }

}
