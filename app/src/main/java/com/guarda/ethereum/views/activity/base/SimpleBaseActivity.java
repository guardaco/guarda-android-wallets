package com.guarda.ethereum.views.activity.base;


import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;
import com.guarda.ethereum.R;

import butterknife.ButterKnife;


public abstract class SimpleBaseActivity extends AppCompatActivity {
    protected ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createLayout();
        ButterKnife.bind(this);
        initDefault();
        initToolbar();
        init(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.navBarColor));
        }
    }

    private void initDefault() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.dialog_msg_please_wait));
        progressDialog.setCancelable(false);
    }

    protected abstract void init(Bundle savedInstanceState);

    protected void createLayout() {
        setContentView(getLayout());
    }

    protected abstract
    @LayoutRes
    int getLayout();

    protected void initToolbar() {
    }

    public void showProgress(String msg) {
        progressDialog.setMessage(msg);
        showProgress();
    }

    public void showProgress() {
        progressDialog.show();
    }

    public void closeProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        closeProgress();
        super.onDestroy();
    }

    public void showError(EditText editText, String error) {

        if (editText.getParent().getParent() instanceof TextInputLayout) {
            ((TextInputLayout) editText.getParent().getParent()).setError(error);
            ((TextInputLayout) editText.getParent().getParent()).setErrorEnabled(true);
        }
    }

    public void hideError(EditText editText) {
        if (editText.getParent().getParent() instanceof TextInputLayout) {
            ((TextInputLayout) editText.getParent().getParent()).setErrorEnabled(false);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}
