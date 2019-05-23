package com.guarda.zcash.sapling.note;

public class ProofAndCv {

    public byte[] proof;
    public byte[] cv;
    public byte[] rcv;

    public ProofAndCv(byte[] proof, byte[] cv, byte[] rcv) {
        this.proof = proof;
        this.cv = cv;
        this.rcv = rcv;
    }
}
