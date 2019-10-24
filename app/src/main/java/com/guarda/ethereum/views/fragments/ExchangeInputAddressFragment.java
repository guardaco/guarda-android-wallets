package com.guarda.ethereum.views.fragments;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.drawable.PictureDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.Callback2;
import com.guarda.ethereum.managers.ChangellyNetworkManager;
import com.guarda.ethereum.managers.ChangenowApi;
import com.guarda.ethereum.managers.ChangenowManager;
import com.guarda.ethereum.managers.ShapeshiftApi;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.ExchangeSpinnerRowModel;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.constants.RequestCode;
import com.guarda.ethereum.models.items.ResponseGenerateAddress;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.screens.exchange.first.ExchangeFragment;
import com.guarda.ethereum.utils.svg.GlideApp;
import com.guarda.ethereum.utils.svg.SvgSoftwareLayerSetter;
import com.guarda.ethereum.views.activity.AmountToSendActivity;
import com.guarda.ethereum.views.activity.DecoderActivity;
import com.guarda.ethereum.views.activity.SharedViewModel;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import timber.log.Timber;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

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
    @BindView(R.id.tv_rate)
    TextView tv_rate;


    private String fromCoin = "";
    private String toCoin = "";

    private BigDecimal minimumAmount = new BigDecimal(0.0);

    private String depositAddress = "";

    private SharedViewModel sharedViewModel;
    private ChangenowApi.SupportedCoinModel coinFrom;
    private ChangenowApi.SupportedCoinModel coinTo;
    private String selectedExchange;
    private RequestBuilder<PictureDrawable> requestBuilder;

    public ExchangeInputAddressFragment() {
        GuardaApp.getAppComponent().inject(this);
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_exchange_input_address;
    }

    @Override
    protected void init() {
        sharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        subscribeUi();

        imageViewScanQr.setOnClickListener((view) -> scanQr_onClick());

        buttonNext.setOnClickListener((view) -> buttonNext_onClick());

        textViewMemoLabel.setVisibility(View.GONE);
        editTextMemo.setVisibility(View.GONE);

        //for coins icons loading
        initImageLoader();

        initBackButton();
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

    private void subscribeUi() {
        sharedViewModel.selectedFrom.observe(this, from -> {
            coinFrom = from;
            textViewFromCoin.setText(coinFrom.name);
        });
        sharedViewModel.selectedTo.observe(this, to -> {
            coinTo = to;
            setToolbarTitle(getString(R.string.title_fragment_exchange_input_address) + " " + coinTo.name);
            textViewToCoin.setText(coinTo.name);
            textViewAddressLabel.setText(getString(R.string.exchange_dest_address_left) + " " + coinTo.name + " " + getString(R.string.exchange_dest_address_right));
            showMemo();
        });
        sharedViewModel.selectedExchange.observe(this, exchange -> {
            selectedExchange = exchange;
            getMinAmount();
            updateSelectedPairRateChangenow();
            updateIconsFromTo();
        });
    }

    private void buttonNext_onClick() {
        if (coinFrom == null || coinTo == null || selectedExchange == null) {
            Timber.e("createExchange: coinFrom == null || coinTo == null || selectedExchange == null");
        }
        String address = editTextAddress.getText().toString();
        String extraId = editTextMemo.getText().toString();

        if (address.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.exchange_error_empty_address), Toast.LENGTH_LONG).show();
        } else if (editTextMemo.getVisibility() == View.VISIBLE && extraId.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.exchange_error_empty_extra), Toast.LENGTH_LONG).show();
        } else {
            showProgress(getString(R.string.exchange_progress_generate_address));

            if ("shapeshift".equalsIgnoreCase(selectedExchange)) {
                String shapeshiftDestAddress = address;
                if (extraId != null)
                    shapeshiftDestAddress += "?dt="+extraId;
                ShapeshiftApi.generateAddress(coinFrom.symbol, coinTo.symbol, shapeshiftDestAddress, walletManager.getWalletFriendlyAddress(), new Callback2<String, ShapeshiftApi.GenerateAddressRespModel>() {
                    @Override
                    public void onResponse(final String status, final ShapeshiftApi.GenerateAddressRespModel resp) {
                        try {
                            getActivity().runOnUiThread(() -> {
                                if ("ok".equals(status)) {
                                    depositAddress = resp.depositAddress;
                                    openAmountToSendActivity(depositAddress);
                                } else {
                                    Toast.makeText(getContext(), getString(R.string.exchange_error_generate_address), Toast.LENGTH_LONG).show();
                                }
                                closeProgress();
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else if ("changelly".equalsIgnoreCase(selectedExchange)) {
                ChangellyNetworkManager.generateAddress(coinFrom.symbol.toLowerCase(), coinTo.symbol.toLowerCase(), address, extraId, new ApiMethods.RequestListener() {
                    @Override
                    public void onSuccess(Object response) {
                        ResponseGenerateAddress addressItem = (ResponseGenerateAddress) response;
                        if (addressItem.getAddress() != null && addressItem.getAddress().getAddress() != null) {
                            depositAddress = addressItem.getAddress().getAddress();
                            openAmountToSendActivity(depositAddress);
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
            } else if ("changenow".equalsIgnoreCase(selectedExchange)) {
                ChangenowApi.generateAddress(coinFrom.symbol, coinTo.symbol, address, extraId, new Callback2<String, ChangenowApi.GenerateAddressRespModel>() {
                    @Override
                    public void onResponse(final String status, final ChangenowApi.GenerateAddressRespModel resp) {
                        try {
                            getActivity().runOnUiThread(() -> {
                                if ("ok".equals(status)) {
                                    depositAddress = resp.depositAddress;
                                    openAmountToSendActivity(depositAddress);
                                } else {
                                    Toast.makeText(getContext(), getString(R.string.exchange_error_generate_address), Toast.LENGTH_LONG).show();
                                }
                                closeProgress();
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    private void getMinAmount() {
        ChangenowManager.getInstance().getMinAmount(coinFrom.symbol, coinTo.symbol, response -> {
            try {
                getActivity().runOnUiThread(() -> setMinAmount(response));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setMinAmount(ChangenowApi.GetRateRespModel response) {
        minimumAmount = response.minimum;
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat(Common.ETH_SHOW_PATTERN, symbols);
        decimalFormat.setRoundingMode(RoundingMode.UP);
        String formattedAmount = decimalFormat.format(minimumAmount);
        textViewMinAmount.setText(String.format("%s %s %s",
                getResources().getString(R.string.minimal_amount),
                formattedAmount,
                fromCoin.toUpperCase()));
    }

    private void updateSelectedPairRateChangenow() {
        try {
            ChangenowManager.getInstance().getRate(coinFrom.symbol, coinTo.symbol, response -> {
                try {
                    getActivity().runOnUiThread(() -> {
                        BigDecimal rate = response.rate.divide(BigDecimal.valueOf(10000.0d), BigDecimal.ROUND_DOWN);
                        tv_rate.setText(String.format("Exchange rate: 1 %s ~ %s %s",
                                coinFrom.symbol.toUpperCase(),
                                rate.toString(),
                                coinTo.symbol.toUpperCase()));
                        Timber.d("updateSelectedPairRateChangenow response.rate.longValue() = %s", response.rate.toPlainString());
                    });
                } catch (Exception e) {
                    Timber.e("updateSelectedPairRateChangenow 1 error=%s", e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            Timber.e("updateSelectedPairRateChangenow 2 error=%s", e.getMessage());
            e.printStackTrace();
        }
    }

    private void showMemo() {
        if (hasExtraField.contains(coinTo.symbol.toLowerCase())) {
            textViewMemoLabel.setText("Memo");
            textViewMemoLabel.setVisibility(View.VISIBLE);
            editTextMemo.setVisibility(View.VISIBLE);
        }
    }

    private void updateIconsFromTo() {
        if (coinFrom == null || coinTo == null) return;

        requestBuilder
                .load(coinFrom.imageUrl)
                .into(imageViewFrom);
        requestBuilder
                .load(coinTo.imageUrl)
                .into(imageViewTo);
    }

    private void initImageLoader() {
        requestBuilder = GlideApp.with(getContext())
                .as(PictureDrawable.class)
                .transition(withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .error(R.drawable.ic_curr_empty_black)
                .listener(new SvgSoftwareLayerSetter());
    }

    private void openAmountToSendActivity(String address) {
        Intent startActivityIntent = new Intent(getActivity(), AmountToSendActivity.class);
        startActivityIntent.putExtra(Extras.WALLET_NUMBER, address);
        startActivityIntent.putExtra(Extras.EXCHANGE_MINAMOUNT, minimumAmount);
        startActivity(startActivityIntent);
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

    @Override
    public boolean onBackPressed() {
        navigateToFragment(new ExchangeFragment());
        return true;
    }

    @Override
    public boolean onHomePressed() {
        onBackPressed();
        return true;
    }

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

}
