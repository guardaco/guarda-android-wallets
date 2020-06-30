package com.guarda.ethereum.views.activity;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.guarda.ethereum.BuildConfig;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.Callback2;
import com.guarda.ethereum.managers.ChangenowApi;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.items.CoinifyPaysResponse;
import com.guarda.ethereum.models.items.CoinifyQuoteResponse;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;
import com.guarda.ethereum.utils.DigitsInputFilter;
import com.guarda.ethereum.views.activity.base.AToolbarMenuActivity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

import static com.guarda.ethereum.models.constants.Common.ETH_SHOW_PATTERN;

public class AmountCoinifyActivity extends AToolbarMenuActivity {

    @BindView(R.id.et_amount_to_send)
    EditText et_amount_to_send;
    @BindView(R.id.et_amount_to_purchase)
    EditText et_amount_to_purchase;
    @BindView(R.id.sp_bank_currency)
    Spinner sp_bank_currency;
    @BindView(R.id.tv_min_sell)
    TextView tv_min_sell;
    @BindView(R.id.tv_max_sell)
    TextView tv_max_sell;
    @BindView(R.id.tv_exch_fee)
    TextView tv_exch_fee;
    @BindView(R.id.tv_exch_rate)
    TextView tv_exch_rate;
    @BindView(R.id.btn_next)
    Button btn_next;

    @Inject
    WalletManager walletManager;

    @Inject
    SharedManager sharedManager;

    private BigDecimal minSellCurrency;
    private float coinifyBTCMinAmount = -1.0f;
    private float coinifyBTCLimitAmount = -1.0f;
    private float outPercentageFee = -1.0f;
    private float coinifyQuoteAmount = -1000.0f;
    private int coinifyQuoteId;
    private String approxSumRate = "";
    private String coinifyCommission = "";
    private String exchCommission = "";
    private double realCurrencyToBtc = 0;
    private double btcToCurrentCrypto = 0;
    private double exchangeCommission = 0.5;
    private String currentFiat = "";


    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        setToolBarTitle(getString(R.string.amount_coinify_title));
        et_amount_to_send.setFilters(new InputFilter[]{new DigitsInputFilter(8, 8, Float.POSITIVE_INFINITY)});
        et_amount_to_purchase.setFilters(new InputFilter[]{new DigitsInputFilter(8, 8, Float.POSITIVE_INFINITY)});

        tv_min_sell.setText(String.format("%s ...", getString(R.string.coinify_amount_min)));
        tv_max_sell.setText(String.format("%s ...", getString(R.string.coinify_amount_max)));
        if (Common.MAIN_CURRENCY.equalsIgnoreCase("btc")) {
            tv_exch_fee.setVisibility(View.GONE);
            tv_exch_rate.setVisibility(View.GONE);
        } else {
            tv_exch_fee.setText(String.format("%s ...", getString(R.string.coinify_amount_exchange)));
            tv_exch_rate.setText(String.format("%s ...", getString(R.string.coinify_amount_rate)));
        }

        et_amount_to_send.requestFocus();
        et_amount_to_send.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setYouGet(s.length() > 0 ? Double.valueOf(s.toString()) : 0);
                hideError(et_amount_to_send);
                if (minSellCurrency == null) {
                    coinifyPays();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        final String[] array = new String[]{"USD", "EUR", "DKK", "GBP"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.view_sp_buy_currency_by_card_item, array);
        sp_bank_currency.setAdapter(adapter);

        sp_bank_currency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFiat = array[position];
                updateYouGetCurr(currentFiat);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_amount_coinify;
    }

