package com.guarda.ethereum.views.activity;


import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputLayout;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.guarda.ethereum.BuildConfig;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.items.CoinifyTradeRespForList;
import com.guarda.ethereum.models.items.CoinifyTradeRespSell;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;
import com.guarda.ethereum.views.activity.base.AToolbarActivity;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

@AutoInjector(GuardaApp.class)
public class ConfirmCoinifyActivity extends AToolbarActivity {

    @BindView(R.id.coinify_send)
    TextView send;
    @BindView(R.id.til_trans_details_sum)
    TextInputLayout til_trans_details_sum;
    @BindView(R.id.til_exch_fee)
    TextInputLayout til_exch_fee;
    @BindView(R.id.exch_fee)
    TextView fee;
    @BindView(R.id.til_trans_details_date)
    TextInputLayout til_trans_details_date;
    @BindView(R.id.et_trans_details_date)
    TextView txfee;
    @BindView(R.id.ll_fees)
    LinearLayout ll_fees;
    @BindView(R.id.coinify_rcv)
    TextView received;
    @BindView(R.id.coinify_email)
    TextView email;
    @BindView(R.id.btn_next)
    Button btnNext;
    @BindView(R.id.coinify_method_fee)
    TextView coinify_method_fee;
    @BindView(R.id.til_coinify_method_fee)
    TextInputLayout til_coinify_method_fee;
    @BindView(R.id.quote_minutes)
    TextView quote_minutes;
    @BindView(R.id.ll_quote)
    LinearLayout ll_quote;
    @BindView(R.id.ll_bank)
    LinearLayout ll_bank;
    @BindView(R.id.coinify_bank_chb)
    CheckBox coinify_bank_chb;
    @BindView(R.id.coinify_bank_chb_text)
    TextView coinify_bank_chb_text;

    @Inject
    SharedManager sharedManager;

    float baseAmount;
    String payMethod = "";
    String btcAddress = "";

    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        setToolBarTitle(getString(R.string.toolbar_title_confirm_coinify));

        baseAmount = getIntent().getFloatExtra(Extras.COINIFY_IN_AMOUNT, 0.0f);
        float amountWithCoinifyFee = 0.0f;

        payMethod = getIntent().getStringExtra(Extras.COINIFY_PAY_METHOD);

        if (!Common.MAIN_CURRENCY.equalsIgnoreCase("btc")) {
            if (payMethod.equalsIgnoreCase("sell")) {
                fee.setText(String.format("%s %s" , getIntent().getStringExtra(Extras.COINIFY_EXCH_FEE), getIntent().getStringExtra(Extras.COINIFY_OUT_AMOUNT_CUR)));
            } else {
                fee.setText(String.format("%s %%" , getIntent().getStringExtra(Extras.COINIFY_AMOUNT_FEE)));
            }
        } else {
            fee.setVisibility(View.GONE);
        }

        btcAddress = getIntent().getStringExtra(Extras.COINIFY_BTC_ADDRESS);
        if (btcAddress != null && !btcAddress.isEmpty()) {
            til_exch_fee.setVisibility(View.GONE);
        }

