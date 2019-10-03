package com.guarda.ethereum.views.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.Callback2;
import com.guarda.ethereum.managers.ChangellyNetworkManager;
import com.guarda.ethereum.managers.ChangenowApi;
import com.guarda.ethereum.managers.ShapeshiftApi;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.constants.RequestCode;
import com.guarda.ethereum.models.items.ResponseGenerateAddress;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.views.activity.AmountToSendActivity;
import com.guarda.ethereum.views.activity.DecoderActivity;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;

@AutoInjector(GuardaApp.class)
public class ExchangeInputAddressFragment extends BaseFragment {

    @Inject
    WalletManager walletManager;
    @BindView(R.id.textViewFromCoin)
    TextView textViewFromCoin;
    @BindView(R.id.textViewToCoin)
    TextView textViewToCoin;
    @BindView(R.id.imageViewFrom)
    ImageView imageViewFrom;
    @BindView(R.id.imageViewTo)
    ImageView imageViewTo;
    @BindView(R.id.textViewAddressLabel)
    TextView textViewAddressLabel;
    @BindView(R.id.editTextAddress)
    EditText editTextAddress;
    @BindView(R.id.textViewMemoLabel)
    TextView textViewMemoLabel;
    @BindView(R.id.editTextMemo)
    EditText editTextMemo;
    @BindView(R.id.imageViewScanQr)
    ImageView imageViewScanQr;
    @BindView(R.id.buttonNext)
    Button buttonNext;
    @BindView(R.id.tv_min_amount)
    TextView textViewMinAmount;

    private ExchangeFragment prevFragment = new ExchangeFragment();
    private String spinnerExchangeSymbol = "";
    private int spinnerFromCoinPosition = 0;
    private int spinnerToCoinPosition = 0;

    private String fromCoin = "";
    private String toCoin = "";
    private String fromCoinName = "";
    private String toCoinName = "";

    private BigDecimal minimumAmount = new BigDecimal(0.0);

    private String depositAddress = "";

    private Map<String, ShapeshiftApi.CoinExternalInfoModel> cryptoCurrenciesInfo = new HashMap<>();

    private static final List<String> hasExtraField = new ArrayList<String>() {{
        add("xrp");
        add("bnbmainnet");
        add("eos");
        add("xmr");
        add("xlm");
        add("xem");
        add("atom");
        add("xdn");
    }};

    public ExchangeInputAddressFragment() {
        GuardaApp.getAppComponent().inject(this);
    }

    public ExchangeInputAddressFragment setData(String spinnerExchangeSymbol, int spinnerFromCoinPosition, int spinnerToCoinPosition, ExchangeFragment prevFragment) {
        this.spinnerExchangeSymbol = spinnerExchangeSymbol;
        this.spinnerFromCoinPosition = spinnerFromCoinPosition;
        this.spinnerToCoinPosition = spinnerToCoinPosition;
        this.prevFragment = prevFragment;
        return this;
    }

    public ExchangeFragment getPrevFragment() {return prevFragment;}
    public String getSpinnerExchangeSymbol() {return spinnerExchangeSymbol;}
    public int getSpinnerFromCoinPosition() {return spinnerFromCoinPosition;}
    public int getSpinnerToCoinPosition() {return spinnerToCoinPosition;}

    public void setCoins(String fromCoin, String toCoin) {
        this.fromCoin = fromCoin;
        this.toCoin = toCoin;
    }

    public void setCoinsNames(String fromCoinName, String toCoinName) {
        this.fromCoinName = fromCoinName;
        this.toCoinName = toCoinName;
    }

    public void setMinimumAmount(BigDecimal minimumAmount) {
        this.minimumAmount = minimumAmount;
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_exchange_input_address;
    }

    @Override
    protected void init() {
        setToolbarTitle(getString(R.string.title_fragment_exchange_input_address) + " " + toCoinName);
        textViewFromCoin.setText(fromCoinName);
        textViewToCoin.setText(toCoinName);
        updateIconFrom();
        updateIconTo();

        textViewAddressLabel.setText(getString(R.string.exchange_dest_address_left) + " " + toCoinName + " " + getString(R.string.exchange_dest_address_right));

        imageViewScanQr.setOnClickListener((view) -> scanQr_onClick());

        buttonNext.setOnClickListener((view) -> buttonNext_onClick());

        textViewMemoLabel.setVisibility(View.GONE);
        editTextMemo.setVisibility(View.GONE);

        if (hasExtraField.contains(toCoin.toLowerCase())) {
            textViewMemoLabel.setText("Memo");
            textViewMemoLabel.setVisibility(View.VISIBLE);
            editTextMemo.setVisibility(View.VISIBLE);
        }

        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat(Common.ETH_SHOW_PATTERN, symbols);
        decimalFormat.setRoundingMode(RoundingMode.UP);
        String formattedAmount = decimalFormat.format(minimumAmount);
        textViewMinAmount.setText(String.format("%s %s %s", getResources().getString(R.string.minimal_amount), formattedAmount, fromCoin));

        initBackButton();
    }



    @Override
    public boolean onBackPressed() {
        navigateToFragment(prevFragment.setData(spinnerExchangeSymbol, spinnerFromCoinPosition, spinnerToCoinPosition));
        return true;
    }



    @Override
    public boolean onHomePressed() {
        onBackPressed();
        return true;
    }



