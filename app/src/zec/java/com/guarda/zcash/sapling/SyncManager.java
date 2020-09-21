package com.guarda.zcash.sapling;

import android.content.Context;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.items.SaplingBlockTree;
import com.guarda.ethereum.repository.RawResourceRepository;
import com.guarda.ethereum.rest.RequestorBtc;
import com.guarda.zcash.sapling.api.ProtoApi;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.key.SaplingCustomFullKey;
import com.guarda.zcash.sapling.rxcall.CallBlockRange;
import com.guarda.zcash.sapling.rxcall.CallBlocksForSync;
import com.guarda.zcash.sapling.rxcall.CallFindWitnesses;
import com.guarda.zcash.sapling.rxcall.CallLastBlock;
import com.guarda.zcash.sapling.rxcall.CallRevertLastBlock;
import com.guarda.zcash.sapling.rxcall.CallSaplingParamsInit;
import com.guarda.zcash.sapling.rxcall.CallValidateSaplingTree;
import com.guarda.zcash.sapling.tree.SaplingMerkleTree;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;
import work.samosudov.rustlib.RustAPI;

import static com.guarda.zcash.sapling.SyncProgress.DOWNLOAD_PHASE;
import static com.guarda.zcash.sapling.SyncProgress.SEARCH_PHASE;
import static com.guarda.zcash.sapling.SyncProgress.SYNCED_PHASE;
import static com.guarda.zcash.sapling.rxcall.CallLastBlock.FIRST_BLOCK_TO_SYNC_MAINNET;

public class SyncManager {

    public static final String STATUS_SYNCING = "Syncing z-address";
    public static final String STATUS_SYNCED = "Synced z-address";
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
    @Inject
    RawResourceRepository rawResourceRepository;

    private PublishSubject<Boolean> progressSubject = PublishSubject.create();
    private PublishSubject<SyncProgress> progressPhase = PublishSubject.create();
    private boolean inProgress;
    private SyncProgress syncProgress = new SyncProgress();
    private SaplingBlockTree nearStateHeightForStartSync;

    public SyncManager() {
        GuardaApp.getAppComponent().inject(this);
    }

    public void startSync() {
        Timber.d("startSync inProgress=%b", inProgress);
        if (inProgress) return;
        progressSubject.onNext(true);
        syncProgress = new SyncProgress();
        progressPhase.onNext(syncProgress);
        inProgress = true;

        nearStateHeightForStartSync = nearStateHeightForStartSync();

        saplingParamsInit();
    }

    public void stopSync() {
        progressSubject.onNext(false);
        syncProgress.setProcessPhase(SYNCED_PHASE);
        progressPhase.onNext(syncProgress);
        inProgress = false;

        compositeDisposable.clear();
        Timber.d("stopSync inProgress=%b", inProgress);
    }

