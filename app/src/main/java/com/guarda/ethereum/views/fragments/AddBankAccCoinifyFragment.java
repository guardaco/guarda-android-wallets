package com.guarda.ethereum.views.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.guarda.ethereum.R;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import butterknife.OnClick;

public class AddBankAccCoinifyFragment extends BaseFragment {

    private String bankType = "";

    public AddBankAccCoinifyFragment() {
    }

    @Override
    protected void init() {
        try {
            getActivity().setTitle(getString(R.string.coinify_add_bank));
        } catch (Exception e) {
            e.printStackTrace();
        }


        initBackButton();
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_add_bank_acc_coinify;
    }

    @OnClick({R.id.cv_int_bank, R.id.cv_dan_bank})
    public void newBankAccount(View view) {
        switch (view.getId()) {
            case R.id.cv_int_bank:
                bankType = "int";
                break;
            case R.id.cv_dan_bank:
                bankType = "dan";
                break;
        }

        goToNewFragment();
    }

    private void goToNewFragment() {
        NewBankAccCoinifyFragment startFragment = new NewBankAccCoinifyFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Extras.COINIFY_BANK_TYPE, bankType);
        startFragment.setArguments(bundle);
        navigateToFragment(startFragment);
//        if (getActivity() != null) {
//            getActivity().getSupportFragmentManager().popBackStack();
//            navigateToFragment(startFragment);
//        }
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