    private void updateIconFrom() {
        Drawable coinIcon = getResources().getDrawable(R.drawable.ic_icon_image_shapeshift);
        Integer id = getContext().getResources().getIdentifier("ic_" + fromCoin.toLowerCase(), "drawable", getContext().getPackageName());
        if (id != null && id != 0) {
            coinIcon = getContext().getResources().getDrawable(id);
        } else {
            coinIcon = getContext().getResources().getDrawable(R.drawable.ic_curr_empty);
        }
        imageViewFrom.setImageDrawable(coinIcon);
    }



    private void updateIconTo() {
        Drawable coinIcon = getResources().getDrawable(R.drawable.ic_icon_image_shapeshift);
        Integer id = getContext().getResources().getIdentifier("ic_" + toCoin.toLowerCase(), "drawable", getContext().getPackageName());
        if (id != null && id != 0) {
            coinIcon = getContext().getResources().getDrawable(id);
        } else {
            coinIcon = getContext().getResources().getDrawable(R.drawable.ic_curr_empty);
        }
        imageViewTo.setImageDrawable(coinIcon);
    }



    private void scanQr_onClick() {
        Intent intent = new Intent(getActivity(), DecoderActivity.class);
        startActivityForResult(intent, RequestCode.QR_CODE_REQUEST_CODE);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCode.QR_CODE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra(Extras.QR_CODE_RESULT);
                if (!result.isEmpty()) {
                    String address = filterAddress(result);
                    editTextAddress.setText(address);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }



    private String filterAddress(String address) {
        Pattern pattern = Pattern.compile("\\w+:w+");
        Matcher matcher = pattern.matcher(address);
        while (matcher.find()) {
            String candidate = matcher.group();
            if (walletManager.isSimilarToAddress(candidate)){
                return candidate;
            }
        }
        return address;
    }



    private void buttonNext_onClick() {
        final ExchangeInputAddressFragment thisFragment = this;
        String address = editTextAddress.getText().toString();
        if ("".equals(address)) {
            Toast.makeText(getContext(), getString(R.string.exchange_error_empty_address), Toast.LENGTH_LONG).show();
        } else {
            showProgress(getString(R.string.exchange_progress_generate_address));

            String extraId = null;
            if (editTextMemo.getVisibility() == View.VISIBLE)
                extraId = editTextMemo.getText().toString();

            if ("shapeshift".equalsIgnoreCase(spinnerExchangeSymbol))
            {
                String shapeshiftDestAddress = new String(address);
                if (extraId != null)
                    shapeshiftDestAddress += "?dt="+extraId;
                ShapeshiftApi.generateAddress(fromCoin, toCoin, shapeshiftDestAddress, walletManager.getWalletFriendlyAddress(), new Callback2<String, ShapeshiftApi.GenerateAddressRespModel>() {
                    @Override
                    public void onResponse(final String status, final ShapeshiftApi.GenerateAddressRespModel resp) {
                        try {
                            thisFragment.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if ("ok".equals(status)) {
                                        depositAddress = resp.depositAddress;
                                        openAmountToSendActivity(depositAddress);
                                    } else {
                                        Toast.makeText(getContext(), getString(R.string.exchange_error_generate_address), Toast.LENGTH_LONG).show();
                                    }
                                    closeProgress();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else if ("changelly".equalsIgnoreCase(spinnerExchangeSymbol)) {
                ChangellyNetworkManager.generateAddress(fromCoin.toLowerCase(), toCoin.toLowerCase(), address, extraId, new ApiMethods.RequestListener() {
                    @Override
                    public void onSuccess(Object response) {
                        ResponseGenerateAddress addressItem = (ResponseGenerateAddress) response;
                        if (addressItem.getAddress() != null && addressItem.getAddress().getAddress() != null) {
                            depositAddress = addressItem.getAddress().getAddress();

                            openAmountToSendActivity(depositAddress);
//                            if ((depositAddress != null) && (depositAddress.indexOf("bitcoincash:") == 0)) {
//                                openAmountToSendActivity(walletManager.cashAddressToLegacy(depositAddress));
//                            } else {
//
//                            }
                        } else {
                            Toast.makeText(getContext(), getString(R.string.exchange_error_generate_address), Toast.LENGTH_LONG).show();
                        }
                        closeProgress();
                    }

                    @Override
                    public void onFailure(String msg) {
                        Toast.makeText(getContext(), getString(R.string.exchange_error_generate_address), Toast.LENGTH_LONG).show();
                        closeProgress();
                    }
                });
            } else if ("changenow".equalsIgnoreCase(spinnerExchangeSymbol)) {
                String changenowDestAddress = new String(address);
                ChangenowApi.generateAddress(fromCoin, toCoin, changenowDestAddress, extraId, new Callback2<String, ChangenowApi.GenerateAddressRespModel>() {
                    @Override
                    public void onResponse(final String status, final ChangenowApi.GenerateAddressRespModel resp) {
                        try {
                            thisFragment.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if ("ok".equals(status)) {
                                        depositAddress = resp.depositAddress;
                                        openAmountToSendActivity(depositAddress);
                                    } else {
                                        Toast.makeText(getContext(), getString(R.string.exchange_error_generate_address), Toast.LENGTH_LONG).show();
                                    }
                                    closeProgress();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }



    private void openAmountToSendActivity(String address) {
        Intent startActivityIntent = new Intent(getActivity(), AmountToSendActivity.class);
        startActivityIntent.putExtra(Extras.WALLET_NUMBER, address);
        startActivityIntent.putExtra(Extras.EXCHANGE_MINAMOUNT, minimumAmount);
        startActivity(startActivityIntent);
    }

}
