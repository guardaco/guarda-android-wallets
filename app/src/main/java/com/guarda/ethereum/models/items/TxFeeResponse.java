package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TxFeeResponse {

    @SerializedName("feeInUsd")
    @Expose
    private String feeInUsd;
    @SerializedName("fee")
    @Expose
    private String fee;

    public String getFeeInUsd() {
        return feeInUsd;
    }

    public void setFeeInUsd(String feeInUsd) {
        this.feeInUsd = feeInUsd;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }
}