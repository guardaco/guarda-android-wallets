package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OutputDescs {

    @SerializedName("cv")
    @Expose
    private String cv;
    @SerializedName("cmu")
    @Expose
    private String cmu;
    @SerializedName("ephemeralKey")
    @Expose
    private String ephemeralKey;
    @SerializedName("encCiphertext")
    @Expose
    private String encCiphertext;
    @SerializedName("outCiphertext")
    @Expose
    private String outCiphertext;
    @SerializedName("proof")
    @Expose
    private String proof;

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


