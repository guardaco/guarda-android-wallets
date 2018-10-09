package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by SV on 18.11.2017.
 */

public class UTXOItem{

    @SerializedName("txid")
    @Expose
    private String txHash;
    @SerializedName("vout")
    @Expose
    private long txOutputN;
    @SerializedName("satoshis")
    @Expose
    private long satoshiValue;

    @SerializedName("confirmations")
    @Expose
    private String confirmations;

    public String getTxHash() {
        return txHash;
    }

    public long getTxOutputN() {
        return txOutputN;
    }

    public long getSatoshiValue() {
        return satoshiValue;
    }

    public String getConfirmations() {
        return confirmations;
    }
}
