package com.guarda.ethereum.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.views.activity.base.TrackOnStopActivity;

import javax.inject.Inject;

import butterknife.BindView;

import static com.guarda.ethereum.models.constants.Extras.GO_TO_TRANS_HISTORY;
import static com.guarda.ethereum.models.constants.Extras.NAVIGATE_TO_FRAGMENT;

public class CongratsActivity extends TrackOnStopActivity {

    @BindView(R.id.tv_congrats_text)
    TextView tvCongratsText;

    @Inject
    SharedManager sharedManager;

    private final static int SHOWING_DELAY = 2000;

    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        tvCongratsText.setText(getIntent().getStringExtra(Extras.CONGRATS_TEXT));

        String fromExtras;
        if (getIntent().getExtras() != null) {
            fromExtras = getIntent().getExtras().getString(Extras.COME_FROM);
        } else {
            fromExtras = null;
        }

        final String comeFrom = fromExtras;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (comeFrom != null) {
                    if (comeFrom.equalsIgnoreCase(Extras.FROM_WITHDRAW) ||
                            comeFrom.equalsIgnoreCase(Extras.FROM_PURCHASE_WEMOVECOIN)) {
                        returnToTransHistory();
                    } else if (comeFrom.equalsIgnoreCase(Extras.FROM_PIN_CODE)) {
                        sharedManager.setIsShowPinAfterCongrats(false);
                        returnToMainActivity();
                    }
                } else {
                    finish();
                }
            }
        }, SHOWING_DELAY);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_congrats;
    }

    @Override
    public void onBackPressed() {
    }

    private void returnToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void returnToTransHistory() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(NAVIGATE_TO_FRAGMENT, GO_TO_TRANS_HISTORY);
        startActivity(intent);
    }
}
