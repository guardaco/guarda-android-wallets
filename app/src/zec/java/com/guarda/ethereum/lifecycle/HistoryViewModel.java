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
import com.guarda.ethereum.rxcall.CallDbFillHistory;
import com.guarda.ethereum.rxcall.CallNotesFromDb;
import com.guarda.ethereum.rxcall.CallUpdateFromDbHistory;
import com.guarda.ethereum.rxcall.CallUpdateTxDetails;
import com.guarda.zcash.sapling.db.DbManager;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class HistoryViewModel extends ViewModel {

    private final WalletManager walletManager;
    private final TransactionsManager transactionsManager;
    private final DbManager dbManager;
    public final static int INPUTS_HASHES = 0;
    public final static int OUTPUTS_HASHES = 1;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private MutableLiveData<Boolean> showHistory = new MutableLiveData<>();
    private MutableLiveData<Boolean> showTxError = new MutableLiveData<>();
    private MutableLiveData<List<TransactionItem>> showActualTxs = new MutableLiveData<>();

    public HistoryViewModel(WalletManager walletManager, TransactionsManager transactionsManager, DbManager dbManager) {
        this.walletManager = walletManager;
        this.transactionsManager = transactionsManager;
        this.dbManager = dbManager;
    }

    public void loadTransactions() {
        RequestorBtc.getTransactionsZecNew(walletManager.getWalletFriendlyAddress(), new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                ZecTxListResponse txListResponse = (ZecTxListResponse) response;
                List<ZecTxResponse> txList = txListResponse.getTxs();
                if (txList == null) {
                    Timber.e("loadTransactions txList == null");
                    return;
                }
                compositeDisposable.add(Observable
                        .fromCallable(new CallDbFillHistory(transactionsManager, txList, walletManager.getWalletFriendlyAddress(), dbManager))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((value) -> {
                            if (value) {
                                showHistory.setValue(true);
                                getAndUpdateSaplingTx();
                            }
                            Timber.d("CallDbFillHistory value=%b", value);
                        }));
            }

            @Override
            public void onFailure(String msg) {
                showTxError.setValue(true);
            }
        });
    }

    public void getAndUpdateSaplingTx() {
        //hashes from shielded inputs
        compositeDisposable.add(Observable
                .fromCallable(new CallNotesFromDb(dbManager, INPUTS_HASHES))
                .subscribeOn(Schedulers.io())
                .subscribe((value) -> {
                    Timber.d("CallDbFillHistory value size=%d", value.size());
                    if (value.isEmpty()) return;

                    for (String hash : value) {
                        updateFromInsight(hash);
                    }
                }));
        //hashes from shielded outputs
        compositeDisposable.add(Observable
                .fromCallable(new CallNotesFromDb(dbManager, OUTPUTS_HASHES))
                .subscribeOn(Schedulers.io())
                .subscribe((value) -> {
                    Timber.d("CallDbFillHistory value size=%d", value.size());
                    if (value.isEmpty()) return;

                    for (String hash : value) {
                        updateFromInsight(hash);
                    }
                }));
    }

    private void updateFromInsight(String hash) {
        RequestorBtc.getOneTx(hash, new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                ZecTxResponse txResponse = (ZecTxResponse) response;
                if (txResponse == null) {
                    Timber.e("getOneTx tx == null");
                    return;
                }
                compositeDisposable.add(Observable
                        .fromCallable(new CallUpdateTxDetails(dbManager, txResponse))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((value) -> {
                            if (value) getTxsFromDb();
                            Timber.d("CallDbFillHistory value=%b", value);
                        }));
            }

            @Override
            public void onFailure(String msg) {
                Timber.e("getOneTx e=%s", msg);
            }
        });
    }

    public void getTxsFromDb() {
        compositeDisposable.add(Observable
                .fromCallable(new CallUpdateFromDbHistory(dbManager))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((list) -> {
                    Timber.d("CallUpdateFromDbHistory list size=%d", list.size());

                    if (list.isEmpty()) return;
                    transactionsManager.setTransactionsList(list);
                    showActualTxs.setValue(list);
                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        private final WalletManager walletManager;
        private final TransactionsManager transactionsManager;
        private final DbManager dbManager;

        public Factory(WalletManager walletManager, TransactionsManager transactionsManager, DbManager dbManager) {
            this.walletManager = walletManager;
            this.transactionsManager = transactionsManager;
            this.dbManager = dbManager;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new HistoryViewModel(this.walletManager, this.transactionsManager, this.dbManager);
        }
    }

    public MutableLiveData<Boolean> getShowHistory() {
        return showHistory;
    }

    public MutableLiveData<Boolean> getShowTxError() {
        return showTxError;
    }

    public MutableLiveData<List<TransactionItem>> getShowActualTxs() {
        return showActualTxs;
    }
}
