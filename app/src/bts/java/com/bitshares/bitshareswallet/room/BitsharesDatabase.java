package com.bitshares.bitshareswallet.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

/**
 * Created by lorne on 31/10/2017.
 */

@Database(entities = {
        BitsharesAsset.class,
        BitsharesMarketTicker.class,
        BitsharesAssetObject.class,
        BitsharesOperationHistory.class,
        BitsharesAccountObject.class
        }, version = 1)
@TypeConverters({RoomConverters.class})
public abstract class BitsharesDatabase extends RoomDatabase {
    public abstract BitsharesDao getBitsharesDao();
}
