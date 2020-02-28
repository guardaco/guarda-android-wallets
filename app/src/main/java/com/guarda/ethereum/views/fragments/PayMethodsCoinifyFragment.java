package com.guarda.ethereum.views.fragments;


import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.items.CoinifyKYCResponse;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;
import com.guarda.ethereum.views.activity.CoinifyKYCExplainActivity;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import java.util.List;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

@AutoInjector(GuardaApp.class)
public class PayMethodsCoinifyFragment extends BaseFragment {

    @BindView(R.id.cv_card)
    CardView cv_card;
    @BindView(R.id.cv_bank)
    CardView cv_bank;

    @Inject
    SharedManager sharedManager;

    private PurchaseServiceFragment prevFragment = null;
    private EnterEmailCoinifyFragment coinifyPrevFragment = null;

    public String selectedService = "coinify";
    public String selectedMethod = "card";

    public PayMethodsCoinifyFragment() {
        GuardaApp.getAppComponent().inject(this);
    }

    public void setCoinifyPrevFragment(EnterEmailCoinifyFragment prevFragment) {
        this.coinifyPrevFragment = prevFragment;
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_methods_coinify;
    }

    @Override
    protected void init() {
        setToolbarTitle("Payment method");
        initBackButton();
    }

    @OnClick({R.id.cv_card, R.id.cv_bank})
    public void chooseMethod(View view) {
        switch (view.getId()) {
            case R.id.cv_card:
                selectedMethod = "card";
                goToPurchaseCoinsFragment();
                break;
            case R.id.cv_bank:
                selectedMethod = "bank";

                showProgress();
                Requestor.coinifyKYCList(sharedManager.getCoinifyAccessToken(), new ApiMethods.RequestListener() {
                    @Override
                    public void onSuccess(Object response) {
                        List<CoinifyKYCResponse> kycList = (List<CoinifyKYCResponse>) response;
                        if (kycList.size() > 0) {
                            if (kycList.get(0).getState().equalsIgnoreCase("completed")) {
                                goToPurchaseCoinsFragment();
                            } else if (kycList.get(0).getState().equalsIgnoreCase("reviewing")) {
                                Toast.makeText(getContext(), "KYC is awaiting manual review", Toast.LENGTH_LONG).show();
                            } else {
                                goToCoinifyKYCExplain(kycList.get(0).getState(), kycList.get(0).getRedirectUrl());
                            }
                        } else {
                            goToCoinifyKYCExplain("new", "");
                        }

                        closeProgress();
                    }

                    @Override
                    public void onFailure(String msg) {
                        Toast.makeText(getContext(), "Service is temporarily unavailable", Toast.LENGTH_LONG).show();
                        closeProgress();
                    }
                });
                break;
        }
    }

    private void goToPurchaseCoinsFragment() {
        final PayMethodsCoinifyFragment thisFragment = this;
        PurchaseCoinsFragment startFragment = new PurchaseCoinsFragment();
        startFragment.setCoinifyPrevFragment(thisFragment);
        navigateToFragment(startFragment);
    }

    private void goToCoinifyKYCExplain(String status, String kycUrl) {
        Intent intent = new Intent(getActivity(), CoinifyKYCExplainActivity.class);
        intent.putExtra(Extras.COINIFY_KYC_STATUS, status);
        intent.putExtra(Extras.COINIFY_KYC_URL, kycUrl);
        startActivity(intent);
    }

    @Override
    public boolean onBackPressed() {
        if (prevFragment != null) {
            navigateToFragment(prevFragment);
            return true;
        }

        if (coinifyPrevFragment != null) {
            navigateToFragment(coinifyPrevFragment);
            return true;
        }
        return false;
    }

    @Override
    public boolean onHomePressed() {
        return onBackPressed();
    }

}
