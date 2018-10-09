package com.guarda.ethereum.views.fragments;


import android.view.View;
import android.widget.TextView;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.ChangellyNetworkManager;
import com.guarda.ethereum.managers.CurrencyListHolder;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.items.ResponseCurrencyItem;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.utils.ClipboardUtils;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

@AutoInjector(GuardaApp.class)
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
