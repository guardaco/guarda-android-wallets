package com.guarda.ethereum.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

/**
 * Created by SV on 28.11.2017.
 */

public class FileUtils {
    public static HashSet<String> readToSet(Context context, String assetName) {
        HashSet<String> set = new HashSet<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(assetName), "UTF-8"));

            String line;
            while ((line = reader.readLine()) != null) {
                set.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return set;
    }
}
