package com.guarda.ethereum.views.fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.lifecycle.HistoryViewModel;
import com.guarda.ethereum.managers.CryptocompareHelper;
import com.guarda.ethereum.managers.NetworkManager;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.TransactionsManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.items.BtgBalanceResponse;
import com.guarda.ethereum.models.items.RespExch;
import com.guarda.ethereum.models.items.TokenBodyItem;
import com.guarda.ethereum.models.items.TokenHeaderItem;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.RequestorBtc;
import com.guarda.ethereum.screens.exchange.first.ExchangeFragment;
import com.guarda.ethereum.views.activity.MainActivity;
import com.guarda.ethereum.views.activity.TransactionDetailsActivity;
import com.guarda.ethereum.views.adapters.TokenAdapter;
import com.guarda.ethereum.views.adapters.TransHistoryAdapter;
import com.guarda.ethereum.views.fragments.base.BaseFragment;
import com.guarda.zcash.sapling.SyncManager;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.rxcall.CallSaplingBalance;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import org.bitcoinj.core.Coin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.guarda.ethereum.models.constants.Common.BLOCK;
import static com.guarda.ethereum.models.constants.Common.EXTRA_TRANSACTION_POSITION;
import static com.guarda.ethereum.models.constants.Extras.CREATE_WALLET;
import static com.guarda.ethereum.models.constants.Extras.FIRST_ACTION_MAIN_ACTIVITY;
import static com.guarda.ethereum.models.constants.Extras.KEY;

@AutoInjector(GuardaApp.class)
public class TransactionHistoryFragment extends BaseFragment {

