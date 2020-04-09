package com.guarda.ethereum.utils;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by psd on 15.01.2018.
 */

public class DebugHelper {
    public static void checkEmptyLastSyncedBlock(String s, Context context) {
        if (s.equalsIgnoreCase("")) {
            try {
                FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
                Bundle params = new Bundle();

                params.putString("device", "brand: " + Build.BRAND + " device: " + Build.DEVICE + " model: " + Build.MODEL);
                params.putString("time", String.valueOf(System.currentTimeMillis()));
                params.putString("full_text", "sharedManager.getLastSyncedBlock() is empty");
                firebaseAnalytics.logEvent("lastBlockEmpty", params);
            } catch (Exception e) {
                Log.e("psd", "Firebase - sharedManager.getLastSyncedBlock()");
            }
        }
    }

    public static void logIventFirebase(String name, String trace, Context c) {
        try {
            FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(c);
            Bundle params = new Bundle();
            params.putString("stacktrace", trace);
            firebaseAnalytics.logEvent(name, params);
        } catch (Exception e) {
            Log.e("psd", "Firebase - DebugHelper");
        }
    }
}
