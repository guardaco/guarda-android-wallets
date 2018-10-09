package com.guarda.ethereum.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.guarda.ethereum.R;

/**
 *
 * Created by SV on 10.08.2017.
 */

public final class ClipboardUtils {

    private ClipboardUtils() {
    }

    public  static void copyToClipBoard(Context context, String text) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(context.getString(R.string.address), text);
        try {
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.d("psd", "ClipboardUtils.copyToClipBoard - " + e.getMessage());
            e.printStackTrace();
        }
    }
}
