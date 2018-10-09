package com.guarda.ethereum.models.items;


import com.google.gson.annotations.SerializedName;

public class ResponseCurrencyItem {

    @SerializedName("jsonrpc")
    private String jsonrpc;
    @SerializedName("id")
    private int id;

    @SerializedName("result")
    private String[] result;

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String[] getResult() {
        return result;
    }

    public void setResult(String[] result) {
        this.result = result;
    }
}
