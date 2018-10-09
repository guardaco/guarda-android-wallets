package com.gravilink.decent.request;

import com.gravilink.decent.DecentAccount;
import com.gravilink.decent.DecentListener;
import com.gravilink.decent.DecentTransaction;
import com.gravilink.decent.DecentWallet;
import com.gravilink.decent.DecentWalletManager;
import com.gravilink.decent.TransferOperation;
import com.neovisionaries.ws.client.WebSocket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class CreateTransaction extends AbstractDecentRequest {

  private static final int GET_BY_NAME = 0;
  private static final int GET_BY_PUBLIC_KEY = 1;
  private static final int GET_BY_ID = 2;

  private DecentTransaction result;
  private TransferOperation op;
  private int RequestStage;

  public CreateTransaction(DecentWalletManager manager, DecentListener adapter, DecentWallet from, DecentAccount to, long amount, long fee) {
    super(manager, adapter, from);
    result = new DecentTransaction(null, 0);
    op = new TransferOperation(from, to, amount, fee);
    result.operations.add(op);
  }


  @Override
  boolean isDatabaseRequired() {
    return true;
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
    if (op.to.isInitialized()) {
      adapter.onTransactionCreated(result);
      return true;
    } else {
      if (op.to.getName() == null) {
        RequestStage = GET_BY_PUBLIC_KEY;
        socket.sendText(String.format(Locale.ENGLISH, "{\"id\":0,\"method\":\"call\",\"params\":[2,\"get_key_references\", [[\"%s\"]]]}",
                op.to.getPublicKey()));
      } else {
        RequestStage = GET_BY_NAME;
        socket.sendText(String.format(Locale.ENGLISH, "{\"id\":0, \"method\":\"call\", \"params\":[0, \"get_account_by_name\", [\"%s\"]]}",
                op.to.getName()));
      }
    }

    return false;
  }

  @Override
  boolean onResponse(WebSocket socket, JSONObject response, int databaseApiIndex, int historyApiIndex, int broadcastApiIndex) {
    try {
      switch (RequestStage) {
        case GET_BY_NAME: {
          JSONObject account = response.getJSONObject("result");
          op.to.setId(account.getString("id"));
          op.to.setName(account.getString("name"));
          op.to.setPublicKey(account.getJSONObject("active").getJSONArray("key_auths").getJSONArray(0).getString(0));
          adapter.onTransactionCreated(result);
          return true;
        }

        case GET_BY_PUBLIC_KEY: {
          JSONArray idList = response.getJSONArray("result");
          idList = idList.getJSONArray(idList.length() - 1);
          String id = idList.getString(idList.length() - 1);
          socket.sendText(String.format(Locale.ENGLISH, "{\"id\":0,\"method\":\"call\",\"params\":[0,\"get_accounts\",[[\"%s\"]]]}",
                  id));
          RequestStage = GET_BY_ID;
          return false;
        }

        case GET_BY_ID: {
          JSONObject account = response.getJSONArray("result").getJSONObject(0);
          op.to.setId(account.getString("id"));
          op.to.setName(account.getString("name"));
          op.to.setPublicKey(account.getJSONArray("key_auths").getJSONArray(0).getString(0));
          adapter.onTransactionCreated(result);
          return true;
        }
      }
    } catch (JSONException e) {
      onError(e);
    }
    return true;
  }
}
