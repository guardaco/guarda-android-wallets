package com.guarda.ethereum.dependencies;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.guarda.ethereum.managers.CurrencyListHolder;
import com.guarda.ethereum.managers.EthereumNetworkManager;
import com.guarda.ethereum.managers.RawNodeManager;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.TransactionsManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.utils.KeyStoreUtils;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by SV on 10.08.2017.
 */

@Module
public class AppModule {

    private Context mContext;

    public AppModule(Context mContext) {
        this.mContext = mContext;
    }

    @Provides
    @Singleton
    Context provideContext() {
        return mContext;
    }

    @Provides
    @Singleton
    WalletManager provideWalletManager(Context context) {
        return new WalletManager(context);
    }

    @Provides
    @Singleton
    EthereumNetworkManager provideEtherreumNetworkManager(WalletManager walletManager) {
        return new EthereumNetworkManager(walletManager);
    }

    @Provides
    @Singleton
    CurrencyListHolder provideCurrencyList() {
        return new CurrencyListHolder();
    }

    @Provides
    @Singleton
    TransactionsManager provideTransactionsHolder() {
        return new TransactionsManager();
    }

    @Provides
    @Singleton
    SharedManager provideSharedManager(){
        return new SharedManager();
    }

    @Provides
    @Singleton
    @RequiresApi(api = Build.VERSION_CODES.M)
    KeyStoreUtils provideKeyStoreUtils(){
        return new KeyStoreUtils();
    }

    @Provides
    @Singleton
    RawNodeManager provideNodeManager(){
        return new RawNodeManager();
    }
}
