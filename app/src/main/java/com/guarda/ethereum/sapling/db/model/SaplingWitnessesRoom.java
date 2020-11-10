package com.guarda.ethereum.sapling.db.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sapling_witnesses")
public class SaplingWitnessesRoom {
    @PrimaryKey
    @NonNull
    private String cm;
    private String witness;
    private Long witnessHeight;

    public SaplingWitnessesRoom(@NonNull String cm, String witness, Long witnessHeight) {
        this.cm = cm;
        this.witness = witness;
        this.witnessHeight = witnessHeight;
    }

    @NonNull
    public String getCm() {
        return cm;
    }

    public String getWitness() {
        return witness;
    }

    public void setWitness(String witness) {
        this.witness = witness;
    }

    public Long getWitnessHeight() {
        return witnessHeight;
    }

    public void setWitnessHeight(Long witnessHeight) {
        this.witnessHeight = witnessHeight;
    }
}
