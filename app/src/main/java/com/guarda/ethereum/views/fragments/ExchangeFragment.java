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

import androidx.fragment.app.Fragment;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.Callback;
import com.guarda.ethereum.managers.Callback2;
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
import com.guarda.ethereum.models.items.ResponseChangellyAmount;
import com.guarda.ethereum.models.items.ResponseChangellyMinAmount;
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
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;

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

    private List<CryptoItem> changellyCoins = new ArrayList<>();

    private Map<String, ShapeshiftApi.CoinExternalInfoModel> cryptoCurrenciesInfo = new HashMap<>();
    private HashMap<String, IconItemResponse> iconTickerMap = new HashMap<>();

    private AtomicInteger exchangeServicesInitCounter = new AtomicInteger(0);
    private boolean isShapeshiftEnabled = true;
    private boolean isChangellyEnabled = true;
    private boolean isChangenowEnabled = true;



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
        loadCoinsExternalInfo(new Callback<Void>() {
            @Override
            public void onResponse(Void response) {
                if (isVisibleOnAttach()) {
                    init_real();
                }
            }
        });
    }



    protected void init_real() {
        GuardaApp.getAppComponent().inject(this);
        setToolbarTitle(getString(R.string.title_purchase));
        textViewExchange.setText(getString(R.string.exchange_choose_service));
        spinnerExchange.setAdapter(new ExchangeSpinnerAdapter(this.getContext(), createExchangeSpinnerRows()));
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

        imageViewAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    navigateToFragment(new ExchangeAboutFragment().setData(((ExchangeSpinnerRowModel)(spinnerExchange.getAdapter().getItem(spinnerExchange.getSelectedItemPosition()))).symbol, spinnerFromCoin.getSelectedItemPosition(), spinnerToCoin.getSelectedItemPosition(), thisFragment));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        buttonStartExchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (Common.MAIN_CURRENCY.equalsIgnoreCase(((ExchangeSpinnerRowModel)spinnerToCoin.getSelectedItem()).symbol)) {
                        ExchangeStartFragment startFragment = new ExchangeStartFragment().setData(((ExchangeSpinnerRowModel)(spinnerExchange.getAdapter().getItem(spinnerExchange.getSelectedItemPosition()))).symbol, spinnerFromCoin.getSelectedItemPosition(), spinnerToCoin.getSelectedItemPosition(), thisFragment);
                        startFragment.setCoins(((ExchangeSpinnerRowModel) spinnerFromCoin.getSelectedItem()).symbol, ((ExchangeSpinnerRowModel) spinnerToCoin.getSelectedItem()).symbol);
                        startFragment.setCoinsNames(((ExchangeSpinnerRowModel) spinnerFromCoin.getSelectedItem()).text, ((ExchangeSpinnerRowModel) spinnerToCoin.getSelectedItem()).text);
                        startFragment.setMinimumAmount(minimumAmount);
                        navigateToFragment(startFragment);
                    } else if (Common.MAIN_CURRENCY.equalsIgnoreCase(((ExchangeSpinnerRowModel)spinnerFromCoin.getSelectedItem()).symbol)) {
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
            }
        });

        //updateInitializedData();
        Log.d("flint", "spinnerExchange.setSelection");
        spinnerExchange.setSelection(spinnerExchangePosition);
        String serviceSysName = ((ExchangeSpinnerRowModel)spinnerExchange.getSelectedItem()).symbol;
        if ("changelly".equals(serviceSysName))
            loadCurrencies_changelly();
        recreateCoinSpinnerLists();

