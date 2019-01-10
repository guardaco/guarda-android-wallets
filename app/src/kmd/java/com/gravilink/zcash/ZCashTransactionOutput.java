package com.gravilink.zcash;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ZCashTransactionOutput {
  public Long n;
  public Long value;
  public String address;
  public String asm;
  public String hex;
  public long regSigs;
  public String type;
  public String txid;

  public ZCashTransactionOutput() {
  }

  public ZCashTransactionOutput(Long n, Long value, String address, String asm, String hex, long regSigs, String type, String txid) {
    this.n = n;
    this.value = value;
    this.address = address;
    this.asm = asm;
    this.hex = hex;
    this.regSigs = regSigs;
    this.type = type;
    this.txid = txid;
  }

  protected void fromJson(JSONObject txout, String txid) throws ZCashException {
    try {
      this.txid = txid;
      JSONObject scriptPubKey = txout.getJSONObject("scriptPubKey");
      JSONArray addresses = scriptPubKey.getJSONArray("addresses");
      n = txout.getLong("n");
      value = txout.getLong("valueZat");
      address = addresses.getString(0);
      asm = scriptPubKey.getString("asm");
      hex = scriptPubKey.getString("hex");
      regSigs = scriptPubKey.getLong("reqSigs");
      type = scriptPubKey.getString("type");
    } catch (JSONException e) {
      throw new ZCashException("Cannot parse ZCashTransactionOutput from JSON.", e);
    }
  }

  public ZCashTransactionOutput(JSONObject txout, String txid) throws ZCashException {
    fromJson(txout, txid);
  }

  @Override
  public boolean equals(Object other) {
    ZCashTransactionOutput out = (ZCashTransactionOutput) other;
    return txid.equals(out.txid) && n.equals(out.n);
  }

  @Override
  public int hashCode() {
    return hex.hashCode();
  }
}
