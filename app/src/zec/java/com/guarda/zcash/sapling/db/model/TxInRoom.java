package com.guarda.zcash.sapling.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "txins",
        foreignKeys = @ForeignKey(
                entity = TxRoom.class,
                parentColumns = "hash",
                childColumns = "txHash",
                onDelete = CASCADE
        ))
public class TxInRoom {

    public TxInRoom(String id, String txHash, String nf) {
        this.id = id;
        this.txHash = txHash;
        this.nf = nf;
    }

    @PrimaryKey
    @NonNull
    private String id;
    @ColumnInfo(name = "txHash", index = true)
    private String txHash;
    private String nf;

    public String getId() {
        return id;
    }

    public String getTxHash() {
        return txHash;
    }

    public String getNf() {
        return nf;
    }
}
