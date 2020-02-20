package com.guarda.ethereum.views.fragments;


import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.customviews.GuardaInputLayout;
import com.guarda.ethereum.managers.Callback2;
import com.guarda.ethereum.managers.ChangellyNetworkManager;
import com.guarda.ethereum.managers.ChangenowApi;
import com.guarda.ethereum.managers.IndacoinManager;
import com.guarda.ethereum.managers.ShapeshiftApi;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.items.CoinifyPaysResponse;
import com.guarda.ethereum.models.items.CoinifyQuoteResponse;
import com.guarda.ethereum.models.items.ResponseChangellyAmount;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;
import com.guarda.ethereum.utils.CalendarHelper;
import com.guarda.ethereum.utils.KeyboardManager;
import com.guarda.ethereum.views.activity.AddrBtcCoinifyActivity;
import com.guarda.ethereum.views.activity.ConfirmCoinifyActivity;
import com.guarda.ethereum.views.activity.EnterNameToPurchaseActivity;
import com.guarda.ethereum.views.activity.PurchaseWemovecoinsActivity;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import timber.log.Timber;

import static com.guarda.ethereum.models.constants.Common.ETH_SHOW_PATTERN;

@AutoInjector(GuardaApp.class)
public class PurchaseCoinsFragment extends BaseFragment {

    @BindView(R.id.et_amount_to_purchase)
    EditText etAmountToPurchase;
    @BindView(R.id.tv_approximately_balance)
    TextView tvApproximatelyBalance;
    @BindView(R.id.tv_commission)
    TextView tvCommission;
    @BindView(R.id.btn_purchase)
    Button btnPurchase;
    @BindView(R.id.gi_input_layout)
    GuardaInputLayout inputLayout;
    @BindView(R.id.sp_wemove_currency)
    Spinner spCurrency;
    @BindView(R.id.ll_chb_alt)
    LinearLayout ll_chb_alt;
    @BindView(R.id.chb_alt)
    CheckBox chb_alt;
    @BindView(R.id.chb_alt_text)
    TextView chb_alt_text;

    @Inject
    SharedManager sharedManager;
    @Inject
    WalletManager walletManager;

    private double exchangeCommission = 1.5;
    private double wemovecoinCommission = 7.5;
    private double realCurrencyToBtc = 0;
    private double btcToCurrentCrypto = 0;
    private float indaMinAmount = 0.0f;
    private float indaLimitAmount = 0.0f;

    private long coinifyQuoteExpiryTime = 0;
    private int coinifyQuoteId = 0;
    private float coinifyQuoteAmount = 0;
    private String currentCurrency = "";
    private float coinifyCardMinAmount = 0.0f;
    private float coinifyCardLimitAmount = -1.0f;
    private float coinifyBankMinAmount = 0.0f;
    private float coinifyBankLimitAmount = -1.0f;
    private Map<String, Float> minimumInAmounts = new HashMap<>();
    private Map<String, Float> limitsInAmounts = new HashMap<>();
    private String coinifySelectedMethod = "card";
    private String choosedCrypto = "";

    private PurchaseServiceFragment prevFragment = null;
    private EnterIndacoinEmailFragment indaPrevFragment = null;
    private PayMethodsCoinifyFragment coinifyPrevFragment = null;

    public PurchaseCoinsFragment() {
        GuardaApp.getAppComponent().inject(this);
    }

    public void setPrevFragment(PurchaseServiceFragment prevFragment) {
        this.prevFragment = prevFragment;
    }

    public void setIndaPrevFragment(EnterIndacoinEmailFragment prevFragment) {
        this.indaPrevFragment = prevFragment;
    }

    public void setCoinifyPrevFragment(PayMethodsCoinifyFragment prevFragment) {
        this.coinifyPrevFragment = prevFragment;
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_purchase_coins;
    }

