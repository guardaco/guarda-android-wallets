package com.guarda.ethereum.views.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.customviews.RobotoLightTypefaceSpan;
import com.guarda.ethereum.managers.ChangellyNetworkManager;
import com.guarda.ethereum.managers.CurrencyListHolder;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.TransactionsManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.constants.RequestCode;
import com.guarda.ethereum.models.items.ResponseCurrencyItem;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rxcall.CallCleanDbLogOut;
import com.guarda.ethereum.views.activity.base.TrackOnStopActivity;
import com.guarda.ethereum.views.fragments.BackupFragment;
import com.guarda.ethereum.views.fragments.DepositFragment;
import com.guarda.ethereum.views.fragments.DepositFragment_decent;
import com.guarda.ethereum.views.fragments.DisabledFragment;
import com.guarda.ethereum.views.fragments.ExchangeFragment;
import com.guarda.ethereum.views.fragments.PurchaseServiceFragment;
import com.guarda.ethereum.views.fragments.SettingsFragment;
import com.guarda.ethereum.views.fragments.TransactionHistoryFragment;
import com.guarda.ethereum.views.fragments.UserWalletFragment;
import com.guarda.ethereum.views.fragments.WithdrawFragment;
import com.guarda.ethereum.views.fragments.base.BaseFragment;
import com.guarda.zcash.sapling.SyncManager;
import com.guarda.zcash.sapling.db.DbManager;

import java.util.List;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.guarda.ethereum.models.constants.Const.CHANGELLY_TIMEOUT;
import static com.guarda.ethereum.models.constants.Extras.CREATE_WALLET;
import static com.guarda.ethereum.models.constants.Extras.FIRST_ACTION_MAIN_ACTIVITY;
import static com.guarda.ethereum.models.constants.Extras.GO_TO_SETTINGS;
import static com.guarda.ethereum.models.constants.Extras.KEY;
import static com.guarda.ethereum.models.constants.Extras.NAVIGATE_TO_FRAGMENT;
import static com.guarda.ethereum.models.constants.Extras.RESTORE_WALLET;

