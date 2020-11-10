package com.guarda.ethereum.sapling.db.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

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

    @Override
    public String toString() {
        return "BlockRoom{" +
                "hash='" + hash + '\'' +
                ", height=" + height +
                ", tree='" + tree + '\'' +
                '}';
    }
}
