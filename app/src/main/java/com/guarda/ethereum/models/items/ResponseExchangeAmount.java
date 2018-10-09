package com.guarda.ethereum.models.items;


import com.google.gson.annotations.SerializedName;

public class ResponseExchangeAmount {


    @SerializedName("ticker")
    private Ticker ticker;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("success")
    private boolean success;

    @SerializedName("clearText")
    private String error;

    public Ticker getTicker() {
        return ticker;
    }

    public void setTicker(Ticker ticker) {
        this.ticker = ticker;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public class Ticker {

        @SerializedName("base")
        private String base;

        @SerializedName("target")
        private String target;

        @SerializedName("price")
        private String price;

        @SerializedName("volume")
        private String volume;

        @SerializedName("change")
        private String change;

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }
    }

}