@AutoInjector(GuardaApp.class)
public class MainActivity extends TrackOnStopActivity {

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.navigation_view)
    NavigationView mNavigationView;
    @BindView(R.id.main_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tv_toolbar_title)
    TextView mToolbarTitle;

    @Inject
    WalletManager walletManager;
    @Inject
    SharedManager sharedManager;
    @Inject
    CurrencyListHolder currentCrypto;
    @Inject
    SyncManager syncManager;
    @Inject
    DbManager dbManager;
    @Inject
    TransactionsManager transactionsManager;

    String firstAction = CREATE_WALLET;
    String key;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        setupSideMenu();
        setToolBarTitle(R.string.app_name);

        if (getIntent().getExtras() != null) {
            firstAction = getIntent().getExtras().getString(FIRST_ACTION_MAIN_ACTIVITY);
            key = getIntent().getExtras().getString(KEY);
        }

        if (firstAction == null || firstAction.equals(CREATE_WALLET)) {
            goToUserWallet();
        } else if (firstAction.equals(RESTORE_WALLET)) {
            goToTransactionHistory(key, firstAction);
        } else if (firstAction.equals(GO_TO_SETTINGS)) {
            goToSettingsFragment();
        }

        changeMenuFontFamily();
    }

    @Override
    protected void onResume() {
        super.onResume();

        long lastUpdate = sharedManager.getLastChangelliCurList();
        if (lastUpdate + CHANGELLY_TIMEOUT > System.currentTimeMillis()) return;

        ChangellyNetworkManager.getCurrencies(new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    ResponseCurrencyItem responseCurrency = (ResponseCurrencyItem) response;
                    currentCrypto.castResponseCurrencyToCryptoItem(responseCurrency, getApplicationContext());
                    sharedManager.setLastChangelliCurList(System.currentTimeMillis());
                } catch (Exception e) {
                    Log.d("psd", "ChangellyNetworkManager.getCurrencies resp - " + e.getMessage());
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(String msg) {}
        });
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String navigateTo = null;
        if (intent.getStringExtra(NAVIGATE_TO_FRAGMENT) != null) {
            navigateTo = intent.getExtras().getString(Extras.NAVIGATE_TO_FRAGMENT);
        }
        if (navigateTo != null) {
            switch (navigateTo) {
                case Extras.GO_TO_TRANS_HISTORY:
                    goToTransactionHistory();
                    break;
                case Extras.GO_TO_PURCHASE:
                    goToPurchaseFragment();
                    break;
            }
        }
    }

    private void changeMenuFontFamily() {
        Menu m = mNavigationView.getMenu();
        for (int i = 0; i < m.size(); i++) {
            MenuItem mi = m.getItem(i);

            SubMenu subMenu = mi.getSubMenu();
            if (subMenu != null && subMenu.size() > 0) {
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem);
                }
            }

            applyFontToMenuItem(mi);
        }
    }

    private void goToTransactionHistory(String key, String firstAction) {
        TransactionHistoryFragment fragment = new TransactionHistoryFragment();
        Bundle args = new Bundle();
        args.putString(FIRST_ACTION_MAIN_ACTIVITY, firstAction);
        args.putString(KEY, key);
        fragment.setArguments(args);
        setToolBarTitle(R.string.title_transaction_history);
        navigateToFragment(fragment);
    }

    private void goToTransactionHistory() {
        setToolBarTitle(R.string.title_transaction_history);
        navigateToFragment(new TransactionHistoryFragment());
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    private void goToUserWallet() {
        UserWalletFragment fragment = new UserWalletFragment();
        setToolBarTitle(R.string.toolbar_title_wallet);
        navigateToFragment(fragment);
    }

    private void goToSettingsFragment() {
        SettingsFragment fragment = new SettingsFragment();
        setToolBarTitle(R.string.title_setting);
        navigateToFragment(fragment);
    }

    private void goToBackupFragment() {
        BackupFragment fragment = new BackupFragment();
        setToolBarTitle(R.string.title_backup);
        navigateToFragment(fragment);

        sharedManager.setIsShowBackupAlert(false);
        ImageView i = (ImageView) mNavigationView.getMenu().getItem(5).getActionView();
        i.setVisibility(View.GONE);
    }

    private void goToPurchaseFragment() {
        PurchaseServiceFragment fragment = new PurchaseServiceFragment();
        setToolBarTitle(getString(R.string.app_purchase_service));
        navigateToFragment(fragment);
    }

    public void setupSideMenu() {
        mNavigationView.setNavigationItemSelectedListener((menuItem) -> {
                mDrawerLayout.closeDrawers();

                String packageName = getApplicationContext().getPackageName();
                Fragment fragment = null;
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fl_main_root);
                switch (menuItem.getItemId()) {
                    case R.id.menu_transaction_history:
                        if (!(currentFragment instanceof TransactionHistoryFragment)) {
                            goToTransactionHistory("", Extras.SHOW_STRICTLY_HISTORY);
                        } else {
                            ((TransactionHistoryFragment)currentFragment).scrollToTop();
                        }
                        break;
                    case R.id.menu_purchase:
                        if (SharedManager.flag_disable_purchase_menu) {
                            setToolBarTitle(R.string.title_purchase);
                            fragment = new DisabledFragment();
                        } else {
                            if (!(currentFragment instanceof ExchangeFragment)) {
                                setToolBarTitle(R.string.title_purchase);
                                fragment = new ExchangeFragment();
                            }
                        }
                        break;
                    case R.id.menu_withdraw:
                        if (!(currentFragment instanceof WithdrawFragment)) {
                            setToolBarTitle(R.string.title_withdraw2);
                            fragment = new WithdrawFragment();
                        }
                        break;
                    case R.id.menu_deposit:
                        if (!(currentFragment instanceof DepositFragment)) {
                            setToolBarTitle(R.string.title_deposit);
                            if ("com.guarda.dct".equals(packageName))
                                fragment = new DepositFragment_decent();
                            else
                                fragment = new DepositFragment();
                        }
                        break;
                    case R.id.menu_backup:
                        if (!(currentFragment instanceof BackupFragment)) {
                            if (sharedManager.getIsPinCodeEnable()) {
                                Intent intent = new Intent(MainActivity.this, ConfirmPinCodeActivity.class);
                                startActivityForResult(intent, RequestCode.CONFIRM_PIN_CODE_REQUEST_MA);
                            } else {
                                goToBackupFragment();
                            }
                        }
                        break;
                    case R.id.menu_settings:
                        if (!(currentFragment instanceof SettingsFragment)) {
                            setToolBarTitle(R.string.title_setting);
                            fragment = new SettingsFragment();
                        }
                        break;
                    case R.id.menu_logout:
                        showLogoutDialog();
                        break;
                }

                if (fragment != null) {
                    navigateToFragment(fragment);
                }

                return true;
        });

        mNavigationView.setItemIconTintList(null);

        ImageView i = (ImageView) mNavigationView.getMenu().getItem(4).getActionView();
        i.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCode.CONFIRM_PIN_CODE_REQUEST_MA) {
            if (resultCode == Activity.RESULT_OK) {
                new Handler().postDelayed(() -> goToBackupFragment(), 100);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_side_menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onHomePressed();
                break;
//            case R.id.toolbar_menu_settings:
//                goToSettingsFragment();
//                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showLogoutDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        View logoutView = li.inflate(R.layout.dialog_logout, null);

        final AlertDialog logoutDialog = new AlertDialog.Builder(this, R.style.LogoutDialogStyle)
                .setView(R.layout.dialog_logout)
                .setCancelable(false)
                .create();

        Button logOut = logoutView.findViewById(R.id.btn_confirm_loguot);
        Button openBackup = logoutView.findViewById(R.id.btn_to_backup);

        logOut.setOnClickListener((v) -> {
            syncManager.stopSync();
            walletManager.clearWallet();
            sharedManager.setLastSyncedBlock("");
            sharedManager.setIsShowBackupAlert(true);
            sharedManager.setIsPinCodeEnable(false);
            transactionsManager.clearLists();
            cleanDbLogOut();
        });
        openBackup.setOnClickListener((v) -> {
            if (sharedManager.getIsPinCodeEnable()) {
                Intent intent = new Intent(MainActivity.this, ConfirmPinCodeActivity.class);
                startActivityForResult(intent, RequestCode.CONFIRM_PIN_CODE_REQUEST_MA);
            } else {
                goToBackupFragment();
            }
            logoutDialog.cancel();
        });

        logoutDialog.setView(logoutView);
        logoutDialog.show();
    }

    private void cleanDbLogOut() {
        compositeDisposable.add(Observable
                .fromCallable(new CallCleanDbLogOut(dbManager))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (latest) -> {
                            finish();
                            Timber.d("cleanDbLogOut done=%s", latest);
                        },
                        (e) -> {
                            finish();
                            Timber.d("cleanDbLogOut err=%s", e.getMessage());
                        }
                )
        );
    }

    private void navigateToFragment(Fragment fragment) {
        if (isFinishing()) return;

        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_up, R.anim.fade_out_animation, R.anim.fade_out_animation, R.anim.slide_down);
        fragmentTransaction.replace(R.id.fl_main_root, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void setToolBarTitle(int resId) {
        mToolbarTitle.setText(resId);
    }

    public void setToolBarTitle(CharSequence title) {
        if (mToolbarTitle != null) {
            mToolbarTitle.setText(title);
            if (TextUtils.isEmpty(title)) {
                mToolbarTitle.setVisibility(View.GONE);
            }
        } else {
            mToolbarTitle.setVisibility(View.VISIBLE);
            setTitle(getTitle().toString() + " " + title);
        }
    }

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new RobotoLightTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            boolean handled = false;
            try {
                List fragmentList = getSupportFragmentManager().getFragments();
                for (Object f : fragmentList) {
                    if (f instanceof BaseFragment) {
                        handled = ((BaseFragment) f).onBackPressed();
                        if (handled)
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!handled) {
                if (!isHomeFragmentOpen())
                    goToHomeScreen();
                else
                    showLogoutDialog();
            }
        }
    }

    protected void onHomePressed() {
        boolean handled = false;
        try {
            List fragmentList = getSupportFragmentManager().getFragments();
            for (Object f : fragmentList) {
                if (f instanceof BaseFragment) {
                    handled = ((BaseFragment) f).onHomePressed();
                    if (handled)
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!handled) {
            mDrawerLayout.openDrawer(Gravity.START);
        }
    }

    private boolean isHomeFragmentOpen() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fl_main_root);
        if (GuardaApp.isTransactionsEmpty && currentFragment instanceof UserWalletFragment) {
            return true;
        } else if (!GuardaApp.isTransactionsEmpty && currentFragment instanceof TransactionHistoryFragment) {
            return true;
        }
        return false;
    }

    private void goToHomeScreen() {
        if (GuardaApp.isTransactionsEmpty) {
            goToUserWallet();
        } else {
            goToTransactionHistory();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        compositeDisposable.clear();
    }

}