package com.guarda.ethereum.sapling.db.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

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
