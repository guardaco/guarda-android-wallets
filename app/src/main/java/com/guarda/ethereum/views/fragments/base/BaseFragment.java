package com.guarda.ethereum.views.fragments.base;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.guarda.ethereum.R;
import com.guarda.ethereum.views.activity.MainActivity;
import com.guarda.ethereum.views.fragments.UserWalletFragment;

import butterknife.ButterKnife;

public abstract class BaseFragment extends Fragment {

    protected View root;
    protected ProgressDialog progressDialog;
    protected boolean isFragmentVisible = false;
    private boolean isVisible;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        isFragmentVisible = true;
        initDefault();

        return inflater.inflate(getLayout(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        init();
    }

    @Override
    public void onResume() {
        super.onResume();
        isFragmentVisible = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isFragmentVisible = false;
    }

    private void initDefault() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(getString(R.string.dialog_msg_please_wait));
        progressDialog.setCancelable(false);
    }

    abstract protected
    @LayoutRes
    int getLayout();

    abstract protected void init();

    protected void navigateToFragment(Fragment fragment) {
        if (isFragmentVisible) {
            if (getActivity().getSupportFragmentManager() != null) {
                getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }

            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fl_main_root, fragment, UserWalletFragment.class.getSimpleName());
            fragmentTransaction.commit();
        }
    }


    public void showProgress(String msg) {
        if (!progressDialog.isShowing() && isAdded()) {
            progressDialog.setMessage(msg);
            showProgress();
        }
    }

    public void showProgress() {
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    public void changeProgressText(String msg){
        progressDialog.setMessage(msg);
    }

    public void closeProgress() {
        try {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {}
    }

    public void showError(EditText editText, String error) {

        if (editText.getParent().getParent() instanceof TextInputLayout){
            ((TextInputLayout)editText.getParent().getParent()).setError(error);
            ((TextInputLayout)editText.getParent().getParent()).setErrorEnabled(true);
        }
    }

    public void hideError(EditText editText){
        if (editText.getParent().getParent() instanceof TextInputLayout){
            ((TextInputLayout)editText.getParent().getParent()).setErrorEnabled(false);
        }
    }

    @Override
    public void onDestroy() {
        if (progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        isVisible = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isVisible = false;
    }

    public boolean isVisibleOnAttach(){
        return isVisible;
    }

    public boolean onBackPressed() {
        return false;
    }

    public boolean onHomePressed() {
        return false;
    }

    public void initBackButton() {
        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initMenuButton() {
        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_side_menu);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setToolbarTitle(String newTitle) {
        try {
            ((MainActivity) getActivity()).setToolBarTitle(newTitle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
