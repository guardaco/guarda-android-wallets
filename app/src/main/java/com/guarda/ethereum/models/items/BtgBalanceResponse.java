
package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BtgBalanceResponse {


    @SerializedName("balance")
    @Expose
    private String balance;

    @SerializedName("balanceSat")
    @Expose
    private long balanceSat;

    public String getBalance() {
        return balance;
    }

    public long getBalanceSat() {
        return balanceSat;
    }
}