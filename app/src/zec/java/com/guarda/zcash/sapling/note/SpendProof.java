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
