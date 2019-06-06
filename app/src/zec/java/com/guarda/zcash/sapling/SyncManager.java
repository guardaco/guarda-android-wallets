package com.guarda.zcash.sapling;

import android.content.Context;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.zcash.RustAPI;
import com.guarda.zcash.sapling.api.ProtoApi;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.key.SaplingCustomFullKey;
import com.guarda.zcash.sapling.rxcall.CallBlockRange;
import com.guarda.zcash.sapling.rxcall.CallFindWitnesses;
import com.guarda.zcash.sapling.rxcall.CallLastBlock;
import com.guarda.zcash.sapling.rxcall.CallSaplingParamsInit;

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
    private boolean paramsInited;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private long endB = 437489;

    @Inject
    DbManager dbManager;
    @Inject
    ProtoApi protoApi;
    @Inject
    WalletManager walletManager;
    @Inject
    Context context;

    public SyncManager() {
        GuardaApp.getAppComponent().inject(this);
    }

    public void startSync() {
        Timber.d("startSync inProgress=%b paramsInited=%b", inProgress, paramsInited);
        if (inProgress) return;

        inProgress = true;

        saplingParamsInit();
    }

    public void stopSync() {
        inProgress = false;

        compositeDisposable.dispose();
        Timber.d("stopSync inProgress=%b paramsInited=%b", inProgress, paramsInited);
    }

    public boolean isSyncInProgress() {
        return inProgress;
    }

    private void saplingParamsInit() {
        compositeDisposable.add(Observable
                .fromCallable(new CallSaplingParamsInit(context))
                .subscribeOn(Schedulers.io())
                .subscribe((latest) -> {
                    Timber.d("saplingParamsInit done=%s", latest);
                    walletManager.setSaplingCustomFullKey(new SaplingCustomFullKey(RustAPI.dPart(walletManager.getPrivateKey().getBytes())));
                    getBlocks();
                }, (e) -> stopAndLogError("saplingParamsInit", e)));
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
                }, (e) -> stopAndLogError("getBlocks", e)));
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
                }, (e) -> stopAndLogError("blockRangeToDb", e)));
    }

    private void getWintesses() {
        compositeDisposable.add(Observable
                .fromCallable(new CallFindWitnesses(dbManager, walletManager.getSaplingCustomFullKey()))
                .subscribeOn(Schedulers.io())
                .subscribe((res) -> {
                    Timber.d("getWintesses finished=%s", res);
                    stopSync();
                }, (e) -> stopAndLogError("getWintesses", e)));
    }

    private void stopAndLogError(String method, Throwable t) {
        stopSync();
        Timber.d("%s e=%s", method, t.getMessage());
    }

}
