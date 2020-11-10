package com.guarda.ethereum.sapling.rxcall;

import com.guarda.ethereum.ZCashException;
import com.guarda.ethereum.globals.Optional;
import com.guarda.ethereum.globals.TypeConvert;
import com.guarda.ethereum.models.items.SaplingBlockTree;
import com.guarda.ethereum.sapling.db.DbManager;
import com.guarda.ethereum.sapling.db.model.BlockRoom;
import com.guarda.ethereum.sapling.db.model.ReceivedNotesRoom;
import com.guarda.ethereum.sapling.db.model.SaplingWitnessesRoom;
import com.guarda.ethereum.sapling.db.model.TxInRoom;
import com.guarda.ethereum.sapling.db.model.TxOutRoom;
import com.guarda.ethereum.sapling.db.model.TxRoom;
import com.guarda.ethereum.sapling.key.SaplingCustomFullKey;
import com.guarda.ethereum.sapling.key.SaplingFullViewingKey;
import com.guarda.ethereum.sapling.note.SaplingNote;
import com.guarda.ethereum.sapling.note.SaplingNotePlaintext;
import com.guarda.ethereum.sapling.tree.IncrementalWitness;
import com.guarda.ethereum.sapling.tree.SaplingMerkleTree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import timber.log.Timber;

import static com.guarda.ethereum.crypto.Utils.bytesToHex;
import static com.guarda.ethereum.crypto.Utils.revHex;
import static com.guarda.ethereum.sapling.note.SaplingNotePlaintext.tryNoteDecrypt;

public class CallFindWitnesses implements Callable<Optional<BlockRoom>> {

    private DbManager dbManager;
    private SaplingCustomFullKey saplingKey;
    private BlockRoom blockRoom;

//    private Long defaultStartHeight = 551912L;
    private Long defaultStartHeight = 900000L;
//    private Long defaultStartHeight = 620000L; //testnet
    private Long startScanBlocksHeight = defaultStartHeight;
    private SaplingBlockTree nearStateHeightForStartSync;

    public CallFindWitnesses(DbManager dbManager, SaplingCustomFullKey saplingKey, BlockRoom blockRoom, SaplingBlockTree nearStateHeightForStartSync) {
        this.dbManager = dbManager;
        this.saplingKey = saplingKey;
        this.blockRoom = blockRoom;
        this.nearStateHeightForStartSync = nearStateHeightForStartSync;
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
            saplingTree = new SaplingMerkleTree(nearStateHeightForStartSync.getTree());
            dbManager.getAppDb().getBlockDao().setTreeByHeight(saplingTree.serialize(), nearStateHeightForStartSync.getHeight());
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
        BlockRoom previousBlock = dbManager.getAppDb().getBlockDao().previousBlock(blockRoom.getHeight());
        if (previousBlock != null) {
            int blockTxsByInputNote = dbManager.getAppDb().getReceivedNotesDao().blockTxsByInputNote(previousBlock.getHash());
            int blockTxsByOutputNote = dbManager.getAppDb().getReceivedNotesDao().blockTxsByOutputNote(previousBlock.getHash());
            if (blockTxsByInputNote + blockTxsByOutputNote == 0) {
                dbManager.getAppDb().getBlockDao().deleteByHeight(previousBlock.getHeight());
            } else {
                Timber.d("previous block has transactions previous height=%d", previousBlock.getHeight());
            }
        }

        if (wtxs.isEmpty()) {
            return new Optional<>(null);
        } else {
            return new Optional<>(blockRoom);
        }
    }

}
