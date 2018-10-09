package com.guarda.ethereum.models.items;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ScriptPubKey {

    @SerializedName("hex")
    @Expose
    private String hex;
    @SerializedName("asm")
    @Expose
    private String asm;
    @SerializedName("addresses")
    @Expose
    private List<String> addresses = null;
    @SerializedName("type")
    @Expose
    private String type;

    public String getHex() {
        return hex;
    }

    public void setHex(String hex) {
        this.hex = hex;
    }

    public String getAsm() {
        return asm;
    }

    public void setAsm(String asm) {
        this.asm = asm;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}