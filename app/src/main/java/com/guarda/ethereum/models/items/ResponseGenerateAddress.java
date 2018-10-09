package com.guarda.ethereum.models.items;


import com.google.gson.annotations.SerializedName;

public class ResponseGenerateAddress {

    @SerializedName("jsonrpc")
    private String jsonrpc;
    @SerializedName("id")
    private int id;
    @SerializedName("result")
    private Address address;

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }


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

    public class Address {

        @SerializedName("address")
        private String address;

        @SerializedName("extraId")
        private String extraId;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getExtraId() {
            return extraId;
        }

        public void setExtraId(String extraId) {
            this.extraId = extraId;
        }
    }
}
