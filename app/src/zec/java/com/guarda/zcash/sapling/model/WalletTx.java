package com.guarda.zcash.sapling.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.guarda.zcash.sapling.note.SaplingNoteData;
import com.guarda.zcash.sapling.note.SaplingOutPoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletTx {
    @Expose
    @SerializedName("txid")
    private String txid;
    @Expose
    @SerializedName("outputDescs")
    private List<OutputDescResp> outputDescs;

    private SaplingOutPoint op;
    private SaplingNoteData nd;

    private Map<SaplingOutPoint, SaplingNoteData> mapSaplingNoteData = new HashMap<>();

    public WalletTx(SaplingOutPoint op) {
        this.op = op;
    }

    public WalletTx(SaplingOutPoint op, SaplingNoteData nd) {
        this.op = op;
        this.nd = nd;
        mapSaplingNoteData.put(op, nd);
    }

    public String getTxid() {
        return txid;
    }

    public List<OutputDescResp> getOutputDescs() {
        return outputDescs;
    }

    public void setOutputDescs(List<OutputDescResp> outputDescs) {
        this.outputDescs = outputDescs;
    }

    public Map<SaplingOutPoint, SaplingNoteData> getMapSaplingNoteData() {
        return mapSaplingNoteData;
    }

}
