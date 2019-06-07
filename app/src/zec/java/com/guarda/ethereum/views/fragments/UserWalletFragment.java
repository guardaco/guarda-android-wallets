package com.guarda.ethereum.views.fragments;


import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.EthereumNetworkManager;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.utils.ClipboardUtils;
import com.guarda.ethereum.views.fragments.base.BaseFragment;
import com.guarda.zcash.sapling.SyncManager;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
    @BindView(R.id.tv_wallet_address_z)
    TextView tv_wallet_address_z;

    @Inject
    WalletManager walletManager;
    @Inject
    EthereumNetworkManager networkManager;
    @Inject
    SharedManager sharedManager;
    @Inject
    SyncManager syncManager;

    public UserWalletFragment() {
        GuardaApp.getAppComponent().inject(this);
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_user_wallet;
    }

    @Override
    protected void init() {
        setCryptoBalance();
        setUSDBalance();
        if (TextUtils.isEmpty(walletManager.getWalletFriendlyAddress())) {
            createWallet(BLOCK);
        } else {
            showExistingWallet();
        }
    }

    private void createWallet(String passphrase) {
        showProgress(getString(R.string.generating_wallet));
        walletManager.createWallet(passphrase, () -> {
            closeProgress();
            showExistingWallet();
        });
    }


    private void showExistingWallet() {
        if (isAdded() && !isDetached()) {
            tvWalletAddress.setText(walletManager.getWalletFriendlyAddress());
            tv_wallet_address_z.setText(walletManager.getSaplingAddress());
        }
        if (isWalletExist()) syncManager.startSync();
    }

    private boolean isWalletExist() {
        return !TextUtils.isEmpty(walletManager.getWalletFriendlyAddress());
    }

    private void setCryptoBalance() {
        tvCryptoCount.setText(String.format("0.00 %s", sharedManager.getCurrentCurrency().toUpperCase()));
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void setUSDBalance() {
        tvUSDCount.setText(String.format("0.00 %s", sharedManager.getLocalCurrency().toUpperCase()));
    }

    @OnClick({R.id.iv_update_address,
            R.id.btn_copy_address,
            R.id.btn_copy_address_z,
            R.id.btn_show_address_qr,
            R.id.btn_show_address_qr_z,
            R.id.btn_top_up_other_currency,
            R.id.btn_top_up_other_currency_z})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_update_address:
                openTransactionHistory();
                break;
            case R.id.btn_copy_address:
                copyAddress();
                break;
            case R.id.btn_copy_address_z:
                copyAddressZ();
                break;
            case R.id.btn_show_address_qr:
                navigateToFragment(new DepositFragment());
                break;
            case R.id.btn_show_address_qr_z:
                navigateToFragment(new DepositFragment());
                break;
            case R.id.btn_top_up_other_currency:
                navigateToFragment(new PurchaseServiceFragment());
                break;
            case R.id.btn_top_up_other_currency_z:
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

    private void copyAddressZ() {
        String address = tv_wallet_address_z.getText().toString();
        if (!TextUtils.isEmpty(address)) {
            ClipboardUtils.copyToClipBoard(getContext(), address);
        }
    }

}
