package com.gravilink.zcash;

public interface WalletCallback<T1, T2> {
  void onResponse(T1 r1, T2 r2);
}
