package com.guarda.ethereum.models.items;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SendRawTxResponse {

    @SerializedName("result")
    @Expose
    private String hashResult;

    @SerializedName("error")
    @Expose
    private String error;


    public String getHashResult() {
        return hashResult;
    }

    public String getError() {
        return error;
    }
}
