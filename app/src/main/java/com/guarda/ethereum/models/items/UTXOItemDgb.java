package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class UTXOItemDgb{

    @SerializedName("txid")
    @Expose
    private String txHash;
    @SerializedName("vout")
    @Expose
    private long txOutputN;
    @SerializedName("amount")
    @Expose
    private BigDecimal amount;

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
        return amount.multiply(BigDecimal.valueOf(100000000)).longValue();
    }

    public String getConfirmations() {
        return confirmations;
    }
}
