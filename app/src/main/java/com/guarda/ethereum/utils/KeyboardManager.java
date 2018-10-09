package com.guarda.ethereum.utils;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public final class KeyboardManager {

    public static void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void hideKeyboard(Activity activity) {
        try {
            if (activity != null && activity.getCurrentFocus() != null) {
                InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static void setFocusAndOpenKeyboard(Context context, final EditText editText){
        final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        editText.postDelayed(new Runnable() {
            @Override
            public void run() {
                editText.requestFocus();
                imm.showSoftInput(editText, 0);
            }
        }, 100);
    }

    public static void disableKeyboardByClickView(View view){
        View.OnTouchListener otl = new View.OnTouchListener() {
            public boolean onTouch (View v, MotionEvent event) {
                return true; // the listener has consumed the event
            }
        };
        view.setOnTouchListener(otl);
    }
}
