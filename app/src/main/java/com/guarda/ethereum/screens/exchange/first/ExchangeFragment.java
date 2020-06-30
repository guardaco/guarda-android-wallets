package com.guarda.ethereum.screens.exchange.first;

import androidx.lifecycle.ViewModelProviders;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.ChangenowApi;
import com.guarda.ethereum.managers.ChangenowManager;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.models.ExchangeSpinnerRowModel;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.utils.GsonUtils;
import com.guarda.ethereum.views.activity.SharedViewModel;
import com.guarda.ethereum.views.adapters.ExchangeSpinnerAdapter;
import com.guarda.ethereum.views.fragments.ExchangeAboutFragment;
import com.guarda.ethereum.views.fragments.ExchangeInputAddressFragment;
import com.guarda.ethereum.views.fragments.ExchangeStartFragment;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

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
    SharedManager sharedManager;
    @Inject
    GsonUtils gsonUtils;

    private ExchangeSpinnerAdapter exchangeFromSpinnerAdapter;
    private ExchangeSpinnerAdapter exchangeToSpinnerAdapter;
    private ExchangeSpinnerAdapter exchangesAdapter;

    private ExchangeViewModel exchangeViewModel;
    private SharedViewModel sharedViewModel;
    private Map<String, ChangenowApi.SupportedCoinModel> mapCurrencies;
    private SortedMap<String, ArrayList<String>> forCoinMap;
    private List<ExchangeSpinnerRowModel> fromCoinsList = new ArrayList<>();
    private List<ExchangeSpinnerRowModel> toCoinsList = new ArrayList<>();
    private String selectedTicker = "";

    public static final String EXCHANGE_DIVIDER_STRING = "10000";
    public static final Double EXCHANGE_DIVIDER_DOUBLE = Double.parseDouble(EXCHANGE_DIVIDER_STRING);

    public ExchangeFragment() { GuardaApp.getAppComponent().inject(this); }

    @Override
    protected int getLayout() { return R.layout.fragment_exchange; }

    @Override
    protected void init () {
        ExchangeViewModel.Factory factory = new ExchangeViewModel.Factory(sharedManager, gsonUtils, Common.MAIN_CURRENCY);
        exchangeViewModel = ViewModelProviders.of(this, factory).get(ExchangeViewModel.class);
        sharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        subscribeUi();

        setToolbarTitle(getString(R.string.title_purchase));
        textViewExchange.setText(getString(R.string.exchange_choose_service));
        exchangesAdapter = new ExchangeSpinnerAdapter(this.getContext(), createExchangeSpinnerRows());
        spinnerExchange.setAdapter(exchangesAdapter);
        textViewSend.setText(getString(R.string.exchange_send));
        textViewReceive.setText(getString(R.string.exchange_receive));
        textViewExchangeRateHeader.setText(getString(R.string.exchange_rate));
        buttonStartExchange.setText(getString(R.string.exchange_start));

        initSpinners();
        initMenuButton();
    }

    private void initSpinners() {
        spinnerFromCoin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateSelectedPairRate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        spinnerToCoin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateSelectedPairRate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
    }

    @OnClick(R.id.buttonStartExchange)
    public void exchange(View v) {
        try {
            String exchange = ((ExchangeSpinnerRowModel) spinnerExchange.getSelectedItem()).symbol;
            ChangenowApi.SupportedCoinModel coinFrom = mapCurrencies.get(((ExchangeSpinnerRowModel) spinnerFromCoin.getSelectedItem()).symbol);
            ChangenowApi.SupportedCoinModel coinTo = mapCurrencies.get(((ExchangeSpinnerRowModel) spinnerToCoin.getSelectedItem()).symbol);
            sharedViewModel.selectExchange(exchange);
            sharedViewModel.selectFrom(coinFrom);
            sharedViewModel.selectTo(coinTo);
            if (Common.MAIN_CURRENCY.equalsIgnoreCase(((ExchangeSpinnerRowModel) spinnerToCoin.getSelectedItem()).symbol)) {
                navigateToFragment(new ExchangeStartFragment());
            } else if (Common.MAIN_CURRENCY.equalsIgnoreCase(((ExchangeSpinnerRowModel) spinnerFromCoin.getSelectedItem()).symbol)) {
                navigateToFragment(new ExchangeInputAddressFragment());
            } else {
                Toast.makeText(getContext(), getString(R.string.exchange_unsupported_pair), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Timber.e("buttonStartExchange error=%s", e.getMessage());
            e.printStackTrace();
        }
    }

    @OnClick(R.id.img_change)
    public void changeDirection(View v) {
        if (exchangeFromSpinnerAdapter == null) return;
        if (exchangeFromSpinnerAdapter.getCount() == 1) {
            fillSpinnersToCoin();
        } else {
            fillSpinnersFromCoin();
        }

        updateSelectedPairRate();
        textViewExchangeRate.setText("");
    }

    private void subscribeUi() {
        exchangeViewModel.currencies.observe(getViewLifecycleOwner(), (map) -> {
            mapCurrencies = map;
            exchangeViewModel.getPairs();
        });

        exchangeViewModel.forCoinMap.observe(getViewLifecycleOwner(), (map) -> {
            forCoinMap = map;
            prepareListsForAdapters();
            initAdapters();
        });
    }

    private void prepareListsForAdapters() {
        if (mapCurrencies == null) return;
        List<String> fromCoins = forCoinMap.get("from");
        if (fromCoins != null) {
            for (String ticker: fromCoins) {
                ChangenowApi.SupportedCoinModel coin = mapCurrencies.get(ticker);
                if (coin == null) continue;
                fromCoinsList.add(new ExchangeSpinnerRowModel(coin.name, coin.symbol, coin.imageUrl));
            }
            Timber.d("prepareListsForAdapters fromCoins=%d, fromCoinsList=%d", fromCoins.size(), fromCoinsList.size());
        }
        List<String> toCoins = forCoinMap.get("to");
        if (toCoins != null) {
            for (String ticker: toCoins) {
                ChangenowApi.SupportedCoinModel coin = mapCurrencies.get(ticker);
                if (coin == null) continue;
                toCoinsList.add(new ExchangeSpinnerRowModel(coin.name, coin.symbol, coin.imageUrl));
            }
            Timber.d("prepareListsForAdapters toCoins=%d, toCoinsList=%d", toCoins.size(), toCoinsList.size());
        }
    }

    private void initAdapters() {
        ExchangeSpinnerRowModel rowModel = getMainCurrencyRow();
        if (rowModel == null) return;
        exchangeFromSpinnerAdapter = new ExchangeSpinnerAdapter(getContext(), Collections.singletonList(rowModel));
        exchangeToSpinnerAdapter = new ExchangeSpinnerAdapter(getContext(), toCoinsList);
        spinnerFromCoin.setAdapter(exchangeFromSpinnerAdapter);
        spinnerToCoin.setAdapter(exchangeToSpinnerAdapter);
    }

    private void fillSpinnersFromCoin() {
        selectedTicker = ((ExchangeSpinnerRowModel) spinnerFromCoin.getSelectedItem()).symbol;
        ExchangeSpinnerRowModel rowModel = getMainCurrencyRow();
        if (rowModel == null) return;
        exchangeFromSpinnerAdapter.updateRows(Collections.singletonList(rowModel));
        exchangeToSpinnerAdapter.updateRows(toCoinsList);
        spinnerToCoin.setSelection(getSelectedTickerPosition(toCoinsList, selectedTicker));
    }

    private void fillSpinnersToCoin() {
        selectedTicker = ((ExchangeSpinnerRowModel) spinnerToCoin.getSelectedItem()).symbol;
        ExchangeSpinnerRowModel rowModel = getMainCurrencyRow();
        if (rowModel == null) return;
        exchangeFromSpinnerAdapter.updateRows(fromCoinsList);
        exchangeToSpinnerAdapter.updateRows(Collections.singletonList(rowModel));
        spinnerFromCoin.setSelection(getSelectedTickerPosition(fromCoinsList, selectedTicker));
    }

    private ExchangeSpinnerRowModel getMainCurrencyRow() {
        ChangenowApi.SupportedCoinModel coin = mapCurrencies.get(Common.MAIN_CURRENCY);
        if (coin == null) return null;
        return new ExchangeSpinnerRowModel(coin.name, coin.symbol, coin.imageUrl);
    }

    private int getSelectedTickerPosition(List<ExchangeSpinnerRowModel> list, String selectedTicker) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).symbol.equalsIgnoreCase(selectedTicker)) return i;
        }
        return 0;
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

    protected void updateSelectedPairRate() {
        String serviceSysName = ((ExchangeSpinnerRowModel) spinnerExchange.getSelectedItem()).symbol;
        Log.d("psd", "updateSelectedPairRate serviceSysName = " + serviceSysName);
        updateSelectedPairRateChangenow();
    }

    protected void updateSelectedPairRateChangenow() {
        try {
            final String fromCoin = ((ExchangeSpinnerRowModel) (spinnerFromCoin.getAdapter().getItem(spinnerFromCoin.getSelectedItemPosition()))).symbol;
            final String toCoin = ((ExchangeSpinnerRowModel) (spinnerToCoin.getAdapter().getItem(spinnerToCoin.getSelectedItemPosition()))).symbol;
            if (!fromCoin.equals(toCoin)) {
                ChangenowManager.getInstance().getRate(fromCoin, toCoin, response -> {
                        try {
                            getActivity().runOnUiThread(() -> {
                                textViewExchangeRateHeader.setVisibility(View.VISIBLE);
                                BigDecimal rate = response.rate.divide(BigDecimal.valueOf(EXCHANGE_DIVIDER_DOUBLE), BigDecimal.ROUND_DOWN);
                                textViewExchangeRate.setText("1 " + fromCoin.toUpperCase() + " ~ " + rate.toString() + " " + toCoin.toUpperCase());
                                Timber.d("updateSelectedPairRateChangenow response.rate.longValue() = %s", response.rate.toPlainString());
                                if (response.rate.compareTo(BigDecimal.ZERO) == 0)
                                    disableStartExchangeButton();
                            });
                        } catch (Exception e) {
                            disableStartExchangeButton();
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

    @OnClick(R.id.imageViewAbout)
    public void about(View v) {
        try {
            navigateToFragment(new ExchangeAboutFragment());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
