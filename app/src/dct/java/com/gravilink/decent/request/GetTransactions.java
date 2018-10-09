package com.gravilink.decent.request;

import com.gravilink.decent.DecentAccount;
import com.gravilink.decent.DecentListener;
import com.gravilink.decent.DecentOperation;
import com.gravilink.decent.DecentTransaction;
import com.gravilink.decent.DecentWallet;
import com.gravilink.decent.DecentWalletManager;
import com.gravilink.decent.TransferOperation;
import com.neovisionaries.ws.client.WebSocket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

public class GetTransactions extends AbstractDecentRequest {

  private static final int GET_LIST = 0;
  private static final int GET_SECOND_USER = 1;
  private static final int GET_BLOCK_INFO = 2;

  private int maxCount;
  private String startId;

  private int RequestStage = 0;
  private int dataCollected = 0;
  private Vector<DecentTransaction> transactionList;
  private HashMap<String, DecentAccount> secondUsers;

  public GetTransactions(DecentWalletManager manager, DecentListener adapter, DecentWallet wallet, int maxCount, String startId) {
    super(manager, adapter, wallet);
    this.maxCount = maxCount;
    this.startId = startId;
    if (this.startId == null)
      this.startId = "1.7.0";
    this.transactionList = new Vector<>();
    secondUsers = new HashMap<>();
  }

  @Override
  boolean isDatabaseRequired() {
    return true;
  }

  @Override
  boolean isHistoryRequired() {
    return true;
  }

  @Override
  boolean isBroadcastRequired() {
    return false;
  }

  @Override
  boolean onReady(WebSocket socket, int databaseApiIndex, int historyApiIndex, int broadcastApiIndex) {
    socket.sendText(String.format(Locale.ENGLISH,
            "{\"id\":0, \"method\":\"call\", \"params\":[%d, \"get_account_history\", [\"%s\", \"1.7.0\", %d, \"%s\"]]}",
            historyApiIndex, wallet.getId(), Math.min(100, maxCount), startId));
    return false;
  }

  @Override
  boolean onResponse(WebSocket socket, JSONObject response, int databaseApiIndex, int historyApiIndex, int broadcastApiIndex) {
    try {
      switch (RequestStage) {
        case GET_LIST: {
          JSONArray result = response.getJSONArray("result");
          if (result.length() == 0) {
            if (transactionList.size() == 0) {
              adapter.onTransactionsGot(transactionList);
              return true;
            }

            requestNames(socket);
            RequestStage++;
            return false;
          }

          String transId = null;
          for (int i = 0; i < result.length(); i++) {
            JSONObject trans = result.getJSONObject(i);
            JSONArray opList = trans.getJSONArray("op");
            transId = trans.getString("id");
            int blockNum = trans.getInt("block_num");

            DecentTransaction transaction = new DecentTransaction(transId, blockNum);
            transactionList.add(transaction);

            for (int j = 0; j < opList.length() / 2; j++) {
              int typeId = opList.getInt(j * 2);
              if (typeId == 0) {
                JSONObject operation = opList.getJSONObject(j * 2 + 1);

                long fee = operation.getJSONObject("fee").getLong("amount");
                long amount = operation.getJSONObject("amount").getLong("amount");
                String from = operation.getString("from");
                String to = operation.getString("to");

                DecentAccount fromWallet, toWallet;

                transaction.operations.add(new TransferOperation(getAccount(from), getAccount(to), amount, fee));
              } else if (typeId == 1) {
                JSONObject operation = opList.getJSONObject(j * 2 + 1);

                long fee = operation.getJSONObject("fee").getLong("amount");
                long amount = fee;
                String from = "from";
                String to = "to";

                DecentAccount fromWallet, toWallet;

                transaction.operations.add(new TransferOperation(new DecentAccount("aaa", "aaa", "aaa"), new DecentAccount("bbb", "bbb", "bbb"), amount, fee));
              } else {
                transaction.operations.add(new DecentOperation(DecentOperation.OperationType.values()[typeId]));
              }
            }
          }

          if (result.length() == 100 && transactionList.size() != maxCount) {
            socket.sendText(String.format(Locale.ENGLISH,
                    "{\"id\":0, \"method\":\"call\", \"params\":[%d, \"get_account_history\", [\"%s\", \"1.7.0\", %d, \"%s\"]]}",
                    historyApiIndex, wallet.getId(), Math.min(100, maxCount - transactionList.size()), startId));
            return false;
          }

          RequestStage++;
          requestNames(socket);

          break;
        }

        case GET_SECOND_USER: {
          JSONObject account = response.getJSONArray("result").getJSONObject(0);
          DecentAccount acc = secondUsers.get(account.getString("id"));
          acc.setName(account.getString("name"));
          acc.setPublicKey(account.getJSONObject("active").getJSONArray("key_auths").getJSONArray(0).getString(0));

          dataCollected++;
          if (dataCollected == secondUsers.size()) {
            requestBlock(socket, databaseApiIndex);
            RequestStage++;
          }

          break;
        }

        case GET_BLOCK_INFO: {
          int id = response.getInt("id");

          SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
          dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
          try {
            transactionList.get(id).timestamp = dateFormat.parse(response.getJSONObject("result").getString("timestamp"));
          } catch (ParseException e) {
            onError(e);
          }

          dataCollected++;
          if (dataCollected == transactionList.size()) {
            adapter.onTransactionsGot(transactionList);
            return true;
          }

          break;
        }
      }
    } catch (JSONException e) {
      onError(e);
      return true;
    }
    return false;
  }

  private DecentAccount getAccount(String id) {
    if (id.equals(wallet.getId())) {
      return wallet;
    }

    DecentAccount acc = secondUsers.get(id);
    if (acc != null) {
      return acc;
    }

    acc = new DecentAccount();
    acc.setId(id);
    secondUsers.put(id, acc);
    return acc;
  }

  private void requestNames(WebSocket socket) {
    dataCollected = 0;

    int i = 0;
    for (HashMap.Entry<String, DecentAccount> acc : secondUsers.entrySet()) {
      socket.sendText(String.format(Locale.ENGLISH, "{\"id\":%d,\"method\":\"call\",\"params\":[0,\"get_accounts\",[[\"%s\"]]]}",
              i, acc.getValue().getId()));
      i++;
    }
  }

  private void requestBlock(WebSocket socket, int databaseApiIndex) {
    dataCollected = 0;

    for (int i = 0; i < transactionList.size(); i++) {
      socket.sendText(String.format(Locale.ENGLISH, "{\"id\":%d,\"method\":\"call\",\"params\":[%d,\"get_block\",[%d]]}",
              i, databaseApiIndex, transactionList.get(i).blockNum));
    }
  }

}
