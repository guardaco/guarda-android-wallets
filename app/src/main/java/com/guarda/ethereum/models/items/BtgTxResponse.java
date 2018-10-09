package com.guarda.ethereum.models.items;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BtgTxResponse {

    @SerializedName("txid")
    @Expose
    private String txid;
    @SerializedName("version")
    @Expose
    private Integer version;
    @SerializedName("locktime")
    @Expose
    private Long locktime;
    @SerializedName("vin")
    @Expose
    private List<Vin> vin = null;
    @SerializedName("vout")
    @Expose
    private List<Vout> vout = null;
    @SerializedName("blockhash")
    @Expose
    private String blockhash;
    @SerializedName("blockheight")
    @Expose
    private Integer blockheight;
    @SerializedName("confirmations")
    @Expose
    private Integer confirmations;
    @SerializedName("time")
    @Expose
    private Integer time;
    @SerializedName("blocktime")
    @Expose
    private Integer blocktime;
    @SerializedName("valueOut")
    @Expose
    private Double valueOut;
    @SerializedName("size")
    @Expose
    private Integer size;
    @SerializedName("valueIn")
    @Expose
    private Double valueIn;
    @SerializedName("fees")
    @Expose
    private Double fees;

    public String getHash() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Long getLocktime() {
        return locktime;
    }

    public void setLocktime(Long locktime) {
        this.locktime = locktime;
    }

    public List<Vin> getVin() {
        return vin;
    }

    public void setVin(List<Vin> vin) {
        this.vin = vin;
    }

    public List<Vout> getVout() {
        return vout;
    }

    public void setVout(List<Vout> vout) {
        this.vout = vout;
    }

    public String getBlockhash() {
        return blockhash;
    }

    public void setBlockhash(String blockhash) {
        this.blockhash = blockhash;
    }

    public Integer getBlockheight() {
        return blockheight;
    }

    public void setBlockheight(Integer blockheight) {
        this.blockheight = blockheight;
    }

    public Integer getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(Integer confirmations) {
        this.confirmations = confirmations;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Integer getBlocktime() {
        return blocktime;
    }

    public void setBlocktime(Integer blocktime) {
        this.blocktime = blocktime;
    }

    public Double getValueOut() {
        return valueOut;
    }

    public void setValueOut(Double valueOut) {
        this.valueOut = valueOut;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Double getValueIn() {
        return valueIn;
    }

    public void setValueIn(Double valueIn) {
        this.valueIn = valueIn;
    }

    public Double getFees() {
        return fees;
    }

    public void setFees(Double fees) {
        this.fees = fees;
    }

}