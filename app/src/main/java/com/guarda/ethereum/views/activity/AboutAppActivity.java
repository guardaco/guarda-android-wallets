package com.guarda.ethereum.views.activity;


import android.os.Bundle;

import com.guarda.ethereum.R;
import com.guarda.ethereum.views.activity.base.AToolbarActivity;

public class AboutAppActivity extends AToolbarActivity{
    @Override
    protected void init(Bundle savedInstanceState) {
        setToolBarTitle(getString(R.string.title_about_app));
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_about_app;
    }
}
