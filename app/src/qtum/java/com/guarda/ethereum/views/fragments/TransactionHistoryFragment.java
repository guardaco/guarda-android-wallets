package com.guarda.ethereum.views.fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.customviews.RateDialog;
import com.guarda.ethereum.managers.CoinmarketcapHelper;
import com.guarda.ethereum.managers.EthereumNetworkManager;
import com.guarda.ethereum.managers.NetworkManager;
import com.guarda.ethereum.managers.RawNodeManager;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.TransactionsManager;
import com.guarda.ethereum.managers.WalletCreationCallback;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.items.BtgBalanceResponse;
import com.guarda.ethereum.models.items.BtgTxListResponse;
import com.guarda.ethereum.models.items.BtgTxResponse;
import com.guarda.ethereum.models.items.QtumTxListResponse;
import com.guarda.ethereum.models.items.RespExch;
import com.guarda.ethereum.models.items.ResponseExchangeAmount;
import com.guarda.ethereum.models.items.TokenBodyItem;
import com.guarda.ethereum.models.items.TokenHeaderItem;
import com.guarda.ethereum.models.items.TokenRequestModel;
import com.guarda.ethereum.models.items.TransactionItem;
import com.guarda.ethereum.models.items.TransactionsInsightListResponse;
import com.guarda.ethereum.models.items.TxInsight;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;
import com.guarda.ethereum.rest.RequestorBtc;
import com.guarda.ethereum.views.activity.MainActivity;
import com.guarda.ethereum.views.activity.TransactionDetailsActivity;
import com.guarda.ethereum.views.adapters.TokenAdapter;
import com.guarda.ethereum.views.adapters.TransHistoryAdapter;
import com.guarda.ethereum.views.fragments.base.BaseFragment;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.ResponseBody;

import static com.guarda.ethereum.models.constants.Common.BLOCK;
import static com.guarda.ethereum.models.constants.Common.EXTRA_TRANSACTION_DECIMALS;
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
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rv_tokens)
    RecyclerView rvTokens;

    private static final String BLANK_BALANCE = "...";
    private boolean isVisible = true;
    private boolean stronglyHistory = false;
    private Handler handler = new Handler();
    private ObjectAnimator loaderAnimation;
    private TokenAdapter tokenAdapter;
    private List<TokenBodyItem> tokensList = new ArrayList<>();
    private List<TokenRequestModel> tokensToRequest = new ArrayList<>();
    private AtomicInteger requestCounter = new AtomicInteger(0);

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
        stronglyHistory = true;

        nsvMainScrollLayout.smoothScrollTo(0, 0);
        setCryptoBalance(BLANK_BALANCE);

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
        }

        tokensToRequest.add(new TokenRequestModel("FENIX", Common.TOKEN_FENIX_B58, 18));
        tokensToRequest.add(new TokenRequestModel("INK", Common.TOKEN_INK_B58, 9));
        tokensToRequest.add(new TokenRequestModel("BOT", Common.TOKEN_BOT_B58, 8));
        tokensToRequest.add(new TokenRequestModel("SPC", Common.TOKEN_SPC_B58, 8));
        tokensToRequest.add(new TokenRequestModel("LSTR", Common.TOKEN_LSTR_B58, 8));
        tokensToRequest.add(new TokenRequestModel("QC", Common.TOKEN_QC_B58, 8));
        tokensToRequest.add(new TokenRequestModel("HPY", Common.TOKEN_HPY_B58, 8));
        tokensToRequest.add(new TokenRequestModel("HLC", Common.TOKEN_HLC_B58, 9));
        tokensToRequest.add(new TokenRequestModel("PLY", Common.TOKEN_PLY_B58, 9));
        tokensToRequest.add(new TokenRequestModel("QBT", Common.TOKEN_QBT_B58, 8));
        tokensToRequest.add(new TokenRequestModel("MED", Common.TOKEN_MED_B58, 8));

        tokensList.add(new TokenBodyItem("FENIX", BigDecimal.valueOf(66), "33", 18));
        tokensList.add(new TokenBodyItem("INK", BigDecimal.valueOf(66), "33", 9));
        tokensList.add(new TokenBodyItem("BOT", BigDecimal.valueOf(66), "33", 8));
        tokensList.add(new TokenBodyItem("SPC", BigDecimal.valueOf(66), "33", 8));
        tokensList.add(new TokenBodyItem("LSTR", BigDecimal.valueOf(66), "33", 8));
        tokensList.add(new TokenBodyItem("QC", BigDecimal.valueOf(66), "33", 8));
        tokensList.add(new TokenBodyItem("HPY", BigDecimal.valueOf(66), "33", 8));
        tokensList.add(new TokenBodyItem("HLC", BigDecimal.valueOf(66), "33", 9));
        tokensList.add(new TokenBodyItem("PLY", BigDecimal.valueOf(66), "33", 9));
        tokensList.add(new TokenBodyItem("QBT", BigDecimal.valueOf(66), "33", 8));
        tokensList.add(new TokenBodyItem("MED", BigDecimal.valueOf(66), "33", 8));

        //initTokens(tokensList);

