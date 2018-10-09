package com.guarda.ethereum.views.activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

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
import com.guarda.ethereum.models.items.CoinifyTradeResponse;
import com.guarda.ethereum.models.items.ResponseGenerateAddress;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;
import com.guarda.ethereum.views.activity.base.AToolbarActivity;

import java.util.List;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import im.delight.android.webview.AdvancedWebView;

import static com.guarda.ethereum.models.constants.Common.MAIN_CURRENCY;

public class CoinifyKYCActivity extends AToolbarActivity implements AdvancedWebView.Listener {

    @BindView(R.id.coinify_kyc)
    AdvancedWebView webView;

    private boolean isShownCongrats = false;
    private String kycUrl = "";

    @Override
    protected void init(Bundle savedInstanceState) {
        setToolBarTitle("Coinify KYC");
        webView.setListener(this, this);

        kycUrl = getIntent().getStringExtra(Extras.COINIFY_KYC_URL);
        if (kycUrl != null) {
            continueLoadWebView(kycUrl);
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_coinify_kyc;
    }

    @Override
    public void onBackPressed() {
        if (!webView.onBackPressed()) { return; }
        super.onBackPressed();
        overridePendingTransition(R.anim.no_slide, R.anim.slide_in_right);
    }

    private void continueLoadWebView(final String url) {

        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("psd", "CoinifyKYCActivity.continueLoadWebView()... url: " + url);

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
                            if ((url.contains(Common.SITE_APP_LINK) || url.contains("https://app-api.coinify.com/kyc/return/isignthis")) && !isShownCongrats) {
                                showCongratsActivity();
                                isShownCongrats = true;
                            }

                            view.loadUrl(url);
                            return true;
                        }

//                        @Override
//                        public void onLoadResource(WebView view, String url) {
//
//                            if ((url.contains(Common.SITE_APP_LINK) || url.startsWith("https://app-api.coinify.com/kyc/")) && !isShownCongrats) {
//                                showCongratsActivity();
//                                isShownCongrats = true;
//                            } else if (url.equals(kycUrl)) {
//                                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
////                                builder.setToolbarColor(getApplicationContext().getResources()
////                                        .getColor(R.color.toolbar_end_color));
//                                CustomTabsIntent customTabsIntent = builder.build();
//                                customTabsIntent.launchUrl(getApplicationContext(), Uri.parse(kycUrl));
////                                CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
////                                        .addDefaultShareMenuItem()
////                                        .setToolbarColor(getApplicationContext().getResources()
////                                                .getColor(R.color.navBarColor))
////                                        .setShowTitle(true)
////                                        .build();
////
////// This is optional but recommended
////                                CustomTabsHelper.addKeepAliveExtra(getApplicationContext(), customTabsIntent.intent);
////
////// This is where the magic happens...
////                                CustomTabsHelper.openCustomTab(getApplicationContext(), customTabsIntent,
////                                        Uri.parse(url),
////                                        new WebViewFallback());
//                            }
//                        }
                    });

                    webView.loadUrl(url);
                    Log.d("!!!!!", "url " + url);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showCongratsActivity() {
        Intent intent = new Intent(this, CongratsActivity.class);
        intent.putExtra(Extras.CONGRATS_TEXT, "KYC application sent");
        intent.putExtra(Extras.COME_FROM, Extras.FROM_PURCHASE_WEMOVECOIN);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        webView.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) { }

    @Override
    public void onPageFinished(String url) { }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) { }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) { }

    @Override
    public void onExternalPageRequest(String url) { }
}
