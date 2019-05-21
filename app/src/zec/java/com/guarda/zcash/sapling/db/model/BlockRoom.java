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
    private String tree;

    public BlockRoom(@NonNull String hash, long height, String tree) {
        this.hash = hash;
        this.height = height;
        this.tree = tree;
    }

    public String getHash() {
        return hash;
    }

    public long getHeight() {
        return height;
    }

    public String getTree() {
        return tree;
    }

    public void setTree(String tree) {
        this.tree = tree;
    }
}
