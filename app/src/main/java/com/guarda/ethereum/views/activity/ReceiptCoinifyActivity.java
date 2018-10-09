package com.guarda.ethereum.views.activity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.items.CoinifyTradeResponse;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;
import com.guarda.ethereum.utils.CalendarHelper;
import com.guarda.ethereum.utils.DateUtils;
import com.guarda.ethereum.utils.QrCodeUtils;
import com.guarda.ethereum.views.activity.base.AToolbarActivity;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

import static com.guarda.ethereum.models.constants.Extras.GO_TO_PURCHASE;
import static com.guarda.ethereum.models.constants.Extras.GO_TO_TRANS_HISTORY;
import static com.guarda.ethereum.models.constants.Extras.NAVIGATE_TO_FRAGMENT;

@AutoInjector(GuardaApp.class)
public class ReceiptCoinifyActivity extends AToolbarActivity {

    @BindView(R.id.rl_card_receipt)
    RelativeLayout rl_card_receipt;
    @BindView(R.id.bank_scroll)
    ScrollView bank_scroll;
    @BindView(R.id.coinify_pay_id)
    TextView coinify_pay_id;
    @BindView(R.id.coinify_you_pay)
    TextView coinify_you_pay;
    @BindView(R.id.coinify_tx_status)
    TextView coinify_tx_status;
    @BindView(R.id.tv_state_expl)
    TextView tv_state_expl;
    @BindView(R.id.coinify_amount)
    TextView coinify_amount;
    @BindView(R.id.coinify_ref_text)
    TextView coinify_ref_text;
    @BindView(R.id.coinify_bank_explain)
    TextView coinify_bank_explain;
    @BindView(R.id.tv_swift)
    TextView tv_swift;
    @BindView(R.id.tv_iban)
    TextView tv_iban;
    @BindView(R.id.tv_type)
    TextView tv_type;
    @BindView(R.id.tv_curr)
    TextView tv_curr;
    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.tv_city)
    TextView tv_city;
    @BindView(R.id.tv_state)
    TextView tv_state;
    @BindView(R.id.tv_street)
    TextView tv_street;
    @BindView(R.id.tv_country)
    TextView tv_country;
    @BindView(R.id.tv_zip)
    TextView tv_zip;
    @BindView(R.id.tv_bank_name)
    TextView tv_bank_name;
    @BindView(R.id.tv_bank_city)
    TextView tv_bank_city;
    @BindView(R.id.tv_bank_state)
    TextView tv_bank_state;
    @BindView(R.id.tv_bank_street)
    TextView tv_bank_street;
    @BindView(R.id.tv_bank_country)
    TextView tv_bank_country;
    @BindView(R.id.tv_bank_zip)
    TextView tv_bank_zip;

    @BindView(R.id.rl_sell_receipt)
    RelativeLayout rl_sell_receipt;
    @BindView(R.id.tv_sell_expl)
    TextView tv_sell_expl;
    @BindView(R.id.tv_trade_id)
    TextView tv_trade_id;
    @BindView(R.id.tv_date_init)
    TextView tv_date_init;
    @BindView(R.id.tv_sell_bank)
    TextView tv_sell_bank;
    @BindView(R.id.tv_holder)
    TextView tv_holder;
    @BindView(R.id.tv_number)
    TextView tv_number;

    @Inject
    SharedManager sharedManager;

    private String selectedMethod;
    private Timer timer;
    private UpdateStateTask updateStateTask;

    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        setToolBarTitle("Order details");
        timer = new Timer();

        selectedMethod = getIntent().getStringExtra(Extras.COINIFY_IN_MEDIUM);
        if (selectedMethod.equals("card")) {
            setCardView();
            updateTradeStatus();
        } else if (selectedMethod.equals("bank")) {
            setBankView();
        } else if (selectedMethod.equals("sell")) {
            setSellView();
        }

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_receipt_coinify;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    @OnClick(R.id.btn_next)
    public void btnNext(View view) {
        returnToPurchase();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        returnToPurchase();
        overridePendingTransition(R.anim.no_slide, R.anim.slide_in_right);
    }

    private void updateTradeStatus() {
        Requestor.coinifyGetTrade(sharedManager.getCoinifyAccessToken(), sharedManager.getCoinifyLastTradeId(), new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                CoinifyTradeResponse ctr = (CoinifyTradeResponse) response;
                if (ctr.getState().equalsIgnoreCase("awaiting_transfer_in")) {
                    coinify_tx_status.setText("awaiting transfer in");
                } else if (ctr.getState().equalsIgnoreCase("rejected")) {
                    tv_state_expl.setText(String.format(getString(R.string.coinify_rejected_state), String.valueOf(ctr.getId())));
                    coinify_tx_status.setText(ctr.getState());
                } else {
                    coinify_tx_status.setText(ctr.getState());
                }

                timer.cancel();
                timer.purge();
                timer = null;
                timer = new Timer();
                updateStateTask = new UpdateStateTask();
                timer.schedule(updateStateTask, 30000);
                Log.d("psd", "ReceiptCoinifyActivity - updateTradeStatus onSuccess: state = " + ctr.getState());
            }

            @Override
            public void onFailure(String msg) {
                Log.d("psd", "ReceiptCoinifyActivity - updateTradeStatus onFailure: " + msg);
            }
        });
    }

    class UpdateStateTask extends TimerTask {
        @Override
        public void run() {
            updateTradeStatus();
        }
    }

    private void setCardView() {
        rl_card_receipt.setVisibility(View.VISIBLE);
        bank_scroll.setVisibility(View.GONE);
        rl_sell_receipt.setVisibility(View.GONE);
        coinify_pay_id.setText(String.valueOf(sharedManager.getCoinifyLastTradeId()));
        coinify_you_pay.setText(getIntent().getStringExtra(Extras.COINIFY_SEND_AMOUNT));
        if (sharedManager.getCoinifyLastTradeState().equalsIgnoreCase("awaiting_transfer_in")) {
            coinify_tx_status.setText("awaiting transfer in");
        } else if (sharedManager.getCoinifyLastTradeState().equalsIgnoreCase("rejected")) {
            tv_state_expl.setText(String.format(getString(R.string.coinify_rejected_state), String.valueOf(sharedManager.getCoinifyLastTradeId())));
            coinify_tx_status.setText(sharedManager.getCoinifyLastTradeState());
        } else {
            coinify_tx_status.setText(sharedManager.getCoinifyLastTradeState());
        }
    }

    private void setBankView() {
        rl_card_receipt.setVisibility(View.GONE);
        bank_scroll.setVisibility(View.VISIBLE);
        rl_sell_receipt.setVisibility(View.GONE);
        Gson gson = new Gson();
        Type listType = new TypeToken<CoinifyTradeResponse>(){}.getType();
        String tradeJson = sharedManager.getCoinifyBankTrade();
        CoinifyTradeResponse tradeResp = gson.fromJson(tradeJson, listType);
        CoinifyTradeResponse.Details dt = tradeResp.getTransferIn().getDetails();
        try {
            float amount = Float.parseFloat(tradeResp.getInAmount());
            coinify_amount.setText(String.valueOf(amount * 1.0025f));
        } catch (Exception e) {
            Log.d("psd", "ReceiptCoinifyActivity - can't parse amount (= " + tradeResp.getInAmount() + ") to Float: " + e.getMessage());
        }
        coinify_ref_text.setText(dt.getReferenceText());
        coinify_bank_explain.setText(String.format(getString(R.string.coinify_remember), dt.getReferenceText()));
        tv_swift.setText(tradeResp.getTransferIn().getDetails().getAccount().getBic());
        tv_iban.setText(tradeResp.getTransferIn().getDetails().getAccount().getNumber());
        tv_type.setText(tradeResp.getTransferIn().getDetails().getAccount().getType());
        tv_curr.setText(tradeResp.getTransferIn().getDetails().getAccount().getCurrency());
        tv_name.setText(tradeResp.getTransferIn().getDetails().getHolder().getName());
        tv_city.setText(tradeResp.getTransferIn().getDetails().getHolder().getAddress().getCity());
        if (tradeResp.getTransferIn().getDetails().getHolder().getAddress().getState() == null) {
            tv_state.setText("-");
        } else {
            tv_state.setText(tradeResp.getTransferIn().getDetails().getHolder().getAddress().getState());
        }
        tv_street.setText(tradeResp.getTransferIn().getDetails().getHolder().getAddress().getStreet());
        tv_country.setText(tradeResp.getTransferIn().getDetails().getHolder().getAddress().getCountry());
        tv_zip.setText(tradeResp.getTransferIn().getDetails().getHolder().getAddress().getZipcode());
        tv_bank_name.setText(tradeResp.getTransferIn().getDetails().getBank().getName());
        tv_bank_city.setText(tradeResp.getTransferIn().getDetails().getBank().getAddress().getCity());
        if (tradeResp.getTransferIn().getDetails().getHolder().getAddress().getState() == null) {
            tv_bank_state.setText("-");
        } else {
            tv_bank_state.setText(tradeResp.getTransferIn().getDetails().getBank().getAddress().getState());
        }
        tv_bank_street.setText(tradeResp.getTransferIn().getDetails().getBank().getAddress().getStreet());
        tv_bank_country.setText(tradeResp.getTransferIn().getDetails().getBank().getAddress().getCountry());
        tv_bank_zip.setText(tradeResp.getTransferIn().getDetails().getBank().getAddress().getZipcode());
    }

    private void setSellView() {
        rl_card_receipt.setVisibility(View.GONE);
        bank_scroll.setVisibility(View.GONE);
        rl_sell_receipt.setVisibility(View.VISIBLE);
        String sellSum = String.valueOf(getIntent().getFloatExtra(Extras.COINIFY_IN_AMOUNT, 0.0f));
        String sellCur = getIntent().getStringExtra(Extras.COINIFY_IN_AMOUNT_CUR);
        String outSum = getIntent().getStringExtra(Extras.COINIFY_OUT_AMOUNT);
        String outCur = getIntent().getStringExtra(Extras.COINIFY_OUT_AMOUNT_CUR);
        tv_sell_expl.setText(String.format(getString(R.string.coinify_sell_explain), sellSum, sellCur, outSum, outCur));
        tv_trade_id.setText(String.valueOf(sharedManager.getCoinifyLastTradeId()));
        Date date = DateUtils.stringToDate(sharedManager.getCoinifyLastTradeUptime());
        tv_date_init.setText(DateFormat.getDateTimeInstance().format(date));
        tv_sell_bank.setText(getIntent().getStringExtra(Extras.COINIFY_BANK_NAME));
        tv_holder.setText(getIntent().getStringExtra(Extras.COINIFY_HOLDER_NAME));
        tv_number.setText(getIntent().getStringExtra(Extras.COINIFY_ACC_NUMBER));
    }

    private void returnToPurchase() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(NAVIGATE_TO_FRAGMENT, GO_TO_PURCHASE);
        startActivity(intent);
    }
}
