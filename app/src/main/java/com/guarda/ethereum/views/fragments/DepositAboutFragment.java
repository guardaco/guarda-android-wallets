package com.guarda.ethereum.views.fragments;

import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.guarda.ethereum.R;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import butterknife.BindView;

public class DepositAboutFragment extends BaseFragment {

    @BindView(R.id.textViewAbout)
    TextView textViewAbout;



    private Fragment prevFragment = new DepositFragment_decent();
    private String spinnerExchangeSymbol = "";
    private int spinnerFromCoinPosition = 0;
    private int spinnerToCoinPosition = 0;



    public DepositAboutFragment setData(Fragment prevFragment) {
        this.prevFragment = prevFragment;
        return this;
    }



    @Override
    protected int getLayout() {
        return R.layout.fragment_exchange_about;
    }



    @Override
    protected void init() {
        setToolbarTitle(getString(R.string.title_fragment_decent_deposit_about));
        textViewAbout.setText(getString(R.string.deposit_decent_about));
        initBackButton();
    }



    @Override
    public boolean onBackPressed() {
        navigateToFragment(prevFragment);
        return true;
    }



    @Override
    public boolean onHomePressed() {
        onBackPressed();
        return true;
    }

}
