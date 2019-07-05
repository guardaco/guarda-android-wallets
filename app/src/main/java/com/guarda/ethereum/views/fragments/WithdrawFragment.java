package com.guarda.ethereum.views.fragments;


import android.app.Activity;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.guarda.ethereum.BuildConfig;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.customviews.RobotoLightEditText;
import com.guarda.ethereum.managers.RawNodeManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.constants.RequestCode;
import com.guarda.ethereum.views.activity.AmountToSendActivity;
import com.guarda.ethereum.views.activity.ScanQrCodeActivity;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;
import segmented_control.widget.custom.android.com.segmentedcontrol.SegmentedControl;

import static com.guarda.ethereum.models.constants.Extras.TOKEN_CODE_EXTRA;

@AutoInjector(GuardaApp.class)
public class WithdrawFragment extends BaseFragment{


    @BindView(R.id.et_send_coins_address)
    RobotoLightEditText etSendCoinsAddress;
    @BindView(R.id.btn_scan_qr)
    ImageButton btScanQr;
    @BindView(R.id.btn_next)
    Button btNext;
    @BindView(R.id.btn_currency_code)
    Button btnSelectedCurrency;
    @BindView(R.id.sp_list_currency)
    Spinner spCurrencies;
    @BindView(R.id.cl_coins_selector_root)
    ConstraintLayout clTokensSelectorRoot;
    @BindView(R.id.cl_address_toggle)
    ConstraintLayout cl_address_toggle;
    @BindView(R.id.segmented_control)
    SegmentedControl segmented_control;

    @Inject
    WalletManager walletManager;

    @Inject
    RawNodeManager tokenManager;

    private boolean isSaplingAddress = false;

    @Override
    protected int getLayout() {
        return R.layout.fragment_withdraw;
    }

    @Override
    protected void init() {
        GuardaApp.getAppComponent().inject(this);
        initViews();
        initAddressField();
        initTokens();
    }

    private void initTokens() {
        if (isTokensAvailable()) {
            clTokensSelectorRoot.setVisibility(View.VISIBLE);
            String[] tokensArray = tokenManager.getWalletTokensCodes()
                    .toArray(new String[tokenManager.getWalletTokensCodes().size()]);
            initDropDownSpinner(tokensArray);
            updateSelectedCurrency(tokensArray[0]);
        } else {
            clTokensSelectorRoot.setVisibility(View.GONE);
            initAddressToggle();
        }
    }

    private boolean isTokensAvailable() {
        return tokenManager.getWalletTokensList() != null
                && !tokenManager.getWalletTokensList().isEmpty();
    }

    private void initAddressField() {
        etSendCoinsAddress
                .setOnPasteListener(
                        text -> etSendCoinsAddress.setText(filterAddress(text))
                );
    }

    private void initViews() {
        etSendCoinsAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideError(etSendCoinsAddress);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
                return WalletManager.isCharSequenceValidForAddress(charSequence);
            }
        };
        etSendCoinsAddress.setFilters(new InputFilter[]{filter});
    }

    @OnClick({R.id.btn_scan_qr, R.id.btn_next})
    public void withdrawButtonsClick(View view) {
        switch (view.getId()) {
            case R.id.btn_scan_qr:
                Intent intent = new Intent(getActivity(), ScanQrCodeActivity.class);
                startActivityForResult(intent, RequestCode.QR_CODE_REQUEST_CODE);
                break;
            case R.id.btn_next:
                String address = etSendCoinsAddress.getText().toString();
                address = filterAddress(address);
                etSendCoinsAddress.setText(address);

                goNext(address);
                break;
        }
    }

    private void goNext(final String address) {
        final WithdrawFragment thisFragment = this;
        if (!address.isEmpty()) {
                    walletManager.isAddressValid(address, (response) -> {
                            try {
                                thisFragment.getActivity().runOnUiThread(() -> {
                                        try {
                                            if (response)
                                                openAmountToSendActivity(address);
                                            else
                                                showError(etSendCoinsAddress, getString(R.string.withdraw_the_withdrawal_address_is_incorrect));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    });
        } else {
            showError(etSendCoinsAddress, getString(R.string.withdraw_address_not_valid));
        }
    }

    private String filterAddress(String address) {
        Pattern pattern = Pattern.compile("\\w+");
        Matcher matcher = pattern.matcher(address);
        while (matcher.find()) {
            String candidate = matcher.group();
            if (walletManager.isSimilarToAddress(candidate)) {
                return candidate;
            }
        }
        return address;
    }

    private void openAmountToSendActivity(String address) {
        Intent startActivityIntent = new Intent(getActivity(), AmountToSendActivity.class);
        startActivityIntent.putExtra(Extras.WALLET_NUMBER, address);
        startActivityIntent.putExtra(Extras.IS_SAPLING_ADDRESS, isSaplingAddress);
        if (isTokensAvailable() && spCurrencies.getSelectedItemPosition() > 0) {
            startActivityIntent.putExtra(TOKEN_CODE_EXTRA,
                    tokenManager.getWalletTokensCodes().get(spCurrencies.getSelectedItemPosition()));
        }
        startActivity(startActivityIntent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCode.QR_CODE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra(Extras.QR_CODE_RESULT);
                if (!result.isEmpty()) {
                    String address = filterAddress(result);
                    etSendCoinsAddress.setText(address);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initAddressToggle() {
        if (BuildConfig.FLAVOR != "zec") return;

        cl_address_toggle.setVisibility(View.VISIBLE);
        segmented_control.setVisibility(View.VISIBLE);
        segmented_control
                .addOnSegmentClickListener(
                        (segmentViewHolder) -> {
                            switch (segmentViewHolder.getAbsolutePosition()) {
                                case 0:
                                    isSaplingAddress = false;
                                    break;
                                case 1:
                                    isSaplingAddress = true;
                                    break;
                            }
                        }
                );

        segmented_control.setSelectedSegment(0);
    }

    @OnClick(R.id.btn_currency_code)
    public void selectCurrency() {
        spCurrencies.performClick();
    }

    private void updateSelectedCurrency(String currency) {
        btnSelectedCurrency.setText(currency);
    }

    private void initDropDownSpinner(final String[] array) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.view_spinner_item, array);
        spCurrencies.setAdapter(adapter);
        spCurrencies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updateSelectedCurrency(array[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
    }
}
