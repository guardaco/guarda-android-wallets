package com.guarda.ethereum.views.fragments;

import androidx.lifecycle.ViewModelProviders;
import android.graphics.drawable.PictureDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Const;
import com.guarda.ethereum.models.items.ResponseGenerateAddress;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.screens.exchange.first.ExchangeFragment;
import com.guarda.ethereum.utils.ClipboardUtils;
import com.guarda.ethereum.utils.QrCodeUtils;
import com.guarda.ethereum.utils.svg.GlideApp;
import com.guarda.ethereum.utils.svg.SvgSoftwareLayerSetter;
import com.guarda.ethereum.views.activity.SharedViewModel;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.guarda.ethereum.screens.exchange.first.ExchangeFragment.EXCHANGE_DIVIDER_DOUBLE;

public class ExchangeStartFragment extends BaseFragment {

    @Inject
    WalletManager walletManager;
    @BindView(R.id.textViewFromCoin)
    TextView textViewFromCoin;
    @BindView(R.id.textViewToCoin)
    TextView textViewToCoin;
    @BindView(R.id.tv_address_wallet)
    TextView textViewAddressWallet;
    @BindView(R.id.tv_tap_to_copy_address)
    TextView textViewTapToCopy;
    @BindView(R.id.iv_qr_code)
    ImageView ivQrCode;
    @BindView(R.id.buttonShowQr)
    Button buttonShowQr;
    @BindView(R.id.buttonShowAddress)
    Button buttonShowAddress;
    @BindView(R.id.textViewHint)
    TextView textViewHint;
    @BindView(R.id.imageViewFrom)
    ImageView imageViewFrom;
    @BindView(R.id.imageViewTo)
    ImageView imageViewTo;
    @BindView(R.id.tv_min_amount)
    TextView textViewMinAmount;
    @BindView(R.id.tv_rate)
    TextView tv_rate;


    private String fromCoin = "";
    private String toCoin = "";

    private String depositAddress = "";
    private boolean showQrCode = true;

    private BigDecimal minimumAmount = new BigDecimal(0.0);

    private SharedViewModel sharedViewModel;
    private ChangenowApi.SupportedCoinModel coinFrom;
    private ChangenowApi.SupportedCoinModel coinTo;
    private String selectedExchange;
    private RequestBuilder<PictureDrawable> requestBuilder;

    public ExchangeStartFragment() {
        GuardaApp.getAppComponent().inject(this);
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_exchange_start;
    }

    @Override
    protected void init() {
        sharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        subscribeUi();

        textViewAddressWallet.setVisibility(View.GONE);
        textViewTapToCopy.setVisibility(View.GONE);
        ivQrCode.setVisibility(View.GONE);
        buttonShowQr.setText(getString(R.string.exchange_show_qr));
        buttonShowAddress.setText(getString(R.string.exchange_show_address));
        showProgress(getString(R.string.exchange_progress_generate_address));

        buttonShowQr.setOnClickListener((View view) -> {
            showQrCode = true;
            updateDepositAddressView();
        });

        buttonShowAddress.setOnClickListener((View view) -> {
            showQrCode = false;
            updateDepositAddressView();
        });

        textViewTapToCopy.setOnClickListener((View view) -> {
            ClipboardUtils.copyToClipBoard(getContext(), depositAddress);
        });

        textViewAddressWallet.setOnClickListener((View view) -> {
            ClipboardUtils.copyToClipBoard(getContext(), depositAddress);
        });

        //for coins icons loading
        initImageLoader();

        initBackButton();
    }

    private void subscribeUi() {
        sharedViewModel.selectedFrom.observe(this, from -> {
            coinFrom = from;
            textViewFromCoin.setText(coinFrom.name);
        });
        sharedViewModel.selectedTo.observe(this, to -> {
            coinTo = to;
            setToolbarTitle(getString(R.string.title_fragment_exchange_start) + " " + coinTo.name);
            textViewToCoin.setText(coinTo.name);
            updateHint();
        });
        sharedViewModel.selectedExchange.observe(this, exchange -> {
            selectedExchange = exchange;
            createExchange();
            getMinAmount();
            updateSelectedPairRateChangenow();
            updateIconsFromTo();
        });
    }

