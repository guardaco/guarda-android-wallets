package com.guarda.ethereum.views.activity.base;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.views.activity.AuthorizationTypeActivity;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.ButterKnife;

import static com.guarda.ethereum.models.constants.Extras.DISABLE_CHECK;


@AutoInjector(GuardaApp.class)
public abstract class BaseActivity extends ABaseActivity {
    @Inject
    WalletManager walletManager;

    protected ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createLayout();
        ButterKnife.bind(this);
        GuardaApp.getAppComponent().inject(this);
        initDefault();
        initToolbar();
        init(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.navBarColor));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkRequiredInstances();
    }

    @Override
    protected void onDestroy() {
        closeProgress();
        super.onDestroy();
    }

    private void checkRequiredInstances() {
        boolean disableCheck = getIntent().getBooleanExtra(DISABLE_CHECK, false);
        if (walletManager != null && walletManager.getWalletFriendlyAddress() == null && !disableCheck) {
            openAuthorizationActivity();
        }
    }

    private void openAuthorizationActivity() {
        Intent intent = new Intent(this, AuthorizationTypeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
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
        if(!this.isFinishing()) {
            progressDialog.show();
        }
    }

    public void closeProgress() {
        if(this.isFinishing()) return;
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void showError(EditText editText, String error) {
        try {
            if (editText.getParent().getParent() instanceof TextInputLayout) {
                ((TextInputLayout) editText.getParent().getParent()).setError(error);
                ((TextInputLayout) editText.getParent().getParent()).setErrorEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideError(EditText editText) {
        try {
            if (editText.getParent().getParent() instanceof TextInputLayout) {
                ((TextInputLayout) editText.getParent().getParent()).setErrorEnabled(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showCustomToast(String text, int intDrawable) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_custom,
                (ViewGroup) findViewById(R.id.custom_toast_container));

        ImageView imageIcon = layout.findViewById(R.id.custom_toast_icon);
        imageIcon.setImageDrawable(ContextCompat.getDrawable(this, intDrawable));

        TextView tv = layout.findViewById(R.id.custom_toast_text);
        tv.setText(text);

        Toast toast = new Toast(this);
        toast.setGravity(Gravity.BOTTOM|Gravity.FILL_HORIZONTAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
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
