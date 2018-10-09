package com.guarda.ethereum.views.activity;


import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.guarda.ethereum.BuildConfig;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.Callback2;
import com.guarda.ethereum.managers.ChangellyNetworkManager;
import com.guarda.ethereum.managers.ChangenowApi;
import com.guarda.ethereum.managers.IndacoinManager;
import com.guarda.ethereum.managers.ShapeshiftApi;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Const;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.items.CoinifyTradeRespForList;
import com.guarda.ethereum.models.items.CoinifyTradeResponse;
import com.guarda.ethereum.models.items.ResponseGenerateAddress;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;
import com.guarda.ethereum.views.activity.base.AToolbarActivity;

import java.util.List;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;

import static com.guarda.ethereum.models.constants.Common.MAIN_CURRENCY;

@AutoInjector(GuardaApp.class)
public class PurchaseWemovecoinsActivity extends AToolbarActivity {

    private final String MAIN_ENDPOINT = "app.guarda.co";
    private final String TEST_ENDPOINT = "apptest.guarda.co";
    private final String MAIN_HTTP_PROTOCOL = "https";
    private final String TEST_HTTP_PROTOCOL = "http";

    @Inject
    WalletManager walletManager;
    @Inject
    SharedManager sharedManager;

    @BindView(R.id.wv_wemovecoins)
    WebView webView;

    private String sum;
    private String name;
    private String currencyName;
    private String email;
    private String phoneCode;
    private String phoneNumber;
    private String generatedAddress = "";
    private String serviceName;
    private String returnUrl;
    private int coinifyQuoteId;
    private String coinifySelectedMethod = "";
    private boolean isShownCongrats = false;
    private String btcAddress = "";

    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        setToolBarTitle(getString(R.string.title_purchase_wemovecoin));

        sum = getIntent().getStringExtra(Extras.PURCHASE_COINS);
        name = getIntent().getStringExtra(Extras.USER_FULL_NAME);
        currencyName = getIntent().getStringExtra(Extras.PURCHASE_CURR);
        email = getIntent().getStringExtra(Extras.USER_EMAIL);
        phoneCode = getIntent().getStringExtra(Extras.COUNTRY_PHONE_CODE);
        phoneNumber = getIntent().getStringExtra(Extras.USER_PHONE);
        serviceName = getIntent().getStringExtra(Extras.PURCHASE_SERVICE);
//        returnUrl = getIntent().getStringExtra(Extras.COINIFY_RETURN_URL);
        coinifyQuoteId = getIntent().getIntExtra(Extras.COINIFY_QUOTE_ID, 0);
        coinifySelectedMethod = getIntent().getStringExtra(Extras.COINIFY_PAY_METHOD);
        btcAddress = getIntent().getStringExtra(Extras.COINIFY_BTC_ADDRESS);

