package com.guarda.ethereum.views.fragments;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.Callback;
import com.guarda.ethereum.managers.ChangellyNetworkManager;
import com.guarda.ethereum.managers.ChangenowApi;
import com.guarda.ethereum.managers.ChangenowManager;
import com.guarda.ethereum.managers.CurrencyListHolder;
import com.guarda.ethereum.managers.ShapeshiftApi;
import com.guarda.ethereum.managers.ShapeshiftManager;
import com.guarda.ethereum.models.ExchangeSpinnerRowModel;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.items.CryptoItem;
import com.guarda.ethereum.models.items.IconItemResponse;
import com.guarda.ethereum.models.items.ResponseCurrencyItem;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;
import com.guarda.ethereum.views.adapters.ExchangeSpinnerAdapter;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import timber.log.Timber;

@AutoInjector(GuardaApp.class)
public class ExchangeFragment extends BaseFragment {

    @BindView(R.id.textViewExchange)
    TextView textViewExchange;
    @BindView(R.id.imageViewAbout)
    ImageView imageViewAbout;
    @BindView(R.id.spinnerExchange)
    Spinner spinnerExchange;
    @BindView(R.id.textViewSend)
    TextView textViewSend;
    @BindView(R.id.spinnerFromCoin)
    Spinner spinnerFromCoin;
    @BindView(R.id.textViewReceive)
    TextView textViewReceive;
    @BindView(R.id.spinnerToCoin)
    Spinner spinnerToCoin;
    @BindView(R.id.textViewExchangeRateHeader)
    TextView textViewExchangeRateHeader;
    @BindView(R.id.textViewExchangeRate)
    TextView textViewExchangeRate;
    @BindView(R.id.buttonStartExchange)
    Button buttonStartExchange;
    @Inject
    CurrencyListHolder currentCrypto;

    private boolean isSpinnerInitialized = false;
    private int spinnerExchangePosition = 0;
    private int spinnerFromCoinPosition = 0;
    private int spinnerToCoinPosition = 0;

    private int spinnerFromPrevPosition = 0;
    private int spinnerToPrevPosition = 0;

    private boolean firstLaunch = true;

    private BigDecimal minimumAmount = new BigDecimal(0.0);

    private Map<String, ShapeshiftApi.CoinExternalInfoModel> cryptoCurrenciesInfo = new HashMap<>();
    private ExchangeSpinnerAdapter exchangeFromSpinnerAdapter;
    private ExchangeSpinnerAdapter exchangeToSpinnerAdapter;
    private ExchangeSpinnerAdapter exchangesAdapter;

    public ExchangeFragment() {
        isSpinnerInitialized = false;
    }

    public ExchangeFragment setData(String spinnerExchangeSymbol, int spinnerFromCoinPosition, int spinnerToCoinPosition) {
        Log.d("flint", "setData: " + spinnerExchangeSymbol + ", " + spinnerFromCoinPosition + ", " + spinnerToCoinPosition);
        this.spinnerExchangePosition = getSpinnerPosBySymbol(spinnerExchange, spinnerExchangeSymbol);
        this.spinnerFromCoinPosition = spinnerFromCoinPosition;
        this.spinnerToCoinPosition = spinnerToCoinPosition;
        isSpinnerInitialized = true;
        return this;
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_exchange;
    }

    @Override
    protected void init () {
        init_real();
    }

