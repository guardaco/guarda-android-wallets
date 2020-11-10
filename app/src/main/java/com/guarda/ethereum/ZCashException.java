package com.guarda.ethereum;

public class ZCashException extends Exception {
  public Exception previous;

  public ZCashException(String s) {
    super(s);
    previous = null;
  }

  public ZCashException(String s, Exception e) {
    super(s);
    previous = e;
  }
}
