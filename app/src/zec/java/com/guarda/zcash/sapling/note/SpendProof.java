package com.guarda.zcash.sapling.note;

import com.google.common.primitives.Bytes;

public class SpendProof {

    private byte[] cv;
    private byte[] anchor;
    private byte[] nullifier;
    private byte[] rk;
    private byte[] zkproof;
    private byte[] alpha;

    public SpendProof(
            byte[] cv,
            byte[] anchor,
            byte[] nullifier,
            byte[] rk,
            byte[] zkproof,
            byte[] alpha) {
        this.cv = cv;
        this.anchor = anchor;
        this.nullifier = nullifier;
        this.rk = rk;
        this.zkproof = zkproof;
        this.alpha = alpha;
    }

    public byte[] getBytes() {
        // cv, anchor, nf, rk, spProof
        return Bytes.concat(cv, anchor, nullifier, rk, zkproof);
    }

    public byte[] getBytesAndAlpha() {
        // cv, anchor, nf, rk, spProof, alpha
        return Bytes.concat(cv, anchor, nullifier, rk, zkproof, alpha);
    }

    public static SpendProof fromBytesWithAlpha(byte[] byteWithAlpha) {

        byte[] cv = new byte[32];
        byte[] anchor = new byte[32];
        byte[] nullifier = new byte[32];
        byte[] rk = new byte[32];
        byte[] zkproof = new byte[192];
        byte[] alpha = new byte[192];

        System.arraycopy(byteWithAlpha, 0, cv, 0, 32);
        System.arraycopy(byteWithAlpha, 32, anchor, 0, 32);
        System.arraycopy(byteWithAlpha, 64, nullifier, 0, 32);
        System.arraycopy(byteWithAlpha, 96, rk, 0, 32);
        System.arraycopy(byteWithAlpha, 128, zkproof, 0, 192);
        System.arraycopy(byteWithAlpha, 128 + 192, alpha, 0, 32);

        return new SpendProof(cv, anchor, nullifier, rk, zkproof, alpha);
    }


    public byte[] getCv() {
        return cv;
    }

    public byte[] getAnchor() {
        return anchor;
    }

    public byte[] getNullifier() {
        return nullifier;
    }

    public byte[] getRk() {
        return rk;
    }

    public byte[] getZkproof() {
        return zkproof;
    }

    public byte[] getAlpha() {
        return alpha;
    }
}
