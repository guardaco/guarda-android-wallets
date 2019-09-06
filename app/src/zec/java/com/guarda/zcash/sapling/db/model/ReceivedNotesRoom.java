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
    private String memo;

    public ReceivedNotesRoom(@NonNull String cm, Integer spent, Long value, String nf, String memo) {
        this.cm = cm;
        this.spent = spent;
        this.value = value;
        this.nf = nf;
        this.memo = memo;
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

    public String getMemo() {
        return memo;
    }

    @Override
    public String toString() {
        return "ReceivedNotesRoom{" +
                "cm='" + cm + '\'' +
                ", spent=" + spent +
                ", value=" + value +
                ", nf='" + nf + '\'' +
                ", memo='" + memo + '\'' +
                '}';
    }
}
