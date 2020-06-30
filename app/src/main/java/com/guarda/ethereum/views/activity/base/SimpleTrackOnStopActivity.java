package com.guarda.ethereum.views.activity.base;


import android.app.Activity;
import android.content.Intent;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.constants.RequestCode;
import com.guarda.ethereum.views.activity.ConfirmPinCodeActivity;

import javax.inject.Inject;


public abstract class SimpleTrackOnStopActivity extends SimpleBaseActivity {


    private final long ALLOWABLE_ABSENCE_TIME = 60000;
    protected boolean isUnblocked = false;
    private final String BACKUP = "backup";

    @Inject
    WalletManager walletManager;
    @Inject
    SharedManager sharedManager;

    @Override
    protected void onPause() {
        super.onPause();
        GuardaApp.setTimeOfExit(System.currentTimeMillis());
    }

    @Override
    protected void onResume() {
//        if (isShouldToBlockScreen()) {
//            if (!isUnblocked) {
//                startPinCodeActivity();
//            }
//        } else {
//            GuardaApp.setTimeOfExit(0);
//            GuardaApp.setTimeOfIgnoreExist(0);
//        }
        super.onResume();
    }

    protected boolean isShouldToBlockScreen() {
        if (!sharedManager.getLastSyncedBlock().isEmpty()) {
            if (sharedManager.getIsPinCodeEnable()) {
                if (GuardaApp.getTimeOfExit() != 0) {
                    long absentTime = System.currentTimeMillis() - GuardaApp.getTimeOfExit() - GuardaApp.getTimeOfIgnoreExist();
                    if (absentTime > ALLOWABLE_ABSENCE_TIME) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCode.RESTORE_FROM_STOP_PIN_CODE_REQUEST_SIMPLE_TRACK) {
            if (resultCode == Activity.RESULT_OK) {
                GuardaApp.setTimeOfExit(0);
                GuardaApp.setTimeOfIgnoreExist(0);
                isUnblocked = true;
//                onResume();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void startPinCodeActivity() {
        Intent intent = new Intent(this, ConfirmPinCodeActivity.class);
        intent.putExtra(Extras.PIN_LOCKED_SCREEN, true);
        startActivityForResult(intent, RequestCode.RESTORE_FROM_STOP_PIN_CODE_REQUEST_SIMPLE_TRACK);
    }



}
