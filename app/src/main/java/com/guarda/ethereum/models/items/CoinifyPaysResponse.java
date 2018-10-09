package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;


public class CoinifyPaysResponse {

    @SerializedName("inMedium")
    @Expose
    private String inMedium;
    @SerializedName("outMedium")
    @Expose
    private String outMedium;
    @SerializedName("inCurrencies")
    @Expose
    private List<String> inCurrencies;
    @SerializedName("outCurrencies")
    @Expose
    private List<String> outCurrencies;
    @SerializedName("outPercentageFee")
    @Expose
    private Float outPercentageFee;
    @SerializedName("minimumInAmounts")
    @Expose
    private Map<String, Float> minimumInAmounts;
    @SerializedName("limitInAmounts")
    @Expose
    private Map<String, Float> limitInAmounts;
    @SerializedName("canTrade")
    @Expose
    private boolean canTrade;

    public String getInMedium() {
        return inMedium;
    }

    public String getOutMedium() {
        return outMedium;
    }

    public List<String> getInCurrencies() {
        return inCurrencies;
    }

    public List<String> getOutCurrencies() {
        return outCurrencies;
    }

    public Float getOutPercentageFee() {
        return outPercentageFee;
    }

    public Map<String, Float> getMinimumInAmounts() {
        return minimumInAmounts;
    }

    public Map<String, Float> getLimitInAmounts() {
        return limitInAmounts;
    }

    public boolean isCanTrade() {
        return canTrade;
    }

}
