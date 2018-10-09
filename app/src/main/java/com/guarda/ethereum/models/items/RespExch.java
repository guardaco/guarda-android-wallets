package com.guarda.ethereum.models.items;


import com.google.gson.annotations.SerializedName;

public class RespExch {

    @SerializedName("price_usd")
    private String priceUsd;

    @SerializedName("price_eur")
    private String priceEur;

    @SerializedName("price_rub")
    private String priceRur;

    @SerializedName("price_gbp")
    private String priceGbp;

    @SerializedName("price_dkk")
    private String priceDkk;

    @SerializedName("price_nok")
    private String priceNok;

    @SerializedName("price_sek")
    private String priceSek;

    @SerializedName("price_inr")
    private String priceInr;

    public String getPrice(String localCurrency) {
        String local = "";
        switch (localCurrency) {
            case "usd": local = this.priceUsd;
                break;
            case "eur": local = this.priceEur;
                break;
            case "rub": local = this.priceRur;
                break;
            case "gbp": local = this.priceGbp;
                break;
            case "dkk": local = this.priceDkk;
                break;
            case "nok": local = this.priceNok;
                break;
            case "sek": local = this.priceSek;
                break;
            case "inr": local = this.priceInr;
                break;
        }
        return local;
    }

    public String getPriceUsd() {
        return priceUsd;
    }

    public void setPriceUsd(String priceUsd) {
        this.priceUsd = priceUsd;
    }

    public String getPriceEur() {
        return priceEur;
    }

    public void setPriceEur(String priceEur) {
        this.priceEur = priceEur;
    }

    public String getPriceRur() {
        return priceRur;
    }

    public void setPriceRur(String priceRur) {
        this.priceRur = priceRur;
    }
}
