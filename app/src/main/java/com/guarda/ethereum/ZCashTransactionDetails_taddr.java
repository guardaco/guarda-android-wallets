package com.guarda.ethereum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

public class ZCashTransactionDetails_taddr implements Comparable<ZCashTransactionDetails_taddr> {
  public String hash;
  boolean mainChain;
  public Long fee;
  public String type;
  boolean shielded;
  Long index;
  public String blockHash;
  public Long blockHeight;
  Long version;
  Long locktime;
  Long time;
  public Long timestamp;
  public Vector<ZCashTransactionOutput> vin;
  public Vector<ZCashTransactionOutput> vout;
  Long value;
  Long shieldedValue;
  Long outputValue;

  public static boolean prepareAfterParsing(Iterable<ZCashTransactionDetails_taddr> txdetails) {
    boolean initialized = true;
    for (ZCashTransactionDetails_taddr details : txdetails) {
      for (ZCashTransactionOutput out : details.vout) {
        out.txid = details.hash;
      }

      initialized = initialized &&
              details.hash != null &&
              details.fee != null &&
              details.type != null &&
              details.index != null &&
              details.blockHash != null &&
              details.blockHeight != null &&
              details.version != null &&
              details.locktime != null &&
              details.time != null &&
              details.timestamp != null &&
              details.vin != null &&
              details.vout != null &&
              details.value != null &&
              details.shieldedValue != null &&
              details.outputValue != null &&
              details.mainChain;
      if (!initialized) {
        return false;
      }
    }

    return true;
  }

  public ZCashTransactionDetails_taddr() {

  }

  public ZCashTransactionDetails_taddr(JSONObject transaction) throws ZCashException {
    try {
      hash = transaction.getString("hash");
      type = transaction.getString("type");
      fee = Double.valueOf(transaction.getDouble("fee") * 1e8).longValue();
      blockHash = transaction.getString("blockHash");
      blockHeight = transaction.getLong("blockHeight");
      timestamp = transaction.getLong("timestamp");
      vin = new Vector<>();
      vout = new Vector<>();
      JSONArray jsonVin = transaction.getJSONArray("vin");
      int length = jsonVin.length();
      for (int i = 0; i < length; i++) {
        JSONObject txin = jsonVin.getJSONObject(i);
        vin.add(new ZCashTransactionInput(txin));
      }

      JSONArray jsonVout = transaction.getJSONArray("vout");
      length = jsonVout.length();
      for (int i = 0; i < length; i++) {
        JSONObject txout = jsonVout.getJSONObject(i);
        vout.add(new ZCashTransactionOutput(txout, hash));
      }
    } catch (JSONException e) {
      throw new ZCashException("Cannot parse ZCashTransactionDetails_taddr from JSON.", e);
    }
  }

  @Override
  public boolean equals(Object obj) {
    ZCashTransactionDetails_taddr other = (ZCashTransactionDetails_taddr) obj;
    return other.hash.equals(hash);
  }

  @Override
  public int compareTo(ZCashTransactionDetails_taddr other) {
    return compareLess(other);
  }

  private int compareLess(ZCashTransactionDetails_taddr other) {
    if (blockHeight < other.blockHeight) {
      return -1;
    } else if (blockHeight > other.blockHeight) {
      return 1;
    } else {
      return hash.compareTo(other.hash);
    }
  }
}
