package com.gravilink.decent;

import java.math.BigInteger;
import java.util.Vector;

public interface DecentListener {
  void onWalletRestored();

  void onImportRequired();

  void onWalletImported();

  void onBalanceGot(BigInteger balance);

  void onFeeGot(long fee);

  void onTransactionsGot(Vector<DecentTransaction> transactions);

  void onTransactionCreated(DecentTransaction decentTransaction);

  void onTransactionPushed(DecentTransaction decentTransaction);

  void onError(Exception e);
}
