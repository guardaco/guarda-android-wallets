package com.guarda.ethereum.views.fragments;

import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.guarda.ethereum.R;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import butterknife.BindView;

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
