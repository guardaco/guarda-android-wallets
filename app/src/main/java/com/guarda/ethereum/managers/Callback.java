package com.guarda.ethereum.managers;


/**
 *
 * Created by SV on 13.08.2017.
 */

public interface Callback<T> {
    void onResponse(T response);
}
