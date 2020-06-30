package com.guarda.ethereum.views.fragments;

import android.widget.TextView;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.screens.exchange.first.ExchangeFragment;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import butterknife.BindView;

public class ExchangeAboutFragment extends BaseFragment {

    @BindView(R.id.textViewAbout)
    TextView textViewAbout;

    @Override
    protected int getLayout() {
        return R.layout.fragment_exchange_about;
    }

    @Override
    protected void init() {
        setToolbarTitle(getString(R.string.title_fragment_exchange_about));
        textViewAbout.setText(getString(R.string.exchange_about));
        initBackButton();
    }

    @Override
    public boolean onBackPressed() {
        navigateToFragment(new ExchangeFragment());
        return true;
    }

    @Override
    public boolean onHomePressed() {
        onBackPressed();
        return true;
    }

}
