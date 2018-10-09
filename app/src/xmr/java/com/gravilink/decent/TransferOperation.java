package com.gravilink.decent;

public class TransferOperation extends DecentOperation {

  public DecentAccount from;
  public DecentAccount to;
  public long amount, fee;

  public TransferOperation(DecentAccount from, DecentAccount to, long amount, long fee) {
    super(OperationType.TRANSFER_OPERATION);
    this.from = from;
    this.to = to;
    this.amount = amount;
    this.fee = fee;
  }

}
