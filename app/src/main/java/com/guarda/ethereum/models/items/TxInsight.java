package com.guarda.ethereum.models.items;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class TxInsight {

    private String coinId;
    @SerializedName("from")
    @Expose
    private String fromAddress;
    @SerializedName("to")
    @Expose
    private String toAddress;
    @SerializedName("block_date_time")
    @Expose
    private String timestamp;
    private BigDecimal fees;
    @SerializedName("value")
    @Expose
    private String value;
    private long valueSat;
    @SerializedName("tx_hash")
    @Expose
    private String txHash;
    private String description;
    @SerializedName("block_height")
    @Expose
    private int blockHeight;
    private boolean isInTx;

    public String getCoinId() {
        return coinId;
    }

    public boolean isInTx() {
        return isInTx;
    }

    public void setIsInTx(boolean inTx) {
        isInTx = inTx;
    }

    public void setCoinId(String coinId) {
        this.coinId = coinId;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getFees() {
        return fees;
    }

    public void setFees(BigDecimal fees) {
        this.fees = fees;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getValueSat() {
        return valueSat;
    }

    public void setValueSat(long valueSat) {
        this.valueSat = valueSat;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(int blockHeight) {
        this.blockHeight = blockHeight;
    }

    @Override
    public String toString() {
        return "from - " + fromAddress + "\n"
                + "to - " + toAddress + "\n"
                + "value - " + value.toString();
    }

}
