package com.guarda.ethereum.sapling.key;

import java.util.Arrays;

public class SaplingCustomFullKey {

    private byte[] ask = new byte[32];
    private byte[] nsk = new byte[32];
    private byte[] ovk = new byte[32];

    private byte[] ak = new byte[32];
    private byte[] nk = new byte[32];
    private byte[] ivk = new byte[32];

    private byte[] d = new byte[11]; //11 bytes
    private byte[] pkd = new byte[32];

    public SaplingCustomFullKey(byte[] seq) {
        System.arraycopy(seq, 0, ask, 0, 32);
        System.arraycopy(seq, 32, nsk, 0, 32);
        System.arraycopy(seq, 64, ovk, 0, 32);

        System.arraycopy(seq, 96, ak, 0, 32);
        System.arraycopy(seq, 128, nk, 0, 32);
        System.arraycopy(seq, 160, ivk, 0, 32);

        System.arraycopy(seq, 192, d, 0, 11);
        System.arraycopy(seq, 203, pkd, 0, 32);
    }

    public byte[] getAsk() {
        return ask;
    }

    public byte[] getNsk() {
        return nsk;
    }

    public byte[] getOvk() {
        return ovk;
    }

    public byte[] getAk() {
        return ak;
    }

    public byte[] getNk() {
        return nk;
    }

    public byte[] getIvk() {
        return ivk;
    }

    public byte[] getD() {
        return d;
    }

    public byte[] getPkd() {
        return pkd;
    }

    @Override
    public String toString() {
        return "SaplingCustomFullKey{" +
                "ask=" + Arrays.toString(ask) +
                ", nsk=" + Arrays.toString(nsk) +
                ", ovk=" + Arrays.toString(ovk) +
                ", ak=" + Arrays.toString(ak) +
                ", nk=" + Arrays.toString(nk) +
                ", ivk=" + Arrays.toString(ivk) +
                ", d=" + Arrays.toString(d) +
                ", pkd=" + Arrays.toString(pkd) +
                '}';
    }

}
