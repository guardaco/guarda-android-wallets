package com.guarda.ethereum.models.items;

public class TokenRequestModel {
    public String name = "";
    public String address = "";
    public int decimals;

    public TokenRequestModel(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public TokenRequestModel(String name, String address, int decimals) {
        this.name = name;
        this.address = address;
        this.decimals = decimals;
    }
}
