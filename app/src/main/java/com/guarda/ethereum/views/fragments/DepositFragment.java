package com.guarda.ethereum.views.fragments;


import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.ChangellyNetworkManager;
import com.guarda.ethereum.managers.CurrencyListHolder;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.items.ResponseCurrencyItem;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.utils.ClipboardUtils;
import com.guarda.ethereum.utils.QrCodeUtils;
import com.guarda.ethereum.views.activity.GenerateAddressActivity;
import com.guarda.ethereum.views.adapters.CryptoAdapter;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

@AutoInjector(GuardaApp.class)
public class DepositFragment extends BaseFragment {

    public String TAG = "DepositFragment";

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
    @Inject
    CurrencyListHolder currentCrypto;

    public final static int QR_CODE_WIDTH = 500;

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
        Log.d("flint", "DepositFragment.init()...");
        GuardaApp.getAppComponent().inject(this);
        if (walletManager.getWalletAddressForDeposit() != null && !walletManager.getWalletAddressForDeposit().isEmpty()) {
            ivQrCode.setImageBitmap(QrCodeUtils.textToQrCode(walletManager.getWalletAddressForDeposit(), QR_CODE_WIDTH));
            tvWalletAddress.setText(walletManager.getWalletAddressForDeposit());
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
