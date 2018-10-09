package com.gravilink.decent;

import java.util.Date;
import java.util.Vector;

public class DecentTransaction {
  public String id;
  public int blockNum;
  public Date timestamp;

  public Vector<DecentOperation> operations;

  public DecentTransaction(String id, int blockNum) {
    this.id = id;
    this.blockNum = blockNum;
    this.timestamp = null;
    this.operations = new Vector<>();
  }
}
