package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by psd on 13.02.2018.
 */

public class LtcBalanceTxsResponse {

    @SerializedName("balance")
    @Expose
    private long balance;

    @SerializedName("txrefs")
    @Expose
    private List<LtcTx> txrefs;

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public List<LtcTx> getTxrefs() {
        return txrefs;
    }

    public void setTxrefs(List<LtcTx> txrefs) {
        this.txrefs = txrefs;
    }
}
