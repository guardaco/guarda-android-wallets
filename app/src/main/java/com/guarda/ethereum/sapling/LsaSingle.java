package com.guarda.ethereum.sapling;

import com.goterl.lazycode.lazysodium.LazySodiumAndroid;
import com.goterl.lazycode.lazysodium.SodiumAndroid;

public class LsaSingle {
    private static LazySodiumAndroid instance;

    public static synchronized LazySodiumAndroid getInstance() {
        if (instance == null) {
            instance = new LazySodiumAndroid(new SodiumAndroid());
        }
        return instance;
    }
}
