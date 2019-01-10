package com.gravilink.zcash.request;

import android.net.Uri;

import com.gravilink.zcash.WalletCallback;
import com.gravilink.zcash.ZCashException;
import com.gravilink.zcash.ZCashTransactionOutput;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import static com.guarda.ethereum.models.constants.KmdExplorer.KMD_EXPLORER_API;
import static com.guarda.ethereum.models.constants.KmdExplorer.KMD_TEST_EXPLORER_API;

public class GetUTXOSRequest extends AbstractZCashRequest implements Runnable {
  private WalletCallback<String, List<ZCashTransactionOutput>> callback;
  private String fromAddr;
  private long minconf;

  public GetUTXOSRequest(String fromAddr, long minconf, WalletCallback<String, List<ZCashTransactionOutput>> callback) {
    this.fromAddr = fromAddr;
    this.callback = callback;
    this.minconf = minconf;
  }

  @Override
  public void run() {
//    String uri = Uri.parse(KMD_EXPLORER_API).buildUpon()
    String uri = Uri.parse(KMD_TEST_EXPLORER_API).buildUpon()
            .appendEncodedPath("addrs")
            .appendEncodedPath(fromAddr)
            .appendEncodedPath("utxo")
            .build().toString();

    try {
      JSONArray utxos = queryExplorerForArray(uri);
      List<ZCashTransactionOutput> outputs = new LinkedList<>();
      int length = utxos.length();
      for (int i = 0; i < length; i++) {
        JSONObject jout = utxos.getJSONObject(i);
        if(minconf > jout.getLong("confirmations")) {
          continue;
        }

        ZCashTransactionOutput out = new ZCashTransactionOutput();
        out.address = fromAddr;
        out.txid = jout.getString("txid");
        out.n = jout.getLong("vout");
        out.value = jout.getLong("satoshis");
        out.hex = jout.getString("scriptPubKey");
        outputs.add(out);
      }

      callback.onResponse("ok", outputs);
    } catch (ZCashException e) {
      callback.onResponse(e.getMessage(), null);
    } catch (JSONException e) {
      callback.onResponse(String.format("Cannot parse utxos from %s", uri), null);
    }
  }

}
