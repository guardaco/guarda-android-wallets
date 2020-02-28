package com.guarda.ethereum.views.fragments;


import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.models.constants.Coinify;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.items.CoinifyAuthResponse;
import com.guarda.ethereum.models.items.CoinifySignUpResponse;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.Requestor;
import com.guarda.ethereum.views.fragments.base.BaseFragment;
import com.hbb20.CountryCodePicker;

import org.json.JSONObject;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

import static com.guarda.ethereum.models.constants.Coinify.COINIFY_GRANT_TYPE_EMAIL_PASS;

@AutoInjector(GuardaApp.class)
public class NewBankAccCoinifyFragment extends BaseFragment {

    @BindView(R.id.tv_bank_name)
    TextView tv_bank_name;
//    @BindView(R.id.til_swift)
//    TextInputLayout til_swift;
    @BindView(R.id.tv_swift)
    TextView tv_swift;
//    @BindView(R.id.til_iban)
//    TextInputLayout til_iban;
    @BindView(R.id.tv_iban)
    TextView tv_iban;
    @BindView(R.id.sp_bank_currency)
    Spinner sp_bank_currency;
    @BindView(R.id.ll_bank_addr)
    LinearLayout ll_bank_addr;
    @BindView(R.id.ccp_country_bank)
    CountryCodePicker ccp_country_bank;
    @BindView(R.id.tv_bank_state)
    TextView tv_bank_state;
    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.tv_street)
    TextView tv_street;
    @BindView(R.id.tv_zip)
    TextView tv_zip;
    @BindView(R.id.tv_city)
    TextView tv_city;
    @BindView(R.id.tv_state)
    TextView tv_state;
    @BindView(R.id.ccp_country_hold)
    CountryCodePicker ccp_country_hold;
    @BindView(R.id.tv_sell_expl)
    TextView tv_sell_expl;
    @BindView(R.id.btn_next)
    Button btn_next;
    @BindView(R.id.bank_checks_chb)
    CheckBox bank_checks_chb;
    @BindView(R.id.bank_checks_chb_text)
    TextView bank_checks_chb_text;

    @Inject
    SharedManager sharedManager;

    private String bankType = "";
    private String currentFiat = "";

    public NewBankAccCoinifyFragment() {
        GuardaApp.getAppComponent().inject(this);
    }

    @Override
    protected void init() {
        try {
            getActivity().setTitle(getString(R.string.coinify_new_bank));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (getArguments() != null) {
            bankType = getArguments().getString(Extras.COINIFY_BANK_TYPE);
        }

        if (bankType != null) {
            if (bankType.equalsIgnoreCase("int")) {
                setIntView();
            } else if (bankType.equalsIgnoreCase("dan")) {
                setDanView();
            }
        }

        final String[] array = new String[]{"EUR", "USD", "GBP", "DKK"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.view_sp_buy_currency_by_card_item, array);
        sp_bank_currency.setAdapter(adapter);

        sp_bank_currency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFiat = array[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SpannableStringBuilder spanTxt = new SpannableStringBuilder(
                getString(R.string.coinify_bank_checks_agree));
        spanTxt.append(" ");
        spanTxt.append("Selling with Coinify");
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                String url = "https://help.coinify.com/section/17-selling-bitcoin";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        }, spanTxt.length() - "Selling with Coinify".length(), spanTxt.length(), 0);
        spanTxt.append(".");

        tv_sell_expl.setMovementMethod(LinkMovementMethod.getInstance());
        tv_sell_expl.setText(spanTxt, TextView.BufferType.SPANNABLE);

        btn_next.setEnabled(false);

        bank_checks_chb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btn_next.setEnabled(true);
                } else {
                    btn_next.setEnabled(false);
                }
            }
        });

        initBackButton();
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_new_bank_acc_coinify;
    }

    @OnClick(R.id.bank_checks_chb_text)
    public void chbText(View view) {
        bank_checks_chb.setChecked(!bank_checks_chb.isChecked());
    }

    private void setIntView() {
        tv_swift.setHint("Bank SWIFT/BIC");
        tv_iban.setHint("Account number (IBAN)");
        sp_bank_currency.setEnabled(true);
        ll_bank_addr.setVisibility(View.VISIBLE);
        tv_state.setVisibility(View.VISIBLE);
        ccp_country_hold.setVisibility(View.VISIBLE);
    }

    private void setDanView() {
        tv_swift.setHint("Bank REG number");
        tv_iban.setHint("Account number");
        sp_bank_currency.setEnabled(false);
        sp_bank_currency.post(new Runnable() {
            @Override
            public void run() {
                sp_bank_currency.setSelection(3);
            }
        });
        ll_bank_addr.setVisibility(View.GONE);
        tv_state.setVisibility(View.GONE);
        ccp_country_hold.setVisibility(View.GONE);
    }

    @OnClick(R.id.btn_next)
    public void sendAccount(View v) {
        if (!isFieldsEmpty()) {
            btn_next.setEnabled(false);

            JsonObject account = new JsonObject();
            account.addProperty("currency", currentFiat);
            account.addProperty("bic", tv_swift.getText().toString().trim());
            account.addProperty("number", tv_iban.getText().toString().trim());
            JsonObject bankAddr = new JsonObject();
            bankAddr.addProperty("country", ccp_country_bank.getSelectedCountryNameCode());
            bankAddr.addProperty("state", tv_bank_state.getText().toString().trim());
            JsonObject bank = new JsonObject();
            bank.add("address", bankAddr);
            JsonObject holdAddr = new JsonObject();
            holdAddr.addProperty("street", tv_street.getText().toString().trim());
            holdAddr.addProperty("zipcode", tv_zip.getText().toString().trim());
            holdAddr.addProperty("city", tv_city.getText().toString().trim());
            holdAddr.addProperty("state", tv_state.getText().toString().trim());
            holdAddr.addProperty("country", ccp_country_hold.getSelectedCountryNameCode());
            JsonObject holder = new JsonObject();
            holder.addProperty("name", tv_name.getText().toString().trim());
            holder.add("address", holdAddr);

            JsonObject createBankAcc = new JsonObject();
            createBankAcc.add("account", account);
            createBankAcc.add("bank", bank);
            createBankAcc.add("holder", holder);

            showProgress();
            Requestor.coinifyPostBankAccounts(sharedManager.getCoinifyAccessToken(), createBankAcc, new ApiMethods.RequestListener() {
                @Override
                public void onSuccess(Object response) {
                    if (response != null) {
//                        onBackPressed();
                        goToListBankAcc();
                    }

                    closeProgress();
                    btn_next.setEnabled(true);
                }

                @Override
                public void onFailure(String msg) {
                    closeProgress();
                    btn_next.setEnabled(true);
                    JsonParser jp = new JsonParser();
                    JsonObject jo = jp.parse(msg).getAsJsonObject();
                    Toast.makeText(getActivity(), jo.get("error_description").toString(), Toast.LENGTH_SHORT).show();
                    Log.d("psd", "New bank acc - coinifyPostBankAccounts onFailure - " + msg);
                }
            });
        }
    }

    private boolean isFieldsEmpty() {
        if (tv_bank_name.getVisibility() == View.VISIBLE && tv_bank_name.getText().toString().trim().length() == 0) {
            tv_bank_name.setError("Can't be empty");
            return true;
        } else if (tv_swift.getVisibility() == View.VISIBLE && tv_swift.getText().toString().trim().length() == 0) {
            tv_swift.setError("Can't be empty");
            return true;
        } else if (tv_iban.getVisibility() == View.VISIBLE && tv_iban.getText().toString().trim().length() == 0) {
            tv_iban.setError("Can't be empty");
            return true;
        } else if (tv_name.getVisibility() == View.VISIBLE && tv_name.getText().toString().trim().length() == 0) {
            tv_name.setError("Can't be empty");
            return true;
        } else if (tv_street.getVisibility() == View.VISIBLE && tv_street.getText().toString().trim().length() == 0) {
            tv_street.setError("Can't be empty");
            return true;
        } else if (tv_zip.getVisibility() == View.VISIBLE && tv_zip.getText().toString().trim().length() == 0) {
            tv_zip.setError("Can't be empty");
            return true;
        } else if (tv_city.getVisibility() == View.VISIBLE && tv_city.getText().toString().trim().length() == 0) {
            tv_city.setError("Can't be empty");
            return true;
        }

        return false;
    }

    private void goToListBankAcc() {
        navigateToFragment(new ListBankAccCoinifyFragment());
    }

    @Override
    public boolean onBackPressed() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
        return true;
    }

    @Override
    public boolean onHomePressed() {
        return onBackPressed();
    }

}
