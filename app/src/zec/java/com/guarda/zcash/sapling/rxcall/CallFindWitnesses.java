package com.guarda.zcash.sapling.rxcall;

import com.guarda.ethereum.BuildConfig;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.zcash.ZCashException;
import com.guarda.zcash.crypto.Utils;
import com.guarda.zcash.globals.TypeConvert;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.db.model.BlockRoom;
import com.guarda.zcash.sapling.db.model.ReceivedNotesRoom;
import com.guarda.zcash.sapling.db.model.SaplingWitnessesRoom;
import com.guarda.zcash.sapling.db.model.TxInRoom;
import com.guarda.zcash.sapling.db.model.TxOutRoom;
import com.guarda.zcash.sapling.db.model.TxRoom;
import com.guarda.zcash.sapling.key.SaplingCustomFullKey;
import com.guarda.zcash.sapling.key.SaplingFullViewingKey;
import com.guarda.zcash.sapling.note.SaplingNote;
import com.guarda.zcash.sapling.note.SaplingNotePlaintext;
import com.guarda.zcash.sapling.tree.IncrementalWitness;
import com.guarda.zcash.sapling.tree.SaplingMerkleTree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import timber.log.Timber;

import static com.guarda.zcash.RustAPI.newAk;
import static com.guarda.zcash.RustAPI.newIvk;
import static com.guarda.zcash.RustAPI.newNk;
import static com.guarda.zcash.RustAPI.newOvk;
import static com.guarda.zcash.crypto.Utils.bytesToHex;
import static com.guarda.zcash.crypto.Utils.revHex;
import static com.guarda.zcash.crypto.Utils.reverseByteArray;
import static com.guarda.zcash.sapling.note.SaplingNotePlaintext.tryNoteDecrypt;

public class CallFindWitnesses implements Callable<Boolean> {

    private byte[] revNewIvk;
    private byte[] revNewAk;
    private byte[] revNewNk;
    private byte[] revNewOvk;

    private DbManager dbManager;
    private SaplingCustomFullKey saplingKey;
    private Long startHeight = 490132L;

    public CallFindWitnesses(DbManager dbManager, SaplingCustomFullKey saplingKey) {
        this.dbManager = dbManager;
        this.saplingKey = saplingKey;
//        revNewIvk = reverseByteArray(newIvk);
//        revNewAk = reverseByteArray(newAk);
//        revNewNk = reverseByteArray(newNk);
//        revNewOvk = reverseByteArray(newOvk);
    }

