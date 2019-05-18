package com.guarda.zcash.sapling.db.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "blocks")
public class BlockRoom {
    @PrimaryKey
    @NonNull
    private String hash;
    private long height;

    public BlockRoom(@NonNull String hash, long height) {
        this.hash = hash;
        this.height = height;
    }

    public String getHash() {
        return hash;
    }

    public long getHeight() {
        return height;
    }
}
