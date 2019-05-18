package com.guarda.zcash.sapling.db.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "received_notes")
public class ReceivedNotesRoom {
    @PrimaryKey
    @NonNull
    private String cm;
    private Integer spent;
    private Long value;

    public ReceivedNotesRoom(@NonNull String cm, Integer spent, Long value) {
        this.cm = cm;
        this.spent = spent;
        this.value = value;
    }

    @NonNull
    public String getCm() {
        return cm;
    }

    public Integer getSpent() {
        return spent;
    }

    public Long getValue() {
        return value;
    }
}
