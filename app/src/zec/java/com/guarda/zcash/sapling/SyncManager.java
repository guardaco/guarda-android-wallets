package com.guarda.zcash.sapling;

import com.guarda.ethereum.GuardaApp;
import com.guarda.zcash.ZCashException;
import com.guarda.zcash.crypto.Utils;
import com.guarda.zcash.globals.TypeConvert;
import com.guarda.zcash.sapling.api.ProtoApi;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.db.model.BlockRoom;
import com.guarda.zcash.sapling.db.model.ReceivedNotesRoom;
import com.guarda.zcash.sapling.db.model.TxOutRoom;
import com.guarda.zcash.sapling.db.model.TxRoom;
import com.guarda.zcash.sapling.model.OutputDescResp;
import com.guarda.zcash.sapling.model.WalletTx;
import com.guarda.zcash.sapling.note.SaplingNoteData;
import com.guarda.zcash.sapling.note.SaplingNotePlaintext;
import com.guarda.zcash.sapling.note.SaplingOutPoint;
import com.guarda.zcash.sapling.rxcall.CallBlockRange;
import com.guarda.zcash.sapling.rxcall.CallLastBlock;
import com.guarda.zcash.sapling.tree.IncrementalWitness;
import com.guarda.zcash.sapling.tree.MerklePath;
import com.guarda.zcash.sapling.tree.SaplingMerkleTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import autodagger.AutoInjector;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.guarda.zcash.RustAPI.newIvk;
import static com.guarda.zcash.crypto.Utils.reverseByteArray;

@AutoInjector(GuardaApp.class)
public class SyncManager {

    private boolean inProgress;
    private Disposable blocksDisposable;
    private ProtoApi protoApi;
    private long endB = 437489;
    private byte[] revNewIvk;

    Map<String, WalletTx> mapWallet = new HashMap<>();
    private MerklePath mp;
    private String exRoot;

    @Inject
    DbManager dbManager;

    public SyncManager() {
        GuardaApp.getAppComponent().inject(this);
        protoApi = new ProtoApi();

        revNewIvk = reverseByteArray(newIvk);
    }

    public void startSync() {
        if (inProgress) return;

        inProgress = true;

        getBlocks();
    }

    public void stopSync() {
        inProgress = false;

        blocksDisposable.dispose();
    }

    private void getBlocks() {
        blocksDisposable = Observable
                .fromCallable(new CallLastBlock())
                .subscribeOn(Schedulers.io())
                .subscribe((latest) -> {
                    Timber.d("getBlocks latest=%d", latest);
                    Observable.fromCallable(new CallBlockRange(protoApi, endB))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((t) -> {
                                Timber.d("accept thread=%s", Thread.currentThread().getName());
                                if (!t) {
                                    Timber.d("paginator.accept(items) null");
                                }

                                if (protoApi.pageNum <= endB) {
                                    getBlocks();
                                } else {
                                    //blocks downloading ended
                                    Timber.d("protoApi.pageNum >= endB");

                                    //TODO: uncomment after tests
//                        getWintesses();
                                }
                            });
                });

    }

    private void tb(long latest) {

    }

