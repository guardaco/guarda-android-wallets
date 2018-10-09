package com.guarda.ethereum.views.activity;

import android.os.Bundle;
import android.webkit.WebView;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.views.activity.base.AToolbarActivity;

import autodagger.AutoInjector;
import butterknife.BindView;

/**
 * Created by psd on 13.12.2017.
 */

@AutoInjector(GuardaApp.class)
public class SettingsWebViewActivity extends AToolbarActivity {

    @BindView(R.id.wv_settings)
    WebView webView;

    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        setToolBarTitle(R.string.empty_string);
        loadWebView(getIntent().getStringExtra(Extras.WEB_VIEW_URL));
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_settings_webview;
    }

    private void loadWebView(String url) {
        webView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_slide, R.anim.slide_in_right);
    }
}
