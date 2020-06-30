package com.guarda.ethereum.views.activity;


import android.os.Bundle;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.ChangellyNetworkManager;
import com.guarda.ethereum.managers.CurrencyListHolder;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.items.CryptoItem;
import com.guarda.ethereum.models.items.ResponseChangellyMinAmount;
import com.guarda.ethereum.models.items.ResponseCurrencyItem;
import com.guarda.ethereum.models.items.ResponseGenerateAddress;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.utils.ClipboardUtils;
import com.guarda.ethereum.utils.QrCodeUtils;
import com.guarda.ethereum.views.activity.base.AToolbarActivity;
import com.guarda.ethereum.views.adapters.CryptoAdapter;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;

import static com.guarda.ethereum.models.constants.Common.ETH_SHOW_PATTERN;
import static com.guarda.ethereum.models.constants.Common.MAIN_CURRENCY;

public class GenerateAddressActivity extends AToolbarActivity {

    public String TAG = "GenerateAddressActivity";

    @BindView(R.id.iv_qr_code)
    ImageView ivQrCode;
    @BindView(R.id.tv_address_wallet)
    TextView tvWalletAddress;
    @BindView(R.id.tv_tap_to_copy_address)
    TextView tvTapToCopyAddress;
    @BindView(R.id.rv_crypto_purchase)
    RecyclerView rvCryptoRecycler;
    @BindView(R.id.btn_currency_code)
    Button btnSelectedCurrency;
    @BindView(R.id.sp_list_currency)
    Spinner spCurrencies;
    @BindView(R.id.tv_min_amount)
    TextView mTvMinAmount;
    @BindView(R.id.tv_destination_tag)
    TextView mTvDestinationTag;
    @BindString(R.string.minimal_amount)
    String mMinimalAmountStr;
    @BindString(R.string.destination_tag)
    String mDestinationTagStr;

    private CryptoAdapter adapter;
    private String selectedCurrency;
    private String generatedAddress;
    private int spinnerClickCount = 0;
    private JsonArray tickersFields;
    private String extraId;

    @Inject
    WalletManager walletManager;
    @Inject
    SharedManager sharedManager;
    @Inject
    CurrencyListHolder currentCrypto;

