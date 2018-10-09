package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Vjoinsplit {

    @SerializedName("vpub_old")
    @Expose
    private String vpub_old;
    @SerializedName("vpub_new")
    @Expose
    private String vpub_new;
    @SerializedName("n")
    @Expose
    private String n;

    public String getVpub_old() {
        return vpub_old;
    }

    public void setVpub_old(String vpub_old) {
        this.vpub_old = vpub_old;
    }

    public String getVpub_new() {
        return vpub_new;
    }

    public void setVpub_new(String vpub_new) {
        this.vpub_new = vpub_new;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }
}


