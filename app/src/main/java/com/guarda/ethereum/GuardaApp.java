package com.guarda.ethereum;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.freshchat.consumer.sdk.Freshchat;
import com.freshchat.consumer.sdk.FreshchatConfig;
import com.getkeepsafe.relinker.ReLinker;
import com.google.firebase.crash.FirebaseCrash;
import com.guarda.ethereum.dependencies.AppModule;
import com.guarda.ethereum.managers.ShapeshiftManager;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.models.constants.Const;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.views.activity.ConfirmPinCodeActivity;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import autodagger.AutoComponent;
import de.adorsys.android.securestoragelibrary.SecurePreferences;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

@AutoComponent(
        modules = {AppModule.class}
)

@Singleton
public class GuardaApp extends Application implements Application.ActivityLifecycleCallbacks {

    public static boolean isTransactionsEmpty = true;
    private static long timeOfExit = 0;
    private static long timeOfIgnoreExist = 0;
    public Context context;
    public static Context context_s;
    public String currentActivity = "";
    public boolean isShowPin = false;

    private static GuardaAppComponent appComponent;
    public GuardaApp instance;

    private SharedManager sharedManager;
    private ReLinker.Logger logcatLogger = (message) -> Timber.d("ReLinker %s", message);

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
        context = this;
        context_s = this;
        appComponent = buildAppComponent();
        registerActivityLifecycleCallbacks(this);
        sharedManager = new SharedManager();

        String packageName = getApplicationContext().getPackageName();
        Log.d("flint", "GuardaApp.onCreate()... packageName: " + packageName);
        if ("com.guarda.dct".equals(packageName)) {
            // buy - это покупка, purchase - обменник
//            SharedManager.flag_disable_buy_menu = true;
//            SharedManager.flag_disable_purchase_menu = true;
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
        if ("com.guarda.zec".equals(packageName)) {
            ReLinker.log(logcatLogger).loadLibrary(getApplicationContext(), "native-lib");
        }


        ShapeshiftManager.getInstance().updateSupportedCoinsList(null);

        FreshchatConfig freshchatConfig = new FreshchatConfig(Const.FRESHCHAT_APP_ID, Const.FRESHCHAT_APP_KEY);
        freshchatConfig.setGallerySelectionEnabled(true);
        Freshchat.getInstance(getApplicationContext()).init(freshchatConfig);

        Map<String, String> userMeta = new HashMap<>();
        userMeta.put("packageName", packageName);
        userMeta.put("platform", "android");
        Freshchat.getInstance(getApplicationContext()).setUserProperties(userMeta);

        FirebaseCrash.setCrashCollectionEnabled(!BuildConfig.DEBUG);

        // Set up Crashlytics, disabled for debug builds
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();
        // Initialize Fabric with the debug-disabled crashlytics.
        Fabric.with(this, crashlyticsKit);

        Timber.plant(new Timber.DebugTree());
    }

    private GuardaAppComponent buildAppComponent() {
        return DaggerGuardaAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
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

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public GuardaApp getInstance() {
        return instance;
    }

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
