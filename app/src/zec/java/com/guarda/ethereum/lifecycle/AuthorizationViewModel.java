package com.guarda.ethereum.lifecycle;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.rxcall.CallCreateWallet;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

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
