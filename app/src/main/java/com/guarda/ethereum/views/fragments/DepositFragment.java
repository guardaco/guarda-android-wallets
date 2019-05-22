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
    private String showedAddress = "";

    @Override
    protected int getLayout() {
        return R.layout.fragment_deposit;
    }

    @OnClick({R.id.tv_tap_to_copy_address, R.id.iv_qr_code, R.id.tv_address_wallet})
    public void onClick(View view) {
        ClipboardUtils.copyToClipBoard(getContext(),  showedAddress);
    }

    @Override
    protected void init() {
        GuardaApp.getAppComponent().inject(this);

        if (walletManager.getWalletAddressForDeposit() != null && !walletManager.getWalletAddressForDeposit().isEmpty()) {
            transparent();
        }

        if (BuildConfig.FLAVOR == "zec") {
            toggle_address.setVisibility(View.VISIBLE);
            toggle_address.setOnChangeListener((position) -> {
                switch (position) {
                    case 0:
                        transparent();
                        break;
                    case 1:
                        sapling();
                        break;
                }
            });
            toggle_address.setCheckedPosition(0);
        }
    }

    private void transparent() {
        showedAddress = walletManager.getWalletAddressForDeposit();
        ivQrCode.setImageBitmap(QrCodeUtils.textToQrCode(showedAddress, QR_CODE_WIDTH));
        tvWalletAddress.setText(showedAddress);
    }

    private void sapling() {
        showedAddress = walletManager.getSaplingAddress();
        ivQrCode.setImageBitmap(QrCodeUtils.textToQrCode(showedAddress, QR_CODE_WIDTH));
        tvWalletAddress.setText(showedAddress);
    }

    @OnClick(R.id.btn_share_adr)
    public void bntShareClick(View view) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, showedAddress);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

}
