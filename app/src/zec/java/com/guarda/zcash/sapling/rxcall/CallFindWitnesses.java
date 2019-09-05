package com.guarda.zcash.sapling.rxcall;

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

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import timber.log.Timber;

import static com.guarda.zcash.crypto.Utils.bytesToHex;
import static com.guarda.zcash.crypto.Utils.revHex;
import static com.guarda.zcash.sapling.note.SaplingNotePlaintext.tryNoteDecrypt;

public class CallFindWitnesses implements Callable<Boolean> {

    private DbManager dbManager;
    private SaplingCustomFullKey saplingKey;

    private Long defaultStartHeight = 551912L;
    private Long startScanBlocksHeight = defaultStartHeight;

    public CallFindWitnesses(DbManager dbManager, SaplingCustomFullKey saplingKey) {
        this.dbManager = dbManager;
        this.saplingKey = saplingKey;
    }

    @Override
    public Boolean call() throws Exception {
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
        }

        Timber.d("startScanBlocksHeight=%d", startScanBlocksHeight);

        //blocks after last block with tree state (excluded)
        List<BlockRoom> blocks = dbManager.getAppDb().getBlockDao().getBlocksOrderedFromHeight(startScanBlocksHeight);

        for (BlockRoom br : blocks) {

            if (br.getHeight() % 1000 == 0) Timber.d("height=%d", br.getHeight());

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

                    if (br.getHeight() < 551937) continue;
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
                                    new String(snp.getMemobytes(), Charset.forName("UTF-16BE"))
                            )
                    );

                    wtxs.put(out.getCmu(), iw);
                    wtxs.put(out.getCmu(), saplingTree.witness());
                }
            }

            //save updated witnesses to DB
            for (Map.Entry<String, IncrementalWitness> wx : wtxs.entrySet()) {
                SaplingWitnessesRoom sw = new SaplingWitnessesRoom(wx.getKey(), IncrementalWitness.toJson(wx.getValue()), br.getHeight());
                existingWitnesses.add(sw);
                Timber.d("iw root=%s at height=%d", wx.getValue().root(), br.getHeight());
            }

        }

        dbManager.getAppDb().getSaplingWitnessesDao().insertList(existingWitnesses);

        //storing sapling tree state
        if (blocks.size() > 0) {
            BlockRoom lastBlock = blocks.get(blocks.size() - 1);
            lastBlock.setTree(saplingTree.serialize());
            dbManager.getAppDb().getBlockDao().insertAll(lastBlock);
            String lastRoot = saplingTree.root();
            Timber.d("save tree lastBlock=%d tree=%s root=%s", lastBlock.getHeight(), saplingTree.serialize(), lastRoot);
        }

        Timber.d("blocks scanning completed");

        return true;
    }

    //mainnet
    private static final String treeOnHeight551912main = "01d192b19aef282b71be4330345048b2e2c11ad7e7919a989eb616b9fe9f6ab2580170b5e8deedb2780affc0a81f27d7c46dc73ad618368c9aad820b079cb558ce580f000188349ea325f12f3f9bc64a17965da7756244a2742f4547928f3fe0f38ca956270126729ec7710ba839684f2522da582c2aa3dc88fc7c6c202ea47f2df40c1e5f0d000000019ec91f70592f5010f156684754b9c584d0caedda479b16e9629e1761eed46c21013a85c25ee7bee68f899b713868e2923ea16ce53b6d8ddb9ecdb4c0f7cc7f4047018fbd5b7f31ba10a344ed1070e5ac858b5fbf137f6609ab40dbf6699efc2ca42b01175555acb7f78f4a9720549438cc73bba265ce4737b9541307a71a5ec40e5c58012557fe4d4cd152a6c7bc0b99fc93c6a84fff74d91962a424dfe8c78184465c3100013457c7289a51a355b01114a154e1666f5a83dd19257db331ccef1ec7a72caa420143359e122f3e93ad3b9dbf673c12c1551a31efde6e4195d98e571e0db70dee47014bc8cc7ccc3f42d408bd00d811eafff7cd23b0d0d656ef383ac81a7789d0011a";

    //testnet
    private static final String treeOnHeight490132test = "013db35afe38f3d1baa4a4a03b1b0fd82db983c3499a2ac5c118cad818731a1b070106f60ba184d3b23ad6897b44f76cf306e3f669d716107a865bfcdc5e336f5e70100101f7d8ff2c22637d1976629b1a3d64b6c187292fdb8c47b4ccd95dbeab0b50020139677f185c05fb21b14c8e222ad70d6d19cf3798bff514a277beecac15dbc570010b5d66215a6696a766db75944dfa69f8addc45dce74725024b3bdc718ab97b3401f13c3d311966ff2736acb7bb09bd9bfb341f8c618c7a17116eee6b87db1d7d3500014614efb00d163f7a8ea6fb17e6d6ab460be947d6ffd4e8c86d9a47a3b3875c33000191323164108492e992dcfc32d3b647b21a4cc598c9925fccbad39c0b23d1de1a01159ce3a35ec35d9c36a8a0cf419b95828d26dced64b5036be9a09e5eac3fa908000000000000011f8322ef806eb2430dc4a7a41c1b344bea5be946efc7b4349c1c9edb14ff9d39";
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
