package com.guarda.ethereum.views.fragments;


import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.CurrencyListHolder;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import javax.inject.Inject;

public class DisabledFragment extends BaseFragment {

    @Inject
    CurrencyListHolder currentCrypto;


    public DisabledFragment() {
        GuardaApp.getAppComponent().inject(this);
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_disabled;
    }

    @Override
    protected void init() {
    }

}