//        spinnerToCoin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d("flint", "spinnerToCoin.OnClick");
//                try {
//                    Field popup = Spinner.class.getDeclaredField("mPopup");
//                    popup.setAccessible(true);
//                    android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(spinnerToCoin);
//                    popupWindow.setHeight(200);
//                } catch (Exception e) {}
//            }
//        });
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

        loadAllExchangeServices();

        loadIconsUrls();
    }



    private void loadAllExchangeServices() {
        exchangeServicesInitCounter.set(0);
        ShapeshiftManager.getInstance().updateSupportedCoinsList(new Callback<Boolean>() {
            @Override
            public void onResponse(Boolean response) {
                try {
                    isShapeshiftEnabled = false;
                    List<ShapeshiftApi.SupportedCoinModel> coins = ShapeshiftManager.getInstance().getSupportedCoins();
                    for (ShapeshiftApi.SupportedCoinModel c : coins) {
                        if (Common.MAIN_CURRENCY.equalsIgnoreCase(c.symbol)) {
                            isShapeshiftEnabled = true;
                            break;
                        }
                    }
                    exchangeServicesInitCounter.addAndGet(1);
                    sortExchangeServiceSpinner();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        ChangenowManager.getInstance().updateSupportedCoinsList(new Callback<Boolean>() {
            @Override
            public void onResponse(Boolean response) {
                try {
                    isChangenowEnabled = false;
                    List<ChangenowApi.SupportedCoinModel> coins = ChangenowManager.getInstance().getSupportedCoins();
                    for (ChangenowApi.SupportedCoinModel c : coins) {
                        if (Common.MAIN_CURRENCY.equalsIgnoreCase(c.symbol)) {
                            isChangenowEnabled = true;
                            break;
                        }
                    }
                    exchangeServicesInitCounter.addAndGet(1);
                    sortExchangeServiceSpinner();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        ChangellyNetworkManager.getCurrencies(new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    isChangellyEnabled = false;
                    ResponseCurrencyItem responseCurrency = (ResponseCurrencyItem) response;
                    List<CryptoItem> coins = currentCrypto.castResponseCurrencyToCryptoItem(responseCurrency, getActivity().getApplicationContext());
                    for (CryptoItem c : coins) {
                        if (Common.MAIN_CURRENCY.equalsIgnoreCase(c.getCode())) {
                            isChangellyEnabled = true;
                            break;
                        }
                    }
                    exchangeServicesInitCounter.addAndGet(1);
                    sortExchangeServiceSpinner();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(String msg) {
                try {
                    isChangellyEnabled = false;
                    exchangeServicesInitCounter.addAndGet(1);
                    sortExchangeServiceSpinner();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }



    private void sortExchangeServiceSpinner() {
        try {
            final ExchangeFragment thisFragment = this;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (spinnerExchange.getAdapter() != null && exchangeServicesInitCounter.get() >= spinnerExchange.getAdapter().getCount()
                            && thisFragment.getContext() != null) {
                        String curSymbol = ((ExchangeSpinnerRowModel)(spinnerExchange.getAdapter().getItem(spinnerExchange.getSelectedItemPosition()))).symbol;
                        spinnerExchange.setAdapter(new ExchangeSpinnerAdapter(thisFragment.getContext(), createExchangeSpinnerRows()));
                        selectSpinnerWithoutOnSelectedEvent(spinnerExchange, getSpinnerPosBySymbol(spinnerExchange, curSymbol));
                        if (!isSpinnerInitialized) {
                            spinnerExchange.setSelection(0);
                            spinnerExchange_onItemSelected();
                        }
                        updateSelectedPairRate();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void loadCoinsExternalInfo(final Callback<Void> onComplete) {
        final Fragment thisFragment = this;
        ShapeshiftApi.getCoinsExternalInfo(new Callback2<String, Map<String, ShapeshiftApi.CoinExternalInfoModel>>() {
            @Override
            public void onResponse(final String status, final Map<String, ShapeshiftApi.CoinExternalInfoModel> resp) {
                try {
                    thisFragment.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if ("ok".equals(status)) {
                                    cryptoCurrenciesInfo = resp;
                                }
                            } catch (Exception e) {}
                            onComplete.onResponse(null);
                        }
                    });
                } catch (Exception e) {
                    onComplete.onResponse(null);
                }
            }
        });
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
            String serviceSysName = ((ExchangeSpinnerRowModel) spinnerExchange.getSelectedItem()).symbol;
            if (!"changelly".equals(serviceSysName)) {
                for (String key : cryptoCurrenciesInfo.keySet()) {
                    ShapeshiftApi.CoinExternalInfoModel coinExternalInfoModel = cryptoCurrenciesInfo.get(key);
                    if (coinExternalInfoModel.memo != null) {
                        removeCoinFromListBySymbol(coinExternalInfoModel.code, list);
                    }
                }
            }
            spinnerFromCoin.getAdapter().notifyAll();
            spinnerToCoin.getAdapter().notifyAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void spinnerExchange_onItemSelected() {
        String serviceSysName = ((ExchangeSpinnerRowModel)spinnerExchange.getSelectedItem()).symbol;
        if ("changelly".equals(serviceSysName))
            loadCurrencies_changelly();
        else if ("shapeshift".equals(serviceSysName))
            loadCurrencies_shapeshift();
        else
            loadCurrencies_changenow();
        spinnerExchangePosition = spinnerExchange.getSelectedItemPosition();
    }



    private void selectSpinnerWithoutOnSelectedEvent(final Spinner spinner, int pos) {
        final AdapterView.OnItemSelectedListener listener = spinner.getOnItemSelectedListener();
        Handler handler = new Handler();
        spinner.setOnItemSelectedListener(null);
        spinner.setSelection(pos);
        handler.post(new Runnable() {
            @Override
            public void run() {
                spinner.setOnItemSelectedListener(listener);
            }
        });
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
            if (isShapeshiftEnabled)
                res.add(new ExchangeSpinnerRowModel(getResources().getDrawable(R.drawable.ic_icon_image_shapeshift), "ShapeShift", "shapeshift"));
            if (isChangenowEnabled)
                res.add(new ExchangeSpinnerRowModel(getResources().getDrawable(R.drawable.ic_change_now), "Changenow", "changenow"));
            if (isChangellyEnabled)
                res.add(new ExchangeSpinnerRowModel(getResources().getDrawable(R.drawable.ic_icon_image_changelly), "Changelly", "changelly"));
            if (!isShapeshiftEnabled)
                res.add(new ExchangeSpinnerRowModel(getResources().getDrawable(R.drawable.ic_icon_image_shapeshift), "ShapeShift", "shapeshift"));
            if (!isChangellyEnabled)
                res.add(new ExchangeSpinnerRowModel(getResources().getDrawable(R.drawable.ic_icon_image_changelly), "Changelly", "changelly"));
            if (!isChangenowEnabled)
                res.add(new ExchangeSpinnerRowModel(getResources().getDrawable(R.drawable.ic_change_now), "Changenow", "changenow"));
        } catch (Exception e) {
            Log.d("flint", "createExchangeSpinnerRows... exception: " + e.toString());
        }
        return res;
    }



    protected List<ExchangeSpinnerRowModel> createSendSpinnerRows_shapeshift() {
        List<ExchangeSpinnerRowModel> res = new ArrayList<>();
        try {
            List<ShapeshiftApi.SupportedCoinModel> supportedCoins = ShapeshiftManager.getInstance().getSupportedCoins();
            for (ShapeshiftApi.SupportedCoinModel coin : supportedCoins) {
                Drawable coinIcon = getResources().getDrawable(R.drawable.ic_curr_empty);
//                Integer id = getContext().getResources().getIdentifier("ic_" + coin.getCode().toLowerCase(), "drawable", getContext().getPackageName());
                String urlIcon = "";
                if (iconTickerMap.get(coin.symbol.toLowerCase()) != null) {
                    urlIcon = iconTickerMap.get(coin.symbol.toLowerCase()).getIconURL();
                } else {
                    Log.d("psd", "createSendSpinnerRows_changelly: iconTickerMap.get(" + coin.symbol + ") is null");
                }

                res.add(new ExchangeSpinnerRowModel(coinIcon, coin.name, coin.symbol, urlIcon));
            }
            Collections.sort(res, new Comparator<ExchangeSpinnerRowModel>() {
                @Override
                public int compare(ExchangeSpinnerRowModel exchangeSpinnerRowModel, ExchangeSpinnerRowModel t1) {
                    return exchangeSpinnerRowModel.text.compareToIgnoreCase(t1.text);
                }
            });
        } catch (Exception e) {
            Log.d("flint", "createSendSpinnerRows_shapeshift... exception: " + e.toString());
        }
        return res;
    }



    protected List<ExchangeSpinnerRowModel> createSendSpinnerRows_changenow () {
        List<ExchangeSpinnerRowModel> res = new ArrayList<>();
        try {
            List<ChangenowApi.SupportedCoinModel> supportedCoins = ChangenowManager.getInstance().getSupportedCoins();
            for (ChangenowApi.SupportedCoinModel coin : supportedCoins) {
                Drawable coinIcon = getResources().getDrawable(R.drawable.ic_curr_empty);
//                Integer id = getContext().getResources().getIdentifier("ic_" + coin.getCode().toLowerCase(), "drawable", getContext().getPackageName());
                String urlIcon = "";
                if (iconTickerMap.get(coin.symbol.toLowerCase()) != null) {
                    urlIcon = iconTickerMap.get(coin.symbol.toLowerCase()).getIconURL();
                } else {
                    Log.d("psd", "createSendSpinnerRows_changelly: iconTickerMap.get(" + coin.symbol + ") is null");
                }

                res.add(new ExchangeSpinnerRowModel(coinIcon, coin.name, coin.symbol, urlIcon));
            }
            Collections.sort(res, new Comparator<ExchangeSpinnerRowModel>() {
                @Override
                public int compare(ExchangeSpinnerRowModel exchangeSpinnerRowModel, ExchangeSpinnerRowModel t1) {
                    return exchangeSpinnerRowModel.text.compareToIgnoreCase(t1.text);
                }
            });
        } catch (Exception e) {
            Log.d("flint", "createSendSpinnerRows_changenow... exception: " + e.toString());
        }
        return res;
    }



    protected List<ExchangeSpinnerRowModel> createSendSpinnerRows_changelly() {
        Log.d("flint", "createSendSpinnerRows_changelly");
        List<ExchangeSpinnerRowModel> res = new ArrayList<>();
        try {
            for (CryptoItem coin : changellyCoins) {
                Drawable coinIcon = getResources().getDrawable(R.drawable.ic_curr_empty);
//                Integer id = getContext().getResources().getIdentifier("ic_" + coin.getCode().toLowerCase(), "drawable", getContext().getPackageName());
                String urlIcon = "";
                if (iconTickerMap.get(coin.getCode().toLowerCase()) != null) {
                    urlIcon = iconTickerMap.get(coin.getCode().toLowerCase()).getIconURL();
                } else {
                    Log.d("psd", "createSendSpinnerRows_changelly: iconTickerMap.get(" + coin.getCode() + ") is null");
                }

                res.add(new ExchangeSpinnerRowModel(coinIcon, coin.getName(), coin.getCode(), urlIcon));
            }
            Collections.sort(res, new Comparator<ExchangeSpinnerRowModel>() {
                @Override
                public int compare(ExchangeSpinnerRowModel exchangeSpinnerRowModel, ExchangeSpinnerRowModel t1) {
                    return exchangeSpinnerRowModel.text.compareToIgnoreCase(t1.text);
                }
            });
        } catch (Exception e) {
            Log.d("flint", "createSendSpinnerRows_changelly... exception: " + e.toString());
        }
        return res;
    }



    protected void updateSelectedPairRate() {
        String serviceSysName = ((ExchangeSpinnerRowModel) spinnerExchange.getSelectedItem()).symbol;
        Log.d("psd", "updateSelectedPairRate serviceSysName = " + serviceSysName);
        if ("changelly".equals(serviceSysName))
            updateSelectedPairRate_changelly();
        else if ("shapeshift".equals(serviceSysName))
            updateSelectedPairRate_shapeshift();
        else
            updateSelectedPairRate_changenow();
    }



    protected void updateSelectedPairRate_shapeshift() {
        try {
            final String fromCoin = ((ExchangeSpinnerRowModel) (spinnerFromCoin.getAdapter().getItem(spinnerFromCoin.getSelectedItemPosition()))).symbol;
            final String toCoin = ((ExchangeSpinnerRowModel) (spinnerToCoin.getAdapter().getItem(spinnerToCoin.getSelectedItemPosition()))).symbol;
            //textViewExchangeRate.setText("1 " + fromCoin + " ~ " + "..." + " " + toCoin);
            Log.d("psd", "ShapeshiftManager.getInstance().getRate " + fromCoin +  " -> " + toCoin);
            if (!fromCoin.equals(toCoin)) {
                ShapeshiftManager.getInstance().getRate(fromCoin, toCoin, new Callback<ShapeshiftApi.GetRateRespModel>() {
                    @Override
                    public void onResponse(final ShapeshiftApi.GetRateRespModel response) {
                        try {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("psd", "ShapeshiftManager.getInstance().getRate " + fromCoin + " -> " + toCoin + " " + response.rate.toString());
                                    textViewExchangeRateHeader.setVisibility(View.VISIBLE);
                                    textViewExchangeRate.setText("1 " + fromCoin + " ~ " + response.rate.toString() + " " + toCoin);
                                    minimumAmount = response.minimum;
                                }
                            });
                        } catch (Exception e) {
                            disableStartExchangeButton();
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception e) {
            disableStartExchangeButton();
            e.printStackTrace();
        }
    }



    protected void updateSelectedPairRate_changenow() {
        try {
            final String fromCoin = ((ExchangeSpinnerRowModel) (spinnerFromCoin.getAdapter().getItem(spinnerFromCoin.getSelectedItemPosition()))).symbol;
            final String toCoin = ((ExchangeSpinnerRowModel) (spinnerToCoin.getAdapter().getItem(spinnerToCoin.getSelectedItemPosition()))).symbol;
            //textViewExchangeRate.setText("1 " + fromCoin + " ~ " + "..." + " " + toCoin);
            if (!fromCoin.equals(toCoin)) {
                ChangenowManager.getInstance().getRate(fromCoin, toCoin, new Callback<ChangenowApi.GetRateRespModel>() {
                    @Override
                    public void onResponse(final ChangenowApi.GetRateRespModel response) {
                        try {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textViewExchangeRateHeader.setVisibility(View.VISIBLE);
                                    textViewExchangeRate.setText("1 " + fromCoin.toUpperCase() + " ~ " + response.rate.toString() + " " + toCoin.toUpperCase());
                                    if (response.rate.longValue() == 0)
                                        disableStartExchangeButton();
                                }
                            });
                        } catch (Exception e) {
                            disableStartExchangeButton();
                            e.printStackTrace();
                        }
                    }
                });
                ChangenowManager.getInstance().getMinAmount(fromCoin, toCoin, new Callback<ChangenowApi.GetRateRespModel>() {
                    @Override
                    public void onResponse(final ChangenowApi.GetRateRespModel response) {
                        try {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    minimumAmount = response.minimum;
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    protected void updateSelectedPairRate_changelly() {
        try {
            final ExchangeFragment thisFragment = this;
            final String fromCoin = ((ExchangeSpinnerRowModel) (spinnerFromCoin.getAdapter().getItem(spinnerFromCoin.getSelectedItemPosition()))).symbol;
            final String toCoin = ((ExchangeSpinnerRowModel) (spinnerToCoin.getAdapter().getItem(spinnerToCoin.getSelectedItemPosition()))).symbol;
            if (!fromCoin.equals(toCoin)) {
                ChangellyNetworkManager.getMinAmount(fromCoin.toLowerCase(), toCoin.toLowerCase(), new ApiMethods.RequestListener() {
                    @Override
                    public void onSuccess(final Object response) {
                        try {
                            thisFragment.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        ResponseChangellyMinAmount resp = (ResponseChangellyMinAmount) response;
                                        BigDecimal respBidDecimal = BigDecimal.valueOf(resp.getAmount());
                                        minimumAmount = respBidDecimal;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(String msg) {
                    }
                });

                Log.d("psd", "ChangellyNetworkManager.getExchangeAmount " + fromCoin + " -> " + toCoin);
                ChangellyNetworkManager.getExchangeAmount(fromCoin.toLowerCase(), toCoin.toLowerCase(), "1", new ApiMethods.RequestListener() {
                    @Override
                    public void onSuccess(final Object response) {
                        try {
                            thisFragment.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        ResponseChangellyAmount amount = (ResponseChangellyAmount) response;
                                        if (amount.getAmount() != null) {
                                            textViewExchangeRateHeader.setVisibility(View.VISIBLE);
                                            textViewExchangeRate.setText("1 " + fromCoin + " ~ " + amount.getAmount() + " " + toCoin);
                                            Log.d("psd", "ChangellyNetworkManager.getExchangeAmount onSuccess " + fromCoin + " -> " + toCoin + " amount " + amount.getAmount());
                                        } else {
                                            disableStartExchangeButton();
                                        }
                                    } catch (Exception e) {
                                        disableStartExchangeButton();
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            disableStartExchangeButton();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(String msg) {
                        disableStartExchangeButton();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void disableStartExchangeButton() {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textViewExchangeRateHeader.setVisibility(View.INVISIBLE);
                    textViewExchangeRate.setText(getResources().getString(R.string.exchange_service_unavailable));
                }
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



    private void loadCurrencies_changelly() {
        ChangellyNetworkManager.getCurrencies(new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    ResponseCurrencyItem responseCurrency = (ResponseCurrencyItem) response;
                    List<CryptoItem> list = currentCrypto.castResponseCurrencyToCryptoItem(responseCurrency, getActivity().getApplicationContext());
                    Collections.sort(list, new Comparator<CryptoItem>() {
                        @Override
                        public int compare(CryptoItem cryptoItem, CryptoItem t1) {
                            return cryptoItem.getName().compareToIgnoreCase(t1.getName());
                        }
                    });
                    changellyCoins = list;
                    recreateCoinSpinnerLists();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String msg) {}
        });
    }



    private void loadCurrencies_shapeshift() {
        ShapeshiftManager.getInstance().updateSupportedCoinsList(new Callback<Boolean>() {
            @Override
            public void onResponse(Boolean response) {
                try {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recreateCoinSpinnerLists();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }



    private void loadCurrencies_changenow() {
        ChangenowManager.getInstance().updateSupportedCoinsList(new Callback<Boolean>() {
            @Override
            public void onResponse(Boolean response) {
                try {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recreateCoinSpinnerLists();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }



    private void loadIconsUrls() {
        Log.d("psd", "loadIconsUrls");
        if (iconTickerMap.size() == 0) {
            Requestor.getIconsUrls(new ApiMethods.RequestListener() {
                @Override
                public void onSuccess(Object response) {
                    if (isAdded()) {
                        try {
                            iconTickerMap = (HashMap<String, IconItemResponse>) response;
                            updateSpinnerTickerIcons(spinnerFromCoin);
                            updateSpinnerTickerIcons(spinnerToCoin);
                        } catch (Exception e) {
                            Log.e("psd", "getIconsUrls (onSuccess)  error: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(String msg) {
                    Log.e("psd", "getIconsUrls: onFailure" + msg);
                }
            });
        }
    }



    private void updateSpinnerTickerIcons(Spinner spinner) {
        for (int i = 0; i < spinner.getAdapter().getCount(); ++i) {
            ExchangeSpinnerRowModel row = (ExchangeSpinnerRowModel)spinner.getAdapter().getItem(i);
            IconItemResponse urlItem = iconTickerMap.get(row.symbol.toLowerCase());
            if (urlItem != null)
                row.url = urlItem.getIconURL();
        }
        ((ExchangeSpinnerAdapter)spinner.getAdapter()).notifyDataSetChanged();
    }



    private void recreateCoinSpinnerLists() {
        Log.d("flint", "recreateCoinSpinnerLists");
        final ExchangeFragment thisFragment = this;
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String serviceSysName = ((ExchangeSpinnerRowModel) spinnerExchange.getSelectedItem()).symbol;
                    List<ExchangeSpinnerRowModel> coinsList = new ArrayList<>();
                    if ("changelly".equals(serviceSysName)) {
                        coinsList = createSendSpinnerRows_changelly();
                    } else if ("shapeshift".equals(serviceSysName)) {
                        coinsList = createSendSpinnerRows_shapeshift();
                    } else {
                        coinsList = createSendSpinnerRows_changenow();
                    }
                    String fromCoinSymbol = "";
                    String toCoinSymbol = "";
                    if ((spinnerFromCoin.getAdapter() != null) && (spinnerToCoin.getAdapter() != null) && (spinnerFromCoin.getAdapter().getCount() > 0) && (spinnerToCoin.getAdapter().getCount() > 0)) {
                        fromCoinSymbol = ((ExchangeSpinnerRowModel) spinnerFromCoin.getSelectedItem()).symbol;
                        toCoinSymbol = ((ExchangeSpinnerRowModel) spinnerToCoin.getSelectedItem()).symbol;
                    }
                    spinnerFromCoin.setAdapter(new ExchangeSpinnerAdapter(thisFragment.getContext(), coinsList));
                    spinnerToCoin.setAdapter(new ExchangeSpinnerAdapter(thisFragment.getContext(), coinsList));
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
//                    spinnerFromPrevPosition = spinnerFromCoin.getSelectedItemPosition();
//                    spinnerToPrevPosition = spinnerToCoin.getSelectedItemPosition();
//                    //setData(spinnerExchange.getSelectedItemPosition(), spinnerFromPrevPosition, spinnerToPrevPosition);
//                    spinnerExchangePosition = spinnerExchange.getSelectedItemPosition();
//                    spinnerFromCoinPosition = spinnerFromPrevPosition;
//                    spinnerToCoinPosition = spinnerToPrevPosition;
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
