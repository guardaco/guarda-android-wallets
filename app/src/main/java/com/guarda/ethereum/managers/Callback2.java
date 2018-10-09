package com.guarda.ethereum.managers;

public interface Callback2<T1, T2> {
    void onResponse(T1 r1, T2 r2);
}
