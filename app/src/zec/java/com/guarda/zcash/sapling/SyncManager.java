package com.guarda.zcash.sapling;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.zcash.sapling.api.ProtoApi;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.db.model.TxOutRoom;
import com.guarda.zcash.sapling.model.OutputDescResp;
import com.guarda.zcash.sapling.model.WalletTx;
import com.guarda.zcash.sapling.note.SaplingNoteData;
import com.guarda.zcash.sapling.note.SaplingOutPoint;
import com.guarda.zcash.sapling.rxcall.CallBlockRange;
import com.guarda.zcash.sapling.rxcall.CallFindWitnesses;
import com.guarda.zcash.sapling.rxcall.CallLastBlock;
import com.guarda.zcash.sapling.tree.MerklePath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import autodagger.AutoInjector;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@AutoInjector(GuardaApp.class)
public class SyncManager {

    private boolean inProgress;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private long endB = 437489;

    @Inject
    DbManager dbManager;
    @Inject
    ProtoApi protoApi;
    @Inject
    WalletManager walletManager;

    public SyncManager() {
        GuardaApp.getAppComponent().inject(this);
    }

    public void startSync() {
        if (inProgress) return;

        inProgress = true;

        getBlocks();
    }

    public void stopSync() {
        inProgress = false;

        compositeDisposable.dispose();
    }

    public boolean isSyncInProgress() {
        return inProgress;
    }

    private void getBlocks() {
        compositeDisposable.add(Observable
                .fromCallable(new CallLastBlock(dbManager, protoApi))
                .subscribeOn(Schedulers.io())
                .subscribe((latest) -> {
                    Timber.d("getBlocks latest=%s", latest);
                    protoApi.pageNum = latest.getLastFromDb();
                    endB = latest.getLatest();
                    blockRangeToDb();
                }));
    }

    private void blockRangeToDb() {
        compositeDisposable.add(Observable.fromCallable(new CallBlockRange(protoApi, endB))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((t) -> {
                    Timber.d("accept thread=%s", Thread.currentThread().getName());
                    if (!t) {
                        Timber.d("paginator.accept(items) null");
                    }

                    if (protoApi.pageNum <= endB) {
                        blockRangeToDb();
                    } else {
                        //blocks downloading ended
                        Timber.d("protoApi.pageNum >= endB");
                        //find wintesses
                        getWintesses();
                    }
                }));
    }

    private void getWintesses() {
        compositeDisposable.add(Observable
                .fromCallable(new CallFindWitnesses(dbManager, walletManager.getSaplingCustomFullKey()))
                .subscribeOn(Schedulers.io())
                .subscribe((res) -> {
                    Timber.d("getWintesses finished=%s", res);
                    stopSync();
                }));
    }

}
