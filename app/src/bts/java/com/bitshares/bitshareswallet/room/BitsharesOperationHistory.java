package com.bitshares.bitshareswallet.room;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.bitshares.bitshareswallet.wallet.graphene.chain.operation_history_object;

/**
 * Created by lorne on 31/10/2017.
 */

@Entity(tableName = "operation_history",
        indices = @Index(value = {"timestamp", "history_id"}, unique = true))
public class BitsharesOperationHistory {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public long timestamp;

    @Embedded
    public operation_history_object operationHistoryObject;
}
