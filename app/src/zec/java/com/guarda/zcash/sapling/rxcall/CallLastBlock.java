package com.guarda.zcash.sapling.rxcall;

import com.guarda.zcash.sapling.api.ProtoApi;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.db.model.BlockRoom;

import java.util.concurrent.Callable;

import timber.log.Timber;

public class CallLastBlock implements Callable<CallLastBlock.BlockSyncRange> {

    DbManager dbManager;
    public final static long FIRST_BLOCK_TO_SYNC = 422044; //block for current wallet, all users create their new wallets after the height

    public CallLastBlock(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public BlockSyncRange call() throws Exception {
        long latest = new ProtoApi().getLastBlock();
        Timber.d("latest = %d", latest);
        BlockRoom blockRoom = dbManager.getAppDb().getBlockDao().getLatestBlock();
        long lastFromDb = blockRoom != null ? blockRoom.getHeight() : FIRST_BLOCK_TO_SYNC;
        Timber.d("lastFromDb = %d", lastFromDb);
        return new BlockSyncRange(latest, lastFromDb);
    }

    public class BlockSyncRange {
        long latest;
        long lastFromDb;

        public BlockSyncRange(long latest, long lastFromDb) {
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
