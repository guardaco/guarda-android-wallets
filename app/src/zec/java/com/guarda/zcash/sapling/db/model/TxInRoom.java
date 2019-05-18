package com.guarda.zcash.sapling.db.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import static android.arch.persistence.room.ForeignKey.CASCADE;

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