    private void coinifyPays() {
        Requestor.coinifyPays(sharedManager.getCoinifyAccessToken(), new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                List<CoinifyPaysResponse> cprList = (List<CoinifyPaysResponse>) response;
                for (CoinifyPaysResponse cpr : cprList) {
                    if (cpr.getInMedium().equalsIgnoreCase("blockchain") && cpr.getOutMedium().equalsIgnoreCase("bank")) {
                        coinifyBTCMinAmount = cpr.getMinimumInAmounts().get("BTC");
                        coinifyBTCLimitAmount = cpr.getLimitInAmounts().get("BTC");
                        outPercentageFee = cpr.getOutPercentageFee();

                        if (!Common.MAIN_CURRENCY.equalsIgnoreCase("btc")) {
                            getChangeNowMinAmount();
                        } else {
                            setYouGet(et_amount_to_send.getText().length() > 0 ? Double.valueOf(et_amount_to_send.getText().toString()) : 0);
                        }
                    }
                }
                Log.d("psd", "coinifyPays: onSuccess - bank sell: min=" + coinifyBTCMinAmount + " max=" + coinifyBTCLimitAmount);
            }

            @Override
            public void onFailure(String msg) {
                Log.d("psd", "coinifyPays - onFailure: " + msg);
            }
        });
    }
    
    private void getChangeNowMinAmount() {
        Log.d("flint", "AmountCoinifyActivity.getChangeNowMinAmount()...");
        ChangenowApi.getMinAmount(Common.MAIN_CURRENCY, "btc", new Callback2<String, ChangenowApi.GetRateRespModel>() {
            @Override
            public void onResponse(String status, ChangenowApi.GetRateRespModel resp) {
                if ("ok".equals(status)) {
                    if (resp.minimum.compareTo(new BigDecimal(Float.toString(coinifyBTCMinAmount))) > 0) {
                        coinifyBTCMinAmount = resp.minimum.floatValue();
                        setYouGet(et_amount_to_send.getText().length() > 0 ? Double.valueOf(et_amount_to_send.getText().toString()) : 0);
                    }
                }
            }
        });
    }

    private void coinifyQuote(final String baseCurrency, String quoteCurrency, final float baseAmount, boolean withExch) {
        JsonObject coinifyQuote = new JsonObject();
        coinifyQuote.addProperty("baseCurrency", baseCurrency);
        coinifyQuote.addProperty("quoteCurrency", quoteCurrency);
        coinifyQuote.addProperty("baseAmount", baseAmount);

        Requestor.coinifyQuote(sharedManager.getCoinifyAccessToken(), coinifyQuote, new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                CoinifyQuoteResponse cqr = (CoinifyQuoteResponse) response;
//                coinifyQuoteExpiryTime = CalendarHelper.jodaTimeToMilliseconds(cqr.getExpiryTime());
                coinifyQuoteId = cqr.getId();
                realCurrencyToBtc = -baseAmount/cqr.getQuoteAmount();

                if (withExch) {
                    if (!Common.MAIN_CURRENCY.equalsIgnoreCase("btc")) {
                        getChangellyExchangeAmount_changenow();
                    } else {
                        btcToCurrentCrypto = 1;
                        setYouGet(et_amount_to_send.getText().length() > 0 ? Double.valueOf(et_amount_to_send.getText().toString()) : 0);
                    }
                } else {
                    enableLoadingView(true);
                    openBankAccActivity();
                }

                closeProgress();

                Log.d("psd", "cqr.getQuoteAmount(): " + cqr.getQuoteAmount() + " realCurrencyToBtc: " + realCurrencyToBtc);
            }

            @Override
            public void onFailure(String msg) {
                closeProgress();
                enableLoadingView(true);
                try {
                    JsonParser jp = new JsonParser();
                    JsonObject jo = jp.parse(msg).getAsJsonObject();
                    Toast.makeText(getApplicationContext(), jo.get("error_description").toString(), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.d("psd", "coinifyQuote - onFailure: " + msg);
            }
        });
    }

    private void getChangellyExchangeAmount_changenow() {
        Log.d("flint", "AmountCoinifyActivity.getChangellyExchangeAmount_changenow()...");
        ChangenowApi.getRate("btc", Common.MAIN_CURRENCY, new Callback2<String, ChangenowApi.GetRateRespModel>() {
            @Override
            public void onResponse(String status, ChangenowApi.GetRateRespModel resp) {
                if ("ok".equals(status))
                    getChangellyExchangeAmount_onSuccess(resp.rate.toString());
            }
        });
    }

    private void getChangellyExchangeAmount_onSuccess(final String amount) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //Log.d("!!!!!", "changelly amount price " + amount);
                        btcToCurrentCrypto = Double.valueOf(amount);
                        setYouGet(et_amount_to_send.getText().length() > 0 ? Double.valueOf(et_amount_to_send.getText().toString()) : 0);
                    } catch (Exception e) {
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setYouGet(double youSell) {
        if (youSell > 0)
            coinifyQuoteAmount = - Float.parseFloat(et_amount_to_send.getText().toString());

        //set min and max amounts
        if (coinifyBTCMinAmount != -1.0f && coinifyBTCLimitAmount != -1.0f) {
            try {
                //delta because exchange rate maybe less than actual current rate.
                float delta = 0.02f;
                if (!Common.MAIN_CURRENCY.equalsIgnoreCase("btc")) {
                    minSellCurrency = new BigDecimal(coinifyBTCMinAmount * (btcToCurrentCrypto * delta)).setScale(5, BigDecimal.ROUND_HALF_UP);
                } else {
                    minSellCurrency = new BigDecimal(coinifyBTCMinAmount).setScale(5, BigDecimal.ROUND_HALF_UP);
                }
                BigDecimal max = new BigDecimal(coinifyBTCLimitAmount * (btcToCurrentCrypto * (1 - delta))).setScale(5, BigDecimal.ROUND_HALF_UP);
                BigDecimal rate = new BigDecimal(1/btcToCurrentCrypto / realCurrencyToBtc).setScale(5, BigDecimal.ROUND_HALF_UP);
                tv_min_sell.setText(String.format("%s %s %s", getString(R.string.coinify_amount_min), minSellCurrency, Common.MAIN_CURRENCY.toUpperCase()));
                tv_max_sell.setText(String.format("%s %s %s", getString(R.string.coinify_amount_max), max, Common.MAIN_CURRENCY.toUpperCase()));
                tv_exch_rate.setText(String.format("%s 1 %s ~ %s %s", getString(R.string.coinify_amount_rate), Common.MAIN_CURRENCY.toUpperCase(), rate, currentFiat));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Log.d("psd", "AmountCoinifyActivity - setYouGet(): " + e.getMessage());
            }

        }

        //set aproximately get
        double approximatelySum = 0;
        double fullCommission = 0;
        if (youSell > 0) {
            approximatelySum = youSell / btcToCurrentCrypto / realCurrencyToBtc;
            fullCommission = (approximatelySum / 100) * (outPercentageFee + exchangeCommission);
        }

        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        DecimalFormat decimalFormat = new DecimalFormat(ETH_SHOW_PATTERN, symbols);
        decimalFormat.setRoundingMode(RoundingMode.DOWN);

        approxSumRate = decimalFormat.format(approximatelySum);
        coinifyCommission = decimalFormat.format((approximatelySum / 100) * (outPercentageFee));
        exchCommission = decimalFormat.format((approximatelySum / 100) * (exchangeCommission));

        approximatelySum = approximatelySum - fullCommission;

        et_amount_to_purchase.setText(decimalFormat.format(approximatelySum));
        tv_exch_fee.setText(String.format("%s %s %s", getString(R.string.coinify_amount_exchange), decimalFormat.format(fullCommission), currentFiat));
    }

    private void updateYouGetCurr(String currentCurr) {
        coinifyPays();

        String currencyFrom = "BTC";
        coinifyQuote(currencyFrom, currentCurr, coinifyQuoteAmount, true);
    }

    @OnClick(R.id.btn_next)
    public void sendClick(View view) {
        String amount = et_amount_to_send.getText().toString();
        if (!amount.isEmpty()) {
            if (Float.parseFloat(amount) > 0) {
                if (walletManager.getBalance().compareTo(new BigDecimal(Float.toString(-coinifyQuoteAmount))) >= 0 || BuildConfig.DEBUG) {
                    if (minSellCurrency != null && minSellCurrency.floatValue() <= -coinifyQuoteAmount) {
                        enableLoadingView(false);
                        String currencyFrom = "BTC";
                        coinifyQuote(currencyFrom, currentFiat, coinifyQuoteAmount, false);
                    } else {
                        showError(et_amount_to_send, getString(R.string.coinify_min_amount));
                    }
                } else {
                    showError(et_amount_to_send, getString(R.string.withdraw_amount_more_than_balance));
                }
            } else {
                showError(et_amount_to_send, getString(R.string.withdraw_amount_can_not_be_empty));
            }
        } else {
            showError(et_amount_to_send, getString(R.string.withdraw_amount_can_not_be_empty));
        }
    }

    private void enableLoadingView(boolean enable) {
        btn_next.setEnabled(enable);
        btn_next.setBackgroundResource(enable ? R.drawable.btn_gradient_background : R.drawable.btn_gradient_background_alpha);
    }

    private void openBankAccActivity() {
        Intent intent = new Intent(this, BankAccCoinifyActivity.class);
        intent.putExtra(Extras.COINIFY_IN_AMOUNT, Float.parseFloat(et_amount_to_send.getText().toString().trim()));
        intent.putExtra(Extras.COINIFY_IN_AMOUNT_CUR, Common.MAIN_CURRENCY.toUpperCase());
        intent.putExtra(Extras.COINIFY_AMOUNT_RATE, approxSumRate);
        intent.putExtra(Extras.COINIFY_OUT_AMOUNT, et_amount_to_purchase.getText().toString());
        intent.putExtra(Extras.COINIFY_OUT_AMOUNT_CUR, currentFiat);
        intent.putExtra(Extras.COINIFY_QUOTE_ID, coinifyQuoteId);
        intent.putExtra(Extras.COINIFY_COINIFY_FEE, coinifyCommission);
        intent.putExtra(Extras.COINIFY_EXCH_FEE, exchCommission);
        startActivity(intent);
    }

}
