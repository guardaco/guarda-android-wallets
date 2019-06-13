package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SpendDescs {

    @SerializedName("cv")
    @Expose
    private String cv;
    @SerializedName("anchor")
    @Expose
    private String anchor;
    @SerializedName("nullifier")
    @Expose
    private String nullifier;
    @SerializedName("rk")
    @Expose
    private String rk;
    @SerializedName("proof")
    @Expose
    private String proof;
    @SerializedName("spendAuthSig")
    @Expose
    private String spendAuthSig;

    public String getCv() {
        return cv;
    }

    public String getAnchor() {
        return anchor;
    }

    public String getNullifier() {
        return nullifier;
    }

    public String getRk() {
        return rk;
    }

    public String getProof() {
        return proof;
    }

    public String getSpendAuthSig() {
        return spendAuthSig;
    }
}


