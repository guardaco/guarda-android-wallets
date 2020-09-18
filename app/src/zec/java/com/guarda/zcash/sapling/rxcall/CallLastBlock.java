package com.guarda.zcash.sapling.rxcall;

import com.guarda.zcash.sapling.api.ProtoApi;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.db.model.BlockRoom;

import java.util.concurrent.Callable;

import timber.log.Timber;


public class CallLastBlock implements Callable<CallLastBlock.BlockSyncRange> {

    private final static long FIRST_BLOCK_TO_SYNC_TESTNET = 490131; //block for current wallet, all users create their new wallets after the height
//    private final static long FIRST_BLOCK_TO_SYNC_MAINNET = 551912; //block for current wallet, all users create their new wallets after the height
    public final static long FIRST_BLOCK_TO_SYNC_MAINNET = 900000; //block for current wallet, all users create their new wallets after the height

    private DbManager dbManager;
    private ProtoApi protoApi;
    private long nearStateHeightForStartSync = FIRST_BLOCK_TO_SYNC_MAINNET;

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

        BlockRoom blockRoom = dbManager.getAppDb().getBlockDao().getLatestBlockWithTree();

        // firstSyncBlockHeight minus one, because of there is no blocks in a database.
        // downloading will start from lastFromDb height (excluded)
        long lastFromDb = blockRoom != null ? blockRoom.getHeight() : firstSyncBlockHeight;
//        long lastFromDb = blockRoom != null ? blockRoom.getHeight() : FIRST_BLOCK_TO_SYNC_TESTNET;

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
