package com.guarda.zcash.sapling;

import android.content.Context;

import com.guarda.ethereum.GuardaApp;
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
//    private ProtoApi protoApi;
    private long endB = 437489;

    Map<String, WalletTx> mapWallet = new HashMap<>();
    private MerklePath mp;
    private String exRoot;

    @Inject
    DbManager dbManager;

//    @Inject
    ProtoApi protoApi;

    public SyncManager() {
        GuardaApp.getAppComponent().inject(this);
//        this.dbManager = new DbManager(context);
        protoApi = new ProtoApi();
//        protoApi = new ProtoApi();
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

    private void getBlocks() {
        compositeDisposable.add(Observable
                .fromCallable(new CallLastBlock(dbManager))
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
                .fromCallable(new CallFindWitnesses(dbManager))
                .subscribeOn(Schedulers.io())
                .subscribe((res) -> {
                    Timber.d("getWintesses latest=%s", res);
                    stopSync();
                }));
    }

    private WalletTx getFilledWalletTx(SaplingOutPoint op) {
        WalletTx w = new WalletTx(op, new SaplingNoteData());
        List<TxOutRoom> outs = dbManager.getAppDb().getTxDao().getAllTxOutputs(op.hashIn);
        w.setOutputDescs(getListOutDescs(outs));
        return w;
    }

    private List<OutputDescResp> getListOutDescs(List<TxOutRoom> outs) {
        List<OutputDescResp> list = new ArrayList<>();
        for (TxOutRoom o : outs) {
            list.add(new OutputDescResp(o.getCmu(), o.getEpk(), o.getCiphertext()));
        }
        return list;
    }
}
