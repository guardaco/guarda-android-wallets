package com.guarda.ethereum.views.fragments;


import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.Callback;
import com.guarda.ethereum.managers.CoinmarketcapHelper;
import com.guarda.ethereum.managers.EthereumNetworkManager;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.WalletCreationCallback;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.items.RespExch;
import com.guarda.ethereum.models.items.ResponseExchangeAmount;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.utils.ClipboardUtils;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import org.web3j.crypto.WalletFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

import static com.guarda.ethereum.models.constants.Common.BLOCK;
import static com.guarda.ethereum.models.constants.Common.ETH_SHOW_PATTERN;

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

    private String curBalance = "0.00";

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
        setCryptoBalance(curBalance);
        setUSDBalance("0.00");
        if (TextUtils.isEmpty(walletManager.getWalletFriendlyAddress())) {
            createWallet(BLOCK);
        } else {
            showExistingWallet();
        }
    }

    private void createWallet(String passphrase) {
        walletManager.createWallet(passphrase, new WalletCreationCallback() {
            @Override
            public void onWalletCreated(WalletFile walletFile) {
                closeProgress();
                showExistingWallet();

            }
        });
        if (isAdded()) {
            showProgress(getString(R.string.generating_wallet));
        }
    }


    private void showExistingWallet() {
        if (walletManager.getWalletFriendlyAddress() != null) {
            showBalance();
            tvWalletAddress.setText(walletManager.getWalletFriendlyAddress());
        }
    }


    private void setCryptoBalance(String balance) {
        tvCryptoCount.setText(String.format("%s " + sharedManager.getCurrentCurrency().toUpperCase(), balance));
    }

    private void showBalance() {
        networkManager.getBalance(walletManager.getWalletFriendlyAddress(), new Callback<BigDecimal>() {
            @Override
            public void onResponse(BigDecimal balance) {
                DecimalFormat decimalFormat = new DecimalFormat(ETH_SHOW_PATTERN);
                decimalFormat.setRoundingMode(RoundingMode.DOWN);
                if (balance != null && !balance.equals(new BigDecimal(0))) {
                    curBalance = decimalFormat.format(balance);
                    setCryptoBalance(curBalance);
                    getLocalBalance(curBalance);

                }
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
