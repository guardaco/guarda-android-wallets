package com.guarda.ethereum;

import com.guarda.ethereum.customviews.RateDialog;
import com.guarda.ethereum.dependencies.AppModule;
import com.guarda.ethereum.dependencies.RetrofitServicesModule;
import com.guarda.ethereum.managers.RawNodeManager;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.screens.exchange.first.ExchangeFragment;
import com.guarda.ethereum.utils.GsonUtils;
import com.guarda.ethereum.utils.KeyStoreUtils;
import com.guarda.ethereum.views.activity.AccessCodeAgainActivity;
import com.guarda.ethereum.views.activity.AddrBtcCoinifyActivity;
import com.guarda.ethereum.views.activity.AmountCoinifyActivity;
import com.guarda.ethereum.views.activity.AmountToSendActivity;
import com.guarda.ethereum.views.activity.AuthorizationTypeActivity;
import com.guarda.ethereum.views.activity.BankAccCoinifyActivity;
import com.guarda.ethereum.views.activity.CoinifyKYCExplainActivity;
import com.guarda.ethereum.views.activity.ConfirmCoinifyActivity;
import com.guarda.ethereum.views.activity.ConfirmPinCodeActivity;
import com.guarda.ethereum.views.activity.CongratsActivity;
import com.guarda.ethereum.views.activity.CreateNewWalletActivity;
import com.guarda.ethereum.views.activity.GenerateAddressActivity;
import com.guarda.ethereum.views.activity.MainActivity;
import com.guarda.ethereum.views.activity.PurchaseWemovecoinsActivity;
import com.guarda.ethereum.views.activity.ReceiptCoinifyActivity;
import com.guarda.ethereum.views.activity.RestoreFromBackupActivity;
import com.guarda.ethereum.views.activity.SellConfirmCoinifyActivity;
import com.guarda.ethereum.views.activity.SendingCurrencyActivity;
import com.guarda.ethereum.views.activity.SettingsWebViewActivity;
import com.guarda.ethereum.views.activity.TransactionDetailsActivity;
import com.guarda.ethereum.views.activity.base.BaseActivity;
import com.guarda.ethereum.views.activity.base.SimpleTrackOnStopActivity;
import com.guarda.ethereum.views.activity.base.TrackOnStopActivity;
import com.guarda.ethereum.views.adapters.TransHistoryAdapter;
import com.guarda.ethereum.views.fragments.BackupFragment;
import com.guarda.ethereum.views.fragments.CustomNodeFragment;
import com.guarda.ethereum.views.fragments.DepositFragment;
import com.guarda.ethereum.views.fragments.DepositFragment_decent;
import com.guarda.ethereum.views.fragments.DisabledFragment;
import com.guarda.ethereum.views.fragments.EnterEmailCoinifyFragment;
import com.guarda.ethereum.views.fragments.ExchangeAboutFragment;
import com.guarda.ethereum.views.fragments.ExchangeInputAddressFragment;
import com.guarda.ethereum.views.fragments.ExchangeStartFragment;
import com.guarda.ethereum.views.fragments.ListBankAccCoinifyFragment;
import com.guarda.ethereum.views.fragments.NewBankAccCoinifyFragment;
import com.guarda.ethereum.views.fragments.PayMethodsCoinifyFragment;
import com.guarda.ethereum.views.fragments.PurchaseCoinsFragment;
import com.guarda.ethereum.views.fragments.PurchaseFragment;
import com.guarda.ethereum.views.fragments.SettingsFragment;
import com.guarda.ethereum.views.fragments.TransactionHistoryFragment;
import com.guarda.ethereum.views.fragments.UserWalletFragment;
import com.guarda.ethereum.views.fragments.WithdrawFragment;
import com.guarda.zcash.sapling.SyncManager;
import com.guarda.zcash.sapling.api.ProtoApi;
import com.guarda.zcash.sapling.db.DbManager;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
public interface GuardaAppComponent {
    //activity
    void inject(MainActivity mainActivity);
    void inject(TrackOnStopActivity trackOnStopActivity);
    void inject(BaseActivity baseActivity);
    void inject(CongratsActivity congratsActivity);
    void inject(ConfirmCoinifyActivity confirmCoinifyActivity);
    void inject(SettingsWebViewActivity settingsWebViewActivity);
    void inject(BankAccCoinifyActivity bankAccCoinifyActivity);
    void inject(AccessCodeAgainActivity accessCodeAgainActivity);
    void inject(ReceiptCoinifyActivity receiptCoinifyActivity);
    void inject(AuthorizationTypeActivity authorizationTypeActivity);
    void inject(SimpleTrackOnStopActivity simpleTrackOnStopActivity);
    void inject(CoinifyKYCExplainActivity coinifyKYCExplainActivity);
    void inject(ConfirmPinCodeActivity confirmPinCodeActivity);
    void inject(AddrBtcCoinifyActivity addrBtcCoinifyActivity);
    void inject(GenerateAddressActivity generateAddressActivity);
    void inject(AmountCoinifyActivity amountCoinifyActivity);
    void inject(AmountToSendActivity amountToSendActivity);
    void inject(SendingCurrencyActivity sendingCurrencyActivity);
    void inject(TransactionDetailsActivity transactionDetailsActivity);
    void inject(RestoreFromBackupActivity restoreFromBackupActivity);
    void inject(SellConfirmCoinifyActivity sellConfirmCoinifyActivity);
    void inject(PurchaseWemovecoinsActivity purchaseWemovecoinsActivity);
    void inject(CreateNewWalletActivity createNewWalletActivity);

    //fragment
    void inject(ExchangeFragment exchangeFragment);
    void inject(DisabledFragment disabledFragment);
    void inject(DepositFragment depositFragment);
    void inject(WithdrawFragment withdrawFragment);
    void inject(SettingsFragment settingsFragment);
    void inject(PurchaseFragment purchaseFragment);
    void inject(NewBankAccCoinifyFragment newBankAccCoinifyFragment);
    void inject(EnterEmailCoinifyFragment enterEmailCoinifyFragment);
    void inject(PurchaseCoinsFragment purchaseCoinsFragment);
    void inject(PayMethodsCoinifyFragment payMethodsCoinifyFragment);
    void inject(ExchangeInputAddressFragment exchangeInputAddressFragment);
    void inject(ListBankAccCoinifyFragment listBankAccCoinifyFragment);
    void inject(BackupFragment backupFragment);
    void inject(DepositFragment_decent depositFragmentDecent);
    void inject(CustomNodeFragment customNodeFragment);
    void inject(ExchangeStartFragment exchangeStartFragment);
    void inject(ExchangeAboutFragment exchangeAboutFragment);
    void inject(UserWalletFragment userWalletFragment);
    void inject(TransactionHistoryFragment transactionHistoryFragment);
    void inject(RateDialog rateDialog);

    //manager
    void inject(WalletManager walletManager);
    void inject(SharedManager sharedManager);
    void inject(SyncManager syncManager);
    void inject(DbManager dbManager);
    void inject(KeyStoreUtils keyStoreUtils);
    void inject(ProtoApi protoApi);
    void inject(GsonUtils gsonUtils);
    void inject(RawNodeManager rawNodeManager);

    //
    void inject(TransHistoryAdapter transHistoryAdapter);
}
