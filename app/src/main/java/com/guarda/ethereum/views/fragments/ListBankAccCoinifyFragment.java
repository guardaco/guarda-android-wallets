package com.guarda.ethereum.views.fragments;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.models.items.CoinifyBankAcc;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;
import com.guarda.ethereum.views.activity.BankAccCoinifyActivity;
import com.guarda.ethereum.views.adapters.BankAccountsAdapter;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;

public class ListBankAccCoinifyFragment extends BaseFragment {

    @BindView(R.id.tv_noacc)
    TextView tv_noacc;
    @BindView(R.id.rv_bank_acc_list)
    RecyclerView rv_bank_acc_list;

    @Inject
    SharedManager sharedManager;

    private String bankType = "";

    public ListBankAccCoinifyFragment() {
    }

    @Override
    protected void init() {
        GuardaApp.getAppComponent().inject(this);

        initBackButton();

        getBankAccounts();
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_list_bank_acc_coinify;
    }

    private void getBankAccounts() {
        showProgress();
        Requestor.coinifyBankAccounts(sharedManager.getCoinifyAccessToken(), new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                List<CoinifyBankAcc> bankAccs = (List<CoinifyBankAcc>) response;
                if (bankAccs.size() != 0) {
                    tv_noacc.setVisibility(View.GONE);

                    BankAccountsAdapter adapter = new BankAccountsAdapter(bankAccs);

                    final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

                    rv_bank_acc_list.setLayoutManager(layoutManager);
                    rv_bank_acc_list.setAdapter(adapter);

                    adapter.setListener(new BankAccountsAdapter.OnItemClickListener() {
                        @Override
                        public void OnItemClick(int position, int bankAccId, String bankName, String holderName, String accNumber) {
                            if (getActivity() != null) {
                                ((BankAccCoinifyActivity) getActivity()).goToConfirm(bankAccId, bankName, holderName, accNumber);
                            }
                        }
                    });
                } else {
                    tv_noacc.setVisibility(View.VISIBLE);
                }


                closeProgress();
            }

            @Override
            public void onFailure(String msg) {
                closeProgress();
                JsonParser jp = new JsonParser();
                JsonObject jo = jp.parse(msg).getAsJsonObject();
                Toast.makeText(getActivity(), jo.get("error_description").toString(), Toast.LENGTH_LONG).show();

                Log.d("psd", "coinifyBankAccounts - onFailure: " + msg + ",  error_description: " + jo.get("error_description").toString());
            }
        });

    }

//    @Override
//    public boolean onBackPressed() {
//        if (prevFragment != null) {
//            navigateToFragment(prevFragment);
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    public boolean onHomePressed() {
//        return onBackPressed();
//    }

}
