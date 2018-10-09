package com.guarda.ethereum.views.fragments;

import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import autodagger.AutoInjector;
import butterknife.BindView;

@AutoInjector(GuardaApp.class)
public class ExplainFragment extends BaseFragment {

    @BindView(R.id.textViewAbout)
    TextView textViewAbout;

    private Fragment prevFragment = new Fragment();

    public void setPrevFragment(Fragment fragment) {
        prevFragment = fragment;
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_explain;
    }

    @Override
    protected void init() {
        setToolbarTitle(getArguments().getString(Extras.EXPLAIN_TITLE));
        textViewAbout.setText(getArguments().getString(Extras.EXPLAIN_TEXT));
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
