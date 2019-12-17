package com.guarda.zcash.sapling.rxcall;

import com.guarda.zcash.sapling.api.ProtoApi;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.db.model.BlockRoom;

import java.util.concurrent.Callable;

import timber.log.Timber;


public class CallLastBlock implements Callable<CallLastBlock.BlockSyncRange> {

    private final static long FIRST_BLOCK_TO_SYNC_TESTNET = 490131; //block for current wallet, all users create their new wallets after the height
    private final static long FIRST_BLOCK_TO_SYNC_MAINNET = 551912; //block for current wallet, all users create their new wallets after the height

    private DbManager dbManager;
    private ProtoApi protoApi;

    public CallLastBlock(DbManager dbManager, ProtoApi protoApi) {
        this.dbManager = dbManager;
        this.protoApi = protoApi;
    }

    @Override
    public BlockSyncRange call() throws Exception {
        Timber.d("started");

        long latestFromServer = protoApi.getLastBlock();
        Timber.d("latestFromServer = %d", latestFromServer);
        BlockRoom blockRoom = dbManager.getAppDb().getBlockDao().getLatestBlock();
        long lastFromDb = blockRoom != null ? blockRoom.getHeight() : FIRST_BLOCK_TO_SYNC_MAINNET;
//        long lastFromDb = blockRoom != null ? blockRoom.getHeight() : FIRST_BLOCK_TO_SYNC_TESTNET;
        Timber.d("lastFromDb = %d", lastFromDb);
        return new BlockSyncRange(latestFromServer, lastFromDb);
    }

    public class BlockSyncRange {
        long latest;
        long lastFromDb;

        private BlockSyncRange(long latest, long lastFromDb) {
            this.latest = latest;
            this.lastFromDb = lastFromDb;
        }

        public long getLatest() {
            return latest;
        }

        public long getLastFromDb() {
            return lastFromDb;
        }

        @Override
        public String toString() {
            return "BlockSyncRange{" +
                    "latest=" + latest +
                    ", lastFromDb=" + lastFromDb +
                    '}';
        }
    }
}
