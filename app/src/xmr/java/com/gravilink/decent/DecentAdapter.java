package com.gravilink.decent;

import java.math.BigInteger;
import java.util.Vector;

public abstract class DecentAdapter implements DecentListener {
  @Override
  public void onWalletRestored() {
  }

  @Override
  public void onImportRequired() {
  }

  @Override
  public void onWalletImported() {
  }

  @Override
  public void onBalanceGot(BigInteger balance) {
  }

  @Override
  public void onFeeGot(long fee) {
  }

  @Override
  public void onTransactionsGot(Vector<DecentTransaction> transactions) {
  }

  @Override
  public void onTransactionCreated(DecentTransaction decentTransaction) {
  }

  @Override
  public void onTransactionPushed(DecentTransaction decentTransaction) {
  }

  @Override
  public void onError(Exception e) {
  }

}