    @BindView(R.id.tv_wallet_count)
    TextView tvCryptoCount;
    @BindView(R.id.tv_wallet_usd_count)
    TextView tvUSDCount;
    @BindView(R.id.fab_menu)
    FloatingActionMenu fabMenu;
    @BindView(R.id.fab_purchase)
    FloatingActionButton fabPurchase;
    @BindView(R.id.fab_deposit)
    FloatingActionButton fabDeposit;
    @BindView(R.id.fab_withdraw)
    FloatingActionButton fabWithdraw;
    @BindView(R.id.tv_sync_status)
    TextView tv_sync_status;
    @BindView(R.id.iv_update_transactions)
    ImageView tvUpdateTransactions;
    @BindView(R.id.rv_transactions_list)
    RecyclerView rvTransactionsList;
    @BindView(R.id.sv_main_scroll_layout)
    NestedScrollView nsvMainScrollLayout;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rv_tokens)
    RecyclerView rvTokens;

    private boolean isVisible = true;
    private ObjectAnimator loaderAnimation;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private HistoryViewModel historyViewModel;
    private TransHistoryAdapter adapter;
    private TokenAdapter tokenAdapter;
    private List<TokenBodyItem> tokensList = new ArrayList<>();
    private String exchangeRate;
    private final String tAddrTitle = "T-address";
    private final String zAddrTitle = "Z-address";
    private Long transparentBalance = 0L;
    private Long saplingBalance = 0L;
    private boolean isUpdating = false;

    @Inject
    WalletManager walletManager;
    @Inject
    TransactionsManager transactionsManager;
    @Inject
    SharedManager sharedManager;
    @Inject
    SyncManager syncManager;
    @Inject
    DbManager dbManager;

    public TransactionHistoryFragment() {
        GuardaApp.getAppComponent().inject(this);
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_transaction_history;
    }

    @Override
    protected void init() {
        HistoryViewModel.Factory factory = new HistoryViewModel.Factory(walletManager, transactionsManager, dbManager, syncManager);
        historyViewModel = ViewModelProviders.of(this, factory).get(HistoryViewModel.class);
        subscribeUi();

        initTransactionHistoryRecycler();

        tokensList.add(new TokenBodyItem(tAddrTitle, new BigDecimal("0"), "0", 8));
        tokensList.add(new TokenBodyItem(zAddrTitle, new BigDecimal("0"), "0", 8));
        initTokens(tokensList);

        nsvMainScrollLayout.smoothScrollTo(0, 0);

        fabMenu.setClosedOnTouchOutside(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initFabHider();
        }

        initRotation(tvUpdateTransactions);
        initMenuButton();

        swipeRefreshLayout.setOnRefreshListener(this::updBalanceHistSync);

        String firstAction = null;
        if (getArguments() != null) {
            firstAction = getArguments().getString(FIRST_ACTION_MAIN_ACTIVITY);
        }
        if (firstAction != null && firstAction.equalsIgnoreCase(CREATE_WALLET)) {
            if (TextUtils.isEmpty(walletManager.getWalletFriendlyAddress())) {
                createWallet(BLOCK);
            }
        } else {
            checkFromRestore();
        }

        updBalanceHistSync();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initFabHider() {
        nsvMainScrollLayout.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY) {
                    fabMenu.setVisibility(View.GONE);
                } else {
                    fabMenu.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void initTokens(List<TokenBodyItem> tokens) {
        Timber.d("initTokens start");
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        RecyclerView.ItemAnimator animator = rvTokens.getItemAnimator();
        if (animator instanceof DefaultItemAnimator) {
            ((DefaultItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        tokenAdapter = new TokenAdapter(generateTokensGroup(tokens));
        rvTokens.setLayoutManager(layoutManager);
        rvTokens.setAdapter(tokenAdapter);
        Timber.d("initTokens end");
    }

    private List<? extends ExpandableGroup> generateTokensGroup(List<TokenBodyItem> tokenBodyItems) {
        String h = "";
        if (isAdded()) h = getString(R.string.own_addresses);
        return Arrays.asList(
                new TokenHeaderItem(h, tokenBodyItems, "2")
        );
    }

    private void updBalanceHistSync() {
        if (isWalletExist()) {
            showBalanceHistory();
            historyViewModel.startSync();
        }
    }

    private void createWallet(String passphrase) {
        showProgress(getStringIfAdded(R.string.generating_wallet));
        walletManager.createWallet(passphrase, () -> {
                closeProgress();
                openUserWalletFragment();
        });
    }

    private void updateFromDbOrEmpty() {
        if (transactionsManager.getTransactionsList().size() == 0 &&
                transparentBalance == 0L &&
                saplingBalance == 0L) {
            GuardaApp.isTransactionsEmpty = true;
            openUserWalletFragment();
        } else {
            GuardaApp.isTransactionsEmpty = false;
            historyViewModel.getTxsFromDb();
        }
    }

    private void openUserWalletFragment() {
        navigateToFragment(new UserWalletFragment());
    }

    private boolean isWalletExist() {
        return !TextUtils.isEmpty(walletManager.getWalletFriendlyAddress());
    }

    private void showBalanceHistory() {
        if (isAdded() && !isDetached() && isVisible && NetworkManager.isOnline(getActivity())) {
            //check if history updating is in progress
            if (isUpdating) return;
            isUpdating = true;
            swipeRefreshLayout.setRefreshing(true);
            loadBalance();
            historyViewModel.loadTransactions();
        } else {
            if (getActivity() != null) {
                ((MainActivity) getActivity()).showCustomToast(getStringIfAdded(R.string.err_network), R.drawable.err_network);
            }
        }
    }

    private void loadBalance() {
        loadTransparentBalance();
        loadShieldedBalance();
    }

    private void loadTransparentBalance() {
        RequestorBtc.getBalanceZecNew(walletManager.getWalletFriendlyAddress(), new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                BtgBalanceResponse balance = (BtgBalanceResponse) response;
                transparentBalance = balance.getBalanceSat();
                walletManager.setMyBalance(balance.getBalanceSat());
                walletManager.setBalance(balance.getBalanceSat());
                String curBalance = WalletManager.getFriendlyBalance(walletManager.getMyBalance());
                setCryptoBalance();
                tokensList.set(0, new TokenBodyItem(tAddrTitle, new BigDecimal(curBalance), curBalance, 8));
                tokenAdapter.notifyItemChanged(0);
                getLocalBalance();
            }

            @Override
            public void onFailure(String msg) {
                if (getActivity() != null) {
                    ((MainActivity) getActivity()).showCustomToast(getStringIfAdded(R.string.err_get_balance), R.drawable.err_balance);
                }
            }
        });
    }

    private void loadShieldedBalance() {
        compositeDisposable.add(Observable
                .fromCallable(new CallSaplingBalance(dbManager))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((balance) -> {
                    Timber.d("CallSaplingBalance balance=%d", balance);

                    if (balance == null) return;
                    saplingBalance = balance;
                    setCryptoBalance();
                    tokensList.set(1, new TokenBodyItem(zAddrTitle, new BigDecimal(Coin.valueOf(balance).toPlainString()), Coin.valueOf(balance).toPlainString(), 8));
                    tokenAdapter.notifyItemChanged(1);
                }));
    }

    private void getLocalBalance() {
        CryptocompareHelper.getExchange(Common.MAIN_CURRENCY.toUpperCase(),
                sharedManager.getLocalCurrency().toUpperCase(),
                new ApiMethods.RequestListener() {
                    @Override
                    public void onSuccess(Object response) {
                        RespExch exchange = (RespExch) response;

                        exchangeRate = exchange.getPrice(sharedManager.getLocalCurrency().toLowerCase());
                        if (exchangeRate != null) updateUsdBalances();
                        setUSDBalance();
                    }

                    @Override
                    public void onFailure(String msg) {
                        Timber.e("CryptocompareHelper.getExchange onFailure=%s", msg);
                    }
                });
    }

    private void updateUsdBalances() {
        for (TokenBodyItem tb : tokensList) {
            if (tb.getTokenNum() == null ||
                    tb.getTokenNum().compareTo(BigDecimal.ZERO) == 0) continue;

            Double res = Double.valueOf(tb.getTokenNum().toString()) * (Double.valueOf(exchangeRate));
            tb.setOtherSum(res);
        }
        tokenAdapter.notifyDataSetChanged();
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void checkFromRestore() {
        Bundle args = getArguments();
        if (args != null) {
            String key = args.getString(KEY);
            if (!TextUtils.isEmpty(key)) {
                showProgress(getStringIfAdded(R.string.restoring_wallet));
                historyViewModel.restoreWallet(key);
            }
        }
    }

    private void initTransactionHistoryRecycler() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvTransactionsList.setLayoutManager(layoutManager);

        adapter = new TransHistoryAdapter();
        adapter.setItemClickListener((txItem) -> {
            transactionsManager.setTxDeatailsItem(txItem);
            Intent detailsIntent = new Intent(getActivity(), TransactionDetailsActivity.class);
            detailsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(detailsIntent);
            getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.no_slide);
        });

        rvTransactionsList.setAdapter(adapter);
    }

    private void setCryptoBalance() {
        Long tb = 0L;
        Long zb = 0L;
        if (transparentBalance != null) tb = transparentBalance;
        if (saplingBalance != null) zb = saplingBalance;

        long sum = tb + zb;

        tvCryptoCount.setText(String.format(Locale.US,
                "%s %s",
                Coin.valueOf(sum).toPlainString(),
                sharedManager.getCurrentCurrency().toUpperCase()));
    }

    private void setUSDBalance() {
        Long tb = 0L;
        Long zb = 0L;
        if (transparentBalance != null) tb = transparentBalance;
        if (saplingBalance != null) zb = saplingBalance;

        long sum = tb + zb;

        double res = Double.valueOf(Coin.valueOf(sum).toPlainString()) * (Double.valueOf(exchangeRate));
        tvUSDCount.setText(String.format("%s %s", Double.toString(round(res, 2)), sharedManager.getLocalCurrency().toUpperCase()));

    }

    @OnClick({R.id.fab_purchase, R.id.fab_withdraw, R.id.fab_deposit})
    public void fabButtonsClick(View view) {
        MainActivity mainActivity = (MainActivity) getActivity();
        switch (view.getId()) {
            case R.id.fab_purchase:
                fabMenu.close(true);
                navigateToFragment(new ExchangeFragment());
                mainActivity.setToolBarTitle(R.string.purchase_purchase);
                break;
            case R.id.fab_withdraw:
                fabMenu.close(true);
                navigateToFragment(new WithdrawFragment());
                mainActivity.setToolBarTitle(R.string.withdraw_address_send);
                break;
            case R.id.fab_deposit:
                fabMenu.close(true);
                navigateToFragment(new DepositFragment());
                mainActivity.setToolBarTitle(R.string.app_your_address);
                break;
        }
    }

    private void subscribeUi() {
        historyViewModel.getShowHistory().observe(getViewLifecycleOwner(), (v) -> {
            if (v) {
                isUpdating = false;
                updateFromDbOrEmpty();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        historyViewModel.getShowTxError().observe(getViewLifecycleOwner(), (v) -> {
            if (v) {
                isUpdating = false;
                swipeRefreshLayout.setRefreshing(false);
                ((MainActivity) getActivity()).showCustomToast(getStringIfAdded(R.string.err_get_history), R.drawable.err_history);
            }
        });

        historyViewModel.getShowActualTxs().observe(getViewLifecycleOwner(), (list) -> {
            adapter.updateList(list);
            adapter.notifyDataSetChanged();
        });

        historyViewModel.getSyncInProgress().observe(getViewLifecycleOwner(), (t) -> {
            setSyncStatus(t);
            Timber.d("getSyncInProgress().observe t=%b", t);
        });

        historyViewModel.getSyncPhaseStatus().observe(getViewLifecycleOwner(), (t) -> {
            setSyncStatus(t);
            Timber.d("getSyncPhaseStatus().observe t=%s", t);
        });

        historyViewModel.getUpdateBalance().observe(getViewLifecycleOwner(), (t) -> {
            if (t) loadShieldedBalance();
            Timber.d("getUpdateBalance().observe t=%b", t);
        });

        historyViewModel.setCurrentStatus();

        historyViewModel.getIsRestored().observe(getViewLifecycleOwner(), (t) -> {

            closeProgress();
            updBalanceHistSync();

            Timber.d("getIsRestored().observe t=%b", t);
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        isVisible = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isVisible = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        compositeDisposable.clear();
    }

    private void setSyncStatus(boolean b) {
        if (b)
            startClockwiseRotation();
        else
            loaderAnimation.cancel();
    }

    private void setSyncStatus(String phase) {
        tv_sync_status.setText(phase);
    }

    private void initRotation(ImageView ivLoader) {
        if (loaderAnimation == null) {
            loaderAnimation = ObjectAnimator.ofFloat(ivLoader, "rotation", 0.0f, 360f);
            loaderAnimation.setDuration(1500);
            loaderAnimation.setRepeatCount(ObjectAnimator.INFINITE);
            loaderAnimation.setInterpolator(new LinearInterpolator());
            loaderAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationCancel(animation);
                    tvUpdateTransactions.setClickable(true);
                }
            });
        }
    }

    private void startClockwiseRotation() {
        if (!loaderAnimation.isRunning()) {
            loaderAnimation.start();
        }
    }

    public void scrollToTop() {
        nsvMainScrollLayout.scrollTo(0, 0);
    }

    private String getStringIfAdded(int resId) {
        if (isAdded()) {
            return getString(resId);
        } else {
            return "";
        }
    }

}
