package com.guarda.ethereum.views.fragments;

import android.widget.TextView;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import autodagger.AutoInjector;
import butterknife.BindView;

@AutoInjector(GuardaApp.class)
public class ExchangeAboutFragment extends BaseFragment {

    @BindView(R.id.textViewAbout)
    TextView textViewAbout;



    private ExchangeFragment prevFragment = new ExchangeFragment();
    private String spinnerExchangeSymbol = "";
    private int spinnerFromCoinPosition = 0;
    private int spinnerToCoinPosition = 0;



    public ExchangeAboutFragment setData(String spinnerExchangeSymbol, int spinnerFromCoinPosition, int spinnerToCoinPosition, ExchangeFragment prevFragment) {
        this.spinnerExchangeSymbol = spinnerExchangeSymbol;
        this.spinnerFromCoinPosition = spinnerFromCoinPosition;
        this.spinnerToCoinPosition = spinnerToCoinPosition;
        this.prevFragment = prevFragment;
        return this;
    }



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
        navigateToFragment(prevFragment.setData(spinnerExchangeSymbol, spinnerFromCoinPosition, spinnerToCoinPosition));
        return true;
    }



    @Override
    public boolean onHomePressed() {
        onBackPressed();
        return true;
    }

}
