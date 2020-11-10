package com.guarda.ethereum;

import com.guarda.ethereum.customviews.RateDialog;
import com.guarda.ethereum.dependencies.AppModule;
import com.guarda.ethereum.lifecycle.ResyncViewModel;
import com.guarda.ethereum.managers.RawNodeManager;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.sapling.SyncManager;
import com.guarda.ethereum.sapling.SyncService;
import com.guarda.ethereum.sapling.api.ProtoApi;
import com.guarda.ethereum.sapling.db.DbManager;
import com.guarda.ethereum.screens.exchange.first.ExchangeFragment;
import com.guarda.ethereum.utils.GsonUtils;
import com.guarda.ethereum.utils.KeyStoreUtils;
import com.guarda.ethereum.views.activity.AccessCodeAgainActivity;
import com.guarda.ethereum.views.activity.AmountToSendActivity;
import com.guarda.ethereum.views.activity.AuthorizationTypeActivity;
import com.guarda.ethereum.views.activity.ConfirmPinCodeActivity;
import com.guarda.ethereum.views.activity.CongratsActivity;
import com.guarda.ethereum.views.activity.CreateNewWalletActivity;
import com.guarda.ethereum.views.activity.MainActivity;
import com.guarda.ethereum.views.activity.RestoreFromBackupActivity;
import com.guarda.ethereum.views.activity.SendingCurrencyActivity;
import com.guarda.ethereum.views.activity.TransactionDetailsActivity;
import com.guarda.ethereum.views.activity.base.BaseActivity;
import com.guarda.ethereum.views.activity.base.SimpleTrackOnStopActivity;
import com.guarda.ethereum.views.activity.base.TrackOnStopActivity;
import com.guarda.ethereum.views.adapters.TransHistoryAdapter;
import com.guarda.ethereum.views.fragments.BackupFragment;
import com.guarda.ethereum.views.fragments.CustomNodeFragment;
import com.guarda.ethereum.views.fragments.DepositFragment;
import com.guarda.ethereum.views.fragments.DisabledFragment;
import com.guarda.ethereum.views.fragments.ExchangeAboutFragment;
import com.guarda.ethereum.views.fragments.ExchangeInputAddressFragment;
import com.guarda.ethereum.views.fragments.ExchangeStartFragment;
import com.guarda.ethereum.views.fragments.SettingsFragment;
import com.guarda.ethereum.views.fragments.TransactionHistoryFragment;
import com.guarda.ethereum.views.fragments.UserWalletFragment;
import com.guarda.ethereum.views.fragments.WithdrawFragment;

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
    void inject(AccessCodeAgainActivity accessCodeAgainActivity);
    void inject(AuthorizationTypeActivity authorizationTypeActivity);
    void inject(SimpleTrackOnStopActivity simpleTrackOnStopActivity);
    void inject(ConfirmPinCodeActivity confirmPinCodeActivity);
    void inject(AmountToSendActivity amountToSendActivity);
    void inject(SendingCurrencyActivity sendingCurrencyActivity);
    void inject(TransactionDetailsActivity transactionDetailsActivity);
    void inject(RestoreFromBackupActivity restoreFromBackupActivity);
    void inject(CreateNewWalletActivity createNewWalletActivity);

    //fragment
    void inject(ExchangeFragment exchangeFragment);
    void inject(DisabledFragment disabledFragment);
    void inject(DepositFragment depositFragment);
    void inject(WithdrawFragment withdrawFragment);
    void inject(SettingsFragment settingsFragment);
    void inject(ExchangeInputAddressFragment exchangeInputAddressFragment);
    void inject(BackupFragment backupFragment);
    void inject(CustomNodeFragment customNodeFragment);
    void inject(ExchangeStartFragment exchangeStartFragment);
    void inject(ExchangeAboutFragment exchangeAboutFragment);
    void inject(UserWalletFragment userWalletFragment);
    void inject(TransactionHistoryFragment transactionHistoryFragment);
    void inject(RateDialog rateDialog);

    //viewmodel
    void inject(ResyncViewModel resyncViewModel);

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
    void inject(SyncService syncService);
}
