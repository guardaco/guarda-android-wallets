package com.guarda.ethereum.models.items;

import com.google.gson.annotations.SerializedName;


public class ResponseChangellyMinAmount {

    @SerializedName("jsonrpc")
    private String jsonrpc;
    @SerializedName("id")
    private int id;
    @SerializedName("result")
    private float amount;

    public float getAmount() {
        return amount;
    }

}