    @Override
    protected void init() {
        Log.d("flint", "PurchaseCoinsFragment.init()...");
        Log.d("flint", "  prevFragment: " + prevFragment);

        choosedCrypto = Common.MAIN_CURRENCY;

        if (prevFragment != null)
            Log.d("flint", "  prevFragment.selectedService: " + prevFragment.selectedService);
        if (coinifyPrevFragment != null)
            Log.d("flint", "  coinifyPrevFragment.selectedService: " + coinifyPrevFragment.selectedService);

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        String[] array0 = getResources().getStringArray(R.array.wemovecoins_currencies);
        if (indaPrevFragment != null && "indacoin".equals(indaPrevFragment.selectedService))
            array0 = new String[]{"USD"};
        if (coinifyPrevFragment != null && "coinify".equals(coinifyPrevFragment.selectedService)) {
            array0 = new String[]{"USD", "EUR", "DKK", "GBP"};
            wemovecoinCommission = 3.0;
            coinifySelectedMethod = coinifyPrevFragment.selectedMethod;
            if (coinifySelectedMethod.equalsIgnoreCase("card")) {
                wemovecoinCommission = 3.0;
            } else if (coinifySelectedMethod.equalsIgnoreCase("bank")) {
                wemovecoinCommission = 0.25;
            }

            //for coinify default currency without exchange
            choosedCrypto = "btc";

            final PurchaseCoinsFragment thisFragment = this;

            SpannableStringBuilder spanTxt = new SpannableStringBuilder(String.format(getString(R.string.coinify_chb_altcoin), Common.MAIN_CURRENCY.toUpperCase()));
            spanTxt.append(" ");
            spanTxt.append("More");
            spanTxt.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    Bundle bundle = new Bundle();
                    bundle.putString(Extras.EXPLAIN_TITLE, "In-app exchange");
                    bundle.putString(Extras.EXPLAIN_TEXT, getString(R.string.coinify_expl_exch_text));

                    ExplainFragment explainFragment = new ExplainFragment();
                    explainFragment.setArguments(bundle);
                    explainFragment.setPrevFragment(thisFragment);

                    navigateToFragment(explainFragment);
                }
            }, spanTxt.length() - "More".length(), spanTxt.length(), 0);

            chb_alt_text.setMovementMethod(LinkMovementMethod.getInstance());
            chb_alt_text.setText(spanTxt, TextView.BufferType.SPANNABLE);

            if (!Common.MAIN_CURRENCY.equalsIgnoreCase("btc")) {
                ll_chb_alt.setVisibility(View.VISIBLE);
            }

            chb_alt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    enableLoadingView(false);

                    if (isChecked) {
                        choosedCrypto = Common.MAIN_CURRENCY;
                    } else {
                        choosedCrypto = "btc";
                    }
                    updateApproximatelyBalance(currentCurrency);
                }
            });
        }

        final String[] array = array0;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.view_sp_buy_currency_by_card_item, array);
        spCurrency.setAdapter(adapter);

        spCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                enableLoadingView(false);
                currentCurrency = array[position];
                updateApproximatelyBalance(currentCurrency);
                hideError(etAmountToPurchase);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        inputLayout.setInputListener(new GuardaInputLayout.onGuardaInputLayoutListener() {
            @Override
            public void onTextChanged(String inputText) {
                etAmountToPurchase.setText(inputText);
            }
        });

        KeyboardManager.disableKeyboardByClickView(etAmountToPurchase);

        etAmountToPurchase.requestFocus();
        etAmountToPurchase.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etAmountToPurchase.setSelection(count);
                setApproximatelySum(s.length() > 0 ? Double.valueOf(s.toString()) : 0);
                hideError(etAmountToPurchase);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        setApproximatelySum(0);

        setToolbarTitle(getString(R.string.app_amount_to_purchase));
        initBackButton();
    }

    @OnClick(R.id.btn_purchase)
    public void sendClick(View view) {
        String amount = etAmountToPurchase.getText().toString();
        if (!amount.isEmpty()) {
            if (Double.parseDouble(amount) > 0) {
                String serviceName = "wemovecoins";
                if (prevFragment != null)
                    serviceName = prevFragment.selectedService;
                if (indaPrevFragment != null)
                    serviceName = indaPrevFragment.selectedService;
                if (coinifyPrevFragment != null)
                    serviceName = coinifyPrevFragment.selectedService;
                if ("wemovecoins".equals(serviceName)) {
                    Intent intent = new Intent(getActivity(), EnterNameToPurchaseActivity.class);
                    intent.putExtra(Extras.PURCHASE_SERVICE, serviceName);
                    intent.putExtra(Extras.PURCHASE_COINS, amount);
                    intent.putExtra(Extras.PURCHASE_CURR, spCurrency.getSelectedItem().toString());
                    startActivity(intent);
                } else if ("indacoin".equals(serviceName)) {
                    if (Float.parseFloat(amount) >= indaMinAmount) {
                        if (indaLimitAmount == 0.0f || Float.parseFloat(amount) <= indaLimitAmount) {
                            Intent intent = new Intent(getActivity(), PurchaseWemovecoinsActivity.class);
                            intent.putExtra(Extras.PURCHASE_SERVICE, getArguments().getString(Extras.PURCHASE_SERVICE));
                            intent.putExtra(Extras.PURCHASE_COINS, amount);
                            intent.putExtra(Extras.USER_FULL_NAME, "anon anon");
                            intent.putExtra(Extras.PURCHASE_CURR, spCurrency.getSelectedItem().toString());
                            intent.putExtra(Extras.USER_EMAIL, getArguments().getString(Extras.USER_EMAIL));
                            intent.putExtra(Extras.COUNTRY_PHONE_CODE, "");
                            intent.putExtra(Extras.USER_PHONE, "");
                            startActivity(intent);

//                    Intent intent = new Intent(getActivity(), EnterEmailToPurchaseActivity.class);
//                    intent.putExtra(Extras.PURCHASE_SERVICE, serviceName);
//                    intent.putExtra(Extras.PURCHASE_COINS, amount);
//                    intent.putExtra(Extras.PURCHASE_CURR, spCurrency.getSelectedItem().toString());
//                    intent.putExtra(Extras.USER_FULL_NAME, "anon anon");
//                    startActivity(intent);
                        } else {
                            showError(etAmountToPurchase, "Limit for USD is " + indaLimitAmount);
                        }
                    } else {
                        showError(etAmountToPurchase, "Minimum for USD is " + indaMinAmount);
                    }
                } else if ("coinify".equals(serviceName)) {
                    //coinify
                    if (coinifySelectedMethod.equalsIgnoreCase("card")) {
                        if (coinifyCardLimitAmount == -1.0f || Float.parseFloat(amount) <= coinifyCardLimitAmount) {
                            if (Float.parseFloat(amount) >= coinifyCardMinAmount) {
                                showProgress();
                                coinifyQuoteAmount = - Float.parseFloat(etAmountToPurchase.getText().toString());
                                coinifyQuote(currentCurrency, "BTC", coinifyQuoteAmount, false);
                            } else {
                                showError(etAmountToPurchase, "Minimal amount for " + currentCurrency.toUpperCase() + " is " + coinifyCardMinAmount);
                            }
                        } else {
                            showError(etAmountToPurchase, "Limit for " + currentCurrency.toUpperCase() + " is " + coinifyCardLimitAmount);
                        }
                    } else if (coinifySelectedMethod.equalsIgnoreCase("bank")) {
                        if (coinifyBankLimitAmount == -1.0f || Float.parseFloat(amount) <= coinifyBankLimitAmount) {
                            if (Float.parseFloat(amount) >= coinifyBankMinAmount) {
                                showProgress();
                                coinifyQuoteAmount = - Float.parseFloat(etAmountToPurchase.getText().toString());
                                coinifyQuote(currentCurrency, "BTC", coinifyQuoteAmount, false);
                            } else {
                                showError(etAmountToPurchase, "Minimal amount for " + currentCurrency.toUpperCase() + " is " + coinifyBankMinAmount);
                            }
                        } else {
                            showError(etAmountToPurchase, "Limit for " + currentCurrency.toUpperCase() + " is " + coinifyBankLimitAmount);
                        }
                    }
                }
                getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.no_slide);
            } else {
                showError(etAmountToPurchase, getString(R.string.withdraw_amount_can_not_be_empty));
            }
        } else {
            showError(etAmountToPurchase, getString(R.string.withdraw_amount_can_not_be_empty));
        }
    }

    private void updateApproximatelyBalance(final String currency) {
        if (indaPrevFragment != null && "indacoin".equals(indaPrevFragment.selectedService)) {
            String currencyTo = "btc";
            IndacoinManager.getEstimate("150", currencyTo, new Callback2<String, IndacoinManager.GetEstimateRespModel>() {
                @Override
                public void onResponse(String status, IndacoinManager.GetEstimateRespModel resp) {
                    if (resp != null) {
                        BigDecimal number = new BigDecimal(resp.estimate);
                        realCurrencyToBtc = 150.0/number.doubleValue();
                        getChangellyExchangeAmount_changenow();
                    } else {
                        Timber.e("IndacoinManager.getEstimate resp=null");
                    }
                }
            });
            if (getArguments() != null) {
                getIndaLimits(getArguments().getString(Extras.USER_EMAIL),"btc");
            }
        } else if (coinifyPrevFragment != null && "coinify".equals(coinifyPrevFragment.selectedService)) {
            String currencyTo = "BTC";
            if (etAmountToPurchase.getText().toString().isEmpty()) {
                coinifyQuoteAmount = -1000.0f;
            } else {
                coinifyQuoteAmount = - Float.parseFloat(etAmountToPurchase.getText().toString());
            }
            coinifyQuote(currency, currencyTo, coinifyQuoteAmount, true);
//            if ((coinifySelectedMethod.equalsIgnoreCase("card") && (coinifyCardMinAmount == 0.0f || coinifyCardLimitAmount == -1.0f)) ||
//                    (coinifySelectedMethod.equalsIgnoreCase("bank") && (coinifyBankMinAmount == 0.0f || coinifyBankLimitAmount == -1.0f))) {
//                coinifyPays();
//            }
            if (minimumInAmounts.size() == 0 || limitsInAmounts.size() == 0) {
                coinifyPays();
            } else {
                setMinMaxAmount();
            }
        } else {
            Requestor.getBtcRate(currency.toUpperCase(), new ApiMethods.RequestListener() {
                @Override
                public void onSuccess(Object response) {
                    Log.d("flint", "PurchaseCoinsFragment.updateApproximatelyBalance()... onSuccess");
                    if (isVisible()) {
                        String btcRateStr = "0";
                        try {
                            btcRateStr = ((ResponseBody) response).string();
                            String[] split = btcRateStr.split(" ");
                            btcRateStr = split[0];
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {

                            Locale locale;
                            btcRateStr = btcRateStr.replace(".", " ");

                            if (currency.equalsIgnoreCase(CountryCurrency.DENMARK.getCurrencyName())) {
                                locale = CountryCurrency.DENMARK.getLocale();
                            } else if (currency.equalsIgnoreCase(CountryCurrency.NORWAY.getCurrencyName())) {
                                locale = CountryCurrency.NORWAY.getLocale();
                            } else if (currency.equalsIgnoreCase(CountryCurrency.SWEDEN.getCurrencyName())) {
                                locale = CountryCurrency.SWEDEN.getLocale();
                            } else if (currency.equalsIgnoreCase(CountryCurrency.UNITED_KINGDOM.getCurrencyName())) {
                                locale = CountryCurrency.UNITED_KINGDOM.getLocale();
                            } else if (currency.equalsIgnoreCase(CountryCurrency.INDIA.getCurrencyName())) {
                                locale = CountryCurrency.INDIA.getLocale();
                            } else {
                                locale = CountryCurrency.EUROPE.getLocale();

                            }

                            NumberFormat format = NumberFormat.getInstance(locale);
                            Number number = format.parse(btcRateStr);

                            realCurrencyToBtc = number.doubleValue();
                            getChangellyExchangeAmount_changenow();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Can't parse this format", Toast.LENGTH_LONG).show();
                        }
                    }

                }

                @Override
                public void onFailure(String msg) {
                    Log.d("flint", "PurchaseCoinsFragment.updateApproximatelyBalance()... onFailure");
                }
            });
        }
    }

    private void getChangellyExchangeAmount() {
        String amount = "1";
        Log.d("flint", "PurchaseCoinsFragment.getChangellyExchangeAmount()...");
        ChangellyNetworkManager.getExchangeAmount("btc", Common.MAIN_CURRENCY, amount, new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                if (isVisible()) {
                    ResponseChangellyAmount amount = (ResponseChangellyAmount) response;
                    if (amount.getAmount() != null) {
                        if (!"0".equals(amount.getAmount())) {
                            getChangellyExchangeAmount_onSuccess(amount.getAmount());
                            return;
                        }
                    }
                }
                getChangellyExchangeAmount_changenow();
            }

            @Override
            public void onFailure(String msg) {
                getChangellyExchangeAmount_changenow();
            }
        });
    }

    private void getChangellyExchangeAmount_changenow() {
        Log.d("flint", "PurchaseCoinsFragment.getChangellyExchangeAmount_changenow()...");
        ChangenowApi.getRate("btc", Common.MAIN_CURRENCY, new Callback2<String, ChangenowApi.GetRateRespModel>() {
            @Override
            public void onResponse(String status, ChangenowApi.GetRateRespModel resp) {
                if ("ok".equals(status))
                    getChangellyExchangeAmount_onSuccess(resp.rate.toString());
//                else
//                    getChangellyExchangeAmount_shapeshift();
            }
        });
    }

    private void getChangellyExchangeAmount_shapeshift() {
        Log.d("flint", "PurchaseCoinsFragment.getChangellyExchangeAmount_shapeshift()...");
        ShapeshiftApi.getRate("btc", Common.MAIN_CURRENCY, new Callback2<String, ShapeshiftApi.GetRateRespModel>() {
            @Override
            public void onResponse(String status, ShapeshiftApi.GetRateRespModel resp) {
                if ("ok".equals(status))
                    getChangellyExchangeAmount_onSuccess(resp.rate.toString());
            }
        });
    }

    private void getChangellyExchangeAmount_onSuccess(final String amount) {
        final Fragment thisFragment = this;
        try {
            thisFragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        enableLoadingView(true);
                        //Log.d("!!!!!", "changelly amount price " + amount);
                        btcToCurrentCrypto = Double.valueOf(amount);
                        setApproximatelySum(etAmountToPurchase.getText().length() > 0 ? Double.valueOf(etAmountToPurchase.getText().toString()) : 0);
                    } catch (Exception e) {
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getIndaLimits(String email, String currencyTo) {
        IndacoinManager.getLimits(email, currencyTo, new Callback2<String, IndacoinManager.GetLimitsRespModel>() {
            @Override
            public void onResponse(String status, IndacoinManager.GetLimitsRespModel resp) {
                if (resp != null) {
                    indaLimitAmount = Float.parseFloat(resp.max);
                    indaMinAmount = Float.parseFloat(resp.min);
                }
            }
        });
    }

    private void coinifyAuth(String grantType, String offlineToken) {
//        Requestor.coinifyAuth(grantType, offlineToken, new ApiMethods.RequestListener() {
//            @Override
//            public void onSuccess(Object response) {
//                CoinifyAuthResponse car = (CoinifyAuthResponse) response;
//                sharedManager.setCoinifyAccessTokenLifeTime(System.currentTimeMillis() +
//                        Long.parseLong(car.getExpires_in()) -
//                        Coinify.DELTA_ACCESS_TOKEN_LIFE_TIME);
//                sharedManager.setCoinifyAccessToken(car.getAccess_token());
//
//                if (System.currentTimeMillis() > coinifyQuoteExpiryTime) {
//                    coinifyQuote(currentCurrency, "btc", -1000);
//                } else {
//                    getChangellyExchangeAmount();
//                }
//            }
//
//            @Override
//            public void onFailure(String msg) {
//                Toast.makeText(getActivity(), "Service is temporarily unavailable", Toast.LENGTH_SHORT).show();
//                Log.d("psd", "PurchaseCoins coinifyAuth onFailure - " + msg);
//            }
//        });
    }

    private void coinifyQuote(final String baseCurrency, String quoteCurrency, final float baseAmount, final boolean withExch) {
        JsonObject coinifyQuote = new JsonObject();
        coinifyQuote.addProperty("baseCurrency", baseCurrency);
        coinifyQuote.addProperty("quoteCurrency", quoteCurrency);
        coinifyQuote.addProperty("baseAmount", baseAmount);

        Requestor.coinifyQuote(sharedManager.getCoinifyAccessToken(), coinifyQuote, new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                CoinifyQuoteResponse cqr = (CoinifyQuoteResponse) response;
                coinifyQuoteExpiryTime = CalendarHelper.jodaTimeToMilliseconds(cqr.getExpiryTime());
                coinifyQuoteId = cqr.getId();
                realCurrencyToBtc = -baseAmount/cqr.getQuoteAmount();

                if (withExch) {
                    if (!choosedCrypto.equalsIgnoreCase("btc")) {
                        getChangellyExchangeAmount_changenow();
                    } else {
                        enableLoadingView(true);
                        btcToCurrentCrypto = 1;
                        setApproximatelySum(etAmountToPurchase.getText().length() > 0 ? Double.valueOf(etAmountToPurchase.getText().toString()) : 0);
                    }
                } else {
                    enableLoadingView(true);
                    if (!choosedCrypto.equalsIgnoreCase("btc") || Common.MAIN_CURRENCY.equalsIgnoreCase("btc")) {
                        Intent intent = new Intent(getActivity(), ConfirmCoinifyActivity.class);
                        intent.putExtra(Extras.COINIFY_IN_AMOUNT, - baseAmount);
                        intent.putExtra(Extras.COINIFY_IN_AMOUNT_CUR, baseCurrency);
                        intent.putExtra(Extras.COINIFY_OUT_AMOUNT, tvApproximatelyBalance.getText().toString());
                        intent.putExtra(Extras.COINIFY_AMOUNT_FEE, String.valueOf(exchangeCommission));
                        intent.putExtra(Extras.COINIFY_QUOTE_ID, cqr.getId());
                        intent.putExtra(Extras.COINIFY_PAY_METHOD, coinifySelectedMethod);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(getActivity(), AddrBtcCoinifyActivity.class);
                        intent.putExtra(Extras.COINIFY_IN_AMOUNT, - baseAmount);
                        intent.putExtra(Extras.COINIFY_IN_AMOUNT_CUR, baseCurrency);
                        intent.putExtra(Extras.COINIFY_OUT_AMOUNT, tvApproximatelyBalance.getText().toString());
                        intent.putExtra(Extras.COINIFY_AMOUNT_FEE, String.valueOf(exchangeCommission));
                        intent.putExtra(Extras.COINIFY_QUOTE_ID, cqr.getId());
                        intent.putExtra(Extras.COINIFY_PAY_METHOD, coinifySelectedMethod);
                        startActivity(intent);
                    }

                }

                closeProgress();

                if (withExch)
//                    tradesList();
//                    coinifyTrade();


                    Log.d("psd", "cqr.getExpiryTime(): " + cqr.getExpiryTime() + " millis: " + coinifyQuoteExpiryTime);
                Log.d("psd", "cqr.getQuoteAmount(): " + cqr.getQuoteAmount() + " realCurrencyToBtc: " + realCurrencyToBtc);
            }

            @Override
            public void onFailure(String msg) {
                enableLoadingView(true);
//                if (withExch)
                closeProgress();
                try {
                    JsonParser jp = new JsonParser();
                    JsonObject jo = jp.parse(msg).getAsJsonObject();
                    Toast.makeText(getActivity(), jo.get("error_description").toString(), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Service is temporarily unavailable", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    Log.d("psd", "coinifyQuote - onFailure: json parse " + msg);
                }

                Log.d("psd", "coinifyQuote - onFailure: " + msg);
            }
        });
    }

    private void coinifyPays() {
        Requestor.coinifyPays(sharedManager.getCoinifyAccessToken(), new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                enableLoadingView(true);

                List<CoinifyPaysResponse> cprList = (List<CoinifyPaysResponse>) response;
                for (CoinifyPaysResponse cpr : cprList) {
                    if (cpr.getInMedium().equalsIgnoreCase(coinifySelectedMethod) && cpr.getOutMedium().equalsIgnoreCase("blockchain")) {
                        minimumInAmounts = cpr.getMinimumInAmounts();
                        limitsInAmounts = cpr.getLimitInAmounts();
                        setMinMaxAmount();
                    }
                }
                Log.d("psd", "coinifyPays: onSuccess - " + "card: " + coinifyCardMinAmount + " " + coinifyCardLimitAmount + " bank: " + coinifyBankMinAmount + " " + coinifyBankLimitAmount);
            }

            @Override
            public void onFailure(String msg) {
                enableLoadingView(true);

                Log.d("psd", "coinifyPays - onFailure: " + msg);
            }
        });
    }

    private void setApproximatelySum(double sum) {
        if (!isAdded()) return;

        double approximatelySum = 0;
        double fullCommission = 0;
        if (sum > 0) {
            approximatelySum = (sum / realCurrencyToBtc) * btcToCurrentCrypto;
            fullCommission = (approximatelySum / 100) * (wemovecoinCommission + exchangeCommission);

            if (coinifyPrevFragment != null && "coinify".equals(coinifyPrevFragment.selectedService)) {
                //Coinify transaction fee = 0.0001 BTC
                fullCommission = fullCommission + 0.0001 * btcToCurrentCrypto;
            }
        }

        approximatelySum = approximatelySum - fullCommission;

        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        DecimalFormat decimalFormat = new DecimalFormat(ETH_SHOW_PATTERN, symbols);
        decimalFormat.setRoundingMode(RoundingMode.DOWN);

        StringBuilder approximately = new StringBuilder();
        approximately.append(getString(R.string.you_will_approximately_get));
        approximately.append(" ~").append(decimalFormat.format(approximatelySum));
        approximately.append(" " + choosedCrypto.toUpperCase());

        StringBuilder commission = new StringBuilder();
        commission.append(getString(R.string.commission));
        commission.append(" ~").append(decimalFormat.format(fullCommission));
        commission.append(" " + choosedCrypto.toUpperCase());

        tvApproximatelyBalance.setText(approximately.toString());
        tvCommission.setText(commission.toString());
    }

    private void setMinMaxAmount() {
        if (coinifySelectedMethod.equalsIgnoreCase("card")) {
            coinifyCardMinAmount = minimumInAmounts.get(currentCurrency.toUpperCase());
            coinifyCardLimitAmount = limitsInAmounts.get(currentCurrency.toUpperCase());
        } else if (coinifySelectedMethod.equalsIgnoreCase("bank")) {
            coinifyBankMinAmount = minimumInAmounts.get(currentCurrency.toUpperCase());
            coinifyBankLimitAmount = limitsInAmounts.get(currentCurrency.toUpperCase());
        }
    }

    private enum CountryCurrency {

        EUROPE("Europe", "EUR",  Locale.ENGLISH),
        DENMARK("Denmark", "DKK", new Locale("en", "DK")),
        NORWAY("Norway", "NOK", new Locale("en", "NO")),
        SWEDEN("Sweden", "SEK", new Locale("en", "SE")),
        UNITED_KINGDOM("United Kingdom", "GBP", Locale.UK),
        INDIA("India", "INR", new Locale("en", "IN"));

        private String countryName;
        private String countryCurrCode;
        private Locale locale;

        CountryCurrency(String countryName, String countryCurrCode, Locale locale) {
            this.countryName = countryName;
            this.countryCurrCode = countryCurrCode;
            this.locale = locale;
        }

        public Locale getLocale() {
            return this.locale;
        }

        public String getCurrencyName() {
            return this.countryCurrCode;
        }
    }

    private void enableLoadingView(boolean enable) {
        chb_alt.setClickable(enable);
        btnPurchase.setEnabled(enable);
        btnPurchase.setBackgroundResource(enable ? R.drawable.btn_gradient_background : R.drawable.btn_gradient_background_alpha);

        if (!enable) {
            tvApproximatelyBalance.setText(String.format("%s %s", getString(R.string.you_will_approximately_get), "..."));
            tvCommission.setText(String.format("%s %s", getString(R.string.commission), "..."));
        }
    }

    @Override
    public boolean onBackPressed() {
        if (prevFragment != null) {
            navigateToFragment(prevFragment);
            return true;
        }

        if (indaPrevFragment != null) {
            navigateToFragment(indaPrevFragment);
            return true;
        }

        if (coinifyPrevFragment != null) {
            navigateToFragment(coinifyPrevFragment);
            return true;
        }
        return false;
    }

    @Override
    public boolean onHomePressed() {
        return onBackPressed();
    }

}
