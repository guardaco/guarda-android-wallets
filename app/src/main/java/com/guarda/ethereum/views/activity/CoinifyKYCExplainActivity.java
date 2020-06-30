package com.guarda.ethereum.views.activity;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.browser.customtabs.CustomTabsIntent;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.items.CoinifyKYCResponse;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;
import com.guarda.ethereum.views.activity.base.AToolbarActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

public class CoinifyKYCExplainActivity extends AToolbarActivity {

    @BindView(R.id.ll_coinify_kyc_status)
    LinearLayout ll_coinify_kyc_status;
    @BindView(R.id.coinify_kyc_status)
    TextView coinify_kyc_status;
    @BindView(R.id.coinify_kyc_explain)
    TextView coinify_kyc_explain;
    @BindView(R.id.coinify_kyc)
    WebView webView;

    @Inject
    SharedManager sharedManager;

    String status = "";
    private boolean isShownCongrats = false;
    private String kycUrl = "";


    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        setToolBarTitle("Verify your Identity");
        status = getIntent().getStringExtra(Extras.COINIFY_KYC_STATUS);
        kycUrl = getIntent().getStringExtra(Extras.COINIFY_KYC_URL);
        if (status != null) {
            switch (status) {
                case "new":
                    ll_coinify_kyc_status.setVisibility(View.INVISIBLE);
                    break;
                case "pending":
                    coinify_kyc_status.setText(status);
                    coinify_kyc_explain.setText("Waiting for action from the user.");
                    break;
                case "rejected":
                    coinify_kyc_status.setText(status);
                    coinify_kyc_explain.setText("KYC review was rejected");
                    break;
                case "failed":
                    coinify_kyc_status.setText(status);
                    coinify_kyc_explain.setText("KYC review failed");
                    break;
                case "expired":
                    coinify_kyc_status.setText(status);
                    coinify_kyc_explain.setText("KYC review expired before it was completed");
                    break;
                case "documentsRequired":
                    coinify_kyc_status.setText("documents required");
                    coinify_kyc_explain.setText("Trader needs to upload more documents");
                    break;
                default:
                    ll_coinify_kyc_status.setVisibility(View.INVISIBLE);
                    break;

            }
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_coinify_kyc_explain;
    }

    @OnClick(R.id.btn_next)
    public void btnNext(View view) {
        if (status.equalsIgnoreCase("pending") || status.equalsIgnoreCase("documentsRequired")) {
//            goToCoinifyKYCActivity(getIntent().getStringExtra(Extras.COINIFY_KYC_URL));
            goToCoinifyKYCActivity(kycUrl);
        } else {
            showProgress();
            JsonObject coinifyKyc = new JsonObject();
            coinifyKyc.addProperty("returnUrl", Common.SITE_APP_LINK);
            Requestor.coinifyPostKYC(sharedManager.getCoinifyAccessToken(), coinifyKyc, new ApiMethods.RequestListener() {
                @Override
                public void onSuccess(Object response) {
                    if (response != null) {
                        CoinifyKYCResponse kycResponse = (CoinifyKYCResponse) response;
                        goToCoinifyKYCActivity(kycResponse.getRedirectUrl());
                    }
                    closeProgress();
                }

                @Override
                public void onFailure(String msg) {
                    closeProgress();
                    Toast.makeText(getApplicationContext(), "Service is temporarily unavailable", Toast.LENGTH_SHORT).show();
                }
            });
        }

        overridePendingTransition(R.anim.slide_in_left, R.anim.no_slide);
    }

    private void goToCoinifyKYCActivity(String url) {
        Intent intent = new Intent(this, CoinifyKYCActivity.class);
        intent.putExtra(Extras.COINIFY_KYC_URL, url);
        startActivity(intent);
//        continueLoadWebView(url);
    }

    private void continueLoadWebView(final String mainUrl) {

        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("psd", "CoinifyKYCActivity.continueLoadWebView()... url: " + mainUrl);

                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.getSettings().setAllowFileAccessFromFileURLs(true);
                    webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
                    webView.getSettings().setAllowFileAccess(true);

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
//                            if (url.contains(Common.SITE_APP_LINK) && !isShownCongrats) {
//                                showCongratsActivity();
//                                isShownCongrats = true;
//                            }
//
//                            view.loadUrl(url);
                            return true;
                        }

                        @Override
                        public void onLoadResource(WebView view, String url) {
                            if ((url.contains(Common.SITE_APP_LINK) || url.startsWith("https://app-api.coinify.com/kyc/")) && !isShownCongrats) {
                                showCongratsActivity();
                                isShownCongrats = true;
                            } else if (url.equals(mainUrl) && 0 == 1) {
                                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
//                                builder.setToolbarColor(getApplicationContext().getResources()
//                                        .getColor(R.color.toolbar_end_color));
                                CustomTabsIntent customTabsIntent = builder.build();
                                customTabsIntent.launchUrl(getApplicationContext(), Uri.parse(mainUrl));
//                                CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
//                                        .addDefaultShareMenuItem()
//                                        .setToolbarColor(getApplicationContext().getResources()
//                                                .getColor(R.color.navBarColor))
//                                        .setShowTitle(true)
//                                        .build();
//
//// This is optional but recommended
//                                CustomTabsHelper.addKeepAliveExtra(getApplicationContext(), customTabsIntent.intent);
//
//// This is where the magic happens...
//                                CustomTabsHelper.openCustomTab(getApplicationContext(), customTabsIntent,
//                                        Uri.parse(url),
//                                        new WebViewFallback());
                            }
                        }
                    });

                    webView.loadUrl(mainUrl);
                    Log.d("!!!!!", "url " + mainUrl);
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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_slide, R.anim.slide_in_right);
    }

}
