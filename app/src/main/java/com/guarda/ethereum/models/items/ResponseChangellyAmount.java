package com.guarda.ethereum.models.items;

import com.google.gson.annotations.SerializedName;


public class ResponseChangellyAmount {

    @SerializedName("jsonrpc")
    private String jsonrpc;
    @SerializedName("id")
    private int id;
    @SerializedName("result")
    private String amount;

    public String getAmount() {
        return amount;
    }

}
