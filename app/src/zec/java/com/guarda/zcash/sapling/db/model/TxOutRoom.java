package com.guarda.zcash.sapling.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "txouts",
        foreignKeys = @ForeignKey(
                entity = TxRoom.class,
                parentColumns = "hash",
                childColumns = "txHash",
                onDelete = CASCADE
        ))
public class TxOutRoom {

    public TxOutRoom(String id, String txHash, String cmu, String epk, String ciphertext) {
        this.id = id;
        this.txHash = txHash;
        this.cmu = cmu;
        this.epk = epk;
        this.ciphertext = ciphertext;
    }

    @PrimaryKey
    @NonNull
    private String id;
    @ColumnInfo(name = "txHash", index = true)
    private String txHash;
    private String cmu;
    private String epk;
    private String ciphertext;

    public String getId() {
        return id;
    }

    public String getTxHash() {
        return txHash;
    }

    public String getCmu() {
        return cmu;
    }

    public String getEpk() {
        return epk;
    }

    public String getCiphertext() {
        return ciphertext;
    }
}
