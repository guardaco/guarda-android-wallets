package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Vout {

    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("n")
    @Expose
    private Integer n;
        @SerializedName("scriptPubKey")
    @Expose
    private ScriptPubKey scriptPubKey;
    @SerializedName("spentTxId")
    @Expose
    private String spentTxId;
    @SerializedName("spentIndex")
    @Expose
    private Integer spentIndex;
    @SerializedName("spentHeight")
    @Expose
    private Integer spentHeight;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getN() {
        return n;
    }

    public void setN(Integer n) {
        this.n = n;
    }

    public ScriptPubKey getScriptPubKey() {
        return scriptPubKey;
    }

    public void setScriptPubKey(ScriptPubKey scriptPubKey) {
        this.scriptPubKey = scriptPubKey;
    }

    public String getSpentTxId() {
        return spentTxId;
    }

    public void setSpentTxId(String spentTxId) {
        this.spentTxId = spentTxId;
    }

    public Integer getSpentIndex() {
        return spentIndex;
    }

    public void setSpentIndex(Integer spentIndex) {
        this.spentIndex = spentIndex;
    }

    public Integer getSpentHeight() {
        return spentHeight;
    }

    public void setSpentHeight(Integer spentHeight) {
        this.spentHeight = spentHeight;
    }

    public String getAddress() {
        if ((scriptPubKey != null) && (scriptPubKey.getAddresses() != null)) {
            return scriptPubKey.getAddresses().get(0);
        } else {
            return "";
        }
    }
}


