package com.guarda.ethereum.views.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
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
import com.guarda.ethereum.managers.BitcoinNodeManager;
import com.guarda.ethereum.managers.Callback2;
import com.guarda.ethereum.managers.ChangenowApi;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.items.CoinifyTradeRespForList;
import com.guarda.ethereum.models.items.CoinifyTradeRespSell;
import com.guarda.ethereum.models.items.SendRawTxResponse;
import com.guarda.ethereum.models.items.TxFeeResponse;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;
import com.guarda.ethereum.views.activity.base.AToolbarActivity;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.WrongNetworkException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

import static com.guarda.ethereum.models.constants.Common.AVG_TX_SIZE_KB;

@AutoInjector(GuardaApp.class)
public class SellConfirmCoinifyActivity extends AToolbarActivity {

    @BindView(R.id.coinify_send)
    TextView send;
    @BindView(R.id.til_trans_details_sum)
    TextInputLayout til_trans_details_sum;
    @BindView(R.id.coinify_fee)
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
    @Inject
    WalletManager walletManager;

    float baseAmount;
    String payMethod = "";

    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        setToolBarTitle(getString(R.string.toolbar_title_confirm_coinify));

        baseAmount = getIntent().getFloatExtra(Extras.COINIFY_IN_AMOUNT, 0.0f);
        float amountWithCoinifyFee = 0.0f;

        payMethod = getIntent().getStringExtra(Extras.COINIFY_PAY_METHOD);

        fee.setText(String.format("%s %s" , getIntent().getStringExtra(Extras.COINIFY_EXCH_FEE), getIntent().getStringExtra(Extras.COINIFY_OUT_AMOUNT_CUR)));

