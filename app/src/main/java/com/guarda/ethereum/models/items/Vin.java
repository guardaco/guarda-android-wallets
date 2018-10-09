package com.guarda.ethereum.models.items;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Vin {

    @SerializedName("txid")
    @Expose
    private String txid;
    @SerializedName("vout")
    @Expose
    private Integer vout;
    @SerializedName("sequence")
    @Expose
    private Long sequence;
    @SerializedName("n")
    @Expose
    private Integer n;

    @SerializedName("addr")
    @Expose
    private String addr;
    @SerializedName("valueSat")
    @Expose
    private long valueSat;
    @SerializedName("value")
    @Expose
    private Double value;
    @SerializedName("doubleSpentTxID")
    @Expose
    private Object doubleSpentTxID;

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public Integer getVout() {
        return vout;
    }

    public void setVout(Integer vout) {
        this.vout = vout;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public Integer getN() {
        return n;
    }

    public void setN(Integer n) {
        this.n = n;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public long getValueSat() {
        return valueSat;
    }

    public void setValueSat(Integer valueSat) {
        this.valueSat = valueSat;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Object getDoubleSpentTxID() {
        return doubleSpentTxID;
    }

    public void setDoubleSpentTxID(Object doubleSpentTxID) {
        this.doubleSpentTxID = doubleSpentTxID;
    }

}