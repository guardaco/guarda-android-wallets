package com.guarda.zcash.request;

import com.guarda.zcash.WalletCallback;
import com.guarda.zcash.ZCashException;
import com.guarda.zcash.ZCashTransactionOutput;
import com.guarda.zcash.ZCashTransaction_ttoz;
import com.guarda.zcash.crypto.DumpedPrivateKey;
import com.guarda.zcash.sapling.key.SaplingCustomFullKey;

import java.util.LinkedList;
import java.util.List;

public class CreateTransaction_ttoz extends AbstractZCashRequest implements Runnable {
  private String fromAddr;
  private String toAddr;
  private String privateKey;
  private SaplingCustomFullKey saplingCustomFullKey;
  private WalletCallback<String, ZCashTransaction_ttoz> callback;
  private long fee;
  private long value;
  private List<ZCashTransactionOutput> utxos;
  private int expiryHeight;

  public CreateTransaction_ttoz(String fromAddr,
                                String toAddr,
                                long value,
                                long fee,
                                String privatekey,
                                SaplingCustomFullKey saplingCustomFullKey,
                                int expiryHeight,
                                WalletCallback<String, ZCashTransaction_ttoz> callback,
                                List<ZCashTransactionOutput> utxos) {
    this.fromAddr = fromAddr;
    this.toAddr = toAddr;
    this.value = value;
    this.fee = fee;
    this.privateKey = privatekey;
    this.saplingCustomFullKey = saplingCustomFullKey;
    this.callback = callback;
    this.utxos = utxos;
    this.expiryHeight = expiryHeight;
  }

  @Override
  public void run() {
    try {
      ZCashTransaction_ttoz tx = createTransaction();
      callback.onResponse("ok", tx);
    } catch (ZCashException e) {
      callback.onResponse(e.getMessage(), null);
    }
  }

  private ZCashTransaction_ttoz createTransaction() throws ZCashException {
    List<ZCashTransactionOutput> outputs = new LinkedList<>();
    long realValue = chooseUTXOs(outputs);
    if (realValue < fee + value) {
      throw new ZCashException("Not enough balance.");
    }

    return new ZCashTransaction_ttoz(DumpedPrivateKey.fromBase58(privateKey), saplingCustomFullKey, fromAddr, toAddr,
            value, fee, expiryHeight, outputs);
  }


  private long chooseUTXOs(List<ZCashTransactionOutput> outputs) {
    long realValue = value + fee;
    long sum = 0;
    for (ZCashTransactionOutput out : utxos) {
      outputs.add(out);
      sum += out.value;
      if (sum >= realValue) {
        break;
      }

    }

    return sum;
  }
}
