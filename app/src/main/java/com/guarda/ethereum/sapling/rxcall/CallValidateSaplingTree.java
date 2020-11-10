package com.guarda.ethereum.sapling.rxcall;

import com.guarda.ethereum.sapling.db.DbManager;
import com.guarda.ethereum.sapling.db.model.BlockRoom;

import java.util.concurrent.Callable;

import timber.log.Timber;

public class CallValidateSaplingTree implements Callable<BlockRoom> {

    private DbManager dbManager;

    public CallValidateSaplingTree(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public BlockRoom call() throws Exception {
        Timber.d("started");

        //get last with stored tree state
        BlockRoom lastBlockWithTree = dbManager.getAppDb().getBlockDao().getLatestBlockWithTree();
        if (lastBlockWithTree == null || lastBlockWithTree.getTree().isEmpty()) return null;

        Timber.d("verifying completed");

        return lastBlockWithTree;
    }

}