        showProgress("Loading");
        if (btcAddress != null && !btcAddress.isEmpty()) {
            generatedAddress = btcAddress;
            loadWebView(sum, name, currencyName, email, phoneCode, phoneNumber, walletManager.getWalletFriendlyAddress());
        } else {
            getBtcAddress(walletManager.getWalletFriendlyAddress());
        }

    }

    private void getBtcAddress(final String walletNum) {
        Log.d("flint", "PurchaseWemovecoinsActivity.getBtcAddress()...");
        showProgress();
        String code = "btc";
        ChangellyNetworkManager.generateAddress(code.toLowerCase(), MAIN_CURRENCY, walletNum, null, new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                ResponseGenerateAddress addressItem = (ResponseGenerateAddress) response;
                if (addressItem.getAddress() != null && addressItem.getAddress().getAddress() != null) {
                    closeProgress();
                    onAddressGenerated(addressItem.getAddress().getAddress());
                } else {
                    getBtcAddress_changenow(walletNum);
                }
            }

            @Override
            public void onFailure(String msg) {
                getBtcAddress_changenow(walletNum);
            }
        });
    }

    private void getBtcAddress_changenow(final String walletNum) {
        Log.d("flint", "PurchaseWemovecoinsActivity.getBtcAddress_changenow()...");
        String code = "btc";
        ChangenowApi.generateAddress(code.toLowerCase(), MAIN_CURRENCY, walletNum, null, new Callback2<String, ChangenowApi.GenerateAddressRespModel>() {
            @Override
            public void onResponse(String status, ChangenowApi.GenerateAddressRespModel resp) {
                Log.d("flint", "PurchaseWemovecoinsActivity.getBtcAddress_changenow()... onResponse: status="+status);
                if ("ok".equals(status)) {
                    onAddressGenerated(resp.depositAddress);
                } else {
                    getBtcAddress_shapeshift(walletNum);
                }
            }
        });
    }

    private void getBtcAddress_shapeshift(final String walletNum) {
        Log.d("flint", "PurchaseWemovecoinsActivity.getBtcAddress_shapeshift()...");
        String code = "btc";
        String returnAddress = Const.COIN_TO_RETURN_ADDRESS.get(code.toUpperCase()) == null ? "" : Const.COIN_TO_RETURN_ADDRESS.get(code.toUpperCase());
        ShapeshiftApi.generateAddress(code.toLowerCase(), MAIN_CURRENCY, walletNum, returnAddress, new Callback2<String, ShapeshiftApi.GenerateAddressRespModel>() {
            @Override
            public void onResponse(String status, ShapeshiftApi.GenerateAddressRespModel resp) {
                Log.d("flint", "PurchaseWemovecoinsActivity.getBtcAddress_shapeshift()... onResponse: status="+status);
                if ("ok".equals(status)) {
                    onAddressGenerated(resp.depositAddress);
                } else {
                    onAddressGenerationFail();
                }
            }
        });
    }

    private void onAddressGenerated(final String addr) {
        Log.d("flint", "PurchaseWemovecoinsActivity.onAddressGenerated(): addr=" + addr);
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    closeProgress();
                    generatedAddress = addr;
                    loadWebView(sum, name, currencyName, email, phoneCode, phoneNumber, generatedAddress);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onAddressGenerationFail() {
        Log.d("flint", "PurchaseWemovecoinsActivity.onAddressGenerationFail()");
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "All exchange services are unavailable now.", Toast.LENGTH_SHORT).show();
                    closeProgress();
                    finish();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_wemovecoins_purchase;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_slide, R.anim.slide_in_right);
    }

    private void loadWebView(String sum, String name, String currencyName, String email, String phoneCode, String phoneNumber, String generatedAddress) {

        String url = "https://guarda.co";
        if ("wemovecoins".equals(serviceName)) {
            Uri.Builder builder = new Uri.Builder();
            String protocol = MAIN_HTTP_PROTOCOL;
            if (Build.VERSION.SDK_INT <= 18)
                protocol = TEST_HTTP_PROTOCOL;
            builder.scheme(protocol)
                    .authority(MAIN_ENDPOINT)
                    .appendQueryParameter("amount", sum)
                    .appendQueryParameter("currency", currencyName)
                    .appendQueryParameter("name", name)
                    .appendQueryParameter("cryptoAddress", generatedAddress)
                    .appendQueryParameter("email", email)
                    .appendQueryParameter("phoneCode", phoneCode)
                    .appendQueryParameter("phonenumber", phoneNumber)
                    .appendQueryParameter("returnurl", Common.SITE_APP_LINK);
            url = builder.build().toString();
            continueLoadWebView(url);
        } else if ("coinify".equals(serviceName)) {
            coinify();
        } else {
            IndacoinManager.createTransaction(sum, "btc", generatedAddress, email, new Callback2<String, IndacoinManager.CreateTransactionRespModel>() {
                @Override
                public void onResponse(String status, IndacoinManager.CreateTransactionRespModel resp) {
                    if (resp != null)
                        continueLoadWebView(resp.paymentUrl);
                    else
                        continueLoadWebView_fail();
                }
            });
        }
    }

    private void continueLoadWebView_fail() {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Loading error", Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void continueLoadWebView(final String url) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("flint", "PurchaseWemovecoinsActivity.continueLoadWebView()... url: " + url);

                    webView.getSettings().setJavaScriptEnabled(true);

                    webView.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            super.onPageFinished(view, url);
                            closeProgress();
                        }

                        @Override
                        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                            // Handle the error
                            Toast.makeText(getApplicationContext(), "Loading error", Toast.LENGTH_LONG).show();
                        }


                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                            if (url.contains(Common.SITE_APP_LINK) && !isShownCongrats) {
                                if (serviceName.equalsIgnoreCase("coinify")) {
                                    showReceiptActivity();
                                    isShownCongrats = true;
                                } else {
                                    showCongratsActivity();
                                    isShownCongrats = true;
                                }
                            }
                            view.loadUrl(url);
                            return true;
                        }
                    });

                    webView.loadUrl(url);
                    Log.d("!!!!!", "url " + url);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (coinifySelectedMethod.equalsIgnoreCase("card")) {
            transferIn.addProperty("medium", "card");
        } else if (coinifySelectedMethod.equalsIgnoreCase("bank")) {
            transferIn.addProperty("medium", "bank");
        }
        //detailsOut
        JsonObject detailsIn = new JsonObject();
