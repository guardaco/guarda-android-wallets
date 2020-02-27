package com.guarda.ethereum.lifecycle;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.guarda.ethereum.managers.TransactionsManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.items.TransactionItem;
import com.guarda.ethereum.models.items.ZecTxListResponse;
import com.guarda.ethereum.models.items.ZecTxResponse;
import com.guarda.ethereum.rest.ApiMethods;
import com.guarda.ethereum.rest.RequestorBtc;
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

import static com.guarda.zcash.sapling.SyncManager.STATUS_SYNCED;
import static com.guarda.zcash.sapling.SyncManager.STATUS_SYNCING;
import static com.guarda.zcash.sapling.SyncProgress.SEARCH_PHASE;
import static com.guarda.zcash.sapling.SyncProgress.SYNCED_PHASE;

public class HistoryViewModel extends ViewModel {

    private final WalletManager walletManager;
    private final TransactionsManager transactionsManager;
    private final DbManager dbManager;
    private final SyncManager syncManager;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private MutableLiveData<Boolean> showHistory = new MutableLiveData<>();
    private MutableLiveData<Boolean> showTxError = new MutableLiveData<>();
    private MutableLiveData<List<TransactionItem>> showActualTxs = new MutableLiveData<>();
    private MutableLiveData<Boolean> syncInProgress = new MutableLiveData<>();
    private MutableLiveData<String> syncPhaseStatus = new MutableLiveData<>();
    private MutableLiveData<Boolean> isRestored = new MutableLiveData<>();
    private MutableLiveData<Boolean> updateBalance = new MutableLiveData<>();

    public static final String Z_TX_KEY_PREFIX = "Z";
    public static final String T_TX_KEY_PREFIX = "T";

    private HistoryViewModel(WalletManager walletManager,
                            TransactionsManager transactionsManager,
                            DbManager dbManager,
                            SyncManager syncManager) {
        this.walletManager = walletManager;
        this.transactionsManager = transactionsManager;
        this.dbManager = dbManager;
        this.syncManager = syncManager;
        initSubscriptions();
    }

    public void restoreWallet(String key) {
        compositeDisposable.add(
                Observable
                        .fromCallable(new CallRestoreWallet(walletManager, key))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((balance) -> isRestored.setValue(true))
        );
    }

    public void loadTransactions() {

        getTxsFromDb();

        RequestorBtc.getTransactionsZecNew(walletManager.getWalletFriendlyAddress(), new ApiMethods.RequestListener() {
            @Override
            public void onSuccess(Object response) {
                ZecTxListResponse txListResponse = (ZecTxListResponse) response;
                List<ZecTxResponse> txList = txListResponse.getTxs();
                if (txList == null || txList.isEmpty()) {
                    Timber.d("loadTransactions txList == null  || txList.isEmpty()");
                    getAndUpdateSaplingTx();
                    return;
                }
                compositeDisposable.add(Observable
                        .fromCallable(new CallDbFillHistory(transactionsManager, txList, walletManager.getWalletFriendlyAddress(), dbManager))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((value) -> {
                            if (value) {
                                getAndUpdateSaplingTx();
                            } else {
                                showTxError.setValue(true);
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

    private void getAndUpdateSaplingTx() {
        //hashes from shielded outputs and inputs
        compositeDisposable.add(Observable
                .fromCallable(new CallNotesFromDb(dbManager))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((hashList) -> {
                    Timber.d("CallNotesFromDb hashList size=%d", hashList.size());
                    if (hashList.isEmpty()) {
                        showHistory.setValue(true);
                        getTxsFromDb();
                        return;
                    }

                    updateFromInsight(hashList);
                }));
    }

    private void updateFromInsight(Set<String> hashList) {
        compositeDisposable.add(
                Observable
                        .just(hashList)
                        .flatMap(hashes -> {
                            Timber.d("updateFromInsight list hashes s=%d", hashes.size());
                            return Observable.fromIterable(hashes);
                        })
                        .flatMap(hash -> {
                            Timber.d("updateFromInsight hash s=%s", hash);
                            return RequestorBtc.getOneTx(hash);
                        })
                        .flatMap(txResponse -> {
                            if (txResponse == null) {
                                Timber.e("getOneTx tx == null");
                                return null;
                            } else {
                                Timber.d("getOneTx tx=%s", txResponse);
                                return Observable
                                        .fromCallable(new CallUpdateTxDetails(
                                                dbManager,
                                                txResponse,
                                                walletManager.getWalletFriendlyAddress()
                                        ));
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                txHash -> Timber.d("subscribe CallUpdateTxDetails tx hash=%s", txHash),
                                e -> Timber.e("updateFromInsight err=%s", e.getMessage()),
                                () -> {
                                    showHistory.setValue(true);
                                    Timber.d("updateFromInsight completed");
                                }
                        )
        );
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

    private void initSubscriptions() {
        compositeDisposable.add(syncManager.getProgressSubject().subscribe(t -> {
            Timber.d("getProgressSubject onNext() t=%b", t);
            syncInProgress.setValue(t);
            if (!t) updateBalance.setValue(true);
        }));

        compositeDisposable.add(syncManager.getSyncProgress().subscribe(progress -> {
            Timber.d("getSyncProgress onNext() progress=%s", progress);

            if (progress.getProcessPhase().equals(SYNCED_PHASE)) {
                syncPhaseStatus.postValue(STATUS_SYNCED);
            } else {
                // 50 % - when blocks downloaded, but searching isn't started
                long range = progress.getToBlock() - progress.getFromBlock();
                if (range == 0 || progress.getCurrentBlock() == 0) {
                    syncPhaseStatus.postValue(STATUS_SYNCING);
                    return;
                }

                double percent = (double) (progress.getCurrentBlock() - progress.getFromBlock()) / range;
                percent = percent * 50;

                if (progress.getProcessPhase().equals(SEARCH_PHASE)) percent += 50;

                String status = String.format("%s (%.0f%%)", STATUS_SYNCING, percent);
                syncPhaseStatus.postValue(status);
            }
        }));
    }

    public void setCurrentStatus() {
        syncInProgress.setValue(syncManager.isInProgress());
    }

    public void startSync() {
        syncManager.startSync();
    }

    public void stopSync() {
        syncManager.stopSync();
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
        private final SyncManager syncManager;

        public Factory(WalletManager walletManager,
                       TransactionsManager transactionsManager,
                       DbManager dbManager,
                       SyncManager syncManager) {
            this.walletManager = walletManager;
            this.transactionsManager = transactionsManager;
            this.dbManager = dbManager;
            this.syncManager = syncManager;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new HistoryViewModel(
                    this.walletManager,
                    this.transactionsManager,
                    this.dbManager,
                    this.syncManager);
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

    public MutableLiveData<Boolean> getSyncInProgress() {
        return syncInProgress;
    }

    public MutableLiveData<String> getSyncPhaseStatus() {
        return syncPhaseStatus;
    }

    public MutableLiveData<Boolean> getIsRestored() {
        return isRestored;
    }

    public MutableLiveData<Boolean> getUpdateBalance() {
        return updateBalance;
    }

}
