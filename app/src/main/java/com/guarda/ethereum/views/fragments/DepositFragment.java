package com.guarda.ethereum.views.fragments;


import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.guarda.ethereum.BuildConfig;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.utils.ClipboardUtils;
import com.guarda.ethereum.utils.QrCodeUtils;
import com.guarda.ethereum.views.fragments.base.BaseFragment;
import com.llollox.androidtoggleswitch.widgets.ToggleSwitch;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

@AutoInjector(GuardaApp.class)
public class DepositFragment extends BaseFragment {

    @BindView(R.id.toggle_address)
    ToggleSwitch toggle_address;
    @BindView(R.id.iv_qr_code)
    ImageView ivQrCode;
    @BindView(R.id.tv_address_wallet)
    TextView tvWalletAddress;
    @BindView(R.id.tv_tap_to_copy_address)
    TextView tvTapToCopyAddress;
    @BindView(R.id.btn_share_adr)
    Button btnShareAdr;

    @Inject
    WalletManager walletManager;

    public final static int QR_CODE_WIDTH = 600;

    @Override
    protected int getLayout() {
        return R.layout.fragment_deposit;
    }

    @OnClick({R.id.tv_tap_to_copy_address, R.id.iv_qr_code, R.id.tv_address_wallet})
    public void onClick(View view) {
        ClipboardUtils.copyToClipBoard(getContext(),  walletManager.getWalletAddressForDeposit());
    }

    @Override
    protected void init() {
        GuardaApp.getAppComponent().inject(this);

        if (walletManager.getWalletAddressForDeposit() != null && !walletManager.getWalletAddressForDeposit().isEmpty()) {
            ivQrCode.setImageBitmap(QrCodeUtils.textToQrCode(walletManager.getWalletAddressForDeposit(), QR_CODE_WIDTH));
            tvWalletAddress.setText(walletManager.getWalletAddressForDeposit());
        }

        if (BuildConfig.FLAVOR == "zec") {
            toggle_address.setVisibility(View.VISIBLE);
            toggle_address.setOnChangeListener((position) -> {
                switch (position) {
                    case 0:
                        ivQrCode.setImageBitmap(QrCodeUtils.textToQrCode(walletManager.getWalletAddressForDeposit(), QR_CODE_WIDTH));
                        tvWalletAddress.setText(walletManager.getWalletAddressForDeposit());
                        break;
                    case 1:
                        ivQrCode.setImageBitmap(QrCodeUtils.textToQrCode(walletManager.getPaymentAddressZ(), QR_CODE_WIDTH));
                        tvWalletAddress.setText(walletManager.getPaymentAddressZ());
                        break;
                }
            });
            toggle_address.setCheckedPosition(0);
        }
    }

    @OnClick(R.id.btn_share_adr)
    public void bntShareClick(View view) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, tvWalletAddress.getText().toString());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

}
