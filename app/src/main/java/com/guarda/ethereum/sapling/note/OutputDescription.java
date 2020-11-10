package com.guarda.ethereum.sapling.note;

import com.guarda.ethereum.crypto.Utils;

import java.util.Arrays;

public class OutputDescription {

    private byte[] cv;
    private byte[] cmu;
    private byte[] ephemeralKey;
    private byte[] encCiphertext;
    private byte[] outCiphertext;
    private byte[] zkproof;

    public OutputDescription(byte[] cv, byte[] cmu, byte[] ephemeralKey, byte[] encCiphertext, byte[] outCiphertext, byte[] zkproof) {
        this.cv = cv;
        this.cmu = cmu;
        this.ephemeralKey = ephemeralKey;
        this.encCiphertext = encCiphertext;
        this.outCiphertext = outCiphertext;
        this.zkproof = zkproof;
    }

    @Override
    public String toString() {
        return "OutputDescription: \ncv=" + Arrays.toString(cv) + " size=" + cv.length + "\n" +
                "cmu=" + Arrays.toString(cmu) + " size=" + cmu.length + "\n" +
                "ephemeralKey=" + Arrays.toString(ephemeralKey) + " size=" + ephemeralKey.length  + "\n" +
                "encCiphertext=" + Arrays.toString(encCiphertext) + " size=" + encCiphertext.length  + "\n" +
                "outCiphertext=" + Arrays.toString(outCiphertext) + " size=" + outCiphertext.length  + "\n" +
                "zkproof=" + Arrays.toString(zkproof) + " size=" + zkproof.length + "\n" +
                "hexs:\n" +
                "cv=" + Utils.bytesToHex(cv) + "\n" +
                "cmu=" + Utils.bytesToHex(cmu) + "\n" +
                "ephemeralKey=" + Utils.bytesToHex(ephemeralKey) + "\n" +
                "encCiphertext=" + Utils.bytesToHex(encCiphertext) + "\n" +
                "outCiphertext=" + Utils.bytesToHex(outCiphertext) + "\n" +
                "zkproof=" + Utils.bytesToHex(zkproof);
    }

}
