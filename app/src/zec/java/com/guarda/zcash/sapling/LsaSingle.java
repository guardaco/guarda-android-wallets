package com.guarda.zcash.sapling;

import com.goterl.lazycode.lazysodium.LazySodiumAndroid;
import com.goterl.lazycode.lazysodium.SodiumAndroid;

import java.nio.charset.Charset;

public class LsaSingle {
    private static LazySodiumAndroid instance;

    public static synchronized LazySodiumAndroid getInstance() {
        if (instance == null) {
            instance = new LazySodiumAndroid(new SodiumAndroid(), Charset.forName("UTF-16BE"));
        }
        return instance;
    }
}