    private void getWintesses() {
        SaplingMerkleTree saplingTree = new SaplingMerkleTree();
        Map<String, IncrementalWitness> existingWitnesses = new HashMap<>();

        List<BlockRoom> blocks = dbManager.getAppDb().getBlockDao().getAllBlocksOrdered();

        for (BlockRoom br : blocks) {
            if (br.getHeight() % 1000 == 0) Timber.d("height=%d", br.getHeight());

            if (br.getHeight() < 422044) continue;

            if (br.getHeight() == 422044) {
                saplingTree = new SaplingMerkleTree(treeOnHeight421720);
                try {
                    Timber.d("saplingTree.serialize() at 422044 root=%s", saplingTree.root());
                } catch (ZCashException e) {
                    Timber.d("saplingTree.serialize() at 422044 errr=%s", e.getMessage());
                }
                Timber.d("saplingTree.serialize() at 422044=%s", saplingTree.serialize());
            }

            if (br.getHeight() == 476226) {
                String h = "6fe7382ce35126c6584b8c16325ba9da6641867eb2e458eaaa1a818d6e360286";
                mapWallet.put(h, getFilledWalletTx(new SaplingOutPoint(h, 0)));
            }

            // SCAN BLOCK
            // map for saving wintesses by note commitment hex for current block
            Map<String, IncrementalWitness> wtxs = new HashMap<>();
            List<TxRoom> blockTxs = dbManager.getAppDb().getTxDao().getAllBlockTxs(br.getHash());
            for (TxRoom tx : blockTxs) {
                //TODO: check nf for spends

                List<TxOutRoom> outs = dbManager.getAppDb().getTxDao().getAllTxOutputs(tx.getHash());
                for (int i = 0; i < outs.size(); i++) {
                    TxOutRoom out = outs.get(i);
                    for(Map.Entry<String, IncrementalWitness> ew : existingWitnesses.entrySet()) {
                        IncrementalWitness iw = ew.getValue();
                        try {
                            iw.append(Utils.revHex(out.getCmu()));
                        } catch (ZCashException e) {
                            Timber.e("getWintesses existingWitnesses.entrySet e=%s", e.getMessage());
                        }
                    }
                    //append every previous wintess
                    //like blockWitnesses in original rust code
                    for(Map.Entry<String, IncrementalWitness> wx : wtxs.entrySet()) {
                        IncrementalWitness iw = wx.getValue();
                        try {
                            iw.append(Utils.revHex(out.getCmu()));
                        } catch (ZCashException e) {
                            Timber.e("getWintesses iw.append(Utils.revHex(out.getCmu())) e=%s", e.getMessage());
                        }
                    }
                    //append to main tree
                    try {
                        saplingTree.append(Utils.revHex(out.getCmu()));
                    } catch (ZCashException zce) {
                        zce.printStackTrace();
                        Timber.e("getWintesses saplingTree.append(out.getCmu()); err=%s", zce.getMessage());
                    }

                    SaplingNotePlaintext snp = tryNoteDecrypt(out);
                    //skip if it's not our note
                    if (snp == null) continue;

                    dbManager.getAppDb().getReceivedNotesDao().insertAll(new ReceivedNotesRoom(out.getCmu(), null, TypeConvert.bytesToLong(snp.vbytes)));
                    wtxs.put(Utils.revHex(out.getCmu()), saplingTree.witness());
                }
            }

            existingWitnesses.putAll(wtxs);
        }


        try {
            //append every previous wintess
            for(Map.Entry<String, IncrementalWitness> ew : existingWitnesses.entrySet()) {
                IncrementalWitness iw = ew.getValue();
                exRoot = iw.root();
                mp = iw.path();
                Timber.d("getWintesses existingWitnesses roots e=%s", exRoot);
            }
            Timber.d("check tree root=%s", saplingTree.root());
        } catch (ZCashException e) {
            e.printStackTrace();
            Timber.e("check roots e=%s", e.getMessage());
        }
        Timber.d("scan ended");
    }

    private SaplingNotePlaintext tryNoteDecrypt(TxOutRoom output) {
        try {
            return SaplingNotePlaintext.decrypt(
                    output.getCiphertext(),
                    Utils.bytesToHex(revNewIvk),
                    output.getEpk(),
                    output.getCmu());
        } catch (ZCashException e) {
            return null;
        }
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

    private static final String treeOnHeight421720 = "01" + //left
            "5495a30aef9e18b9c774df6a9fcd583748c8bba1a6348e70f59bc9f0c2bc673b" +
            "00" + //right
            "0f" + //parents size is 15
            "00000000" + // 4 empty parents
            "01" +
            "8054b75173b577dc36f2c80dfc41f83d6716557597f74ec54436df32d4466d57" +
            "00" +
            "01" +
            "20f1825067a52ca973b07431199d5866a0d46ef231d08aa2f544665936d5b452" +
            "01" +
            "68d782e3d028131f59e9296c75de5a101898c5e53108e45baa223c608d6c3d3d" +
            "01" +
            "fb0a8d465b57c15d793c742df9470b116ddf06bd30d42123fdb7becef1fd6364" +
            "00" +
            "01" +
            "a86b141bdb55fd5f5b2e880ea4e07caf2bbf1ac7b52a9f504977913068a91727" +
            "00" +
            "01" +
            "dd960b6c11b157d1626f0768ec099af9385aea3f31c91111a8c5b899ffb99e6b" +
            "01" +
            "92acd61b1853311b0bf166057ca433e231c93ab5988844a09a91c113ebc58e18" +
            "01" +
            "9fbfd76ad6d98cafa0174391546e7022afe62e870e20e16d57c4c419a5c2bb69";
}
