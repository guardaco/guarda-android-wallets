package com.guarda.ethereum.views.fragments;


import android.content.Intent;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.ChangellyNetworkManager;
import com.guarda.ethereum.managers.CurrencyListHolder;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.items.ResponseCurrencyItem;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.views.activity.GenerateAddressActivity;
import com.guarda.ethereum.views.adapters.CryptoAdapter;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;

@AutoInjector(GuardaApp.class)
public class PurchaseFragment extends BaseFragment {

    private String TAG = "PurchaseFragment";

    @BindView(R.id.rv_crypto_purchase)
    RecyclerView rvCryptoRecycler;

    private CryptoAdapter adapter;

    @Inject
    CurrencyListHolder currentCrypto;

    @Override
    protected int getLayout() {
        return R.layout.fragment_purchase;
    }

    @Override
    protected void init() {
        GuardaApp.getAppComponent().inject(this);
            initRecycler();
    }

    private void initRecycler() {
        adapter = new CryptoAdapter();

        adapter.setItemClickListener(new CryptoAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position, String name, String code) {
                Intent intent = new Intent(new Intent(getActivity(), GenerateAddressActivity.class));
                intent.putExtra(Extras.SELECTED_CURRENCY, code);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.no_slide);
            }
        });

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        rvCryptoRecycler.setLayoutManager(layoutManager);
        rvCryptoRecycler.setAdapter(adapter);

        if (currentCrypto.getListOfCurrencies() == null
                || currentCrypto.getListOfCurrencies().isEmpty()) {
            loadCurrencies();
        } else {
            adapter.updateList(currentCrypto.getListOfCurrencies());
        }

    }

    private void loadCurrencies() {
        showProgress(getString(R.string.loader_loading_available_currencies));
        ChangellyNetworkManager.getCurrencies(new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                if (isVisible()) {
                    ResponseCurrencyItem responseCurrency = (ResponseCurrencyItem) response;

                    adapter.updateList(currentCrypto.castResponseCurrencyToCryptoItem(responseCurrency, getActivity().getApplicationContext()));
                    closeProgress();
                }

            }

            @Override
            public void onFailure(String msg) {
                closeProgress();
            }
        });
    }

}
