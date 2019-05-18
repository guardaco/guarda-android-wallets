package com.guarda.zcash.sapling.key;

public class SaplingFullViewingKey {

    private String ak;
    private String nk;
    private String ovk;

    public SaplingFullViewingKey(String ak, String nk, String ovk) {
        this.ak = ak;
        this.nk = nk;
        this.ovk = ovk;
    }

    public String getAk() {
        return ak;
    }

    public String getNk() {
        return nk;
    }

    public String getOvk() {
        return ovk;
    }
}
