package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class IconsListResponse {

    @SerializedName("txs")
    @Expose
    private List<BtgTxResponse> txs = null;

    public List<BtgTxResponse> getTxs() {
        return txs;
    }

    public void setTxs(List<BtgTxResponse> txs) {
        this.txs = txs;
    }

}