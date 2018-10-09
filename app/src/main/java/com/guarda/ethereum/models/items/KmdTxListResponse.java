package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class KmdTxListResponse {

    @SerializedName("totalItems")
    @Expose
    private Integer pagesTotal;

    @SerializedName("items")
    @Expose
    private List<BtgTxResponse> txs = null;

    public Integer getPagesTotal() {
        return pagesTotal;
    }

    public void setPagesTotal(Integer pagesTotal) {
        this.pagesTotal = pagesTotal;
    }

    public List<BtgTxResponse> getTxs() {
        return txs;
    }

    public void setTxs(List<BtgTxResponse> txs) {
        this.txs = txs;
    }

}

