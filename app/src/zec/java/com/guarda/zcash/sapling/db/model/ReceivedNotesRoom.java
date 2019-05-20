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
    private String nf;

    public ReceivedNotesRoom(@NonNull String cm, Integer spent, Long value, String nf) {
        this.cm = cm;
        this.spent = spent;
        this.value = value;
        this.nf = nf;
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

    public String getNf() {
        return nf;
    }
}
