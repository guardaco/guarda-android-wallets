package com.guarda.zcash.sapling.rxcall;

import com.guarda.zcash.ZCashException;
import com.guarda.zcash.globals.Optional;
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

import static com.guarda.zcash.crypto.Utils.bytesToHex;
import static com.guarda.zcash.crypto.Utils.revHex;
import static com.guarda.zcash.sapling.note.SaplingNotePlaintext.tryNoteDecrypt;

public class CallFindWitnesses implements Callable<Optional<BlockRoom>> {

    private DbManager dbManager;
    private SaplingCustomFullKey saplingKey;
    private BlockRoom blockRoom;

    private Long defaultStartHeight = 551912L;
//    private Long defaultStartHeight = 620000L; //testnet
    private Long startScanBlocksHeight = defaultStartHeight;

    public CallFindWitnesses(DbManager dbManager, SaplingCustomFullKey saplingKey, BlockRoom blockRoom) {
        this.dbManager = dbManager;
        this.saplingKey = saplingKey;
        this.blockRoom = blockRoom;
    }

    @Override
    public Optional<BlockRoom> call() throws Exception {
        Timber.d("started");
        SaplingMerkleTree saplingTree;
        List<SaplingWitnessesRoom> existingWitnesses = dbManager.getAppDb().getSaplingWitnessesDao().getAllWitnesses();

        //get last with stored tree state
        BlockRoom lastBlockWithTree = dbManager.getAppDb().getBlockDao().getLatestBlockWithTree();
        if (lastBlockWithTree != null && !lastBlockWithTree.getTree().isEmpty()) {
            startScanBlocksHeight = lastBlockWithTree.getHeight();
            saplingTree = new SaplingMerkleTree(lastBlockWithTree.getTree());
        } else {
            saplingTree = new SaplingMerkleTree(treeOnHeight551912main);
            dbManager.getAppDb().getBlockDao().setTreeByHeight(saplingTree.serialize(), defaultStartHeight);
        }

        Timber.d("startScanBlocksHeight=%d", startScanBlocksHeight);

        // SCAN BLOCK
        // map for saving wintesses by note commitment hex for current block
        Map<String, IncrementalWitness> wtxs = new HashMap<>();
        List<TxRoom> blockTxs = dbManager.getAppDb().getTxDao().getAllBlockTxs(blockRoom.getHash());
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
                        iw.append(revHex(out.getCmu()));
                    } catch (ZCashException e) {
                        Timber.e("getWintesses existingWitnesses.entrySet e=%s", e.getMessage());
                    }
                    ew.setWitness(IncrementalWitness.toJson(iw));
                    ew.setWitnessHeight(blockRoom.getHeight());
                }
                //append every previous wintess
                //like blockWitnesses in original rust code
                for (Map.Entry<String, IncrementalWitness> wx : wtxs.entrySet()) {
                    IncrementalWitness iw = wx.getValue();
                    try {
                        iw.append(revHex(out.getCmu()));
                    } catch (ZCashException e) {
                        Timber.e("getWintesses iw.append(Utils.revHex(out.getCmu())) e=%s", e.getMessage());
                    }
                }
                //append to main tree
                try {
                    saplingTree.append(revHex(out.getCmu()));
                } catch (ZCashException zce) {
                    zce.printStackTrace();
                    Timber.e("getWintesses saplingTree.append(out.getCmu()); err=%s", zce.getMessage());
                }

                if (blockRoom.getHeight() < 551937) continue;
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
                //add new received note
                dbManager.getAppDb().getReceivedNotesDao().insertAll(
                        new ReceivedNotesRoom(
                                out.getCmu(),
                                null,
                                TypeConvert.bytesToLong(snp.vbytes),
                                nf,
                                new String(snp.getMemobytes())
                        )
                );

                wtxs.put(out.getCmu(), saplingTree.witness());
            }
        }

        //save updated witnesses to DB
        for (Map.Entry<String, IncrementalWitness> wx : wtxs.entrySet()) {
            SaplingWitnessesRoom sw = new SaplingWitnessesRoom(wx.getKey(), IncrementalWitness.toJson(wx.getValue()), blockRoom.getHeight());
            existingWitnesses.add(sw);
            Timber.d("iw root=%s at height=%d", wx.getValue().root(), blockRoom.getHeight());
        }
        dbManager.getAppDb().getSaplingWitnessesDao().insertList(existingWitnesses);

        blockRoom.setTree(saplingTree.serialize());
        dbManager.getAppDb().getBlockDao().update(blockRoom);

        Timber.d("save tree lastBlock=%d root=%s tree=%s", blockRoom.getHeight(), saplingTree.root(), saplingTree.serialize());

        // Remove previous block if it isn't contain wallet's transactions
        long previousHeight = blockRoom.getHeight() - 1;
        int witnessesByBlock = dbManager.getAppDb().getSaplingWitnessesDao().countByBlockHeight(previousHeight);
        if (witnessesByBlock == 0) {
            dbManager.getAppDb().getBlockDao().deleteByHeight(previousHeight);
        } else {
            Timber.d("previous block has transactions previousHeight=%d", previousHeight);
        }

        if (wtxs.isEmpty()) {
            return new Optional<>(null);
        } else {
            return new Optional<>(blockRoom);
        }
    }

    //mainnet
    private static final String treeOnHeight551912main = "01d192b19aef282b71be4330345048b2e2c11ad7e7919a989eb616b9fe9f6ab2580170b5e8deedb2780affc0a81f27d7c46dc73ad618368c9aad820b079cb558ce580f000188349ea325f12f3f9bc64a17965da7756244a2742f4547928f3fe0f38ca956270126729ec7710ba839684f2522da582c2aa3dc88fc7c6c202ea47f2df40c1e5f0d000000019ec91f70592f5010f156684754b9c584d0caedda479b16e9629e1761eed46c21013a85c25ee7bee68f899b713868e2923ea16ce53b6d8ddb9ecdb4c0f7cc7f4047018fbd5b7f31ba10a344ed1070e5ac858b5fbf137f6609ab40dbf6699efc2ca42b01175555acb7f78f4a9720549438cc73bba265ce4737b9541307a71a5ec40e5c58012557fe4d4cd152a6c7bc0b99fc93c6a84fff74d91962a424dfe8c78184465c3100013457c7289a51a355b01114a154e1666f5a83dd19257db331ccef1ec7a72caa420143359e122f3e93ad3b9dbf673c12c1551a31efde6e4195d98e571e0db70dee47014bc8cc7ccc3f42d408bd00d811eafff7cd23b0d0d656ef383ac81a7789d0011a";

    //testnet
    private static final String treeOnHeight620000test = "0170cf036ea1ea3c6e08432e18b6a372ca0b8b83671cc13ab0cf9e28c182f6c36f00100000013f3fc2c16ac4780f1c472ca65534ab08911f325a9edde5ea7f24364b47c9a95300017621b12e518cbbbdb7511ab423e0bddda412ed61ed3cff5be2140de65d6a0069010576153a5a2098812e7a028c37c3398e186f398c9b07bc199784ab97e5535c3e0000019a6ce2f0f7dbb2de493a315abf62d8ca96ccc701f116b6ddfae33870a2183d3c01c9d3564eff54ebc328eab2e4f1150c3637f4f47516f879a0cfebdf49fe7b1d5201c104705fac60a85596010e41260d07f3a64f38f37a112eaef41cd9d736edc5270145e3d4899fcd7f0f1236ae31eafb3f4b65ad6b11a17eae1729cec09bd3afa01a000000011f8322ef806eb2430dc4a7a41c1b344bea5be946efc7b4349c1c9edb14ff9d39";

}
