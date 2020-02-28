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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.guarda.ethereum.BuildConfig;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.customviews.RateDialog;
import com.guarda.ethereum.managers.Callback;
import com.guarda.ethereum.managers.CoinmarketcapHelper;
import com.guarda.ethereum.managers.EthereumNetworkManager;
import com.guarda.ethereum.managers.EtherscanHelper;
import com.guarda.ethereum.managers.EthplorerHelper;
import com.guarda.ethereum.managers.NetworkManager;
import com.guarda.ethereum.managers.RawNodeManager;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.TransactionsManager;
import com.guarda.ethereum.managers.WalletCreationCallback;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.items.AddressInfoResp;
import com.guarda.ethereum.models.items.RespExch;
import com.guarda.ethereum.models.items.TokenBodyItem;
import com.guarda.ethereum.models.items.TokenHeaderItem;
import com.guarda.ethereum.models.items.TokenListItem;
import com.guarda.ethereum.models.items.TokenTxResponse;
import com.guarda.ethereum.models.items.TokensTxListResponse;
import com.guarda.ethereum.models.items.TransactionResponse;
import com.guarda.ethereum.models.items.TransactionsListResponse;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;
import com.guarda.ethereum.utils.CalendarHelper;
import com.guarda.ethereum.utils.CurrencyUtils;
import com.guarda.ethereum.utils.TokensUtils;
import com.guarda.ethereum.views.activity.MainActivity;
import com.guarda.ethereum.views.activity.TransactionDetailsActivity;
import com.guarda.ethereum.views.adapters.TokenAdapter;
import com.guarda.ethereum.views.adapters.TransHistoryAdapter;
import com.guarda.ethereum.views.fragments.base.BaseFragment;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import org.web3j.crypto.WalletFile;
import org.web3j.utils.Convert;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

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
    private HashMap<String, String> mapTickers = new HashMap<>();
    private HashMap<String, String> mapCurrencyNameByCode = new HashMap<>();
    HashMap<String, TokenListItem> mapContractToken = new HashMap<>();
    private BigInteger blockHeight;

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

    private TransHistoryAdapter adapter;

    public TransactionHistoryFragment() {
        GuardaApp.getAppComponent().inject(this);
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_transaction_history;
    }

    @Override
    protected void init() {
        stronglyHistory = true;

        nsvMainScrollLayout.smoothScrollTo(0, 0);
        setCryptoBalance(curBalance);

        setUSDBalance("...");

        fabMenu.setClosedOnTouchOutside(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initFabHider();
        }

        initRotation(tvUpdateTransactions);
        initMenuButton();
        rvTransactionsList.getItemAnimator().setChangeDuration(0);

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
            if (sharedManager.getTokensInfo().length() > 0) {
                initTokensMapping();
            }
//            checkFromRestore();
            if (isWalletExist()) {
                updateData(true);
                askRating();
            } else {
                checkFromRestore();
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
        Log.d("psd", "initTokens start");
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        RecyclerView.ItemAnimator animator = rvTokens.getItemAnimator();
        if (animator instanceof DefaultItemAnimator) {
            ((DefaultItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        tokenAdapter = new TokenAdapter(generateTokensGroup(tokens));
        rvTokens.setLayoutManager(layoutManager);
        rvTokens.setAdapter(tokenAdapter);
        Log.d("psd", "initTokens end");
    }

    private List<? extends ExpandableGroup> generateTokensGroup(List<TokenBodyItem> tokenBodyItems) {
        String h = "";
        if (isAdded()) h = getString(R.string.tokens);
        return Arrays.asList(
                new TokenHeaderItem(h, tokenBodyItems, getTokenHeaderSum(tokenBodyItems))
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
        Log.d("psd", "showTokens start");
        if (isShouldSyncTokensInfo()) {
            getTokensInfo(true);
//            String mapFromPref = sharedManager.getMapTokens();
//            if (mapFromPref.isEmpty()) {
//
//            } else {
//                mapContractToken = TokensUtils.getMapContractToken(walletManager.getWalletFriendlyAddress(), mapFromPref);
//                fillTokensTickersValue(transactionsManager.getTransactionsList());
//                loadTokens();
//            }

//            Requestor.getTokensInfo(new ApiMethods.RequestListener() {
//                @Override
//                public void onSuccess(Object response) {
//                    if (isAdded() && !isDetached() && isVisible) {
//                        try {
//                            String tokensJson = ((ResponseBody) response).string();
////                            initTokensMapping();
//                            sharedManager.setTokensInfo(tokensJson);
//                            sharedManager.setTokenUpdateDate(System.currentTimeMillis());
//                            loadTokens();
//                        } catch (IOException e) {
//                            FirebaseCrash.report(e);
//                            FirebaseCrash.logcat(Log.ERROR, "getTokensInfo", e.toString());
//                            e.printStackTrace();
//                        }
//                        Log.d("psd", "showTokens onSuccess");
//                    }
//                }
//
//                @Override
//                public void onFailure(String msg) {
//                    if (isAdded() && !isDetached() && isVisible) {
//                        loaderAnimation.cancel();
//                        ((MainActivity) getActivity()).showCustomToast(getString(R.string.err_tokens), R.drawable.err_tokens);
//                    }
//                }
//            });
        } else {
            if (mapContractToken == null || mapContractToken.size() == 0) {
                mapContractToken = TokensUtils.getMapContractToken(sharedManager.getMapTokens());
            }
            fillTokensTickersValue(transactionsManager.getTransactionsList());
            loadTokens();
        }

    }

    private void getTokensInfo(boolean withLoad) {
        Requestor.getTokens(new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                List<TokenListItem> tokenListItems = (List<TokenListItem>) response;
                mapContractToken = TokensUtils.fillMapContractToken(tokenListItems);
                fillTokensTickersValue(transactionsManager.getTransactionsList());
                TokensUtils.setMapContractToken(sharedManager,
                        mapContractToken);

                sharedManager.setTokenUpdateDate(System.currentTimeMillis());

                if (withLoad)
                    loadTokens();
            }

            @Override
            public void onFailure(String msg) {
                if (isAdded() && !isDetached() && isVisible) {
                    loaderAnimation.cancel();
                    ((MainActivity) getActivity()).showCustomToast(getString(R.string.err_tokens), R.drawable.err_tokens);
                }
            }
        });
    }

    private boolean isShouldSyncTokensInfo() {
        return !isTodayTokenSynced() || sharedManager.getMapTokens().isEmpty();
    }

    private boolean isTodayTokenSynced() {
        String lastSyncTime = CalendarHelper.parseDateToddMMyyyy(sharedManager.getTokenUpdateDate());
        String currentTime = CalendarHelper.parseDateToddMMyyyy(System.currentTimeMillis());
        return lastSyncTime.equals(currentTime);
    }

    private void loadTokens() {
        Log.d("psd", "loadTokens start");
        tokensList.clear();
        EthplorerHelper.getTokensBalances(walletManager.getWalletFriendlyAddress(), new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                AddressInfoResp addressInfoResp = (AddressInfoResp) response;
                if (addressInfoResp.getTokens() != null) {
                    tokensList = TokensUtils.setTokenList(addressInfoResp);
                    mNodeManager.clearTokensList();
                    mNodeManager.addTokensList(tokensList);
                    mNodeManager.updateTokensCodes();
                    if (tokensList.size() > 0 && mapCurrencyNameByCode.size() == 0 && getContext() != null) {
                        mapCurrencyNameByCode = CurrencyUtils.getCurrencyNameByCode(getContext());
                    }

                    if (!TransactionHistoryFragment.this.isDetached() || isFragmentVisible) {
                        startClockwiseRotation();
                        Log.d("psd", "assembleAllTokensCourse start");
                        assembleAllTokensCourse();
                    }
                }
            }

            @Override
            public void onFailure(String msg) {

            }
        });

//        mNodeManager.getTokens(new Callback<List<TokenBodyItem>>() {
//            @Override
//            public void onResponse(List<TokenBodyItem> tokens) {
//                if (isAdded() && !isDetached() && isVisible) {
//                    Log.d("psd", "loadTokens onResponse");
//                    if (tokens.size() > 0 && mapCurrencyNameByCode.size() == 0) {
//                        mapCurrencyNameByCode = CurrencyUtils.getCurrencyNameByCode(getContext());
//                    }
//                    tokensList = tokens;
//                    if (!TransactionHistoryFragment.this.isDetached() || isFragmentVisible) {
//                        startClockwiseRotation();
//                        Log.d("psd", "assembleAllTokensCourse start");
//                        assembleAllTokensCourse();
//                    }
//                }
//            }
//        });
    }

    private void assembleAllTokensCourse() {
//        HashMap<String, String> map = CurrencyUtils.getCurrencyNameByCode(getContext());
        if (tokenRequestCount < tokensList.size()) {
            String fullTokenName = mapCurrencyNameByCode.get(tokensList.get(tokenRequestCount).getTokenName());
            if (fullTokenName != null) {
                fullTokenName = fullTokenName.trim();
                CoinmarketcapHelper.getExchange(fullTokenName,
                        sharedManager.getLocalCurrency().toLowerCase(),
                        new ApiMethods.RequestListener() {
                            @Override
                            public void onSuccess(Object response) {
                                if (isAdded() && !isDetached() && isVisible) {
                                    List<RespExch> exchange = (List<RespExch>) response;
                                    try {
                                        if (exchange.get(0).getPrice(sharedManager.getLocalCurrency().toLowerCase()) != null) {
                                            String localBalance = tokensList.get(tokenRequestCount).getTokenNum().toString().replace(",", "");
                                            Double res = Double.valueOf(localBalance) * (Double.valueOf(exchange.get(0).getPrice(sharedManager.getLocalCurrency().toLowerCase())));
                                            tokensList.get(tokenRequestCount).setOtherSum(round(res, 2));
                                        } else {
                                            tokensList.get(tokenRequestCount).setOtherSum(-1.0);
                                        }
                                    } catch (Exception e) {
                                        FirebaseCrash.report(e);
                                        FirebaseCrash.logcat(Log.ERROR, "assembleAllTokensCourse", e.toString());
                                        e.printStackTrace();
                                    }
                                    tokenRequestCount++;
                                    assembleAllTokensCourse();
                                }
                            }

                            @Override
                            public void onFailure(String msg) {
                                FirebaseCrash.log(msg);
                                loaderAnimation.cancel();
                            }
                        });
            } else {
                tokenRequestCount++;
                assembleAllTokensCourse();
            }
        } else {
            initTokens(tokensList);
            loaderAnimation.cancel();
            tokenRequestCount = 0;
        }
    }

    private void createWallet(String passphrase) {
        Log.d("psd", "createWallet start");
        if (isAdded()) {
            showProgress(getString(R.string.generating_wallet));
        }

        walletManager.createWallet(passphrase, new WalletCreationCallback() {
            @Override
            public void onWalletCreated(WalletFile walletFile) {
                closeProgress();
                openUserWalletFragment();
                Log.d("psd", "createWallet end");
            }
        });
    }

    private void showTransactions(boolean cache) {
        if (cache)
            loadFromCache();

        loadTransactions();
    }

    private void loadFromCache() {
        Log.d("psd", "loadFromCache start");
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
        Log.d("psd", "loadFromCache end");
    }

    private void loadTransactions() {
        Log.d("psd", "loadTransactions start");
        startClockwiseRotation();
        EtherscanHelper.getTransactions(walletManager.getWalletFriendlyAddress(), new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                if (isAdded() && !isDetached() && isVisible) {
                    TransactionsListResponse listResponse = (TransactionsListResponse) response;
                    List<TransactionResponse> transactionsList = listResponse.getResult();
                    if (transactionsList != null) {
                        transactionsManager.setTransactionsList(transactionsList);
                        fillTokensTickersValue(transactionsList);
                        initTransactionHistoryRecycler();
                        if (transactionsList.size() == 0) {
                            GuardaApp.isTransactionsEmpty = true;
                            openUserWalletFragment();
                        } else {
                            GuardaApp.isTransactionsEmpty = false;
                            updateTxsCache(transactionsList);
                        }
                        loadTokensTxs();
                    }
                    loaderAnimation.cancel();
                }
                Log.d("psd", "loadTransactions onSuccess");
            }

            @Override
            public void onFailure(String msg) {
                if (isAdded() && !isDetached() && isVisible) {
                    loaderAnimation.cancel();
                    FirebaseCrash.logcat(Log.ERROR, "getTransactions", msg);
                    if (isAdded() && !isDetached()) {
                        ((MainActivity) getActivity()).showCustomToast(getString(R.string.err_get_history), R.drawable.err_history);
                    }
                }
                Log.d("psd", "loadTransactions onFailure");
            }
        });

    }

    private void loadTokensTxs() {
        Log.d("psd", "loadTokensTxs start");
        startClockwiseRotation();
        EthplorerHelper.getTransactions(walletManager.getWalletFriendlyAddress(), new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                if (isAdded() && !isDetached() && isVisible) {
                    TokensTxListResponse listResponse = (TokensTxListResponse) response;
                    List<TokenTxResponse> transactionsList = listResponse.getResult();
                    List<TransactionResponse> txlist = addTokensInputTransactions(transactionsList);
                    transactionsManager.setTransactionsList(txlist);
//                    fillTokensTickersValue(txlist);
                    initTokensMapping();
                    initTransactionHistoryRecycler();
                    if (transactionsList.size() == 0) {
                        GuardaApp.isTransactionsEmpty = true;
                        openUserWalletFragment();
                    } else {
                        GuardaApp.isTransactionsEmpty = false;
                        updateTxsCache(txlist);
                    }
                }
                loaderAnimation.cancel();
                Log.d("psd", "loadTokensTxs onSuccess");
            }

            @Override
            public void onFailure(String msg) {
                if (isAdded() && !isDetached() && isVisible) {
                    loaderAnimation.cancel();
                    FirebaseCrash.logcat(Log.ERROR, "getTransactions", msg);
                    ((MainActivity) getActivity()).showCustomToast(getString(R.string.err_tokens_txs), R.drawable.err_tokens);
                }
                Log.d("psd", "loadTokensTxs onFailure");
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
        Log.d("psd", "showBalance start");
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
                    Log.d("psd", "showBalance onResponse");
                }
            }
        });

    }

    private void getLocalBalance(final String balance) {
        Log.d("psd", "getLocalBalance start");
        CoinmarketcapHelper.getExchange(Common.MAIN_CURRENCY_NAME,
                sharedManager.getLocalCurrency().toLowerCase(),
                new ApiMethods.RequestListener() {
                    @Override
                    public void onSuccess(Object response) {
                        try {
                            if (isAdded() && !isDetached() && isVisible) {
                                List<RespExch> exchange = (List<RespExch>) response;

                                String localBalance = balance.replace(",", "");

                                if (exchange.get(0).getPrice(sharedManager.getLocalCurrency().toLowerCase()) != null) {
                                    Double res = Double.valueOf(localBalance) * (Double.valueOf(exchange.get(0).getPrice(sharedManager.getLocalCurrency().toLowerCase())));
                                    setUSDBalance(Double.toString(round(res, 2)));
                                } else {
                                    setUSDBalance("...");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.d("psd", "getLocalBalance onSuccess");
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
        Log.d("psd", "checkFromRestore start");
        Bundle args = getArguments();
        if (args != null) {
            String key = args.getString(KEY);
            if (!TextUtils.isEmpty(key)) {
                showProgress(getString(R.string.restoring_wallet));
                walletManager.restoreFromBlock(key, new WalletCreationCallback() {
                    @Override
                    public void onWalletCreated(WalletFile walletFile) {
                        closeProgress();
                        if (isVisible && walletFile != null) {
                            startClockwiseRotation();
                            updateData(true);
                        }
                        Log.d("psd", "checkFromRestore onWalletCreated");
                    }
                });
            }
        }
    }

    private void initTransactionHistoryRecycler() {
        Log.d("psd", "initTransactionHistoryRecycler start");
        adapter = new TransHistoryAdapter();
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
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showBalance();
                            showTokens();
                        }
                    });
                }
            }
        });

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        rvTransactionsList.setLayoutManager(layoutManager);
        rvTransactionsList.setAdapter(adapter);
        Log.d("psd", "initTransactionHistoryRecycler end");
    }

    private void setCryptoBalance(String balance) {
        tvCryptoCount.setText(String.format(Locale.US, "%s " + sharedManager.getCurrentCurrency().toUpperCase(),
                balance));
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
                mainActivity.setToolBarTitle(R.string.app_amount_to_purchase);
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
            getBlockNumber();
            showTransactions(withCache);
            showBalance();
            showTokens();
        } else {
            loaderAnimation.cancel();
            ((MainActivity) getActivity()).showCustomToast(getString(R.string.err_network), R.drawable.err_network);
        }

    }

    private void getBlockNumber() {
        Log.d("psd", "getBlockNumber start");
        networkManager.getBlocNumber(new Callback<BigInteger>() {
            @Override
            public void onResponse(BigInteger block) {
                Log.d("psd", "getBlockNumber end");
                blockHeight = block;
            }
        });
    }

    private void updateTxsCache(List<TransactionResponse> transactionsList) {
        Log.d("psd", "updateTxsCache start");
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
        Log.d("psd", "updateTxsCache end");
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

    private void initTokensMapping() {
//        Log.d("psd", "initTokensMapping start");
//        JsonParser jp = new JsonParser();
//        try {
//            String str = sharedManager.getTokensInfo();
//            Object obj = jp.parse(str);
//            JsonArray arrayOfTickers = (JsonArray) obj;
//            for (int i = 0; i < arrayOfTickers.size(); i++) {
//                JsonObject j = (JsonObject) arrayOfTickers.get(i);
//                String tkr = j.get("name").getAsString();
//                JsonArray params = (JsonArray) j.get("params");
//                JsonObject p = (JsonObject) params.get(0);
//                String to = p.get("to").getAsString();
//                mapTickers.put(to.toLowerCase(), tkr);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.e("psd", e.toString());
//        }
//        Log.d("psd", "initTokensMapping end");
    }

    private void fillTokensTickersValue(final List<TransactionResponse> transactionsList) {
        Log.d("psd", "fillTokensTickersValue start");
        if (transactionsList == null || mapContractToken == null || mapContractToken.size() == 0) return;

        for (TransactionResponse tp : transactionsList) {
            TokenListItem token = mapContractToken.get(tp.getTo().toLowerCase());
            if (token != null) {
                tp.setTicker(token.getTicker().toUpperCase());
            } else {
                Integer pos = transactionsManager.getHashPositionMap().get(tp.getHash());
                transactionsManager.getTransactionsList().get(pos).setTicker(Common.MAIN_CURRENCY.toUpperCase());
            }
        }

        Log.d("psd", "fillTokensTickersValue end");
    }

    private List<TransactionResponse> addTokensInputTransactions(List<TokenTxResponse> txTokenList) {
        Log.d("psd", "addTokensInputTransactions start");
        List<TransactionResponse> txlist = transactionsManager.getTransactionsList();

        if (mapContractToken == null || mapContractToken.size() == 0) {
            return txlist;
        }

        for (TokenTxResponse txr : txTokenList) {
            boolean isExist = false;
            int p = 0;
            TokenListItem token = mapContractToken.get(txr.getContract().toLowerCase());
            if (token != null)
                p = token.getPrecision();

            for (TransactionResponse tr : txlist) {
                if (tr.getHash().equalsIgnoreCase(txr.getHash())) {
                    if (token != null) {
                        tr.setTicker(token.getTicker().toUpperCase());

                        BigDecimal v = new BigDecimal(txr.getValue());
//                    tr.setValue(Convert.toWei(v, Convert.Unit.ETHER).toBigInteger().toString());
                        BigDecimal b = BigDecimal.TEN.pow(p);
                        tr.setValue(v.toBigInteger().toString());
                    }
                    isExist = true;
                }
            }

            if (!isExist) {
                if (token != null)
                    txr.setTicker(token.getTicker().toUpperCase());
                try {
                    txr.setConfirmations((blockHeight.subtract(new BigInteger(txr.getBlockNumber())).toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                txlist.add(new TransactionResponse(txr, p));
            }
        }

        Collections.sort(txlist, new Comparator<TransactionResponse>() {
            @Override
            public int compare(TransactionResponse v1, TransactionResponse v2) {
                if (v1.getTimeStamp() < v2.getTimeStamp())
                    return 1;
                else if (v1.getTimeStamp() > v2.getTimeStamp())
                    return -1;
                return 0;
            }
        });
        Log.d("psd", "addTokensInputTransactions end");
        return txlist;
    }

}
