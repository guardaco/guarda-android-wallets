package com.guarda.ethereum.views.activity;


import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.guarda.ethereum.R;
import com.guarda.ethereum.views.activity.base.AToolbarActivity;

import butterknife.BindView;

public class TermOfUseActivity extends AToolbarActivity{

    @BindView(R.id.tv_terms_of_service)
    TextView tvTermsOfService;

    @Override
    protected void init(Bundle savedInstanceState) {
        setToolBarTitle(getString(R.string.title_term_of_use));
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_term_of_use;
    }
}
