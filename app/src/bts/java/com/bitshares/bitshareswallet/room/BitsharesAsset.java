package com.bitshares.bitshareswallet.room;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.bitshares.bitshareswallet.wallet.asset;
import com.bitshares.bitshareswallet.wallet.graphene.chain.asset_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.object_id;

/**
 * Created by lorne on 31/10/2017.
 */

@Entity(tableName = "balance", indices = {@Index(value = {"asset_id", "type"}, unique = true)})
public class BitsharesAsset {
    public static final int TYPE_AVALIABLE = 0;
    public static final int TYPE_SELL_ORDER = 1;

    @PrimaryKey(autoGenerate = true)
    public int id;

    public long amount;
    public String currency;
    public object_id<asset_object> asset_id;
    public int type;
    public long precision;
}
