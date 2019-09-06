package com.guarda.zcash.sapling;

import android.content.Context;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.rest.RequestorBtc;
import com.guarda.zcash.RustAPI;
import com.guarda.zcash.sapling.api.ProtoApi;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.key.SaplingCustomFullKey;
import com.guarda.zcash.sapling.rxcall.CallBlockRange;
import com.guarda.zcash.sapling.rxcall.CallFindWitnesses;
import com.guarda.zcash.sapling.rxcall.CallLastBlock;
import com.guarda.zcash.sapling.rxcall.CallRevertLastBlocks;
import com.guarda.zcash.sapling.rxcall.CallSaplingParamsInit;
import com.guarda.zcash.sapling.rxcall.CallValidateSaplingTree;
import com.guarda.zcash.sapling.tree.SaplingMerkleTree;

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

    public static final String STATUS_SYNCING = "Syncing...";
    public static final String STATUS_SYNCED = "Synced";
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private long endB = 518945;

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

        saplingParamsInit();
    }

    public void stopSync() {
        progressSubject.onNext(false);
        inProgress = false;

        compositeDisposable.clear();
        Timber.d("stopSync inProgress=%b", inProgress);
    }

    public PublishSubject<Boolean> getProgressSubject() {
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

                    //if blocks downloading starts from last db height it will rewrite the block with empty tree field
                    protoApi.pageNum = latest.getLastFromDb() + 1;
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
                    validateSaplingTree();
                }, (e) -> stopAndLogError("getWintesses", e)));
    }

    private void validateSaplingTree() {
        compositeDisposable.add(Observable
                .fromCallable(new CallValidateSaplingTree(dbManager))
                .flatMap(it -> {
                            Timber.d("validateSaplingTree height=%d", it.getHeight());
                            String raw = RequestorBtc.getRawBlockByHash(it.getHash()).blockingFirst().getRawblock();
                            String root = new SaplingMerkleTree(it.getTree()).root();
                            Timber.d("lastBlockWithTree=%d root=%s raw=%s", it.getHeight(), root, raw);
                            boolean isContained = raw.toLowerCase().contains(root.toLowerCase());
                            return Observable.just(isContained);
                        }
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (res) -> {
                            Timber.d("validateSaplingTree finished=%s", res);
                            if (res) {
                                stopSync();
                            } else {
                                revertLastBlocks();
                            }
                        },
                        (e) -> stopAndLogError("validateSaplingTree", e)
                )
        );
    }

    private void revertLastBlocks() {
        compositeDisposable.add(
                Observable
                        .fromCallable(new CallRevertLastBlocks(dbManager))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                (res) -> {
                                    stopSync();
                                    Timber.d("revertLastBlocks completed=%s", res);
                                },
                                (e) -> stopAndLogError("revertLastBlocks", e)
                        )
        );
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
        Timber.e("%s error=%s", method, t.getMessage());
    }

}
