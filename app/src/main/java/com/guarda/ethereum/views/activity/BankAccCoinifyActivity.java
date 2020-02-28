package com.guarda.ethereum.views.activity;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.views.activity.base.AToolbarExtMenuActivity;
import com.guarda.ethereum.views.fragments.AddBankAccCoinifyFragment;
import com.guarda.ethereum.views.fragments.ListBankAccCoinifyFragment;

import autodagger.AutoInjector;

@AutoInjector(GuardaApp.class)
public class BankAccCoinifyActivity extends AToolbarExtMenuActivity {

    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        setToolBarTitle("Choose bank account");
        initListView();

    }

    @Override
    protected int getLayout() {
        return R.layout.activity_bankacc_coinify;
    }

    @Override
    public void goToExt() {
        //button from toolbar to add new account clicked
        goToAddBankAccFragment();
    }

    private void initListView() {
//        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_up, R.anim.fade_out_animation, R.anim.fade_out_animation, R.anim.slide_down);
        fragmentTransaction.replace(R.id.fl_main_root, new ListBankAccCoinifyFragment());
        fragmentTransaction.commit();
    }



    private void goToAddBankAccFragment() {
        navigateToFragment(new AddBankAccCoinifyFragment());
    }

    public void goToConfirm(int bankAccId, String bankName, String holderName, String accNumber) {
        Intent intent = new Intent(this, SellConfirmCoinifyActivity.class);
        intent.putExtra(Extras.COINIFY_BANK_ACC_ID, bankAccId);
        intent.putExtra(Extras.COINIFY_BANK_NAME, bankName);
        intent.putExtra(Extras.COINIFY_HOLDER_NAME, holderName);
        intent.putExtra(Extras.COINIFY_ACC_NUMBER, accNumber);
        intent.putExtra(Extras.COINIFY_IN_AMOUNT, getIntent().getFloatExtra(Extras.COINIFY_IN_AMOUNT, 0.0f));
        intent.putExtra(Extras.COINIFY_IN_AMOUNT_CUR, getIntent().getStringExtra(Extras.COINIFY_IN_AMOUNT_CUR));
        intent.putExtra(Extras.COINIFY_AMOUNT_RATE, getIntent().getStringExtra(Extras.COINIFY_AMOUNT_RATE));
        intent.putExtra(Extras.COINIFY_OUT_AMOUNT, getIntent().getStringExtra(Extras.COINIFY_OUT_AMOUNT));
        intent.putExtra(Extras.COINIFY_OUT_AMOUNT_CUR, getIntent().getStringExtra(Extras.COINIFY_OUT_AMOUNT_CUR));
        intent.putExtra(Extras.COINIFY_COINIFY_FEE, getIntent().getStringExtra(Extras.COINIFY_COINIFY_FEE));
        intent.putExtra(Extras.COINIFY_EXCH_FEE, getIntent().getStringExtra(Extras.COINIFY_EXCH_FEE));
        intent.putExtra(Extras.COINIFY_QUOTE_ID, getIntent().getIntExtra(Extras.COINIFY_QUOTE_ID, 0));
        intent.putExtra(Extras.COINIFY_PAY_METHOD, "sell");
        startActivity(intent);
    }

    private void navigateToFragment(Fragment fragment) {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_up, R.anim.fade_out_animation, R.anim.fade_out_animation, R.anim.slide_down);
        fragmentTransaction.replace(R.id.fl_main_root, fragment);
        fragmentTransaction.commit();
    }

}