//        loadTokensExchangeRate();
    }

    private void loadTokensExchangeRate(String fromTicker) {
        Requestor.getExchangeAmountCmc(fromTicker, sharedManager.getLocalCurrency(), new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    List<RespExch> resp = (List<RespExch>) response;
                    BigDecimal price = new BigDecimal(resp.get(0).getPrice(sharedManager.getLocalCurrency().toLowerCase()));
                    for (TokenBodyItem tokenBodyItem : tokensList) {
                        if (tokenBodyItem.getTokenName().equalsIgnoreCase(fromTicker)) {
                            Double sum = price.doubleValue() * tokenBodyItem.getTokenNum().doubleValue();
                            tokenBodyItem.setOtherSum(sum);
                        }
                    }
                    tokenAdapter.notifyDataSetChanged();
                    Log.d("flint", "loadTokensExchangeRate.onSuccess: " + price);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                decrementRequestCounter();
            }

            @Override
            public void onFailure(String msg) {
                decrementRequestCounter();
                Log.d("flint", "loadTokensExchangeRate.onFailure: " + msg);
            }
        });
    }

    private void loadTokens() {
        if (requestCounter.get() > 0)
            return;

        requestCounter.set(tokensToRequest.size()*3);

        tokensList.clear();

        final TransactionHistoryFragment thisFragment = this;

        class MyRunnable implements Runnable {
            private TokenRequestModel tokenRequestModel = null;
            public MyRunnable(TokenRequestModel tokenRequestModel) {
                this.tokenRequestModel = tokenRequestModel;
            }
            @Override
            public void run() {
                RequestorBtc.getTokenBalanceQtumNew(tokenRequestModel.address, walletManager.getWalletFriendlyAddress(), new ApiMethods.RequestListener() {
                    @Override
                    public void onSuccess(Object response) {
                        try {
                            ResponseBody responseBody = (ResponseBody) response;
                            BigDecimal responseDecimal = new BigDecimal(responseBody.string()).divide(BigDecimal.TEN.pow(tokenRequestModel.decimals));
                            synchronized (tokensList) {
                                tokensList.add(new TokenBodyItem(tokenRequestModel.name, responseDecimal, "", 9));
                            }
                            mNodeManager.clearTokensList();
                            mNodeManager.addTokenToList(new TokenBodyItem(Common.MAIN_CURRENCY_NAME, BigDecimal.valueOf(9), "", 9));
                            mNodeManager.addTokensList(tokensList);
                            mNodeManager.updateTokensCodes();
                            loadTokensExchangeRate(tokenRequestModel.name);
                            Log.d("flint", "getTokensBalanceQtumNew.onSuccess: " + responseDecimal);
                        } catch (Exception e) {}
                        decrementRequestCounter();
                    }

                    @Override
                    public void onFailure(String msg) {
//                        ((MainActivity) getActivity()).showCustomToast(getString(R.string.err_tokens), R.drawable.err_tokens);
                        loadTokensExchangeRate(tokenRequestModel.name);
                        decrementRequestCounter();
                        Log.d("flint", "getTokensBalanceQtumNew.onFailure: " + msg);
                    }
                });
            }
        }

        class MyRunnable2 implements Runnable {
            private TokenRequestModel tokenRequestModel = null;
            public MyRunnable2(TokenRequestModel tokenRequestModel) {
                this.tokenRequestModel = tokenRequestModel;
            }
            @Override
            public void run() {
                Log.d("flint", "getTokenTransactionsQtumNew...");
                RequestorBtc.getTokenTransactionsQtumNew(tokenRequestModel.address, walletManager.getWalletFriendlyAddress(), new ApiMethods.RequestListener() {
                    @Override
                    public void onSuccess(Object response) {
                        try {
                            TransactionsInsightListResponse responseBody = (TransactionsInsightListResponse) response;
                            List<TxInsight> txList = responseBody.getTxs();
                            updateTransactionsListWithToken(tokenRequestModel, txList);
                            Log.d("flint", "getTokenTransactionsQtumNew.onSuccess: size=" + txList.size());
                        } catch (Exception e) {
                            Log.d("flint", "getTokenTransactionsQtumNew.onSuccess... exception: " + e.toString());
                        }
                        decrementRequestCounter();

                        if (thisFragment.getActivity() == null) return;

                        thisFragment.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showTransactions();
                            }
                        });
                    }

                    @Override
                    public void onFailure(String msg) {
                        Log.d("flint", "getTokenTransactionsQtumNew.onFailure: " + msg);
                        decrementRequestCounter();
                        if (thisFragment.getActivity() == null) return;

                        thisFragment.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showTransactions();
                            }
                        });
                    }
                });
            }
        }

        for (TokenRequestModel tokenRequestModel : tokensToRequest) {
            new MyRunnable(tokenRequestModel).run();
        }

        for (TokenRequestModel tokenRequestModel : tokensToRequest) {
            new MyRunnable2(tokenRequestModel).run();
        }
    }

    synchronized private void decrementRequestCounter() {
        if (requestCounter.addAndGet(-1) <= 0) {
            try {
                initTokens(tokensList);
                rvTransactionsList.getAdapter().notifyDataSetChanged();
            } catch (Exception e) {
                Log.e("flint", "TransactionHistoryFragment.decrementRequestCounter... exception: " + e.toString());
            }
        }
    }

    synchronized private void updateTransactionsListWithToken(TokenRequestModel tokenRequestModel, List<TxInsight> txList) {
        List<TransactionItem> trx = transactionsManager.getTransactionsList();
        long blockchainHeight = -1;
        if (trx.size() > 0) {
            TransactionItem itm = trx.get(0);
            blockchainHeight = itm.getBlockHeight() + itm.getConfirmations();
        }
        for (TxInsight txi : txList) {
            TransactionItem item = new TransactionItem();
            BigInteger sum = new BigInteger(txi.getValue());
            item.setSum(sum.longValue());
            item.setFrom(txi.getFromAddress());
            item.setTo(txi.getToAddress());
            try {
                if (txi.getToAddress().equals(walletManager.getWalletFriendlyAddress()))
                    item.setOut(false);
                else
                    item.setOut(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (blockchainHeight < 0)
                item.setConfirmations(100);
            else
                item.setConfirmations(blockchainHeight - txi.getBlockHeight());
            item.setTokenTicker(tokenRequestModel.name);
            SimpleDateFormat sdftime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            sdftime.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date txiTime = new Date();
            try {txiTime = sdftime.parse(txi.getTimestamp());} catch (Exception e){}
            item.setTime(txiTime.getTime()/1000);
            item.setHash(txi.getTxHash());
            trx.add(0, item);
        }
        Collections.sort(trx, new Comparator<TransactionItem>() {
            @Override
            public int compare(TransactionItem v1, TransactionItem v2) {
                if (v1.getTime() < v2.getTime())
                    return 1;
                else if (v1.getTime() > v2.getTime())
                    return -1;
                return 0;
            }
        });
    }

    private void initTokens(List<TokenBodyItem> tokens) {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
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


    @Override
    public void onResume() {
        super.onResume();
        if (isWalletExist()) {
            updateData(true);
        }
    }

    @Override
    public void onPause() {
        handler.removeCallbacksAndMessages(null);

        super.onPause();
    }

    private void createWallet(String passphrase) {
        if (isAdded()) {
            showProgress(getString(R.string.generating_wallet));
        }
        walletManager.createWallet(passphrase, new WalletCreationCallback() {
            @Override
            public void onWalletCreated(Object walletFile) {
                closeProgress();
                openUserWalletFragment();
            }
        });
    }

    private void showTransactions() {
        initTransactionHistoryRecycler();
        loaderAnimation.cancel();
        if (transactionsManager.getTransactionsList().size() == 0) {
            GuardaApp.isTransactionsEmpty = true;
            openUserWalletFragment();
        } else {
            GuardaApp.isTransactionsEmpty = false;
            updateTxsCache();
        }
    }

    private void openUserWalletFragment() {
        if (!stronglyHistory) {
            navigateToFragment(new UserWalletFragment());
        }
    }

    private boolean isWalletExist() {
        return !TextUtils.isEmpty(walletManager.getWalletFriendlyAddress());
    }

    private void showBalance(boolean withCache) {
        if (withCache)
            loadFromCache();

        startClockwiseRotation();
        loadBalance();
        loadTransactions();
        askRating();
    }

    private void loadFromCache() {
        String jsonFromPref = sharedManager.getTxsCache();
        if (!jsonFromPref.equalsIgnoreCase("")) {
            Gson gson = new Gson();
            Type listType = new TypeToken<Map<String, ArrayList<TransactionItem>>>(){}.getType();
            Map<String, ArrayList<TransactionItem>> addrTxsMap = gson.fromJson(jsonFromPref, listType);
            ArrayList<TransactionItem> txList = addrTxsMap.get(walletManager.getWalletFriendlyAddress());
            if (txList != null) {
                transactionsManager.setTransactionsList(txList);
                initTransactionHistoryRecycler();
            }
        }
    }

    private void loadTransactions() {
        RequestorBtc.getTransactionsQtumNew(walletManager.getWalletFriendlyAddress(), new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                QtumTxListResponse txListResponse = (QtumTxListResponse) response;
                List<BtgTxResponse> txList = txListResponse.getTxs();
                if (txList == null) return;
                List<TransactionItem> txItems = transactionsManager.transformTxToFriendlyNew(txList, walletManager.getWalletFriendlyAddress());
                transactionsManager.setTransactionsList(txItems);
                loadTokens();
            }

            @Override
            public void onFailure(String msg) {
                Log.d("flint", "getTransactionsQtumNew.onFailure: " + msg);
                loaderAnimation.cancel();
                if (isAdded() && !isDetached()) {
                    ((MainActivity) getActivity()).showCustomToast(getString(R.string.err_get_history), R.drawable.err_history);
                }
            }
        });
    }

    private void loadBalance() {
        RequestorBtc.getBalanceQtumNew(walletManager.getWalletFriendlyAddress(), new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                loaderAnimation.cancel();
                ResponseBody balance = (ResponseBody) response;
                try {
                    String strBalance = balance.string();
                    walletManager.setMyBalance(Long.parseLong(strBalance));
                    walletManager.setBalance(Long.parseLong(strBalance));
                } catch (Exception e) {
                    walletManager.setBalance(0l);
                }
                String curBalance = WalletManager.getFriendlyBalance(walletManager.getMyBalance());
                setCryptoBalance(curBalance);
                getLocalBalance(curBalance);
            }

            @Override
            public void onFailure(String msg) {
                loaderAnimation.cancel();
                if (isAdded()) {
                    ((MainActivity) getActivity()).showCustomToast(getString(R.string.err_get_balance), R.drawable.err_balance);
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
                        List<RespExch> exchange = (List<RespExch>) response;

                        String localBalance = balance.replace(",", "");

                        if (exchange.get(0).getPrice(sharedManager.getLocalCurrency().toLowerCase()) != null) {
                            Double res = Double.valueOf(localBalance) * (Double.valueOf(exchange.get(0).getPrice(sharedManager.getLocalCurrency().toLowerCase())));
                            setUSDBalance(Double.toString(round(res, 2)));
                        } else {
                            setUSDBalance("...");
                        }
                    }

                    @Override
                    public void onFailure(String msg) {
                        loaderAnimation.cancel();
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
                if (key.substring(0, 4).equalsIgnoreCase("xprv")) {
                    walletManager.restoreFromBlockByXPRV(key, new WalletCreationCallback() {
                        @Override
                        public void onWalletCreated(Object walletFile) {
                            closeProgress();
                            if (isVisible) {
                                updateData(true);
                            }
                        }
                    });
                } else if ((key.length() == 51 || key.length() == 52) && !key.trim().contains(" ")) {
                    walletManager.restoreFromBlockByWif(key, new WalletCreationCallback() {
                        @Override
                        public void onWalletCreated(Object walletFile) {
                            if (isVisible) {
                                closeProgress();
                                showBalance(true);
                            }
                        }
                    });
                } else {
                    walletManager.restoreFromBlock(key, new WalletCreationCallback() {
                        @Override
                        public void onWalletCreated(Object walletFile) {
                            closeProgress();
                            if (isVisible) {
                                updateData(true);
                            }
                        }
                    });
                }
            }
        }
    }

    private void initTransactionHistoryRecycler() {
        TransHistoryAdapter adapter = new TransHistoryAdapter(tokensToRequest);
        new Handler();
        adapter.setItemClickListener(new TransHistoryAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position) {
                TransactionItem ti = transactionsManager.getTxByPosition(position);
                int dec = TransHistoryAdapter.getDecimal(ti, tokensToRequest);
                Intent detailsIntent = new Intent(getActivity(), TransactionDetailsActivity.class);
                detailsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                detailsIntent.putExtra(EXTRA_TRANSACTION_POSITION, position);
                detailsIntent.putExtra(EXTRA_TRANSACTION_DECIMALS, dec);
                startActivity(detailsIntent);
                getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.no_slide);
            }
        });

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        rvTransactionsList.setLayoutManager(layoutManager);
        rvTransactionsList.setAdapter(adapter);

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
                if (SharedManager.flag_disable_buy_menu) {
                    mainActivity.setToolBarTitle(getString(R.string.app_amount_to_purchase));
                    navigateToFragment(new DisabledFragment());
                } else {
                    mainActivity.setToolBarTitle(getString(R.string.app_amount_to_purchase));
                    navigateToFragment(new PurchaseCoinsFragment());
                }
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
            if (walletManager.getWalletFriendlyAddress() == null) return;

            showBalance(withCache);
            loadTokens();
        } else {
            loaderAnimation.cancel();
            if (isAdded() && !isDetached()) {
                ((MainActivity) getActivity()).showCustomToast(getString(R.string.err_network), R.drawable.err_network);
            }
        }

    }

    private void updateTxsCache() {
        if (walletManager.getWalletFriendlyAddress() == null || walletManager.getWalletFriendlyAddress().equalsIgnoreCase(""))
            return;

        Gson gson = new Gson();
        Type listType = new TypeToken<Map<String, ArrayList<TransactionItem>>>(){}.getType();
        String jsonFromPref = sharedManager.getTxsCache();
        Map<String, ArrayList<TransactionItem>> addrTxsMap = gson.fromJson(jsonFromPref, listType);

        if (addrTxsMap == null)
            addrTxsMap = new HashMap<>();

        addrTxsMap.put(walletManager.getWalletFriendlyAddress(), (ArrayList<TransactionItem>) transactionsManager.getTransactionsList());
        String jsonToPref = gson.toJson(addrTxsMap);
        sharedManager.setTxsCache(jsonToPref);
    }

    private void askRating() {
        if (!sharedManager.getIsAskRate() || transactionsManager.getTransactionsList().size() < 3) {
            return;
        }

        int nextShowRate = sharedManager.getNextShowRate();
        int nowCount = 0;
        for (TransactionItem tr : transactionsManager.getTransactionsList()) {
            if (tr.getConfirmations() >= TransHistoryAdapter.MIN_CONFIRMATIONS) {
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
