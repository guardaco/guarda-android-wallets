package com.guarda.ethereum.views.fragments;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import com.freshchat.consumer.sdk.Freshchat;
import com.guarda.ethereum.BuildConfig;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.constants.RequestCode;
import com.guarda.ethereum.views.activity.ConfirmPinCodeActivity;
import com.guarda.ethereum.views.activity.CreateAccessCodeActivity;
import com.guarda.ethereum.views.activity.SettingsWebViewActivity;
import com.guarda.ethereum.views.fragments.base.BaseFragment;
import com.guarda.zcash.sapling.SyncManager;
import com.guarda.zcash.sapling.db.DbManager;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;

import static com.guarda.ethereum.models.constants.Common.PRIVACY_POLICE_LINK;
import static com.guarda.ethereum.models.constants.Common.TERM_OF_USE_LINK;

public class SettingsFragment extends BaseFragment {

    @BindView(R.id.switch_security_code)
    SwitchCompat swSecureCode;
    @BindView(R.id.sp_local_currency)
    Spinner spLocalCurrency;
    @BindView(R.id.ver_about)
    TextView verAbout;
    @BindView(R.id.ll_custom_node)
    LinearLayout ll_custom_node;
    @BindView(R.id.ll_support)
    LinearLayout ll_support;
    @BindView(R.id.ll_terms_of_use)
    LinearLayout ll_terms_of_use;
    @BindView(R.id.ll_privacy_policy)
    LinearLayout ll_privacy_policy;

    @Inject
    SharedManager sharedManager;
    @Inject
    SyncManager syncManager;
    @Inject
    DbManager dbManager;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected int getLayout() {
        return R.layout.fragment_settings;
    }

    @Override
    protected void init() {
        GuardaApp.getAppComponent().inject(this);
        initLocalCurrency();
        initSwitchChecked();
        setVersion();
        initMenuButton();
        if (BuildConfig.DEBUG) {
            ll_custom_node.setVisibility(View.VISIBLE);
        }
    }

    private void initLocalCurrency() {
        final String[] array = getResources().getStringArray(R.array.currencies);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.view_spinner_item, array);
        spLocalCurrency.setAdapter(adapter);

        for (int i = 0; i < array.length; i++) {
            String currency = array[i];
            if (currency.equalsIgnoreCase(sharedManager.getLocalCurrency())) {
                spLocalCurrency.setSelection(i);
            }
        }

        spLocalCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                sharedManager.setLocalCurrency(array[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    private void initSwitchChecked() {
        swSecureCode.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked != sharedManager.getIsPinCodeEnable()) {
                if (swSecureCode.isChecked()) {
                    startActivity(new Intent(getActivity(), CreateAccessCodeActivity.class));
                } else {
                    Intent intent = new Intent(getActivity(), ConfirmPinCodeActivity.class);
                    startActivityForResult(intent, RequestCode.CONFIRM_PIN_CODE_REQUEST);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ll_support.setEnabled(true);
        ll_terms_of_use.setEnabled(true);
        ll_privacy_policy.setEnabled(true);
        swSecureCode.setChecked(sharedManager.getIsPinCodeEnable());
    }

    @OnClick({R.id.ll_local_currency,
            R.id.ll_about_app, R.id.ll_terms_of_use, R.id.ll_privacy_policy, R.id.ll_support, R.id.ll_custom_node})
    public void settingItemClick(View view) {
        switch (view.getId()) {
            case R.id.ll_local_currency:
                spLocalCurrency.performClick();
                break;
            case R.id.ll_terms_of_use:
                ll_terms_of_use.setEnabled(false);
                openWebURL(TERM_OF_USE_LINK);
                break;
            case R.id.ll_privacy_policy:
                ll_privacy_policy.setEnabled(false);
                openWebURL(PRIVACY_POLICE_LINK);
                break;
            case R.id.ll_support:
                ll_support.setEnabled(false);
                openSupportConversation();
                break;
            case R.id.ll_custom_node:
                openCustomNode();
                break;
        }
    }

    private void openCustomNode() {
        navigateToFragment(new CustomNodeFragment());
    }

    private void openSupportConversation() {
        Freshchat.showConversations(getContext());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCode.CONFIRM_PIN_CODE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                swSecureCode.setChecked(false);
                sharedManager.setIsPinCodeEnable(false);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                swSecureCode.setChecked(true);
            }
        }
    }

    public void openWebURL(String inURL) {
        Intent intent = new Intent(getActivity(), SettingsWebViewActivity.class);
        intent.putExtra(Extras.WEB_VIEW_URL, inURL);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.no_slide);
    }

    private void setVersion() {
        String pl = "%s %s";
        if (BuildConfig.DEBUG) pl = "%s %s-debug";
        try {
            PackageManager manager = getActivity().getPackageManager();
            PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
            verAbout.setText(String.format(pl, getString(R.string.about_ver), info.versionName));
        } catch (Exception e) {
            Log.e("psd", "Error getting appVersion - " + e.toString());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}
