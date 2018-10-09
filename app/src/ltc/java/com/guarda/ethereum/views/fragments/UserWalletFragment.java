package com.guarda.ethereum.views.fragments;


import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.CoinmarketcapHelper;
import com.guarda.ethereum.managers.EthereumNetworkManager;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.WalletCreationCallback;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.items.BalanceAndTxResponse;
import com.guarda.ethereum.models.items.RespExch;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.RequestorBtc;
import com.guarda.ethereum.utils.ClipboardUtils;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import org.bitcoinj.core.Coin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

import static com.guarda.ethereum.models.constants.Common.BLOCK;

@AutoInjector(GuardaApp.class)
public class UserWalletFragment extends BaseFragment {

    @BindView(R.id.tv_wallet_count)
    TextView tvCryptoCount;
    @BindView(R.id.tv_wallet_usd_count)
    TextView tvUSDCount;
    @BindView(R.id.iv_update_address)
    ImageView ivUpdateAddress;
    @BindView(R.id.tv_wallet_address)
    TextView tvWalletAddress;
    @BindView(R.id.btn_copy_address)
    Button btCopyAddress;
    @BindView(R.id.btn_show_address_qr)
    Button btShowQr;

    private static final String BLANK_BALANCE = "0.0";

    @Inject
    WalletManager walletManager;

    @Inject
    EthereumNetworkManager networkManager;

    @Inject
    SharedManager sharedManager;

    public UserWalletFragment() {
        GuardaApp.getAppComponent().inject(this);
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_user_wallet;
    }

    @Override
    protected void init() {
        setCryptoBalance(BLANK_BALANCE);
        setUSDBalance("0.00");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(walletManager.getWalletFriendlyAddress())) {
                    createWallet(BLOCK);
                } else {
                    showExistingWallet();
                }
            }
        }, 500);

    }

    private void createWallet(String passphrase) {
        if (isAdded()) {
            showProgress(getString(R.string.generating_wallet));
        }
        walletManager.createWallet(passphrase, new WalletCreationCallback() {
            @Override
            public void onWalletCreated(Object walletFile) {
                closeProgress();
                showExistingWallet();

            }
        });
    }


    private void showExistingWallet() {
        if (walletManager.getWalletFriendlyAddress() != null) {
            showBalance();
            tvWalletAddress.setText(walletManager.getWalletFriendlyAddress());
        }
    }


    private void setCryptoBalance(String balance) {
        String displayBalance = balance + " " + sharedManager.getCurrentCurrency().toUpperCase();
        tvCryptoCount.setText(displayBalance);
    }

    private void showBalance() {

        RequestorBtc.getBalanceAndTxLtc(walletManager.getWalletFriendlyAddress(), new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                BalanceAndTxResponse balance = (BalanceAndTxResponse) response;
                Coin coin = Coin.valueOf(balance.getFinalBalance());
                String curBalance = WalletManager.getFriendlyBalance(coin);
                setCryptoBalance(curBalance);
                getLocalBalance(curBalance);
            }

            @Override
            public void onFailure(String msg) {

            }
        });

    }

    private void getLocalBalance(final String balance) {
        CoinmarketcapHelper.getExchange(Common.MAIN_CURRENCY_NAME,
                sharedManager.getLocalCurrency().toLowerCase(),
                new ApiMethods.RequestListener() {
                    @Override
                    public void onSuccess(Object response) {
                        List<RespExch> exchange = (List<RespExch>) response;
                        Log.d("!!!!!", "exchange: " + exchange.get(0).getPrice(sharedManager.getLocalCurrency().toLowerCase()));

                        String localBalance = balance.replace(",", "");
                        String exchPrice = exchange.get(0).getPrice(sharedManager.getLocalCurrency().toLowerCase());

                        if (localBalance == null || exchPrice == null) return;

                        Double res = Double.valueOf(localBalance) * (Double.valueOf(exchPrice));
                        setUSDBalance(Double.toString(round(res, 2)));
                    }

                    @Override
                    public void onFailure(String msg) {
                    }
                });
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void setUSDBalance(String balance) {
        tvUSDCount.setText(String.format("%s %s", balance, sharedManager.getLocalCurrency().toUpperCase()));
    }

    @OnClick({R.id.iv_update_address, R.id.btn_copy_address, R.id.btn_show_address_qr, R.id.btn_top_up_other_currency})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_update_address:
                openTransactionHistory();
                break;
            case R.id.btn_copy_address:
                copyAddress();
                break;
            case R.id.btn_show_address_qr:
                navigateToFragment(new DepositFragment());
                break;
            case R.id.btn_top_up_other_currency:
                navigateToFragment(new PurchaseServiceFragment());
                break;
        }
    }

    private void openTransactionHistory() {
        navigateToFragment(new TransactionHistoryFragment());
    }

    private void copyAddress() {
        String address = tvWalletAddress.getText().toString();
        if (!TextUtils.isEmpty(address)) {
            ClipboardUtils.copyToClipBoard(getContext(), address);
        }
    }

}
