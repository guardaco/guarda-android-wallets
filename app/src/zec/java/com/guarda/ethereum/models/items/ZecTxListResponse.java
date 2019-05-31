package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ZecTxListResponse {

    @SerializedName("txs")
    @Expose
    private List<ZecTxResponse> txs = null;

    public List<ZecTxResponse> getTxs() {
        return txs;
    }

    public void setTxs(List<ZecTxResponse> txs) {
        this.txs = txs;
    }

}