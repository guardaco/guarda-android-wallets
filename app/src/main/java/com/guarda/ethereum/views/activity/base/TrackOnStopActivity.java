package com.guarda.ethereum.views.activity.base;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.guarda.ethereum.BuildConfig;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.constants.RequestCode;
import com.guarda.ethereum.views.activity.ConfirmPinCodeActivity;
import com.guarda.ethereum.managers.SharedManager;

import javax.inject.Inject;

import autodagger.AutoInjector;

import static com.guarda.ethereum.models.constants.Extras.DISABLE_CHECK;

@AutoInjector(GuardaApp.class)
public abstract class TrackOnStopActivity extends BaseActivity {


    private final long ALLOWABLE_ABSENCE_TIME = 0;
    private boolean isUnblocked = false;
    private final String BACKUP = "backup";

    @Inject
    SharedManager sharedManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GuardaApp.getAppComponent().inject(this);
    }

    protected boolean isShouldToBlockScreen() {
        Log.d("psd", "isShouldToBlockScreen trackonstopactivity");
        if (!sharedManager.getLastSyncedBlock().isEmpty()) {
            if (sharedManager.getIsPinCodeEnable()) {
                if (GuardaApp.getTimeOfExit() != 0) {
                    long absentTime = System.currentTimeMillis() - GuardaApp.getTimeOfExit() - GuardaApp.getTimeOfIgnoreExist();
//                    if (absentTime > ALLOWABLE_ABSENCE_TIME) {
                    if (absentTime > GuardaApp.getTimeOfIgnoreExist()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void startPinCodeActivity() {
        Intent intent = new Intent(this, ConfirmPinCodeActivity.class);
        intent.putExtra(Extras.PIN_LOCKED_SCREEN, true);
        startActivityForResult(intent, RequestCode.RESTORE_FROM_STOP_PIN_CODE_REQUEST);
    }

}
