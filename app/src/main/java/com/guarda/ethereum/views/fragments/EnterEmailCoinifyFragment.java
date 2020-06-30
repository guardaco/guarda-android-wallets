package com.guarda.ethereum.views.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.guarda.ethereum.BuildConfig;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.models.constants.Coinify;
import com.guarda.ethereum.models.constants.Const;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.items.CoinifyAuthResponse;
import com.guarda.ethereum.models.items.CoinifySignUpResponse;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;
import com.guarda.ethereum.views.activity.AmountCoinifyActivity;
import com.guarda.ethereum.views.activity.EnterNameToPurchaseActivity;
import com.guarda.ethereum.views.fragments.base.BaseFragment;
import com.hbb20.CountryCodePicker;

import java.util.regex.Matcher;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

import static com.guarda.ethereum.models.constants.Coinify.COINIFY_GRANT_TYPE_EMAIL_PASS;

public class EnterEmailCoinifyFragment extends BaseFragment {

    @BindView(R.id.ccp_country)
    CountryCodePicker ccpCountry;
    @BindView(R.id.et_email)
    EditText etEmail;
    @BindView(R.id.et_pass)
    EditText etPass;
    @BindView(R.id.coinify_terms_chb)
    CheckBox coinifyTermsChb;
    @BindView(R.id.coinify_terms_chb_text)
    TextView coinifyTermsChbText;
    @BindView(R.id.btn_next)
    Button btNext;

    @Inject
    SharedManager sharedManager;

    String selectedCountry = "";
    private final int MIN_NAME_LENGTH = 5;
    private PurchaseServiceFragment prevFragment = null;

    public String selectedService = "coinify";

    public EnterEmailCoinifyFragment() {
        GuardaApp.getAppComponent().inject(this);
    }

    public void setPrevFragment(PurchaseServiceFragment prevFragment) {
        this.prevFragment = prevFragment;
    }

