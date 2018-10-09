package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class CoinifyQuoteResponse {

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("baseCurrency")
    @Expose
    private String baseCurrency;
    @SerializedName("quoteCurrency")
    @Expose
    private String quoteCurrency;
    @SerializedName("baseAmount")
    @Expose
    private float baseAmount;
    @SerializedName("quoteAmount")
    @Expose
    private float quoteAmount;
    @SerializedName("expiryTime")
    @Expose
    private String expiryTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public String getQuoteCurrency() {
        return quoteCurrency;
    }

    public void setQuoteCurrency(String quoteCurrency) {
        this.quoteCurrency = quoteCurrency;
    }

    public float getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(float baseAmount) {
        this.baseAmount = baseAmount;
    }

    public float getQuoteAmount() {
        return quoteAmount;
    }

    public void setQuoteAmount(float quoteAmount) {
        this.quoteAmount = quoteAmount;
    }

    public String getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(String expiryTime) {
        this.expiryTime = expiryTime;
    }
}
