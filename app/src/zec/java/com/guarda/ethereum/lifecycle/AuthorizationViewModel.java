package com.guarda.ethereum.lifecycle;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.guarda.ethereum.managers.TransactionsManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.items.TransactionItem;
import com.guarda.ethereum.models.items.ZecTxListResponse;
import com.guarda.ethereum.models.items.ZecTxResponse;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.RequestorBtc;
import com.guarda.ethereum.rxcall.CallCreateWallet;
import com.guarda.ethereum.rxcall.CallDbFillHistory;
import com.guarda.ethereum.rxcall.CallNotesFromDb;
import com.guarda.ethereum.rxcall.CallRestoreWallet;
import com.guarda.ethereum.rxcall.CallUpdateFromDbHistory;
import com.guarda.ethereum.rxcall.CallUpdateTxDetails;
import com.guarda.zcash.sapling.SyncManager;
import com.guarda.zcash.sapling.db.DbManager;

import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class AuthorizationViewModel extends ViewModel {

    private final WalletManager walletManager;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private MutableLiveData<Boolean> isCreated = new MutableLiveData<>();

    private AuthorizationViewModel(WalletManager walletManager) {
        this.walletManager = walletManager;
    }

    public void createWallet() {
        compositeDisposable.add(
                Observable
                        .fromCallable(new CallCreateWallet(walletManager))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((created) -> isCreated.setValue(true))
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        private final WalletManager walletManager;

        public Factory(WalletManager walletManager) {
            this.walletManager = walletManager;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new AuthorizationViewModel(
                    this.walletManager);
        }
    }

    public MutableLiveData<Boolean> getIsCreated() {
        return isCreated;
    }
}
