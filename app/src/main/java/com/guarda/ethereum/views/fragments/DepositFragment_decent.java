package com.guarda.ethereum.views.fragments;


import android.content.Intent;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.ChangellyNetworkManager;
import com.guarda.ethereum.managers.CurrencyListHolder;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.items.ResponseCurrencyItem;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.utils.ClipboardUtils;
import com.guarda.ethereum.utils.Coders;
import com.guarda.ethereum.utils.QrCodeUtils;
import com.guarda.ethereum.views.activity.GenerateAddressActivity;
import com.guarda.ethereum.views.adapters.CryptoAdapter;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

public class DepositFragment_decent extends BaseFragment {

    public String TAG = "DepositFragment";

    @BindView(R.id.iv_qr_code)
    ImageView ivQrCode;
    @BindView(R.id.tv_address_wallet)
    TextView tvWalletAddress;
    @BindView(R.id.tv_tap_to_copy_address)
    TextView tvTapToCopyAddress;
    @BindView(R.id.rv_crypto_purchase)
    RecyclerView rvCryptoRecycler;
    @BindView(R.id.tv_deposit_caption)
    TextView tvDepositCaption;
    @BindView(R.id.btn_share_adr)
    Button btnShareAdr;
    @BindView(R.id.btn_display_encrypted_alt)
    Button btn_display_encrypted_alt;
    @BindView(R.id.tv_your_address)
    TextView tv_your_address;
    @BindView(R.id.imageViewAbout)
    ImageView imageViewAbout;

    private CryptoAdapter adapter;

    @Inject
    WalletManager walletManager;
    @Inject
    CurrencyListHolder currentCrypto;

    public final static int QR_CODE_WIDTH = 500;
    private boolean showEncryptedAddress = false;

    @Override
    protected int getLayout() {
        return R.layout.fragment_deposit_dct;
    }

    @OnClick({R.id.tv_tap_to_copy_address, R.id.iv_qr_code, R.id.tv_address_wallet, R.id.btn_copy_address})
    public void onClick(View view) {
        ClipboardUtils.copyToClipBoard(getContext(), walletManager.getWalletAddressForDeposit());
    }

    @Override
    protected void init() {
        Log.d("flint", "DepositFragment.init()...");
        GuardaApp.getAppComponent().inject(this);
        if (walletManager.getWalletAddressForDeposit() != null && !walletManager.getWalletAddressForDeposit().isEmpty()) {
            ivQrCode.setImageBitmap(QrCodeUtils.textToQrCode(walletManager.getWalletAddressForDeposit(), QR_CODE_WIDTH));
            tvWalletAddress.setText(walletManager.getWalletAddressForDeposit());
        }

        try {
            tvDepositCaption.setText(R.string.empty_string);
            ((CardView) rvCryptoRecycler.getParent()).setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        btn_display_encrypted_alt.setOnClickListener((prm) -> {
            showEncryptedAddress = !showEncryptedAddress;
            updateShowEncryptedButtonText();
        });

        imageViewAbout.setOnClickListener((prm) -> {
            Fragment thisFragment = this;
            try {
                navigateToFragment(new DepositAboutFragment().setData(thisFragment));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        updateShowEncryptedButtonText();

        initMenuButton();
        setToolbarTitle(getString(R.string.title_deposit));
    }

    private void updateShowEncryptedButtonText() {
        if (showEncryptedAddress) {
            btn_display_encrypted_alt.setText(R.string.app_display_your_address);
            tv_your_address.setText(R.string.app_your_address_alt);
            if (walletManager.getWalletAddressForDeposit() != null && !walletManager.getWalletAddressForDeposit().isEmpty())
                tvWalletAddress.setText("u"+ Coders.md5(walletManager.getWalletAddressForDeposit()));
        }
        else {
            btn_display_encrypted_alt.setText(R.string.app_display_your_address_alt);
            tv_your_address.setText(R.string.app_your_address);
            if (walletManager.getWalletAddressForDeposit() != null && !walletManager.getWalletAddressForDeposit().isEmpty())
                tvWalletAddress.setText(walletManager.getWalletAddressForDeposit());
        }
    }

    private void initRecycler() {
        adapter = new CryptoAdapter();

        adapter.setItemClickListener(new CryptoAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position, String name, String code) {
                Intent intent = new Intent(new Intent(getActivity(), GenerateAddressActivity.class));
                intent.putExtra(Extras.SELECTED_CURRENCY, code);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.no_slide);
            }
        });

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        rvCryptoRecycler.setLayoutManager(layoutManager);
        rvCryptoRecycler.setAdapter(adapter);

        if (currentCrypto.getListOfCurrencies() == null
                || currentCrypto.getListOfCurrencies().isEmpty()) {
            loadCurrencies();
        } else {
            adapter.updateList(currentCrypto.getListOfCurrencies());
        }

    }


    private void loadCurrencies() {
        showProgress(getString(R.string.loader_loading_available_currencies));
        ChangellyNetworkManager.getCurrencies(new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                ResponseCurrencyItem responseCurrency = (ResponseCurrencyItem) response;
                if (getContext() != null) {
                    adapter.updateList(currentCrypto.castResponseCurrencyToCryptoItem(responseCurrency, getActivity().getApplicationContext()));
                }
                closeProgress();
            }

            @Override
            public void onFailure(String msg) {
                Log.d(TAG, "request failure: " + msg);
                closeProgress();
            }
        });
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
