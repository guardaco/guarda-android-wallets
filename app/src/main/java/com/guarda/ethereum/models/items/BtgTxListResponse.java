package com.guarda.ethereum.models.items;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BtgTxListResponse {

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