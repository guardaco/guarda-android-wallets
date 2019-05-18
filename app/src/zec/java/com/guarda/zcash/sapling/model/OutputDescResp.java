package com.guarda.zcash.sapling.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OutputDescResp {
    @Expose
    @SerializedName("cv")
    private String cv;
    @Expose
    @SerializedName("cmu")
    private String cmu;
    @Expose
    @SerializedName("ephemeralKey")
    private String ephemeralKey;
    @Expose
    @SerializedName("encCiphertext")
    private String encCiphertext;
    @Expose
    @SerializedName("outCiphertext")
    private String outCiphertext;
    @Expose
    @SerializedName("proof")
    private String proof;

    public OutputDescResp(String cmu, String ephemeralKey, String encCiphertext) {
        this.cmu = cmu;
        this.ephemeralKey = ephemeralKey;
        this.encCiphertext = encCiphertext;
    }

    public String getCv() {
        return cv;
    }

    public String getCmu() {
        return cmu;
    }

    public String getEphemeralKey() {
        return ephemeralKey;
    }

    public String getEncCiphertext() {
        return encCiphertext;
    }

    public String getOutCiphertext() {
        return outCiphertext;
    }

    public String getProof() {
        return proof;
    }
}
