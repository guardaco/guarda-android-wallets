package com.guarda.ethereum.sapling.rxcall;

import com.guarda.ethereum.sapling.api.ProtoApi;
import com.guarda.ethereum.sapling.db.DbManager;
import com.guarda.ethereum.sapling.db.model.BlockRoom;

import java.util.concurrent.Callable;

import timber.log.Timber;


public class CallLastBlock implements Callable<CallLastBlock.BlockSyncRange> {

    private final static long FIRST_BLOCK_TO_SYNC_TESTNET = 490131; //block for current wallet, all users create their new wallets after the height
//    private final static long FIRST_BLOCK_TO_SYNC_MAINNET = 551912; //block for current wallet, all users create their new wallets after the height
    public final static long FIRST_BLOCK_TO_SYNC_MAINNET_TESTNET = 900000; //block for current wallet, all users create their new wallets after the height

    private DbManager dbManager;
    private ProtoApi protoApi;
    private long nearStateHeightForStartSync = FIRST_BLOCK_TO_SYNC_MAINNET_TESTNET;

    public CallLastBlock(DbManager dbManager, ProtoApi protoApi, long nearStateHeightForStartSync) {
        this.dbManager = dbManager;
        this.protoApi = protoApi;
        this.nearStateHeightForStartSync = nearStateHeightForStartSync;
    }

    @Override
    public BlockSyncRange call() {
        Timber.d("started");

        long latestFromServer = protoApi.getLastBlock();
        Timber.d("latestFromServer = %d", latestFromServer);

        long firstSyncBlockHeight = nearStateHeightForStartSync;

        BlockRoom blockRoomWithTree = dbManager.getAppDb().getBlockDao().getLatestBlockWithTree();
        BlockRoom blockRoomNewest = dbManager.getAppDb().getBlockDao().getLatestBlock();

        long lastFromDb;
        // for new created wallet
        if (blockRoomWithTree == null && blockRoomNewest == null) {
            lastFromDb = firstSyncBlockHeight;
        // height from DB increasing (plus one) to prevent overriding block tree state
        } else if (blockRoomWithTree != null) {
            lastFromDb = blockRoomWithTree.getHeight() + 1;
        } else {
            lastFromDb = blockRoomNewest.getHeight() + 1;
        }

        Timber.d("lastFromDb = %d", lastFromDb);
        return new BlockSyncRange(latestFromServer, lastFromDb);
    }

    public class BlockSyncRange {
        long lastFromServer;
        long lastFromDb;

        private BlockSyncRange(long lastFromServer, long lastFromDb) {
            this.lastFromServer = lastFromServer;
            this.lastFromDb = lastFromDb;
        }

        public long getLastFromServer() {
            return lastFromServer;
        }
        public long getLastFromDb() {
            return lastFromDb;
        }

        @Override
        public String toString() {
            return "BlockSyncRange{" +
                    "lastFromServer=" + lastFromServer +
                    ", lastFromDb=" + lastFromDb +
                    '}';
        }
    }
}
