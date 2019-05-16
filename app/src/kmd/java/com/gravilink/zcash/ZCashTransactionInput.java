package com.guarda.zcash;

import org.json.JSONException;
import org.json.JSONObject;

public class ZCashTransactionInput extends ZCashTransactionOutput {
  long sequence;
  String coinbase;

  public ZCashTransactionInput() {
  }

  public void copyDataFrom(ZCashTransactionOutput output) {
    super.address = output.address;
    super.asm = output.asm;
    super.hex = output.hex;
    super.n = output.n;
    super.regSigs = output.regSigs;
    super.type = output.type;
    super.value = output.value;
  }

  public ZCashTransactionInput(JSONObject txin) throws ZCashException {
    try {
      sequence = txin.getLong("sequence");
      if (txin.has("coinbase")) {
        coinbase = txin.getString("coinbase");
        return;
      }

      coinbase = null;
      txid = txin.getString("txid");
      super.fromJson(txin.getJSONObject("retrievedVout"), txid);
    } catch (JSONException e) {
      throw new ZCashException("Cannot parse ZCashTransactionInput from JSON", e);
    }
  }

  public boolean isCoinbase() {
    return coinbase == null;
  }
}
