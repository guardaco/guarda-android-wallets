package com.guarda.zcash.sapling.rxcall;

import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.db.model.BlockRoom;

import java.util.List;
import java.util.concurrent.Callable;

import timber.log.Timber;


public class CallBlocksForSync implements Callable<List<BlockRoom>> {

    private DbManager dbManager;

//    private Long startScanBlocksHeight = 620000L; //testnet
    private Long startScanBlocksHeight = 551912L;

    public CallBlocksForSync(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public List<BlockRoom> call() throws Exception {
        Timber.d("started");

        //get last with stored tree state
        BlockRoom lastBlockWithTree = dbManager.getAppDb().getBlockDao().getLatestBlockWithTree();
        if (lastBlockWithTree != null && !lastBlockWithTree.getTree().isEmpty()) {
            startScanBlocksHeight = lastBlockWithTree.getHeight();
        }

        Timber.d("startScanBlocksHeight=%d", startScanBlocksHeight);

        //blocks after last block with tree state (excluded)
        List<BlockRoom> blocks = dbManager.getAppDb().getBlockDao().getBlocksOrderedFromHeight(startScanBlocksHeight);
        return blocks;
    }
}