    protected void init_real() {
        GuardaApp.getAppComponent().inject(this);
        setToolbarTitle(getString(R.string.title_purchase));
        textViewExchange.setText(getString(R.string.exchange_choose_service));
        exchangesAdapter = new ExchangeSpinnerAdapter(this.getContext(), createExchangeSpinnerRows());
        spinnerExchange.setAdapter(exchangesAdapter);
        textViewSend.setText(getString(R.string.exchange_send));
        textViewReceive.setText(getString(R.string.exchange_receive));
        textViewExchangeRateHeader.setText(getString(R.string.exchange_rate));
        buttonStartExchange.setText(getString(R.string.exchange_start));

        final ExchangeFragment thisFragment = this;

        spinnerExchange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spinnerExchange_onItemSelected();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spinnerFromCoin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                checkOwnCoin_fromCoin();
                updateSelectedPairRate();
                spinnerFromPrevPosition = spinnerFromCoin.getSelectedItemPosition();
                spinnerToPrevPosition = spinnerToCoin.getSelectedItemPosition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spinnerToCoin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                checkOwnCoin_toCoin();
                updateSelectedPairRate();
                spinnerFromPrevPosition = spinnerFromCoin.getSelectedItemPosition();
                spinnerToPrevPosition = spinnerToCoin.getSelectedItemPosition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        imageViewAbout.setOnClickListener((View view) -> {
            try {
                navigateToFragment(new ExchangeAboutFragment().setData(((ExchangeSpinnerRowModel)(spinnerExchange.getAdapter().getItem(spinnerExchange.getSelectedItemPosition()))).symbol, spinnerFromCoin.getSelectedItemPosition(), spinnerToCoin.getSelectedItemPosition(), thisFragment));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        buttonStartExchange.setOnClickListener((View view) -> {
            try {
                if (Common.MAIN_CURRENCY.equalsIgnoreCase(((ExchangeSpinnerRowModel) spinnerToCoin.getSelectedItem()).symbol)) {
                    ExchangeStartFragment startFragment = new ExchangeStartFragment().setData(((ExchangeSpinnerRowModel)(spinnerExchange.getAdapter().getItem(spinnerExchange.getSelectedItemPosition()))).symbol, spinnerFromCoin.getSelectedItemPosition(), spinnerToCoin.getSelectedItemPosition(), thisFragment);
                    startFragment.setCoins(((ExchangeSpinnerRowModel) spinnerFromCoin.getSelectedItem()).symbol, ((ExchangeSpinnerRowModel) spinnerToCoin.getSelectedItem()).symbol);
                    startFragment.setCoinsNames(((ExchangeSpinnerRowModel) spinnerFromCoin.getSelectedItem()).text, ((ExchangeSpinnerRowModel) spinnerToCoin.getSelectedItem()).text);
                    startFragment.setMinimumAmount(minimumAmount);
                    navigateToFragment(startFragment);
                } else if (Common.MAIN_CURRENCY.equalsIgnoreCase(((ExchangeSpinnerRowModel) spinnerFromCoin.getSelectedItem()).symbol)) {
                    ExchangeInputAddressFragment exchangeInputAddressFragment = new ExchangeInputAddressFragment().setData(((ExchangeSpinnerRowModel)(spinnerExchange.getAdapter().getItem(spinnerExchange.getSelectedItemPosition()))).symbol, spinnerFromCoin.getSelectedItemPosition(), spinnerToCoin.getSelectedItemPosition(), thisFragment);
                    exchangeInputAddressFragment.setCoins(((ExchangeSpinnerRowModel) spinnerFromCoin.getSelectedItem()).symbol, ((ExchangeSpinnerRowModel) spinnerToCoin.getSelectedItem()).symbol);
                    exchangeInputAddressFragment.setCoinsNames(((ExchangeSpinnerRowModel) spinnerFromCoin.getSelectedItem()).text, ((ExchangeSpinnerRowModel) spinnerToCoin.getSelectedItem()).text);
                    exchangeInputAddressFragment.setMinimumAmount(minimumAmount);
                    navigateToFragment(exchangeInputAddressFragment);
                } else {
                    Toast.makeText(getContext(), getString(R.string.exchange_unsupported_pair), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Log.d("flint", "spinnerExchange.setSelection");
        spinnerExchange.setSelection(spinnerExchangePosition);

        recreateCoinSpinnerLists();

        spinnerToCoin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                try {
                    Field popup = Spinner.class.getDeclaredField("mPopup");
                    popup.setAccessible(true);
                    android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(spinnerToCoin);
                    Rect rc = new Rect();
                    spinnerToCoin.getWindowVisibleDisplayFrame(rc);
                    popupWindow.setHeight(rc.bottom - rc.top - 160);
                    popupWindow.setAnchorView(spinnerToCoin);
                } catch (Exception e) {}
                return false;
            }
        });

        initMenuButton();

    }

    private void removeCoinFromListBySymbol(String symbol, List<ExchangeSpinnerRowModel> list) {
        for (int i = 0; i < list.size(); ++i) {
            ExchangeSpinnerRowModel row = list.get(i);
            if (row.symbol.equalsIgnoreCase(symbol)) {
                list.remove(i);
                break;
            }
        }
    }

    private void fixCoinsList_disableMemoCoins(List<ExchangeSpinnerRowModel> list) {
        try {
            for (String key : cryptoCurrenciesInfo.keySet()) {
                ShapeshiftApi.CoinExternalInfoModel coinExternalInfoModel = cryptoCurrenciesInfo.get(key);
                if (coinExternalInfoModel.memo != null) {
                    removeCoinFromListBySymbol(coinExternalInfoModel.code, list);
                }
            }
            exchangeFromSpinnerAdapter.notifyDataSetChanged();
            exchangeToSpinnerAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void spinnerExchange_onItemSelected() {
        loadCurrencies_changenow();
        spinnerExchangePosition = spinnerExchange.getSelectedItemPosition();
    }

    private void selectSpinnerWithoutOnSelectedEvent(final Spinner spinner, int pos) {
        final AdapterView.OnItemSelectedListener listener = spinner.getOnItemSelectedListener();
        Handler handler = new Handler();
        spinner.setOnItemSelectedListener(null);
        spinner.setSelection(pos);
        handler.post(() -> spinner.setOnItemSelectedListener(listener));
    }

    private void updateInitializedData() {
        if (isSpinnerInitialized) {
            selectSpinnerWithoutOnSelectedEvent(spinnerExchange, spinnerExchangePosition);
            selectSpinnerWithoutOnSelectedEvent(spinnerFromCoin, spinnerFromCoinPosition);
            selectSpinnerWithoutOnSelectedEvent(spinnerToCoin, spinnerToCoinPosition);
            spinnerFromPrevPosition = spinnerFromCoinPosition;
            spinnerToPrevPosition = spinnerToCoinPosition;
            updateSelectedPairRate();
        } else {
            selectOwnCoin(spinnerToCoin);
            spinnerToPrevPosition = spinnerToCoin.getSelectedItemPosition();
            if (spinnerToPrevPosition == spinnerFromCoin.getSelectedItemPosition()) {
                try {
                    selectSpinnerWithoutOnSelectedEvent(spinnerFromCoin, spinnerToPrevPosition+1);
                    spinnerFromPrevPosition = spinnerFromCoin.getSelectedItemPosition();
                } catch (Exception e) {}
            }
            spinnerFromCoinPosition = spinnerFromCoin.getSelectedItemPosition();
            spinnerToCoinPosition = spinnerToCoin.getSelectedItemPosition();
            spinnerFromPrevPosition = spinnerFromCoinPosition;
            spinnerToPrevPosition = spinnerToCoinPosition;
            if (!(spinnerFromCoinPosition == spinnerToCoinPosition || spinnerFromCoinPosition == -1 || spinnerToCoinPosition == -1)) {
                isSpinnerInitialized = true;
                chooseBtcWhenFirstLaunch();
            }
        }
        updateServiceEnabledStatus();
    }

    private void updateServiceEnabledStatus() {
        try {
            boolean isEnabled = false;
            for (int i = 0; i < spinnerFromCoin.getAdapter().getCount(); ++i) {
                ExchangeSpinnerRowModel row = (ExchangeSpinnerRowModel) spinnerFromCoin.getAdapter().getItem(i);
                if (Common.MAIN_CURRENCY.equalsIgnoreCase(row.symbol))
                    isEnabled = true;
            }

            if (isEnabled) {
                textViewSend.setVisibility(View.VISIBLE);
                textViewReceive.setVisibility(View.VISIBLE);
                spinnerFromCoin.setVisibility(View.VISIBLE);
                spinnerToCoin.setVisibility(View.VISIBLE);
                textViewSend.setText(getString(R.string.exchange_send));
                textViewExchangeRateHeader.setVisibility(View.VISIBLE);
                textViewExchangeRate.setVisibility(View.VISIBLE);
            } else {
                textViewSend.setVisibility(View.VISIBLE);
                textViewReceive.setVisibility(View.INVISIBLE);
                spinnerFromCoin.setVisibility(View.INVISIBLE);
                spinnerToCoin.setVisibility(View.INVISIBLE);
                textViewSend.setText(getResources().getString(R.string.exchange_service_unavailable));
                textViewExchangeRateHeader.setVisibility(View.INVISIBLE);
                textViewExchangeRate.setVisibility(View.INVISIBLE);
                isSpinnerInitialized = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectOwnCoin(Spinner spinner) {
        try {
            int sel = 0;
            for (int i = 0; i < spinner.getAdapter().getCount(); ++i) {
                ExchangeSpinnerRowModel row = (ExchangeSpinnerRowModel) spinner.getAdapter().getItem(i);
                if (row.symbol.compareToIgnoreCase(Common.MAIN_CURRENCY) == 0) {
                    sel = i;
                    break;
                }
            }
            selectSpinnerWithoutOnSelectedEvent(spinner, sel);
        } catch (Exception e) {
            Log.e("flint", "ExchangeFragment.selectOwnCoin... exception: " + e.toString());
        }
    }

    protected List<ExchangeSpinnerRowModel> createExchangeSpinnerRows() {
        List<ExchangeSpinnerRowModel> res = new ArrayList<>();
        try {
            res.add(new ExchangeSpinnerRowModel(getResources().getDrawable(R.drawable.ic_change_now), "Changenow", "changenow"));
        } catch (Exception e) {
            Log.d("flint", "createExchangeSpinnerRows... exception: " + e.toString());
        }
        return res;
    }

    protected List<ExchangeSpinnerRowModel> createSendSpinnerRows_changenow () {
        List<ExchangeSpinnerRowModel> res = new ArrayList<>();
        try {
            List<ChangenowApi.SupportedCoinModel> supportedCoins = ChangenowManager.getInstance().getSupportedCoins();
            for (ChangenowApi.SupportedCoinModel coin : supportedCoins) {
                Drawable coinIcon = getResources().getDrawable(R.drawable.ic_curr_empty);
                String coinName = coin.name.isEmpty() ? coin.symbol.toUpperCase() : coin.name;
                res.add(new ExchangeSpinnerRowModel(coinIcon, coinName, coin.symbol, coin.imageUrl));
            }
            Collections.sort(res, (ExchangeSpinnerRowModel exchangeSpinnerRowModel, ExchangeSpinnerRowModel t1) ->
                    exchangeSpinnerRowModel.text.compareToIgnoreCase(t1.text));
        } catch (Exception e) {
            Log.d("flint", "createSendSpinnerRows_changenow... exception: " + e.toString());
        }
        return res;
    }

    protected void updateSelectedPairRate() {
        String serviceSysName = ((ExchangeSpinnerRowModel) spinnerExchange.getSelectedItem()).symbol;
        Log.d("psd", "updateSelectedPairRate serviceSysName = " + serviceSysName);
        updateSelectedPairRate_changenow();
    }

    protected void updateSelectedPairRate_changenow() {
        try {
            final String fromCoin = ((ExchangeSpinnerRowModel) (spinnerFromCoin.getAdapter().getItem(spinnerFromCoin.getSelectedItemPosition()))).symbol;
            final String toCoin = ((ExchangeSpinnerRowModel) (spinnerToCoin.getAdapter().getItem(spinnerToCoin.getSelectedItemPosition()))).symbol;
            if (!fromCoin.equals(toCoin)) {
                ChangenowManager.getInstance().getRate(fromCoin, toCoin, response -> {
                        try {
                            getActivity().runOnUiThread(() -> {
                                textViewExchangeRateHeader.setVisibility(View.VISIBLE);
                                textViewExchangeRate.setText("1 " + fromCoin.toUpperCase() + " ~ " + response.rate.toString() + " " + toCoin.toUpperCase());
                                Timber.d("updateSelectedPairRate_changenow response.rate.longValue() = %s", response.rate.toPlainString());
                                if (response.rate.compareTo(BigDecimal.ZERO) == 0)
                                    disableStartExchangeButton();
                            });
                        } catch (Exception e) {
                            disableStartExchangeButton();
                            e.printStackTrace();
                        }
                    });
                ChangenowManager.getInstance().getMinAmount(fromCoin, toCoin, response -> {
                        try {
                            getActivity().runOnUiThread(() -> minimumAmount = response.minimum);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disableStartExchangeButton() {
        try {
            getActivity().runOnUiThread(() -> {
                textViewExchangeRateHeader.setVisibility(View.INVISIBLE);
                textViewExchangeRate.setText(getResources().getString(R.string.exchange_service_unavailable));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkOwnCoin_fromCoin() {
        String fromCoinStr = ((ExchangeSpinnerRowModel)spinnerFromCoin.getSelectedItem()).symbol;
        String toCoinStr = ((ExchangeSpinnerRowModel)spinnerToCoin.getSelectedItem()).symbol;
        if (fromCoinStr.equalsIgnoreCase(Common.MAIN_CURRENCY)) {
            selectSpinnerWithoutOnSelectedEvent(spinnerToCoin, spinnerFromPrevPosition);
        } else if (!toCoinStr.equalsIgnoreCase(Common.MAIN_CURRENCY)) {
            selectOwnCoin(spinnerToCoin);
        }
    }

    private void checkOwnCoin_toCoin() {
        String fromCoinStr = ((ExchangeSpinnerRowModel)spinnerFromCoin.getSelectedItem()).symbol;
        String toCoinStr = ((ExchangeSpinnerRowModel)spinnerToCoin.getSelectedItem()).symbol;
        if (toCoinStr.equalsIgnoreCase(Common.MAIN_CURRENCY)) {
            selectSpinnerWithoutOnSelectedEvent(spinnerFromCoin, spinnerToPrevPosition);
        } else if (!fromCoinStr.equalsIgnoreCase(Common.MAIN_CURRENCY)) {
            selectOwnCoin(spinnerFromCoin);
        }
    }

    private void loadCurrencies_changenow() {
        ChangenowManager.getInstance().updateSupportedCoinsList((Boolean response) -> {
            try {
                getActivity().runOnUiThread(() -> recreateCoinSpinnerLists());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void recreateCoinSpinnerLists() {
        Log.d("flint", "recreateCoinSpinnerLists");
        final ExchangeFragment thisFragment = this;
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    List<ExchangeSpinnerRowModel> coinsList = createSendSpinnerRows_changenow();
                    String fromCoinSymbol = "";
                    String toCoinSymbol = "";
                    if ((spinnerFromCoin.getAdapter() != null) && (spinnerToCoin.getAdapter() != null) && (spinnerFromCoin.getAdapter().getCount() > 0) && (spinnerToCoin.getAdapter().getCount() > 0)) {
                        fromCoinSymbol = ((ExchangeSpinnerRowModel) spinnerFromCoin.getSelectedItem()).symbol;
                        toCoinSymbol = ((ExchangeSpinnerRowModel) spinnerToCoin.getSelectedItem()).symbol;
                    }
                    exchangeFromSpinnerAdapter = new ExchangeSpinnerAdapter(thisFragment.getContext(), coinsList);
                    exchangeToSpinnerAdapter = new ExchangeSpinnerAdapter(thisFragment.getContext(), coinsList);
                    spinnerFromCoin.setAdapter(exchangeFromSpinnerAdapter);
                    spinnerToCoin.setAdapter(exchangeToSpinnerAdapter);
                    fixCoinsList_disableMemoCoins(coinsList);
                    spinnerToCoin.setDropDownVerticalOffset(0);
                    if ((spinnerFromCoin.getAdapter() != null) && (spinnerToCoin.getAdapter() != null) && (spinnerFromCoin.getAdapter().getCount() > 0) && (spinnerToCoin.getAdapter().getCount() > 0)) {
                        if (!"".equals(fromCoinSymbol) && !"".equals(toCoinSymbol)) {
                            selectSpinnerWithCoinSymbol(spinnerFromCoin, fromCoinSymbol);
                            selectSpinnerWithCoinSymbol(spinnerToCoin, toCoinSymbol);
                            spinnerFromCoinPosition = spinnerFromCoin.getSelectedItemPosition();
                            spinnerToCoinPosition = spinnerToCoin.getSelectedItemPosition();
                        }
                    }
                    updateInitializedData();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectSpinnerWithCoinSymbol(Spinner spinner, String coinSymbol) {
        try {
            spinner.setSelection(getSpinnerPosBySymbol(spinner, coinSymbol));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getSpinnerPosBySymbol(Spinner spinner, String coinSymbol) {
        int sel = 0;
        try {
            for (int i = 0; i < spinner.getAdapter().getCount(); ++i) {
                ExchangeSpinnerRowModel row = (ExchangeSpinnerRowModel) spinner.getAdapter().getItem(i);
                if (coinSymbol.equalsIgnoreCase(row.symbol)) {
                    sel = i;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sel;
    }

    private void chooseBtcWhenFirstLaunch() {
        int btcPosition = 0;
        int ethPosition = 0;
        for (int i = 0; i < spinnerFromCoin.getAdapter().getCount(); ++i) {
            ExchangeSpinnerRowModel row = (ExchangeSpinnerRowModel) spinnerFromCoin.getAdapter().getItem(i);
            if ("btc".equalsIgnoreCase((row.symbol)))
                btcPosition = i;
            if ("eth".equalsIgnoreCase((row.symbol)))
                ethPosition = i;
        }

        chooseBtcWhenFirstLaunch(btcPosition, ethPosition);
    }

    private void chooseBtcWhenFirstLaunch(int btc, int eth) {
        if (firstLaunch) {
            if (!Common.MAIN_CURRENCY.equalsIgnoreCase("btc")) {
                spinnerFromCoin.setSelection(btc);
            } else {
                spinnerFromCoin.setSelection(eth);
            }
            firstLaunch = false;
        }
    }

}
