package com.guarda.ethereum.models.items;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TransactionsInsightListResponse {

    @SerializedName("addresses")
    @Expose
    private List<String> addresses;
    @SerializedName("items")
    @Expose
    private List<TxInsight> txs = null;

    public List<String> getPagesTotal() {
        return addresses;
    }

    public void setPagesTotal(List<String> pagesTotal) {
        this.addresses = pagesTotal;
    }

    public List<TxInsight> getTxs() {
        return txs;
    }

    public void setTxs(List<TxInsight> txs) {
        this.txs = txs;
    }

}
