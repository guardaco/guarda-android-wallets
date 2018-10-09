package com.guarda.ethereum.managers;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class WalletWords {

    public static String getWords(Context context) {
        AssetManager assetManager = context.getAssets();
        String words = null;
        InputStream stream = null;
        try {
            stream = assetManager.open("brainkeydict.txt");
        } catch (IOException e) {
        }
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        try {
            Reader in = new InputStreamReader(stream, "UTF-8");
            for (; ; ) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }
            words = out.toString();
            in.close();
        } catch (Exception e) {}

        return words;
    }

}