    public final static int QR_CODE_WIDTH = 500;

    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        Log.d("flint", "GenerateAddressActivity.init()...");
        if (walletManager.getWalletFriendlyAddress() == null) {
            walletManager.restoreFromBlock0(sharedManager.getLastSyncedBlock(), new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initContinue();
                        }
                    });
                }
            });
            return;
        }
        initContinue();
    }

    private void initContinue() {
        setToolBarTitle(getString(R.string.your_exchange_address));
        try {
            ivQrCode.setImageBitmap(QrCodeUtils.textToQrCode(walletManager.getWalletFriendlyAddress(), QR_CODE_WIDTH));
        } catch (Exception e) {
            e.printStackTrace();
        }
        tvWalletAddress.setText(walletManager.getWalletFriendlyAddress());

        selectedCurrency = getIntent().getStringExtra(Extras.SELECTED_CURRENCY);

        getAddress(walletManager.getWalletFriendlyAddress(), selectedCurrency);

        initRecycler();
        scrollToTop();
    }

    private void initDropDownSpinner(final String[] array) {

        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(spCurrencies);
            popupWindow.setHeight(800);
        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.view_spinner_item, array);
        spCurrencies.setAdapter(adapter);
        spCurrencies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (++spinnerClickCount > 1) {

                    String walletNum = String.valueOf(walletManager.getWalletFriendlyAddress());
                    getAddress(walletNum, array[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
    }

    private void scrollToTop() {
        ((NestedScrollView) findViewById(R.id.scroll_view)).scrollTo(5, 10);
    }

    private void updateSelectedCurrency(String currency) {
        selectedCurrency = currency;
        btnSelectedCurrency.setText(selectedCurrency);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_generate_address;
    }

    @OnClick({R.id.tv_tap_to_copy_address, R.id.iv_qr_code, R.id.tv_address_wallet, R.id.tv_destination_tag})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_tap_to_copy_address:
            case R.id.iv_qr_code:
            case R.id.tv_address_wallet:
                ClipboardUtils.copyToClipBoard(this, generatedAddress);
                break;
            case R.id.tv_destination_tag:
                ClipboardUtils.copyToClipBoard(this, extraId);
                break;
        }
    }

    @OnClick(R.id.btn_currency_code)
    public void selectCurrency() {
        spCurrencies.performClick();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_slide, R.anim.slide_in_right);
    }

    private void initRecycler() {
        adapter = new CryptoAdapter();

        adapter.setItemClickListener(new CryptoAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position, String name, String code) {

                String walletNum = String.valueOf(walletManager.getWalletFriendlyAddress());
                getAddress(walletNum, code);

            }
        });

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        rvCryptoRecycler.setLayoutManager(layoutManager);
        rvCryptoRecycler.setAdapter(adapter);

        if (currentCrypto.getListOfCurrencies() == null
                || currentCrypto.getListOfCurrencies().isEmpty()) {
            loadCurrencies();
        } else {
            adapter.updateList(currentCrypto.getListOfCurrencies());
            initDropDownSpinner(getArrayOfCryptoCode(currentCrypto.getListOfCurrencies()));
        }

        initTokensFields();
    }

    private void getAddress(String walletNum, final String code) {
        showProgress();
        ChangellyNetworkManager.generateAddress(code.toLowerCase(), MAIN_CURRENCY, walletNum, null, new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                closeProgress();

                ResponseGenerateAddress addressItem = (ResponseGenerateAddress) response;
                if (addressItem.getAddress() != null && addressItem.getAddress().getAddress() != null) {
                    generatedAddress = addressItem.getAddress().getAddress();
                    extraId = addressItem.getAddress().getExtraId();
                    if (generatedAddress != null) {
                        getMinAmount(code.toLowerCase(), MAIN_CURRENCY);
                        ivQrCode.setImageBitmap(QrCodeUtils.textToQrCode(generatedAddress, QR_CODE_WIDTH));
                        tvWalletAddress.setText(generatedAddress);
                        showExtraField(extraId, code.toLowerCase());
                        updateSelectedCurrency(code.toLowerCase());
                        scrollToTop();
                    }
                } else {
                    Toast.makeText(GenerateAddressActivity.this, R.string.error_failed_to_create_currency_address, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(String msg) {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                closeProgress();
                finish();
            }
        });
    }

    private void showExtraField(String extraId, String currencyCode) {
        if (extraId == null) {
            mTvDestinationTag.setVisibility(View.GONE);
        } else {
            if (!TextUtils.isEmpty(extraId)) {
                String fieldName = getFieldName(currencyCode);
                String tag = mDestinationTagStr + "\n" + extraId;
                if (!fieldName.equalsIgnoreCase("")) {
                    tag = fieldName + ":\n" + extraId;
                }
                mTvDestinationTag.setVisibility(View.VISIBLE);
                mTvDestinationTag.setText(tag);
            }
        }
    }

    private void getMinAmount(final String from, String to) {
        ChangellyNetworkManager.getMinAmount(from, to, new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
                symbols.setDecimalSeparator('.');
                DecimalFormat decimalFormat = new DecimalFormat(ETH_SHOW_PATTERN, symbols);
                decimalFormat.setRoundingMode(RoundingMode.DOWN);
                String formatedAmount = decimalFormat.format(((ResponseChangellyMinAmount) response).getAmount());
                String minStr = mMinimalAmountStr + " "
                        + formatedAmount
                        + " " + from.toUpperCase();
                mTvMinAmount.setText(minStr);
            }

            @Override
            public void onFailure(String msg) {
            }
        });
    }

    private void loadCurrencies() {
        showProgress(getString(R.string.loader_loading_available_currencies));
        ChangellyNetworkManager.getCurrencies(new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                ResponseCurrencyItem responseCurrency = (ResponseCurrencyItem) response;

                adapter.updateList(currentCrypto.castResponseCurrencyToCryptoItem(responseCurrency,
                        getApplicationContext()));
                initDropDownSpinner(getArrayOfCryptoCode(currentCrypto.getListOfCurrencies()));
                closeProgress();
            }

            @Override
            public void onFailure(String msg) {
                closeProgress();
            }
        });
    }

    private String[] getArrayOfCryptoCode(List<CryptoItem> listOfCurrencies) {
        String[] array = new String[listOfCurrencies.size()];
        for (int i = 0; i < listOfCurrencies.size(); i++) {
            array[i] = listOfCurrencies.get(i).getCode();
        }
        return array;
    }

    private void initTokensFields() {
        JsonParser jp = new JsonParser();
        InputStream is;
        byte[] buffer;
        try {
            is = getAssets().open(Common.EXTRA_FIELDS);
            int size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            String str = new String(buffer);

            Object obj = jp.parse(str);
            tickersFields = (JsonArray) obj;
        } catch (Exception e) {
            Log.e("psd", e.toString());
        }
    }

    private String getFieldName(String code) {
        if (tickersFields == null) return "";
        for (int i = 0; i < tickersFields.size(); i++){
            try {
                JsonObject j = (JsonObject) tickersFields.get(i);
                String ticker = j.get("ticker").getAsString();
                if (code.equalsIgnoreCase(ticker)) {
                    Log.i("psd", code);
                    return j.get("field").getAsString();
                }
            } catch (Exception e) {
                Log.e("psd", e.toString());
            }
        }
        return "";
    }
}
