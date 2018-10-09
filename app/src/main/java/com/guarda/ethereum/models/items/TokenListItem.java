package com.guarda.ethereum.models.items;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TokenListItem {

    @SerializedName("ticker")
    @Expose
    private String ticker;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("precision")
    @Expose
    private int precision;
    @SerializedName("smartContract")
    @Expose
    private String smartContract;

    public String getTicker() {
        return ticker;
    }

    public String getTitle() {
        return title;
    }

    public int getPrecision() {
        return precision;
    }

    public String getSmartContract() {
        return smartContract;
    }
}
