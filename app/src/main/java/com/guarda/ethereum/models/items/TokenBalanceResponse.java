package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TokenBalanceResponse {

    @SerializedName("result")
    @Expose
    private String result;

    public String getResult() {
        return result;
    }

    @Override
    public String toString() {
        return result;
    }
}