//        detailsIn.addProperty("provider", "isignthis");
        detailsIn.addProperty("returnUrl", "https://guarda.co/");
//        detailsIn.addProperty("redirectUrl", "https://guarda.co/");
        transferIn.add("details", detailsIn);
        //transferOut
        JsonObject transferOut = new JsonObject();

        transferOut.addProperty("medium", "blockchain");
        //detailsOut
        JsonObject detailsOut = new JsonObject();
        //TODO: change to account (address)
//        details.addProperty("account", walletManager.getWalletFriendlyAddress());
        if (generatedAddress.isEmpty()) {
            closeProgress();
            Toast.makeText(getApplicationContext(), "All exchange services are unavailable now.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (BuildConfig.DEBUG) {
//            detailsOut.addProperty("account", "n3jYBjCzgGNydQwf83Hz6GBzGBhMkKfgL1");
            detailsOut.addProperty("account", generatedAddress);
        } else {
            detailsOut.addProperty("account", generatedAddress);
        }
//        detailsOut.addProperty("provider", "isignthis");
//        detailsOut.addProperty("returnUrl", "https://guarda.co");
//        detailsOut.addProperty("redirectUrl", "https://guarda.co");
        transferOut.add("details", detailsOut);

        JsonObject trade = new JsonObject();
        trade.addProperty("priceQuoteId", coinifyQuoteId);
        trade.add("transferIn", transferIn);
        trade.add("transferOut", transferOut);

        Log.d("psd", "PurchaseCoins coinifyTrade - json trade = " + trade.toString());
        Requestor.coinifyTrade(sharedManager.getCoinifyAccessToken(), trade, new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                CoinifyTradeResponse ctr = (CoinifyTradeResponse) response;
                sharedManager.setCoinifyLastTradeId(ctr.getId());
                sharedManager.setCoinifyLastTradeState(ctr.getState());

                if (coinifySelectedMethod.equalsIgnoreCase("card")) {
                    continueLoadWebView(ctr.getTransferIn().getDetails().getRedirectUrl());
                } else if (coinifySelectedMethod.equalsIgnoreCase("bank")) {
                    Gson gson = new Gson();
                    String tradeJson = gson.toJson(ctr);
                    sharedManager.setCoinifyBankTrade(tradeJson);
                    showReceiptActivity();
                }

                closeProgress();
                Log.d("psd", "PurchaseCoins coinifyTrade success - trade id = " + ctr.getId());
            }

            @Override
            public void onFailure(String msg) {
                closeProgress();
                JsonParser jp = new JsonParser();
                JsonObject jo = jp.parse(msg).getAsJsonObject();
                Toast.makeText(getApplicationContext(), jo.get("error_description").toString(), Toast.LENGTH_SHORT).show();
                Log.d("psd", "PurchaseCoins coinifyTrade onFailure - " + msg);
            }
        });
    }

    private void showCongratsActivity() {
        Intent intent = new Intent(this, CongratsActivity.class);
        intent.putExtra(Extras.CONGRATS_TEXT, getString(R.string.result_transaction_successful));
        intent.putExtra(Extras.COME_FROM, Extras.FROM_PURCHASE_WEMOVECOIN);
        startActivity(intent);
    }

    private void showReceiptActivity() {
        Intent intent = new Intent(this, ReceiptCoinifyActivity.class);
        intent.putExtra((Extras.COINIFY_IN_MEDIUM), coinifySelectedMethod);
        intent.putExtra(Extras.COINIFY_SEND_AMOUNT, getIntent().getStringExtra(Extras.COINIFY_SEND_AMOUNT));
        startActivity(intent);
    }
}