    @Override
    protected void init() {
        setToolbarTitle(getString(R.string.toolbar_title_enter_email));

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideError(etEmail);
                if (validateEmail(s.toString())) {
                    etEmail.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_confirm, 0);
                } else {
                    etEmail.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ImageView iv = ccpCountry.getRootView().findViewById(R.id.imageView_arrow);
        iv.setVisibility(View.GONE);
        RelativeLayout rl = ccpCountry.getRootView().findViewById(R.id.rlClickConsumer);
        rl.setMinimumHeight(300);
        rl.setBackground(getResources().getDrawable(R.drawable.ic_exchangespinner_background_wr));

        TextView tv = ccpCountry.getRootView().findViewById(R.id.textView_selectedCountry);
        tv.setTextColor(getResources().getColor(R.color.baseDarkGreyTextColor));
        tv.setGravity(0);
        ccpCountry.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                selectedCountry = ccpCountry.getSelectedCountryCode();
            }
        });

        btNext.setEnabled(false);

        Spannable spannable = new SpannableString(getString(R.string.coinify_terms_accept));
        spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.blueHintColor)), 17, 52, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        coinifyTermsChbText.setText(spannable);

        coinifyTermsChb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btNext.setEnabled(true);
                } else {
                    btNext.setEnabled(false);
                }
            }
        });

        initBackButton();

        if (BuildConfig.DEBUG) {
            authEmailPass(COINIFY_GRANT_TYPE_EMAIL_PASS, "samosudovd@gmail.com", "zsxdcf33");
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_enter_email_coinify;
    }

    @OnClick(R.id.coinify_terms_chb_text)
    public void terms(View view) {
        String url = "https://coinify.com/legal/";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @OnClick(R.id.btn_next)
    public void nextClick() {
        String country = ccpCountry.getSelectedCountryNameCode();
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString();
        int partnerId;
        if (BuildConfig.DEBUG) {
            partnerId = Const.SANDBOX_COINIFY_PARTNER_ID;
        } else {
            partnerId = Const.PROD_COINIFY_PARTNER_ID;
        }

        if (!country.isEmpty()) {
            if (!email.isEmpty() && validateEmail(email)) {
                if (!pass.isEmpty()) {
                    showProgress();
                    btNext.setEnabled(false);

//                    if (!email.equalsIgnoreCase(sharedManager.getCoinifyEmail())) {
                        signUp(email, country, pass, partnerId);
//                    } else {
//                        authOfflineToken(COINIFY_GRANT_TYPE_OFFLINE_TOKEN, sharedManager.getCoinifyOfflineToken());
//                    }

                } else {
                    showError(etPass, "Password can not be empty");
                }
            } else {
                showError(etEmail, getString(R.string.email_is_not_valid));
            }
        }
    }

    private void signUp(final String email, String country, final String pass, int partnerId) {
        JsonObject signUp = new JsonObject();
        JsonObject profile = new JsonObject();
        JsonObject address = new JsonObject();
        address.addProperty("country", country);
        profile.add("address", address);
        signUp.addProperty("email", email.trim());
        signUp.addProperty("password", pass);
//      TODO: add partner id
        signUp.addProperty("partnerId", partnerId);
        signUp.add("profile", profile);
        signUp.addProperty("generateOfflineToken", true);

        Log.d("psd", "EnterEmailCoinify - signUp json obj: " + signUp.toString());
        Requestor.coinifySignUp(signUp, new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                if (response != null) {
                    CoinifySignUpResponse csur = (CoinifySignUpResponse) response;
                    sharedManager.setCoinifyEmail(csur.getTrader().getEmail());
                    sharedManager.setCoinifyOfflineToken(csur.getOfflineToken());
//                    authOfflineToken(COINIFY_GRANT_TYPE_OFFLINE_TOKEN, csur.getOfflineToken());
                    Toast.makeText(getActivity(), "Please click on the verification link in an email", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Service is temporarily unavailable", Toast.LENGTH_SHORT).show();
                }

                closeProgress();
                btNext.setEnabled(true);
            }

            @Override
            public void onFailure(String msg) {
                authEmailPass(COINIFY_GRANT_TYPE_EMAIL_PASS, email, pass);

                Log.d("psd", "EnterEmail signUp onFailure - " + msg);
            }
        });
    }

    private void authOfflineToken(String grantType, String offlineToken) {
        JsonObject authOfflineToken = new JsonObject();
        authOfflineToken.addProperty("grant_type", grantType);
        authOfflineToken.addProperty("offline_token", offlineToken);

        auth(authOfflineToken);
    }

    private void authEmailPass(String grantType, String email, String pass) {
        JsonObject authEmailPass = new JsonObject();
        authEmailPass.addProperty("grant_type", grantType);
        authEmailPass.addProperty("email", email);
        authEmailPass.addProperty("password", pass);

        auth(authEmailPass);
    }

    private void auth(JsonObject jsonObject) {
        Requestor.coinifyAuth(jsonObject, new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                CoinifyAuthResponse car = (CoinifyAuthResponse) response;
                Log.d("psd", "System.currentTimeMillis()/1000 = " + String.valueOf(System.currentTimeMillis()) +
                        " Long.parseLong(car.getExpires_in()) * 1000 = " + Long.parseLong(car.getExpires_in()) * 1000 +
                        " Coinify.DELTA_ACCESS_TOKEN_LIFE_TIME = " + Coinify.DELTA_ACCESS_TOKEN_LIFE_TIME +
                        " sum = " + String.valueOf(System.currentTimeMillis()/1000 + Long.parseLong(car.getExpires_in()) * 1000 - Coinify.DELTA_ACCESS_TOKEN_LIFE_TIME));

                sharedManager.setCoinifyAccessTokenLifeTime(System.currentTimeMillis() +
                        Long.parseLong(car.getExpires_in()) * 1000 -
                        Coinify.DELTA_ACCESS_TOKEN_LIFE_TIME);
                sharedManager.setCoinifyAccessToken(car.getAccess_token());
                String email = etEmail.getText().toString().trim();
                if (!email.isEmpty()) {
                    sharedManager.setCoinifyEmail(email);
                }
                Log.d("psd", "EnterEmail auth token = " + car.getAccess_token());
                closeProgress();
                btNext.setEnabled(true);

                if (prevFragment.buyOrSell.equals("buy")) {
                    goToPayMethodsFragment();
                } else if (prevFragment.buyOrSell.equals("sell")) {
                    goToAmountCoinifyActivity();
                }
            }

            @Override
            public void onFailure(String msg) {
                closeProgress();
                btNext.setEnabled(true);

                try {
                    JsonParser jp = new JsonParser();
                    JsonObject jo = jp.parse(msg).getAsJsonObject();
                    Toast.makeText(getActivity(), jo.get("error_description").toString(), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Service is temporarily unavailable", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    Log.d("psd", "coinifyQuote - onFailure: json parse " + msg);
                }
                Log.d("psd", "EnterEmail auth onFailure - " + msg);
            }
        });
    }

    private void goToPayMethodsFragment() {
        final EnterEmailCoinifyFragment thisFragment = this;
        PayMethodsCoinifyFragment startFragment = new PayMethodsCoinifyFragment();
        startFragment.setCoinifyPrevFragment(thisFragment);
        navigateToFragment(startFragment);
    }

    private void goToAmountCoinifyActivity() {
        Intent intent = new Intent(getActivity(), AmountCoinifyActivity.class);
        startActivity(intent);
    }

    private boolean validateEmail(String text) {
        Matcher matcher = Patterns.EMAIL_ADDRESS.matcher(text);
        return matcher.matches();
    }

    @Override
    public boolean onBackPressed() {
        if (prevFragment != null) {
            navigateToFragment(prevFragment);
            return true;
        }
        return false;
    }

    @Override
    public boolean onHomePressed() {
        return onBackPressed();
    }

}
