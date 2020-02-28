package com.guarda.ethereum.views.fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.widget.NestedScrollView;
import androidx.core.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.DefaultItemAnimator;
import androidx.appcompat.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.customviews.RateDialog;
import com.guarda.ethereum.managers.Callback;
import com.guarda.ethereum.managers.CoinmarketcapHelper;
import com.guarda.ethereum.managers.EthereumNetworkManager;
import com.guarda.ethereum.managers.GastrackerHelper;
import com.guarda.ethereum.managers.NetworkManager;
import com.guarda.ethereum.managers.RawNodeManager;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.TransactionsManager;
import com.guarda.ethereum.managers.WalletCreationCallback;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.items.RespExch;
import com.guarda.ethereum.models.items.TokenBodyItem;
import com.guarda.ethereum.models.items.TokenHeaderItem;
import com.guarda.ethereum.models.items.TransactionResponse;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;
import com.guarda.ethereum.utils.CalendarHelper;
import com.guarda.ethereum.utils.CurrencyUtils;
import com.guarda.ethereum.views.activity.MainActivity;
import com.guarda.ethereum.views.activity.TransactionDetailsActivity;
import com.guarda.ethereum.views.adapters.TokenAdapter;
import com.guarda.ethereum.views.adapters.TransHistoryAdapter;
import com.guarda.ethereum.views.fragments.base.BaseFragment;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import org.web3j.crypto.WalletFile;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.ResponseBody;

