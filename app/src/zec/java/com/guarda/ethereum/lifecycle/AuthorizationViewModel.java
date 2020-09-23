package com.guarda.ethereum.lifecycle;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.rxcall.CallCreateWallet;
import com.guarda.zcash.sapling.api.ProtoApi;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class AuthorizationViewModel extends ViewModel {

    private final WalletManager walletManager;
    private ProtoApi protoApi;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private MutableLiveData<Boolean> isCreated = new MutableLiveData<>();

    private AuthorizationViewModel(WalletManager walletManager, ProtoApi protoApi) {
        this.walletManager = walletManager;
        this.protoApi = protoApi;
    }

    public void createWallet() {
        compositeDisposable.add(
                Observable
                        .fromCallable(new CallCreateWallet(walletManager, protoApi))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                (created) -> isCreated.setValue(created),
                                (e) -> {
                                    Timber.d("CallCreateWallet err=%s", e.getMessage());
                                }
                        )
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        private final WalletManager walletManager;
        private ProtoApi protoApi;

        public Factory(WalletManager walletManager, ProtoApi protoApi) {
            this.walletManager = walletManager;
            this.protoApi = protoApi;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new AuthorizationViewModel(
                    this.walletManager, this.protoApi
            );
        }
    }

    public MutableLiveData<Boolean> getIsCreated() {
        return isCreated;
    }
}
