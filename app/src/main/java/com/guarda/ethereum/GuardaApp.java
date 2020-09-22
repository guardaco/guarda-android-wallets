package com.guarda.ethereum;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;

import com.facebook.stetho.Stetho;
import com.freshchat.consumer.sdk.Freshchat;
import com.freshchat.consumer.sdk.FreshchatConfig;
import com.guarda.ethereum.dependencies.AppModule;
import com.guarda.ethereum.managers.ShapeshiftManager;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.models.constants.Const;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.views.activity.ConfirmPinCodeActivity;
import com.jakewharton.threetenabp.AndroidThreeTen;

import java.util.HashMap;
import java.util.Map;

import de.adorsys.android.securestoragelibrary.SecurePreferences;
import timber.log.Timber;
import work.samosudov.rustlib.RustAPI;

public class GuardaApp extends Application implements Application.ActivityLifecycleCallbacks {

    public static boolean isTransactionsEmpty = true;
    private static long timeOfExit = 0;
    private static long timeOfIgnoreExist = 0;
    public String currentActivity = "";
    public boolean isShowPin = false;

    public static GuardaAppComponent appComponent;
    public GuardaApp instance;

    private SharedManager sharedManager;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public static GuardaAppComponent getAppComponent() {
        return appComponent;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appComponent = DaggerGuardaAppComponent
                .builder()
                .appModule(new AppModule(this))
                .build();

        registerActivityLifecycleCallbacks(this);
        sharedManager = new SharedManager();

        String packageName = getApplicationContext().getPackageName();
        Log.d("flint", "GuardaApp.onCreate()... packageName: " + packageName);
        if ("com.guarda.dct".equals(packageName)) {
            SharedManager.flag_create_new_wallet_screen = true;
        } else if ("com.guarda.etc".equals(packageName)) {
            SharedManager.flag_etc_eth_private_key_showing_fix = true;
        } else if ("com.guarda.ethereum".equals(packageName)) {
            SharedManager.flag_etc_eth_private_key_showing_fix = true;
        } else if ("com.guarda.clo".equals(packageName)) {
            SharedManager.flag_etc_eth_private_key_showing_fix = true;
        } else if ("com.guarda.bts".equals(packageName)) {
            SharedManager.flag_create_new_wallet_screen = true;
        }
        // [initially both are disabled. check and enable function is placed in CurrencyListHolder.castResponseCurrencyToCryptoItem]
        SharedManager.flag_disable_buy_menu = false; // !!! since we have 3 exchange services, make buy menu initially enabled
        //SharedManager.flag_disable_purchase_menu = true;
        SharedManager.flag_disable_purchase_menu = false; // !!! since we have two exchange services, changelly's check for exchange activity was disabled

        if ("com.guarda.clo".equals(packageName)) {
            SharedManager.flag_disable_buy_menu = true;
        }
        try {
            /////////////////
            // chinese crutch: force java lazy initialization of SecurePreferences, it's needed for old versions of android
            SecurePreferences.setValue("chinese_crutch_key", "chinese_crutch_value");
            SecurePreferences.setValue("chinese_crutch_key", "");
            // chinese crutch
            /////////////////
        } catch (Exception e) {
            Log.d("psd", "SecurePreferences doesn't support or something else - " + e.getMessage());
            e.printStackTrace();
            //for devices that are not support SecurePreferences
            sharedManager.setIsSecureStorageSupported(false);
        }
        if ("com.guarda.btc".equals(packageName) ||
                "com.guarda.bch".equals(packageName) ||
                "com.guarda.btg".equals(packageName) ||
                "com.guarda.ltc".equals(packageName) ||
                "com.guarda.sbtc".equals(packageName) ||
                "com.guarda.dgb".equals(packageName) ||
                "com.guarda.kmd".equals(packageName) ||
                "com.guarda.bts".equals(packageName) ||
                "com.guarda.qtum".equals(packageName)) {
            sharedManager.setHasWifXprvKeys(true);
        }
        //init ndk lib for ZEC
        if (BuildConfig.FLAVOR == "zec") {
            RustAPI.init(getApplicationContext());
            if (BuildConfig.DEBUG)
                Stetho.initializeWithDefaults(this);
        }

        ShapeshiftManager.getInstance().updateSupportedCoinsList(null);

        FreshchatConfig freshchatConfig = new FreshchatConfig(Const.FRESHCHAT_APP_ID, Const.FRESHCHAT_APP_KEY);
        freshchatConfig.setGallerySelectionEnabled(true);
        Freshchat.getInstance(getApplicationContext()).init(freshchatConfig);

        Map<String, String> userMeta = new HashMap<>();
        userMeta.put("packageName", packageName);
        userMeta.put("platform", "android");
        Freshchat.getInstance(getApplicationContext()).setUserProperties(userMeta);

        Timber.plant(new Timber.DebugTree());

        AndroidThreeTen.init(this);
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (sharedManager.getIsPinCodeEnable()) {
            //show pin code in cases other than resumed activity is AuthorizationTypeActivity or CreateAccessCodeActivity or AccessCodeAgainActivity
            if (!activity.getLocalClassName().equalsIgnoreCase("com.guarda.ethereum.views.activity.AuthorizationTypeActivity")
                    && !activity.getLocalClassName().equalsIgnoreCase("com.guarda.ethereum.views.activity.CreateAccessCodeActivity")
                    && !activity.getLocalClassName().equalsIgnoreCase("com.guarda.ethereum.views.activity.AccessCodeAgainActivity")
                    && !activity.getLocalClassName().equalsIgnoreCase("com.guarda.ethereum.views.activity.CongratsActivity")) {
                //show pin code if last paused activity == resumed activity
                if (currentActivity.equalsIgnoreCase(activity.getLocalClassName()) ||
                        //or if app launches (goes from AuthorizationTypeActivity to MainActivity)
                        (currentActivity.equalsIgnoreCase("com.guarda.ethereum.views.activity.AuthorizationTypeActivity")
                                && activity.getLocalClassName().equalsIgnoreCase("com.guarda.ethereum.views.activity.MainActivity"))
                        //or if last paused activity was ConfirmPinCodeActivity
                        || (currentActivity.equalsIgnoreCase("com.guarda.ethereum.views.activity.ConfirmPinCodeActivity")
                        && !sharedManager.getPinWasCorrect())) {
                    startPinCodeActivity();
                }
            }
        }

        if (!activity.getLocalClassName().equalsIgnoreCase("com.guarda.ethereum.views.activity.ConfirmPinCodeActivity")) {
            isShowPin = !(currentActivity.equalsIgnoreCase(activity.getLocalClassName()));
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        currentActivity = activity.getLocalClassName();
    }

    @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) { }
    @Override public void onActivityStopped(Activity activity) { }
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
    @Override public void onActivityStarted(Activity activity) { }
    @Override public void onActivityDestroyed(Activity activity) { }

    public static long getTimeOfExit() {
        return timeOfExit;
    }

    public static void setTimeOfExit(long currentTime) {
        GuardaApp.timeOfExit = currentTime;
    }

    public static long getTimeOfIgnoreExist() {
        return timeOfIgnoreExist;
    }

    public static void setTimeOfIgnoreExist(long timeOfIgnoreExist) {
        GuardaApp.timeOfIgnoreExist = timeOfIgnoreExist;
    }

    public void startPinCodeActivity() {
        Intent intent = new Intent(this, ConfirmPinCodeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Extras.PIN_LOCKED_SCREEN, true);
        startActivity(intent);
    }

}