import static com.guarda.ethereum.models.constants.Common.BLOCK;
import static com.guarda.ethereum.models.constants.Common.ETH_SHOW_PATTERN;
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
    @BindView(R.id.fab_buy)
    FloatingActionButton fabBuy;
    @BindView(R.id.fab_purchase)
    FloatingActionButton fabPurchase;
    @BindView(R.id.fab_deposit)
    FloatingActionButton fabDeposit;
    @BindView(R.id.fab_withdraw)
    FloatingActionButton fabWithdraw;
    @BindView(R.id.iv_update_transactions)
    ImageView tvUpdateTransactions;
    @BindView(R.id.rv_transactions_list)
    RecyclerView rvTransactionsList;
    @BindView(R.id.sv_main_scroll_layout)
    NestedScrollView nsvMainScrollLayout;
    @BindView(R.id.rv_tokens)
    RecyclerView rvTokens;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefreshLayout;

    private String curBalance = "...";
    private boolean isVisible = true;
    private boolean stronglyHistory = false;
    private TokenAdapter tokenAdapter;
    private List<TokenBodyItem> tokensList = new ArrayList<>();
    private ProgressDialog tokensProgressDialog;
    private int tokenRequestCount = 0;
    private ObjectAnimator loaderAnimation;

    @Inject
    WalletManager walletManager;

    @Inject
    EthereumNetworkManager networkManager;

    @Inject
    TransactionsManager transactionsManager;

    @Inject
    SharedManager sharedManager;

    @Inject
    RawNodeManager mNodeManager;

    public TransactionHistoryFragment() {
        GuardaApp.getAppComponent().inject(this);
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_transaction_history;
    }

    @Override
    protected void init() {
        nsvMainScrollLayout.smoothScrollTo(0, 0);
        setCryptoBalance(curBalance);

        setUSDBalance("...");

        fabMenu.setClosedOnTouchOutside(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initFabHider();
        }

        initRotation(tvUpdateTransactions);
        initMenuButton();

        swipeRefreshLayout.setProgressViewEndTarget(false, -2000);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                updateData(false);
            }
        });

        String firstAction = null;
        if (getArguments() != null) {
            firstAction = getArguments().getString(FIRST_ACTION_MAIN_ACTIVITY);
        }
        if (firstAction != null && firstAction.equalsIgnoreCase(CREATE_WALLET)) {
            if (TextUtils.isEmpty(walletManager.getWalletFriendlyAddress())) {
                createWallet(BLOCK);
            }
        } else {
            if (firstAction != null && firstAction.equalsIgnoreCase(Extras.SHOW_STRICTLY_HISTORY)) {
                stronglyHistory = true;
            }
            checkFromRestore();
            if (isWalletExist()) {
                updateData(true);
                askRating();
            }
        }

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
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        RecyclerView.ItemAnimator animator = rvTokens.getItemAnimator();
        if (animator instanceof DefaultItemAnimator) {
            ((DefaultItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        tokenAdapter = new TokenAdapter(generateTokensGroup(tokens));
        rvTokens.setLayoutManager(layoutManager);
        rvTokens.setAdapter(tokenAdapter);
    }

    private List<? extends ExpandableGroup> generateTokensGroup(List<TokenBodyItem> tokenBodyItems) {
        return Arrays.asList(
                new TokenHeaderItem(getString(R.string.tokens), tokenBodyItems, getTokenHeaderSum(tokenBodyItems))
        );
    }

    private String getTokenHeaderSum(List<TokenBodyItem> tokens) {
        Double res = 0.0;
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i) != null)
                if (tokens.get(i).getOtherSum() >= 0) {
                    res = res + tokens.get(i).getOtherSum();
                }
        }

        return String.format("%s %s", Double.toString(round(res, 2)), sharedManager.getLocalCurrency().toUpperCase());
    }

    private void showTokens() {
        if (isShouldSyncTokensInfo()) {
            Requestor.getTokensInfo(new ApiMethods.RequestListener() {
                @Override
                public void onSuccess(Object response) {
                    if (isAdded() && !isDetached() && isVisible) {
                        try {
                            String tokensJson = ((ResponseBody) response).string();
                            sharedManager.setTokensInfo(tokensJson);
                            sharedManager.setTokenUpdateDate(System.currentTimeMillis());
                            loadTokens();
                        } catch (IOException | ClassCastException e) {
                            FirebaseCrash.logcat(Log.ERROR, "showTokens", e.toString());
                            FirebaseCrash.report(e);
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(String msg) {
                    if (isAdded() && !isDetached() && isVisible) {
                        ((MainActivity) getActivity()).showCustomToast(getString(R.string.err_tokens), R.drawable.err_tokens);
                        closeProgress();
                    }
                }
            });
        } else {
            loadTokens();
        }

    }

    private void loadTokens() {
        tokensList.clear();
        mNodeManager.getTokens(tokens -> {
                if (isAdded() && !isDetached() && isVisible) {
                    tokensList = tokens;
                    if (!TransactionHistoryFragment.this.isDetached() || isFragmentVisible) {
                        assembleAllTokensCourse();
                    }
                }
        });
    }

    private boolean isShouldSyncTokensInfo() {
        return sharedManager.getTokensInfo().isEmpty() || !isTodayTokenSynced();

    }

    private boolean isTodayTokenSynced() {
        String lastSyncTime = CalendarHelper.parseDateToddMMyyyy(sharedManager.getTokenUpdateDate());
        String currentTime = CalendarHelper.parseDateToddMMyyyy(System.currentTimeMillis());
        return lastSyncTime.equals(currentTime);
    }

    private void assembleAllTokensCourse() {
        HashMap<String, String> map = CurrencyUtils.getCurrencyNameByCode(getContext());
        if (tokenRequestCount < tokensList.size()) {
            String fullTokenName = map.get(tokensList.get(tokenRequestCount).getTokenName());
            if (fullTokenName != null) {
                fullTokenName = fullTokenName.trim();
                CoinmarketcapHelper.getExchange(fullTokenName,
                        sharedManager.getLocalCurrency().toLowerCase(),
                        new ApiMethods.RequestListener() {
                            @Override
                            public void onSuccess(Object response) {
                                if (isAdded() && !isDetached() && isVisible) {
                                    List<RespExch> exchange = (List<RespExch>) response;
                                    if (exchange.get(0).getPrice(sharedManager.getLocalCurrency().toLowerCase()) != null) {
                                        try {
                                            String localBalance = tokensList.get(tokenRequestCount).getTokenNum().toString().replace(",", "");
                                            Double res = Double.valueOf(localBalance) * (Double.valueOf(exchange.get(0).getPrice(sharedManager.getLocalCurrency().toLowerCase())));
                                            tokensList.get(tokenRequestCount).setOtherSum(round(res, 2));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        tokensList.get(tokenRequestCount).setOtherSum(-1.0);
                                    }
                                    tokenRequestCount++;
                                    assembleAllTokensCourse();
                                }
                            }

                            @Override
                            public void onFailure(String msg) {
                                FirebaseCrash.log(msg);
                                Log.d("psd", "assembleAllTokensCourse() - " + msg);
                            }
                        });
            } else {
                tokenRequestCount++;
                assembleAllTokensCourse();
            }
        } else {
            initTokens(tokensList);
            closeProgress();
            tokenRequestCount = 0;
        }
    }

    private void createWallet(String passphrase) {
        if (isAdded()) {
            showProgress(getString(R.string.generating_wallet));
        }
        walletManager.createWallet(passphrase, new WalletCreationCallback() {
            @Override
            public void onWalletCreated(WalletFile walletFile) {
                closeProgress();
                openUserWalletFragment();
            }
        });
    }

    private void showTransactions(boolean cache) {
        if (cache)
            loadFromCache();

        loadTransactions();
    }

    private void loadFromCache() {
        String jsonFromPref = sharedManager.getTxsCache();
        if (!jsonFromPref.equalsIgnoreCase("")) {
            Gson gson = new Gson();
            Type listType = new TypeToken<Map<String, ArrayList<TransactionResponse>>>(){}.getType();
            Map<String, ArrayList<TransactionResponse>> addrTxsMap = gson.fromJson(jsonFromPref, listType);
            ArrayList<TransactionResponse> txList = addrTxsMap.get(walletManager.getWalletFriendlyAddress());
            if (txList != null) {
                transactionsManager.setTransactionsList(txList);
                initTransactionHistoryRecycler();
            }
        }
    }

    private void loadTransactions() {
        startClockwiseRotation();
        //EtcchainHelper.getTransactions(walletManager.getWalletFriendlyAddress(), new ApiMethods.RequestListener() {
        GastrackerHelper.getTransactions(walletManager.getWalletFriendlyAddress(), new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                if (isAdded() && !isDetached() && isVisible) {
                    LinkedTreeMap respTree = (LinkedTreeMap)response;
                    ArrayList items = (ArrayList)respTree.get("items");
                    List<TransactionResponse> transactionsList = new ArrayList<>();
                    for (Object itObj : items) {
                        LinkedTreeMap itMap = (LinkedTreeMap)itObj;
                        TransactionResponse tr = new TransactionResponse();
                        tr.setConfirmations(Long.valueOf(((Double)itMap.get("confirmations")).longValue()).toString());
                        tr.setFrom((String)itMap.get("from"));
                        tr.setTo((String)itMap.get("to"));
                        LinkedTreeMap value = (LinkedTreeMap) itMap.get("value");
                        //tr.setValue( BigDecimal.valueOf((Double)value.get("ether")).toPlainString() );
                        tr.setValue( (String)value.get("hex") );
                        tr.setHash((String)itMap.get("hash"));
                        tr.setTimeStamp("1000000000");

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                        try {
                            tr.setTimeStamp(Long.valueOf(dateFormat.parse((String)itMap.get("timestamp")).getTime()/1000).toString());
                        } catch (Exception e) {}

                        transactionsList.add(tr);
                    }
                    transactionsManager.setTransactionsList(transactionsList);
                    initTransactionHistoryRecycler();
                    loaderAnimation.cancel();
                    if (transactionsList.size() == 0) {
                        GuardaApp.isTransactionsEmpty = true;
                        openUserWalletFragment();
                    } else {
                        GuardaApp.isTransactionsEmpty = false;
                        updateTxsCache(transactionsList);
                    }
                }
            }
//            public void onSuccessBak(Object response) {
//                if (isAdded() && !isDetached() && isVisible) {
//                    List<TransactionResponse> transactionsList = (List<TransactionResponse>) response;
//                    transactionsManager.setTransactionsList(transactionsList);
//                    initTransactionHistoryRecycler();
//                    loaderAnimation.cancel();
//                    if (transactionsList.size() == 0) {
//                        GuardaApp.isTransactionsEmpty = true;
//                        openUserWalletFragment();
//                    } else {
//                        GuardaApp.isTransactionsEmpty = false;
//                        updateTxsCache(transactionsList);
//                    }
//                }
//            }

            @Override
            public void onFailure(String msg) {
                if (isAdded() && !isDetached() && isVisible) {
                    FirebaseCrash.logcat(Log.ERROR, "getTransactions", msg);
                    loaderAnimation.cancel();
                    ((MainActivity) getActivity()).showCustomToast(getString(R.string.err_get_history), R.drawable.err_history);
                }
            }
        });
    }

    private void openUserWalletFragment() {
        if (!stronglyHistory) {
            navigateToFragment(new UserWalletFragment());
        }
    }

    private boolean isWalletExist() {
        return !TextUtils.isEmpty(walletManager.getWalletFriendlyAddress());
    }

    private void showBalance() {
        networkManager.getBalance(walletManager.getWalletFriendlyAddress(), new Callback<BigDecimal>() {
            @Override
            public void onResponse(BigDecimal balance) {
                if (isAdded() && !isDetached() && isVisible) {
                    if (balance != null) {
                        try {
                            walletManager.setBalance(balance);
                            DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
                            symbols.setDecimalSeparator('.');
                            symbols.setGroupingSeparator(',');
                            DecimalFormat decimalFormat = new DecimalFormat(ETH_SHOW_PATTERN, symbols);
                            decimalFormat.setRoundingMode(RoundingMode.DOWN);
                            curBalance = decimalFormat.format(balance);
                            setCryptoBalance(curBalance);
                            getLocalBalance(curBalance);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (isAdded() && !isDetached()) {
                            ((MainActivity) getActivity()).showCustomToast(getString(R.string.err_get_balance), R.drawable.err_balance);
                        }
                    }
                }
            }
        });
    }

    private void getLocalBalance(final String balance) {
        CoinmarketcapHelper.getExchange(Common.MAIN_CURRENCY_NAME,
                sharedManager.getLocalCurrency().toLowerCase(),
                new ApiMethods.RequestListener() {
                    @Override
                    public void onSuccess(Object response) {
                        if (isAdded() && !isDetached() && isVisible) {
                            List<RespExch> exchange = (List<RespExch>) response;
                            try {
                                String localBalance = balance.replace(",", ".");

                                if (exchange.get(0).getPrice(sharedManager.getLocalCurrency().toLowerCase()) != null) {
                                    Double res = Double.valueOf(localBalance) * (Double.valueOf(exchange.get(0).getPrice(sharedManager.getLocalCurrency().toLowerCase())));
                                    setUSDBalance(Double.toString(round(res, 2)));
                                } else {
                                    setUSDBalance("...");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(String msg) {
                    }
                });
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
                showProgress(getString(R.string.restoring_wallet));
                walletManager.restoreFromBlock(key, new WalletCreationCallback() {
                    @Override
                    public void onWalletCreated(WalletFile walletFile) {
                        closeProgress();
                        if (isVisible) {
                            startClockwiseRotation();
                            updateData(true);
                        }
                    }
                });
            }
        }
    }

    private void initTransactionHistoryRecycler() {
        TransHistoryAdapter adapter = new TransHistoryAdapter();
        new Handler();
        adapter.setItemClickListener(new TransHistoryAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position) {
                Intent detailsIntent = new Intent(getActivity(), TransactionDetailsActivity.class);
                detailsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                detailsIntent.putExtra(EXTRA_TRANSACTION_POSITION, position);
                startActivity(detailsIntent);
                getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.no_slide);
            }
        });

        adapter.setOnDataChanged(new Runnable() {
            @Override
            public void run() {
                try {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showBalance();
                            showTokens();
                        }
                    });
                } catch (Exception e) {
                }
            }
        });

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        rvTransactionsList.setLayoutManager(layoutManager);
        rvTransactionsList.setAdapter(adapter);

    }

    private void setCryptoBalance(String balance) {
        tvCryptoCount.setText(String.format(Locale.US, "%s " + sharedManager.getCurrentCurrency().toUpperCase(), balance));
    }

    private void setUSDBalance(String balance) {
        tvUSDCount.setText(String.format("%s %s", balance, sharedManager.getLocalCurrency().toUpperCase()));
    }

    @OnClick({R.id.fab_buy, R.id.fab_purchase, R.id.fab_withdraw, R.id.fab_deposit})
    public void fabButtonsClick(View view) {
        MainActivity mainActivity = (MainActivity) getActivity();
        switch (view.getId()) {
            case R.id.fab_buy:
                fabMenu.close(true);
                navigateToFragment(new PurchaseServiceFragment());
                break;
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

    @OnClick(R.id.iv_update_transactions)
    public void onUpdateClick() {
        updateData(false);
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

    private void updateData(boolean withCache) {
        if (isAdded() && !isDetached() && isVisible && NetworkManager.isOnline(getActivity())) {
            showTransactions(withCache);
            showBalance();
            showTokens();
        } else {
            loaderAnimation.cancel();
            ((MainActivity) getActivity()).showCustomToast(getString(R.string.err_network), R.drawable.err_network);
        }

    }

    private void updateTxsCache(List<TransactionResponse> transactionsList) {
        if (walletManager.getWalletFriendlyAddress() == null || walletManager.getWalletFriendlyAddress().equalsIgnoreCase(""))
            return;

        Gson gson = new Gson();
        Type listType = new TypeToken<Map<String, ArrayList<TransactionResponse>>>(){}.getType();
        String jsonFromPref = sharedManager.getTxsCache();
        Map<String, ArrayList<TransactionResponse>> addrTxsMap = gson.fromJson(jsonFromPref, listType);

        if (addrTxsMap == null)
            addrTxsMap = new HashMap<>();

        addrTxsMap.put(walletManager.getWalletFriendlyAddress(), (ArrayList<TransactionResponse>) transactionsList);
        String jsonToPref = gson.toJson(addrTxsMap);
        sharedManager.setTxsCache(jsonToPref);
    }

    private void askRating() {
        if (!sharedManager.getIsAskRate() || transactionsManager.getTransactionsList().size() < 3) {
            return;
        }

        int nextShowRate = sharedManager.getNextShowRate();
        int nowCount = 0;
        for (TransactionResponse tr : transactionsManager.getTransactionsList()) {
            if (tr.getConfirmations() != null && Integer.parseInt(tr.getConfirmations()) >= TransHistoryAdapter.MIN_CONFIRMATIONS) {
                nowCount++;
            }
        }

        if (nextShowRate == 0) {
            sharedManager.setNextShowRate(nowCount + 3);
            return;
        } else if (nowCount >= nextShowRate) {
            if (sharedManager.getIsWaitFourTr()) {
                showRateDialog(nowCount + 4);
                sharedManager.setIsWaitFourTr(false);
            } else {
                showRateDialog(nowCount + 7);
            }
        }
    }

    private void showRateDialog(int inc) {
        if (isAdded()) {
            RateDialog rateDialog = new RateDialog();
            rateDialog.show(getActivity().getFragmentManager(), "RateDialog");
            sharedManager.setNextShowRate(inc);
        }
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

}
