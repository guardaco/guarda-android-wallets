package com.guarda.ethereum.views.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.guarda.ethereum.R;
import com.guarda.ethereum.models.ExchangeSpinnerRowModel;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.views.activity.TransactionDetailsActivity;
import com.guarda.ethereum.views.adapters.ExchangeSpinnerAdapter;
import com.guarda.ethereum.views.adapters.PurchaseServicesAdapter;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class PurchaseServiceFragment extends BaseFragment {

    @BindView(R.id.rv_services_list)
    RecyclerView rv_services_list;

    public String selectedService = "wemovecoins";
    public String buyOrSell = "buy";


    @Override
    protected void init() {
        final PurchaseServiceFragment thisFragment = this;

        PurchaseServicesAdapter adapter = new PurchaseServicesAdapter(getActivity());
        adapter.notifyItemRemoved(0);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        rv_services_list.setLayoutManager(layoutManager);
        rv_services_list.setAdapter(adapter);


        adapter.setBuyClickListener(new PurchaseServicesAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position) {
                if (position == 0) {
                    if (Common.MAIN_CURRENCY.equalsIgnoreCase("bts") ||
                            Common.MAIN_CURRENCY.equalsIgnoreCase("zec") ||
                            Common.MAIN_CURRENCY.equalsIgnoreCase("qtum")) {
                        setIndacoinItem(thisFragment);
                    } else {
                        setCoinifyItem(thisFragment);
                        buyOrSell = "buy";
                    }
                } else if (position == 1) {
                    if (Common.MAIN_CURRENCY.equalsIgnoreCase("bts") ||
                            Common.MAIN_CURRENCY.equalsIgnoreCase("zec") ||
                            Common.MAIN_CURRENCY.equalsIgnoreCase("qtum")) {
                        //nothing
                    } else {
                        setIndacoinItem(thisFragment);
                    }
                }
            }
        });

        adapter.setSellClickListener(new PurchaseServicesAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position) {
                if (position == 0) {
                    if (Common.MAIN_CURRENCY.equalsIgnoreCase("bts") ||
                            Common.MAIN_CURRENCY.equalsIgnoreCase("zec") ||
                            Common.MAIN_CURRENCY.equalsIgnoreCase("qtum")) {
                        //nothing
                    } else {
                        setCoinifyItem(thisFragment);
                        buyOrSell = "sell";
                    }
                }
            }
        });

        setToolbarTitle(getString(R.string.app_purchase_service));
        initMenuButton();
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_purchase_service;
    }

    private void setCoinifyItem(PurchaseServiceFragment thisFragment) {
        selectedService = "coinify";
        EnterEmailCoinifyFragment startFragment = new EnterEmailCoinifyFragment();
        startFragment.setPrevFragment(thisFragment);
        navigateToFragment(startFragment);
    }

    private void setIndacoinItem(PurchaseServiceFragment thisFragment) {
        selectedService = "indacoin";
        EnterIndacoinEmailFragment startFragment = new EnterIndacoinEmailFragment();
        startFragment.setPrevFragment(thisFragment);
        navigateToFragment(startFragment);
    }

    private void setWmcItem(PurchaseServiceFragment thisFragment) {
        selectedService = "wemovecoins";
        PurchaseCoinsFragment startFragment = new PurchaseCoinsFragment();
        startFragment.setPrevFragment(thisFragment);
        navigateToFragment(startFragment);
    }
}