        til_coinify_method_fee.setHint(String.format("%s Order", getIntent().getStringExtra(Extras.COINIFY_OUT_AMOUNT_CUR)));
        txfee.setText(String.format("%s %s", getIntent().getStringExtra(Extras.COINIFY_COINIFY_FEE), getIntent().getStringExtra(Extras.COINIFY_OUT_AMOUNT_CUR)));
        coinify_method_fee.setText(String.format("%s %s", getIntent().getStringExtra(Extras.COINIFY_AMOUNT_RATE), getIntent().getStringExtra(Extras.COINIFY_OUT_AMOUNT_CUR)));
        coinify_bank_chb_text.setText(R.string.coinify_sell_confirm);
        btnNext.setEnabled(false);

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.HALF_EVEN);
        send.setText(String.format("%s %s", getIntent().getFloatExtra(Extras.COINIFY_IN_AMOUNT, 0.0f), getIntent().getStringExtra(Extras.COINIFY_IN_AMOUNT_CUR)));

        String tvapx = getIntent().getStringExtra(Extras.COINIFY_OUT_AMOUNT);
        received.setText(String.format("%s %s", tvapx, getIntent().getStringExtra(Extras.COINIFY_OUT_AMOUNT_CUR)));

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
        return R.layout.activity_sell_confirm_coinify;
    }

    @OnClick(R.id.coinify_bank_chb_text)
    public void chbText(View view) {
        coinify_bank_chb.setChecked(!coinify_bank_chb.isChecked());
    }

    @OnClick(R.id.btn_next)
    public void btnNext(View view) {
        coinify();

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
                    sharedManager.setCoinifyLastTradeUptime(ctr.getUpdateTime());

                    changeNowGenAddr(ctr.getTransferIn().getDetails().getAccount());
                    Log.d("psd", "PurchaseCoins coinifyTrade success - trade id = " + ctr.getId());
                }
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

    private void changeNowGenAddr(String coinifyBtcAddr) {
        ChangenowApi.generateAddress(Common.MAIN_CURRENCY, "BTC", coinifyBtcAddr, null, new Callback2<String, ChangenowApi.GenerateAddressRespModel>() {
            @Override
            public void onResponse(final String status, final ChangenowApi.GenerateAddressRespModel resp) {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if ("ok".equals(status)) {
                                getTxFee(resp.depositAddress);
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.exchange_error_generate_address), Toast.LENGTH_LONG).show();
                                closeProgress();
                            }
                        }
                    });
                } catch (Exception e) {
                    closeProgress();
                    Toast.makeText(getApplicationContext(), getString(R.string.exchange_error_generate_address), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }

    private void getTxFee(String depAddr) {
        Requestor.getTxFee(new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    HashMap<String, TxFeeResponse> feesMap = (HashMap<String, TxFeeResponse>) response;
                    BigDecimal fee = new BigDecimal(feesMap.get(Common.MAIN_CURRENCY.toLowerCase()).getFee());
                    //fee to fee per Kb (1 Kb is 1000 bytes)
                    fee = fee.divide(AVG_TX_SIZE_KB, BigDecimal.ROUND_HALF_UP);
                    fee = fee.setScale(8, BigDecimal.ROUND_HALF_UP).stripTrailingZeros();
                    sendTx(depAddr, fee);
                } catch (Exception e) {
                    closeProgress();
                    Toast.makeText(getApplicationContext(), "Can not get exchange fee. Please, try later", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    Log.d("psd", "getTxFee - onSuccess: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(String msg) {
                closeProgress();
                Toast.makeText(getApplicationContext(), "Can not get exchange fee. Please, try later", Toast.LENGTH_LONG).show();
                Log.d("psd", "getTxFee - onFailure: " + msg);
            }
        });
    }

    private void sendTx(String depAddr, BigDecimal fee) {
        try {
            String hex = walletManager.generateHexTx(depAddr, Coin.parseCoin(String.valueOf(baseAmount)).getValue(), Coin.parseCoin(fee.toPlainString()).getValue());
            if (hex.equals(WalletManager.SMALL_SENDING)) {
                closeProgress();
                Toast.makeText(getApplicationContext(), getString(R.string.small_sum_of_tx), Toast.LENGTH_LONG).show();
            } else if (hex.equals(WalletManager.NOT_ENOUGH_MONEY)) {
                closeProgress();
                Toast.makeText(getApplicationContext(), getString(R.string.not_enough_money_to_send), Toast.LENGTH_LONG).show();
                if (BuildConfig.DEBUG) {
                    showReceiptActivity(123.45f);
                }
            } else {
                BitcoinNodeManager.sendTransaction(hex, new ApiMethods.RequestListener() {
                    @Override
                    public void onSuccess(Object response) {
                        SendRawTxResponse res = (SendRawTxResponse) response;
                        Log.d("TX_RES", "res " + res.getHashResult() + " error " + res.getError());
                        closeProgress();
                        showReceiptActivity(baseAmount);
                    }

                    @Override
                    public void onFailure(String msg) {
                        closeProgress();
                        Log.d("svcom", "failure - " + msg);
                        Toast.makeText(getApplicationContext(), "Error while sending transaction", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (WrongNetworkException wne) {
            closeProgress();
            Log.e("psd", wne.toString());
            Toast.makeText(this, R.string.send_wrong_address, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error while sending transaction", Toast.LENGTH_SHORT).show();
            closeProgress();
            e.printStackTrace();
        }

    }

    private void showReceiptActivity(Float sendAmount) {
        Intent intent = new Intent(this, ReceiptCoinifyActivity.class);
        intent.putExtra((Extras.COINIFY_IN_MEDIUM), "sell");
        intent.putExtra(Extras.COINIFY_SEND_AMOUNT, String.valueOf(sendAmount));
        intent.putExtra(Extras.COINIFY_IN_AMOUNT, getIntent().getFloatExtra(Extras.COINIFY_IN_AMOUNT, 0.0f));
        intent.putExtra(Extras.COINIFY_IN_AMOUNT_CUR, getIntent().getStringExtra(Extras.COINIFY_IN_AMOUNT_CUR));
        intent.putExtra(Extras.COINIFY_OUT_AMOUNT, getIntent().getStringExtra(Extras.COINIFY_OUT_AMOUNT));
        intent.putExtra(Extras.COINIFY_OUT_AMOUNT_CUR, getIntent().getStringExtra(Extras.COINIFY_OUT_AMOUNT_CUR));
        intent.putExtra(Extras.COINIFY_BANK_NAME, getIntent().getStringExtra(Extras.COINIFY_BANK_NAME));
        intent.putExtra(Extras.COINIFY_HOLDER_NAME, getIntent().getStringExtra(Extras.COINIFY_HOLDER_NAME));
        intent.putExtra(Extras.COINIFY_ACC_NUMBER, getIntent().getStringExtra(Extras.COINIFY_ACC_NUMBER));
//        intent.putExtra(Extras.COINIFY_SELL_ADDRESS, address);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_slide, R.anim.slide_in_right);
    }

}
