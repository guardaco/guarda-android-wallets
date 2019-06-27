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
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

@AutoInjector(GuardaApp.class)
public class SyncManager {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private long endB = 518945;
    private long lastSync = 0L;
    private static final long SYNC_PERIOD = 2 * 60 * 1000;

    @Inject
    DbManager dbManager;
    @Inject
    ProtoApi protoApi;
    @Inject
    WalletManager walletManager;
    @Inject
    Context context;

    private PublishSubject<Boolean> progressSubject = PublishSubject.create();
    private boolean inProgress;

    public SyncManager() {
        GuardaApp.getAppComponent().inject(this);
    }

    public void startSync() {
        Timber.d("startSync inProgress=%b", inProgress);
        if (inProgress) return;
        progressSubject.onNext(true);
        inProgress = true;

        if (System.currentTimeMillis() - lastSync < SYNC_PERIOD) {
            stopSync();
            Timber.d("sync period last=%d", lastSync);
            return;
        }

        saplingParamsInit();
    }

    public void stopSync() {
        progressSubject.onNext(false);
        inProgress = false;
        lastSync = System.currentTimeMillis();

        compositeDisposable.clear();
        Timber.d("stopSync inProgress=%b", inProgress);
    }

    public PublishSubject<Boolean> getPublishSubject() {
        return progressSubject;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    private void saplingParamsInit() {
        compositeDisposable.add(Observable
                .fromCallable(new CallSaplingParamsInit(context))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((latest) -> {
                    Timber.d("saplingParamsInit done=%s", latest);
                    saplingKeyInit();
                    getBlocks();
                }, (e) -> stopAndLogError("saplingParamsInit", e)));
    }

    private void getBlocks() {
        compositeDisposable.add(Observable
                .fromCallable(new CallLastBlock(dbManager, protoApi))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((res) -> {
                    Timber.d("getWintesses finished=%s", res);
                    stopSync();
                }, (e) -> stopAndLogError("getWintesses", e)));
    }

    private void saplingKeyInit() {
        if (walletManager.getSaplingCustomFullKey() == null) {
            byte[] p = RustAPI.dPart(walletManager.getPrivateKey().getBytes());
            SaplingCustomFullKey k = new SaplingCustomFullKey(p);
            walletManager.setSaplingCustomFullKey(k);
            Timber.d("saplingKeyInit inited");
        }
    }

    private void stopAndLogError(String method, Throwable t) {
        stopSync();
        Timber.d("%s e=%s", method, t.getMessage());
    }

}
