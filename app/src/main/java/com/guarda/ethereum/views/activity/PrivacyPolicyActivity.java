package com.guarda.ethereum.views.activity;


import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.guarda.ethereum.R;
import com.guarda.ethereum.views.activity.base.AToolbarActivity;

import butterknife.BindView;

public class PrivacyPolicyActivity extends AToolbarActivity{

    @BindView(R.id.tv_privacy_police)
    TextView tvPrivacyPolicy;

    @Override
    protected void init(Bundle savedInstanceState) {
        setToolBarTitle(getString(R.string.title_privacy_policy));
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_privacy_policy;
    }
}