    public PublishSubject<Boolean> getProgressSubject() {
        return progressSubject;
    }
    public PublishSubject<SyncProgress> getSyncProgress() {
        return progressPhase;
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
                    if (saplingKeyInit()) {
                        getBlocks();
                    } else {
                        stopSync();
                    }
                }, (e) -> stopAndLogError("saplingParamsInit", e)));
    }

    private void getBlocks() {
        compositeDisposable.add(Observable
                .fromCallable(new CallLastBlock(dbManager, protoApi, nearStateHeightForStartSync.getHeight()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((blockSyncRange) -> {
                    Timber.d("getBlocks blockSyncRange=%s", blockSyncRange);

                    if (blockSyncRange.getLastFromServer() == 0) {
                        stopAndLogError("getBlocks", new Exception("can't get last block from litenode"));
                        return;
                    }

                    syncProgress.setFromBlock(blockSyncRange.getLastFromDb());
                    syncProgress.setToBlock(blockSyncRange.getLastFromServer());
                    syncProgress.setProcessPhase(DOWNLOAD_PHASE);
                    progressPhase.onNext(syncProgress);

                    //if blocks downloading starts from last db height it will rewrite the block with empty tree field
                    protoApi.pageNum = blockSyncRange.getLastFromDb();
                    endB = blockSyncRange.getLastFromServer();
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
                        syncProgress.setCurrentBlock(protoApi.pageNum);
                        syncProgress.setProcessPhase(DOWNLOAD_PHASE);
                        progressPhase.onNext(syncProgress);

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
        compositeDisposable.add(
                Observable.fromCallable(new CallBlocksForSync(dbManager, nearStateHeightForStartSync.getHeight()))
                        .flatMap(listBlocks -> {
                            if (!listBlocks.isEmpty()) {
                                syncProgress.setFromBlock(listBlocks.get(0).getHeight());
                                syncProgress.setToBlock(listBlocks.get(listBlocks.size() - 1).getHeight());
                                syncProgress.setCurrentBlock(listBlocks.get(0).getHeight());
                                syncProgress.setProcessPhase(SEARCH_PHASE);
                                progressPhase.onNext(syncProgress);
                            }

                            Timber.d("CallBlocksForSync finished=%s thread=%s", listBlocks.size(), Thread.currentThread());
                            return Observable.fromIterable(listBlocks);
                        })
                        .flatMap(
                                block -> {
                                    syncProgress.setCurrentBlock(block.getHeight());
                                    syncProgress.setProcessPhase(SEARCH_PHASE);
                                    progressPhase.onNext(syncProgress);

                                    Timber.d("block for witnessing: %s thread=%s", block.getHeight(), Thread.currentThread());
                                    return Observable.fromCallable(
                                            new CallFindWitnesses(
                                                    dbManager,
                                                    walletManager.getSaplingCustomFullKey(),
                                                    block,
                                                    nearStateHeightForStartSync));
                                }
                        )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                (blockRoom) -> {
                                    if (!blockRoom.isEmpty()) {
//                                        validateSaplingTree();
                                        Timber.d("CallFindWitnesses block blockRoom height=%d", blockRoom.get().getHeight());
                                    }
                                },
                                (e) -> stopAndLogError("CallFindWitnesses", e),
                                () -> {
                                    Timber.d("CallFindWitnesses completed");
//                                    validateSaplingTree();
                                    stopSync();
                                })
        );
    }

    private void validateSaplingTree() {
        compositeDisposable.add(Observable
                .fromCallable(new CallValidateSaplingTree(dbManager))
                .flatMap(it -> {
                            Timber.d("validateSaplingTree height=%d thread=%s", it.getHeight(), Thread.currentThread());
                            String raw = RequestorBtc.getRawBlockByHash(it.getHash()).blockingFirst().getRawblock();

                            if (raw == null || raw.isEmpty()) return Observable.just(true);

                            String root = new SaplingMerkleTree(it.getTree()).root();
                            Timber.d("lastBlockWithTree=%d root=%s raw=%s", it.getHeight(), root, raw);
                            boolean isContained = raw.toLowerCase().contains(root.toLowerCase());
                            return Observable.just(isContained);
                        }
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (isContained) -> {
                            Timber.d("validateSaplingTree finished isContained(right)=%s", isContained);
                            if (isContained) {

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
                        .fromCallable(new CallRevertLastBlock(dbManager))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                (res) -> {
                                    syncProgress.setProcessPhase(SYNCED_PHASE);
                                    progressPhase.onNext(syncProgress);
                                    Timber.d("revertLastBlocks (all blocks dropped) completed=%s", res);
                                },
                                (e) -> stopAndLogError("revertLastBlocks", e)
                        )
        );
    }

    private boolean saplingKeyInit() {
        if (walletManager.getSaplingCustomFullKey() == null) {
            byte[] p = RustAPI.dPart(walletManager.getPrivateKey().getBytes());
            if (p.length != 235) {
                Timber.e("saplingKeyInit wrong key length=%d", p.length);
                return false;
            }
            SaplingCustomFullKey k = new SaplingCustomFullKey(p);
            walletManager.setSaplingCustomFullKey(k);
            Timber.d("saplingKeyInit inited");
        }
        return true;
    }

    private void stopAndLogError(String method, Throwable t) {
        stopSync();
        Timber.e("%s error=%s", method, t.getMessage());
    }

    /**
     * Find nearest tree state in list of states {@code treeStates} from which we can to start
     * syncing wallet.
     */
    private SaplingBlockTree nearStateHeightForStartSync() {
        long firsSyncBlockHeight = walletManager.getCreateHeight();
        if (firsSyncBlockHeight == 0) {
            walletManager.setCreateHeight(FIRST_BLOCK_TO_SYNC_MAINNET);
            firsSyncBlockHeight = FIRST_BLOCK_TO_SYNC_MAINNET;
        }

        SaplingBlockTree nearestTreeStateHeight = rawResourceRepository.treeStates.get(0);
        for (SaplingBlockTree saplingBlockTree : rawResourceRepository.treeStates) {
            long height = saplingBlockTree.getHeight();
            if (height > nearestTreeStateHeight.getHeight() &&
                    height <= firsSyncBlockHeight) {
                nearestTreeStateHeight = saplingBlockTree;
            }
        }

        return nearestTreeStateHeight;
    }

}
