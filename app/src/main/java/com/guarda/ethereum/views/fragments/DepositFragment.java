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

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import segmented_control.widget.custom.android.com.segmentedcontrol.SegmentedControl;
import timber.log.Timber;

public class DepositFragment extends BaseFragment {

    @BindView(R.id.segmented_control)
    SegmentedControl segmented_control;
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
            segmented_control.setVisibility(View.VISIBLE);
            segmented_control
                    .addOnSegmentClickListener(
                            (segmentViewHolder) -> {
                                switch (segmentViewHolder.getAbsolutePosition()) {
                                    case 0:
                                        transparent();
                                        break;
                                    case 1:
                                        sapling();
                                        break;
                                }
                            }
                    );
            segmented_control.setSelectedSegment(0);
        }
    }

    private void transparent() {
        showedAddress = walletManager.getWalletAddressForDeposit();
        if (showedAddress == null) {
            Timber.e("transparent showedAddress == null");
            return;
        }
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
