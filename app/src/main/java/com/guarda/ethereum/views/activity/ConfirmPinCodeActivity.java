package com.guarda.ethereum.views.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.customviews.GuardaPinCodeLayout;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.utils.Coders;
import com.guarda.ethereum.views.activity.base.APinCodeActivity;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;

public class ConfirmPinCodeActivity extends APinCodeActivity {

    private static final int MAX_WRONG_ATTEMPTS = 3;
    private static final int SHORT_BLOCK_TIME_MIN = 5;
    private static final int LONG_BLOCK_TIME_MIN = 15;

    @BindView(R.id.gpl_pin_code)
    GuardaPinCodeLayout ilInputLayout;

    @Inject
    SharedManager sharedManager;

    private String savedPinCode;
    private String pinCode;
    private Boolean isScreenLocked = false;

    private int wrongAttemptsCount = MAX_WRONG_ATTEMPTS;
    private Handler handler;
    long timeOfOpeningScreen = 0;

    Runnable pinCodeBlockChecker = new Runnable() {
        @Override
        public void run() {
            if (isPinCodeInputLock()) {
                disableInput();
                handler.postDelayed(this, 1000);
            } else {
                enableInput();
                wrongAttemptsCount = 0;
            }
        }
    };

    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        setToolBarTitle("");
        isScreenLocked = getIntent().getBooleanExtra(Extras.PIN_LOCKED_SCREEN, false);
        timeOfOpeningScreen = System.currentTimeMillis();

        handler = new Handler();
        savedPinCode = sharedManager.getPinCode();

        ilInputLayout.setInputListener((String text) -> {
            pinCode = text;
            checkPinCode(pinCode);
        });

        if (isScreenLocked) {
            androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedManager.setPinWasCorrect(false);
        handler.post(pinCodeBlockChecker);
    }

    private boolean isPinCodeInputLock() {
        long enableInputTime = 0;
        int blockedTime = 0;
        switch (PinBlockType.values()[sharedManager.getPinBlockType()]) {
            case Unblock:
                ilInputLayout.removeError();
                return false;
            case ShortBlock:
                enableInputTime = (SHORT_BLOCK_TIME_MIN * 60 * 1000) + sharedManager.getLastWrongPinCodeTime();
                blockedTime = SHORT_BLOCK_TIME_MIN;
                break;
            case LongBlock:
                blockedTime = LONG_BLOCK_TIME_MIN;
                enableInputTime = (LONG_BLOCK_TIME_MIN * 60 * 1000) + sharedManager.getLastWrongPinCodeTime();
                break;
        }

        if (System.currentTimeMillis() > enableInputTime) {
            sharedManager.setLastWrongPinCodeTime(0);
            wrongAttemptsCount = 0;
            ilInputLayout.removeError();
            enableInput();
            return false;
        } else {
            ilInputLayout.setErrorMessage(String.format(Locale.US, getString(R.string.warning_many_unsuccessful_attempts_pin_code), blockedTime));
        }
        return true;

    }

    private void checkPinCode(String pinCode) {
        boolean isPinCorrect = false;
        if (pinCode.length() > 0) {
            ilInputLayout.removeError();
        }

        if (pinCode.length() == 4) {
            wrongAttemptsCount++;
            if (wrongAttemptsCount <= MAX_WRONG_ATTEMPTS) {
                pinCode = Coders.getSha1Hex(pinCode);
                if (savedPinCode.equals(pinCode)) {
                    isPinCorrect = true;
                    sharedManager.setLastWrongPinCodeTime(0);
                    sharedManager.setPinBlockType(PinBlockType.Unblock.ordinal());
                    isScreenLocked = false;
                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_OK, returnIntent);
                    GuardaApp.setTimeOfIgnoreExist(System.currentTimeMillis() - timeOfOpeningScreen);
                    sharedManager.setPinWasCorrect(true);
                    finish();
                } else {
                    ilInputLayout.callError();
                    ilInputLayout.setErrorMessage(getString(R.string.incorrect_pin_warning));
                }
            }

            if (wrongAttemptsCount >= MAX_WRONG_ATTEMPTS && !isPinCorrect) {
                sharedManager.setLastWrongPinCodeTime(System.currentTimeMillis());
                ilInputLayout.callError();

                disableInput();

                switch (PinBlockType.values()[sharedManager.getPinBlockType()]) {
                    case Unblock:
                        sharedManager.setPinBlockType(PinBlockType.ShortBlock.ordinal());
                        break;
                    case ShortBlock:
                        sharedManager.setPinBlockType(PinBlockType.LongBlock.ordinal());
                        break;
                    case LongBlock:
                        sharedManager.setPinBlockType(PinBlockType.LongBlock.ordinal());
                        break;
                }

                handler.post(pinCodeBlockChecker);
            }

        }
    }

    private void disableInput() {
        ilInputLayout.disableButtons();
    }

    private void enableInput() {
        ilInputLayout.enableButtons();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_confirm_pin_code;
    }

    @Override
    public void onBackPressed() {
        if (!isScreenLocked) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(pinCodeBlockChecker);
    }

    private enum PinBlockType {
        Unblock, ShortBlock, LongBlock
    }
}
