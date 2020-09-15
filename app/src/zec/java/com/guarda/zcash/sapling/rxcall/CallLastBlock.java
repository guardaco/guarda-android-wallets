package com.guarda.zcash.sapling.rxcall;

import com.guarda.ethereum.managers.SharedManager;
import com.guarda.zcash.sapling.api.ProtoApi;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.db.model.BlockRoom;

import java.util.concurrent.Callable;

import timber.log.Timber;


public class CallLastBlock implements Callable<CallLastBlock.BlockSyncRange> {

    private final static long FIRST_BLOCK_TO_SYNC_TESTNET = 490131; //block for current wallet, all users create their new wallets after the height
//    private final static long FIRST_BLOCK_TO_SYNC_MAINNET = 551912; //block for current wallet, all users create their new wallets after the height
    private final static long FIRST_BLOCK_TO_SYNC_MAINNET = 900000; //block for current wallet, all users create their new wallets after the height

    private DbManager dbManager;
    private ProtoApi protoApi;
    private SharedManager sharedManager;

    public CallLastBlock(DbManager dbManager, ProtoApi protoApi, SharedManager sharedManager) {
        this.dbManager = dbManager;
        this.protoApi = protoApi;
        this.sharedManager = sharedManager;
    }

    @Override
    public BlockSyncRange call() {
        Timber.d("started");

        long latestFromServer = protoApi.getLastBlock();
        Timber.d("latestFromServer = %d", latestFromServer);

        BlockRoom blockRoom = dbManager.getAppDb().getBlockDao().getLatestBlock();

        long lastFromDb = blockRoom != null ? blockRoom.getHeight() : FIRST_BLOCK_TO_SYNC_MAINNET;
//        long lastFromDb = blockRoom != null ? blockRoom.getHeight() : FIRST_BLOCK_TO_SYNC_TESTNET;

        long firsSyncBlockHeight = sharedManager.getFirstSyncBlockHeight();
        if (firsSyncBlockHeight == 0) {
            sharedManager.setFirstSyncBlockHeight(FIRST_BLOCK_TO_SYNC_MAINNET);
            firsSyncBlockHeight = FIRST_BLOCK_TO_SYNC_MAINNET;
        }

        Timber.d("lastFromDb = %d", lastFromDb);
        return new BlockSyncRange(latestFromServer, lastFromDb, firsSyncBlockHeight);
    }

    public class BlockSyncRange {
        long latest;
        long lastFromDb;
        long firsSyncBlockHeight;

        private BlockSyncRange(long latest, long lastFromDb, long firsSyncBlockHeight) {
            this.latest = latest;
            this.lastFromDb = lastFromDb;
            this.firsSyncBlockHeight = firsSyncBlockHeight;
        }

        public long getLatest() {
            return latest;
        }
        public long getLastFromDb() {
            return lastFromDb;
        }
        public long getFirsSyncBlockHeight() {
            return firsSyncBlockHeight;
        }

        @Override
        public String toString() {
            return "BlockSyncRange{" +
                    "latest=" + latest +
                    ", lastFromDb=" + lastFromDb +
                    ", firsSyncBlockHeight=" + firsSyncBlockHeight +
                    '}';
        }
    }
}
