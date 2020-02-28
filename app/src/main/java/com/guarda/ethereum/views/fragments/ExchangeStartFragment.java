package com.guarda.ethereum.views.fragments;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.Callback2;
import com.guarda.ethereum.managers.ChangellyNetworkManager;
import com.guarda.ethereum.managers.ChangenowApi;
import com.guarda.ethereum.managers.ShapeshiftApi;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Const;
import com.guarda.ethereum.models.items.ResponseGenerateAddress;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.utils.ClipboardUtils;
import com.guarda.ethereum.utils.QrCodeUtils;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;

@AutoInjector(GuardaApp.class)
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



    private ExchangeFragment prevFragment = new ExchangeFragment();
    private String spinnerExchangeSymbol = "";
    private int spinnerFromCoinPosition = 0;
    private int spinnerToCoinPosition = 0;

    private String fromCoin = "";
    private String toCoin = "";
    private String fromCoinName = "";
    private String toCoinName = "";

    private String depositAddress = "";
    private boolean showQrCode = true;

    private BigDecimal minimumAmount = new BigDecimal(0.0);



    public ExchangeStartFragment() {
        GuardaApp.getAppComponent().inject(this);
    }



    public ExchangeStartFragment setData(String spinnerExchangeSymbol, int spinnerFromCoinPosition, int spinnerToCoinPosition, ExchangeFragment prevFragment) {
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
        return R.layout.fragment_exchange_start;
    }



    @Override
    protected void init() {
        final ExchangeStartFragment thisFragment = this;
        setToolbarTitle(getString(R.string.title_fragment_exchange_start) + " " + toCoinName);
        textViewFromCoin.setText(fromCoinName);
        textViewToCoin.setText(toCoinName);
        textViewAddressWallet.setVisibility(View.GONE);
        textViewTapToCopy.setVisibility(View.GONE);
        ivQrCode.setVisibility(View.GONE);
        textViewHint.setText(getString(R.string.exchange_generate_hint_left) + " " + fromCoinName + " " + getString(R.string.exchange_generate_hint_mid) + " " + toCoinName);
        buttonShowQr.setText(getString(R.string.exchange_show_qr));
        buttonShowAddress.setText(getString(R.string.exchange_show_address));
        showProgress(getString(R.string.exchange_progress_generate_address));
        String returnAddress = Const.COIN_TO_RETURN_ADDRESS.get(fromCoin.toUpperCase()) == null ? "" : Const.COIN_TO_RETURN_ADDRESS.get(fromCoin.toUpperCase());
        if ("shapeshift".equalsIgnoreCase(spinnerExchangeSymbol))
        {
            ShapeshiftApi.generateAddress(fromCoin, toCoin, walletManager.getWalletAddressForDeposit(), returnAddress, new Callback2<String, ShapeshiftApi.GenerateAddressRespModel>() {
                @Override
                public void onResponse(final String status, final ShapeshiftApi.GenerateAddressRespModel resp) {
                    try {
                        thisFragment.getActivity().runOnUiThread(new Runnable() {
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
        } else if ("changelly".equalsIgnoreCase(spinnerExchangeSymbol)) {
            ChangellyNetworkManager.generateAddress(fromCoin.toLowerCase(), toCoin.toLowerCase(), walletManager.getWalletFriendlyAddress(), null, new ApiMethods.RequestListener() {
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
        } else if ("changenow".equalsIgnoreCase(spinnerExchangeSymbol)) {
            ChangenowApi.generateAddress(fromCoin, toCoin, walletManager.getWalletAddressForDeposit(), "", new Callback2<String, ChangenowApi.GenerateAddressRespModel>() {
                @Override
                public void onResponse(final String status, final ChangenowApi.GenerateAddressRespModel resp) {
                    try {
                        thisFragment.getActivity().runOnUiThread(new Runnable() {
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
        }


        buttonShowQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showQrCode = true;
                updateDepositAddressView();
            }
        });

        buttonShowAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showQrCode = false;
                updateDepositAddressView();
            }
        });

        textViewTapToCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardUtils.copyToClipBoard(getContext(), depositAddress);
            }
        });

        textViewAddressWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardUtils.copyToClipBoard(getContext(), depositAddress);
            }
        });

        updateIconFrom();
        updateIconTo();

        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat(Common.ETH_SHOW_PATTERN, symbols);
        decimalFormat.setRoundingMode(RoundingMode.UP);
        String formattedAmount = decimalFormat.format(minimumAmount);
        textViewMinAmount.setText(String.format("%s %s %s", getResources().getString(R.string.minimal_amount), formattedAmount, fromCoin));

        initBackButton();
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



    private void updateDepositAddressView() {
        try {
            if (showQrCode) {
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

}
