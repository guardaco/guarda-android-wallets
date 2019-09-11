package com.guarda.zcash.sapling.db.model;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;



@Entity(tableName = "tx_details")
public class TxDetailsRoom {

    @PrimaryKey
    @NonNull
    private String primaryHash;
    @NonNull
    private String hash;
    private Long time;
    private Long sum;
    private Long confirmations;
    private String fromAddress;
    private String toAddress;
    private Boolean isOut;

    public TxDetailsRoom(@NonNull String primaryHash,
                         @NonNull String hash,
                         Long time,
                         Long sum,
                         Long confirmations,
                         String fromAddress,
                         String toAddress,
                         Boolean isOut) {
        this.primaryHash = primaryHash;
        this.hash = hash;
        this.time = time;
        this.sum = sum;
        this.confirmations = confirmations;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.isOut = isOut;
    }

    @NonNull
    public String getPrimaryHash() {
        return primaryHash;
    }

    @NonNull
    public String getHash() {
        return hash;
    }

    public Long getTime() {
        return time;
    }

    public Long getSum() {
        return sum;
    }

    public Long getConfirmations() {
        return confirmations;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public Boolean getOut() {
        return isOut;
    }
}