        if (payMethod.equalsIgnoreCase("card")) {
            coinify_method_fee.setText("3%");
            ll_bank.setVisibility(View.GONE);
            ll_quote.setVisibility(View.VISIBLE);
            amountWithCoinifyFee = baseAmount * 1.03f;
        } else if (payMethod.equalsIgnoreCase("bank")) {
            coinify_method_fee.setText("0.25%");
//            quote_minutes.setText("Bank processing takes 2-3 business days. The exchange rate used to complete the order will be available at the moment of processing it, which could be different from the one you see now.");
//            quote_minutes.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            ll_quote.setVisibility(View.GONE);
            ll_bank.setVisibility(View.VISIBLE);
            btnNext.setEnabled(false);
            amountWithCoinifyFee = baseAmount * 1.0025f;
        } else if (payMethod.equalsIgnoreCase("sell")) {
            if (BuildConfig.DEBUG)
                setToolBarTitle("SELL");

            til_trans_details_sum.setHint("You sell");
            til_coinify_method_fee.setHint(String.format("%s Order", getIntent().getStringExtra(Extras.COINIFY_OUT_AMOUNT_CUR)));
            txfee.setText(String.format("%s %s", getIntent().getStringExtra(Extras.COINIFY_COINIFY_FEE), getIntent().getStringExtra(Extras.COINIFY_OUT_AMOUNT_CUR)));
            coinify_method_fee.setText(String.format("%s %s", getIntent().getStringExtra(Extras.COINIFY_AMOUNT_RATE), getIntent().getStringExtra(Extras.COINIFY_OUT_AMOUNT_CUR)));
            coinify_bank_chb_text.setText(R.string.coinify_sell_confirm);
            btnNext.setEnabled(false);
        }

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.HALF_EVEN);
        if (payMethod.equalsIgnoreCase("sell")) {
            send.setText(String.format("%s %s", getIntent().getFloatExtra(Extras.COINIFY_IN_AMOUNT, 0.0f), getIntent().getStringExtra(Extras.COINIFY_IN_AMOUNT_CUR)));
        } else {
            send.setText(String.format("%s %s", df.format(amountWithCoinifyFee), getIntent().getStringExtra(Extras.COINIFY_IN_AMOUNT_CUR)));
        }

        String tvapx = getIntent().getStringExtra(Extras.COINIFY_OUT_AMOUNT);
        if (tvapx.contains("~")) {
            received.setText(tvapx.substring(tvapx.lastIndexOf("~")));
        } else {
            received.setText(String.format("%s %s", tvapx, getIntent().getStringExtra(Extras.COINIFY_OUT_AMOUNT_CUR)));
        }

        email.setText(sharedManager.getCoinifyEmail());

        coinify_bank_chb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btnNext.setEnabled(true);
                } else {
                    btnNext.setEnabled(false);
                }
            }
        });
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_confirm_coinify;
    }

    @OnClick(R.id.coinify_bank_chb_text)
    public void chbText(View view) {
        coinify_bank_chb.setChecked(!coinify_bank_chb.isChecked());
    }

    @OnClick(R.id.btn_next)
    public void btnNext(View view) {
        if ((payMethod.equalsIgnoreCase("sell"))) {
            coinify();
        } else {
            Intent intent = new Intent(this, PurchaseWemovecoinsActivity.class);
            intent.putExtra(Extras.PURCHASE_SERVICE, "coinify");
            intent.putExtra(Extras.COINIFY_QUOTE_ID, getIntent().getIntExtra(Extras.COINIFY_QUOTE_ID, 0));
            intent.putExtra(Extras.COINIFY_SEND_AMOUNT, send.getText().toString());
            intent.putExtra(Extras.COINIFY_PAY_METHOD, payMethod);
            intent.putExtra(Extras.COINIFY_BTC_ADDRESS, getIntent().getStringExtra(Extras.COINIFY_BTC_ADDRESS));
            startActivity(intent);
        }

        overridePendingTransition(R.anim.slide_in_left, R.anim.no_slide);
    }

    private void coinify() {
        showProgress();
        tradesList();
    }

    private void tradesList() {
        Requestor.coinifyTradesList(sharedManager.getCoinifyAccessToken(), new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                List<CoinifyTradeRespForList> ctrList = (List<CoinifyTradeRespForList>) response;
                if (ctrList.size() > 0 && ctrList.get(0).getState().equalsIgnoreCase("awaiting_transfer_in")) {
                    Requestor.coinifyCancelTrade(sharedManager.getCoinifyAccessToken(), ctrList.get(0).getId(), new ApiMethods.RequestListener() {
                        @Override
                        public void onSuccess(Object response) {
                            coinifyTrade();

                            Log.d("psd", "PurchaseCoins cancelTrade success - list = ");
                        }

                        @Override
                        public void onFailure(String msg) {
                            closeProgress();
                            Toast.makeText(getApplicationContext(), "Service is temporarily unavailable", Toast.LENGTH_SHORT).show();
                            Log.d("psd", "PurchaseCoins cancelTrade onFailure - " + msg);
                        }
                    });
                } else {
                    coinifyTrade();
                }

                Log.d("psd", "PurchaseCoins tradesList success - list = " + ctrList.toString());
            }

            @Override
            public void onFailure(String msg) {
                closeProgress();
                Toast.makeText(getApplicationContext(), "Service is temporarily unavailable", Toast.LENGTH_SHORT).show();
                Log.d("psd", "PurchaseCoins tradesList onFailure - " + msg);
            }
        });
    }

    private void coinifyTrade() {
        //transferIn
        JsonObject transferIn = new JsonObject();
        transferIn.addProperty("medium", "blockchain");

        //transferOut
        JsonObject transferOut = new JsonObject();

        transferOut.addProperty("medium", "bank");
        transferOut.addProperty("mediumReceiveAccountId", getIntent().getIntExtra(Extras.COINIFY_BANK_ACC_ID, 0));

        JsonObject trade = new JsonObject();
        trade.addProperty("priceQuoteId", getIntent().getIntExtra(Extras.COINIFY_QUOTE_ID, 0));
        trade.add("transferIn", transferIn);
        trade.add("transferOut", transferOut);

        Log.d("psd", "PurchaseCoins coinifyTrade - json trade = " + trade.toString());
        Requestor.coinifyTradeSell(sharedManager.getCoinifyAccessToken(), trade, new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                if (response != null) {
                    CoinifyTradeRespSell ctr = (CoinifyTradeRespSell) response;
                    sharedManager.setCoinifyLastTradeId(ctr.getId());
                    sharedManager.setCoinifyLastTradeState(ctr.getState());

                    showReceiptActivity(ctr.getTransferIn().getSendAmount(), ctr.getTransferIn().getDetails().getAccount());
                    Log.d("psd", "PurchaseCoins coinifyTrade success - trade id = " + ctr.getId());
                }

                closeProgress();
            }

            @Override
            public void onFailure(String msg) {
                closeProgress();
                JsonParser jp = new JsonParser();
                JsonObject jo = jp.parse(msg).getAsJsonObject();
                Toast.makeText(getApplicationContext(), jo.get("error_description").toString(), Toast.LENGTH_LONG).show();
                Log.d("psd", "PurchaseCoins coinifyTrade onFailure - " + msg);
            }
        });
    }

    private void showReceiptActivity(Float sendAmount, String address) {
        Intent intent = new Intent(this, ReceiptCoinifyActivity.class);
        intent.putExtra((Extras.COINIFY_IN_MEDIUM), "sell");
        intent.putExtra(Extras.COINIFY_SEND_AMOUNT, sendAmount);
        intent.putExtra(Extras.COINIFY_SELL_ADDRESS, address);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_slide, R.anim.slide_in_right);
    }

}