    @Override
    public Boolean call() throws Exception {
        SaplingMerkleTree saplingTree = new SaplingMerkleTree();
        List<SaplingWitnessesRoom> existingWitnesses = dbManager.getAppDb().getSaplingWitnessesDao().getAllWitnesses();

//        Long lastHeight = dbManager.getAppDb().getSaplingWitnessesDao().getLastHeight();
//        Timber.d("last witness height lastHeight=%d", lastHeight);
//        if (lastHeight != null && lastHeight > startHeight) startHeight = lastHeight;

        List<BlockRoom> blocks = dbManager.getAppDb().getBlockDao().getAllBlocksOrdered();

        for (BlockRoom br : blocks) {

            if (br.getHeight() % 1000 == 0) Timber.d("height=%d", br.getHeight());

            if (br.getHeight() < startHeight) continue;

            // SCAN BLOCK
            // map for saving wintesses by note commitment hex for current block
            Map<String, IncrementalWitness> wtxs = new HashMap<>();
            List<TxRoom> blockTxs = dbManager.getAppDb().getTxDao().getAllBlockTxs(br.getHash());
            List<String> nullifiers = dbManager.getAppDb().getReceivedNotesDao().getAllNf();
            for (TxRoom tx : blockTxs) {

                //check nullifiers and update spend status for the note
                List<TxInRoom> spends = dbManager.getAppDb().getTxDao().getAllTxInputs(tx.getHash());
                for (TxInRoom in : spends) {
                    if (nullifiers.contains(in.getNf())) {
                        dbManager.getAppDb().getReceivedNotesDao().spentNoteByNf(in.getNf());
                    }
                }

                List<TxOutRoom> outs = dbManager.getAppDb().getTxDao().getAllTxOutputs(tx.getHash());
                for (int i = 0; i < outs.size(); i++) {
                    TxOutRoom out = outs.get(i);
                    for (SaplingWitnessesRoom ew : existingWitnesses) {
                        IncrementalWitness iw = IncrementalWitness.fromJson(ew.getWitness());
                        try {
                            iw.append(Utils.revHex(out.getCmu()));
                        } catch (ZCashException e) {
                            Timber.e("getWintesses existingWitnesses.entrySet e=%s", e.getMessage());
                        }
                        ew.setWitness(IncrementalWitness.toJson(iw));
                        ew.setWitnessHeight(br.getHeight());
                    }
                    //append every previous wintess
                    //like blockWitnesses in original rust code
                    for (Map.Entry<String, IncrementalWitness> wx : wtxs.entrySet()) {
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

                    //FIXME: delete after tests
                    if (br.getHeight() < 499642) continue;
                    SaplingNotePlaintext snp = tryNoteDecrypt(out, saplingKey);

                    //skip if it's not our note
                    if (snp == null) continue;

                    IncrementalWitness iw = saplingTree.witness();
                    int position = iw.position();
                    SaplingNote sNote = snp.getSaplingNote();
                    String nf = sNote.nullifier(
                            new SaplingFullViewingKey(
                                    revHex(bytesToHex(saplingKey.getAk())),
                                    revHex(bytesToHex(saplingKey.getNk())),
                                    revHex(bytesToHex(saplingKey.getOvk()))),
                            position);
                    dbManager.getAppDb().getReceivedNotesDao().insertAll(new ReceivedNotesRoom(out.getCmu(), null, TypeConvert.bytesToLong(snp.vbytes), nf));
                    wtxs.put(out.getCmu(), iw);
                    wtxs.put(out.getCmu(), saplingTree.witness());
                }
            }

            if (br.getHeight() == startHeight) {
                saplingTree = new SaplingMerkleTree(treeOnHeight490132);
                try {
                    Timber.d("saplingTree.serialize() at 490132 root=%s", saplingTree.root());
                } catch (ZCashException e) {
                    Timber.d("saplingTree.serialize() at 490132 errr=%s", e.getMessage());
                }
                Timber.d("saplingTree.serialize() at 490132=%s", saplingTree.serialize());
            }

            //save updated witnesses to DB
            for (Map.Entry<String, IncrementalWitness> wx : wtxs.entrySet()) {
                SaplingWitnessesRoom sw = new SaplingWitnessesRoom(wx.getKey(), IncrementalWitness.toJson(wx.getValue()), br.getHeight());
                existingWitnesses.add(sw);
                Timber.d("iw root=%s at height=%d", wx.getValue().root(), br.getHeight());
            }

        }

        dbManager.getAppDb().getSaplingWitnessesDao().insertList(existingWitnesses);

        Timber.d("blocks scanning completed");

        return true;
    }

    private static final String treeOnHeight490132 = "013db35afe38f3d1baa4a4a03b1b0fd82db983c3499a2ac5c118cad818731a1b070106f60ba184d3b23ad6897b44f76cf306e3f669d716107a865bfcdc5e336f5e70100101f7d8ff2c22637d1976629b1a3d64b6c187292fdb8c47b4ccd95dbeab0b50020139677f185c05fb21b14c8e222ad70d6d19cf3798bff514a277beecac15dbc570010b5d66215a6696a766db75944dfa69f8addc45dce74725024b3bdc718ab97b3401f13c3d311966ff2736acb7bb09bd9bfb341f8c618c7a17116eee6b87db1d7d3500014614efb00d163f7a8ea6fb17e6d6ab460be947d6ffd4e8c86d9a47a3b3875c33000191323164108492e992dcfc32d3b647b21a4cc598c9925fccbad39c0b23d1de1a01159ce3a35ec35d9c36a8a0cf419b95828d26dced64b5036be9a09e5eac3fa908000000000000011f8322ef806eb2430dc4a7a41c1b344bea5be946efc7b4349c1c9edb14ff9d39";
    private static final String treeOnHeight503098 = "01ddc7b052355b383224bdc33251a84c31a19cc7deefff4791e05ab52f5e39474c001000000001987c53ebd9a220b4a650e25f8fbd1cc8b9e33fc228cb37b27557a1714fd00a65000147fd44a22dbf3f5ceca5f60af2962ff3de3c8c6e2ad03f2b1f6817a66a5d036f0000000001b98b14cab05247195b3b3be3dd8639bae99a0dd10bed1282ac25b62a134afd7200000000011f8322ef806eb2430dc4a7a41c1b344bea5be946efc7b4349c1c9edb14ff9d39";
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
