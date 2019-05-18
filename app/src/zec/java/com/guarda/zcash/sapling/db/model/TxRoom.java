package com.guarda.zcash.sapling.db.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "txs",
        foreignKeys = @ForeignKey(
                entity = BlockRoom.class,
                parentColumns = "hash",
                childColumns = "blockHash",
                onDelete = CASCADE
        ))
public class TxRoom {

    public TxRoom(String hash, String blockHash) {
        this.hash = hash;
        this.blockHash = blockHash;
    }

    @PrimaryKey
    @NonNull
    private String hash;
    @ColumnInfo(name = "blockHash", index = true)
    private String blockHash;

    public String getHash() {
        return hash;
    }

    public String getBlockHash() {
        return blockHash;
    }
}