    private void createExchange() {
        if (coinFrom == null || coinTo == null || selectedExchange == null) {
            Timber.e("createExchange: coinFrom == null || coinTo == null || selectedExchange == null");
        }
        String returnAddress = Const.COIN_TO_RETURN_ADDRESS.get(fromCoin.toUpperCase()) == null ? "" : Const.COIN_TO_RETURN_ADDRESS.get(fromCoin.toUpperCase());
        if ("shapeshift".equalsIgnoreCase(selectedExchange)) {
            ShapeshiftApi.generateAddress(coinFrom.symbol, coinTo.symbol, walletManager.getWalletAddressForDeposit(), returnAddress, new Callback2<String, ShapeshiftApi.GenerateAddressRespModel>() {
                @Override
                public void onResponse(final String status, final ShapeshiftApi.GenerateAddressRespModel resp) {
                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if ("ok".equals(status)) {
                                    depositAddress = resp.depositAddress;
                                } else {
                                    showQrCode = false;
                                    depositAddress = getResources().getString(R.string.fragment_disabled_text);
                                }
                                updateDepositAddressView();
                                closeProgress();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        closeProgress();
                    }
                }
            });
        } else if ("changelly".equalsIgnoreCase(selectedExchange)) {
            ChangellyNetworkManager.generateAddress(coinFrom.symbol.toLowerCase(), coinTo.symbol.toLowerCase(), walletManager.getWalletFriendlyAddress(), null, new ApiMethods.RequestListener() {
                @Override
                public void onSuccess(Object response) {
                    ResponseGenerateAddress addressItem = (ResponseGenerateAddress) response;
                    if (addressItem.getAddress() != null && addressItem.getAddress().getAddress() != null) {
                        depositAddress = addressItem.getAddress().getAddress();
                    } else {
                        showQrCode = false;
                        depositAddress = getResources().getString(R.string.exchange_service_unavailable);
                    }
                    updateDepositAddressView();
                    closeProgress();
                }

                @Override
                public void onFailure(String msg) {
                    closeProgress();
                }
            });
        } else if ("changenow".equalsIgnoreCase(selectedExchange)) {
            ChangenowApi.generateAddress(coinFrom.symbol, coinTo.symbol, walletManager.getWalletAddressForDeposit(), "", new Callback2<String, ChangenowApi.GenerateAddressRespModel>() {
                @Override
                public void onResponse(final String status, final ChangenowApi.GenerateAddressRespModel resp) {
                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if ("ok".equals(status)) {
                                    depositAddress = resp.depositAddress;
                                } else {
                                    showQrCode = false;
                                    depositAddress = getString(R.string.fragment_disabled_text);
                                }
                                updateDepositAddressView();
                                closeProgress();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        closeProgress();
                    }
                }
            });
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
                coinFrom.name.toUpperCase()));
    }

    private void updateSelectedPairRateChangenow() {
        try {
            ChangenowManager.getInstance().getRate(coinFrom.symbol, coinTo.symbol, response -> {
                try {
                    getActivity().runOnUiThread(() -> {
                        BigDecimal rate = response.rate.divide(BigDecimal.valueOf(EXCHANGE_DIVIDER_DOUBLE), BigDecimal.ROUND_DOWN);
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

    private void updateHint() {
        if (coinFrom != null && coinTo != null) {
            textViewHint.setText(String.format("%s %s %s %s",
                    getString(R.string.exchange_generate_hint_left),
                    coinFrom.name,
                    getString(R.string.exchange_generate_hint_mid),
                    coinTo.name));
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

    private void updateDepositAddressView() {
        try {
            if (showQrCode) {
                if (depositAddress.equals(getString(R.string.fragment_disabled_text))) return;
                textViewAddressWallet.setVisibility(View.GONE);
                textViewTapToCopy.setVisibility(View.GONE);
                ivQrCode.setVisibility(View.VISIBLE);
                ivQrCode.setImageBitmap(QrCodeUtils.textToQrCode(depositAddress, 180));
            } else {
                textViewAddressWallet.setVisibility(View.VISIBLE);
                textViewTapToCopy.setVisibility(View.VISIBLE);
                ivQrCode.setVisibility(View.GONE);
                textViewAddressWallet.setText(depositAddress);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initImageLoader() {
        requestBuilder = GlideApp.with(getContext())
                .as(PictureDrawable.class)
                .transition(withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .error(R.drawable.ic_curr_empty_black)
                .listener(new SvgSoftwareLayerSetter());
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

}
