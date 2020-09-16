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
    public final static long FIRST_BLOCK_TO_SYNC_MAINNET = 900000; //block for current wallet, all users create their new wallets after the height

    private DbManager dbManager;
    private ProtoApi protoApi;
    private SharedManager sharedManager;
    private long nearStateHeightForStartSync = FIRST_BLOCK_TO_SYNC_MAINNET;

    public CallLastBlock(DbManager dbManager, ProtoApi protoApi, SharedManager sharedManager, long nearStateHeightForStartSync) {
        this.dbManager = dbManager;
        this.protoApi = protoApi;
        this.sharedManager = sharedManager;
        this.nearStateHeightForStartSync = nearStateHeightForStartSync;
    }

    @Override
    public BlockSyncRange call() {
        Timber.d("started");

        long latestFromServer = protoApi.getLastBlock();
        Timber.d("latestFromServer = %d", latestFromServer);

        long firsSyncBlockHeight = nearStateHeightForStartSync;

        BlockRoom blockRoom = dbManager.getAppDb().getBlockDao().getLatestBlockWithTree();

        // firsSyncBlockHeight minus one, because of there is no blocks in database.
        // downloading will start from lastFromDb height (excluded)
        long lastFromDb = blockRoom != null ? blockRoom.getHeight() : firsSyncBlockHeight - 1;
//        long lastFromDb = blockRoom != null ? blockRoom.getHeight() : FIRST_BLOCK_TO_SYNC_TESTNET;

        Timber.d("lastFromDb = %d", lastFromDb);
        return new BlockSyncRange(latestFromServer, lastFromDb, firsSyncBlockHeight);
    }

    public class BlockSyncRange {
        long lastFromServer;
        long lastFromDb;
        long firsSyncBlockHeight;

        private BlockSyncRange(long lastFromServer, long lastFromDb, long firsSyncBlockHeight) {
            this.lastFromServer = lastFromServer;
            this.lastFromDb = lastFromDb;
            this.firsSyncBlockHeight = firsSyncBlockHeight;
        }

        public long getLastFromServer() {
            return lastFromServer;
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
                    "lastFromServer=" + lastFromServer +
                    ", lastFromDb=" + lastFromDb +
                    ", firsSyncBlockHeight=" + firsSyncBlockHeight +
                    '}';
        }
    }
}
