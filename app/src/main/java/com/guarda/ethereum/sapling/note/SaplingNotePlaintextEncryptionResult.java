package com.guarda.ethereum.sapling.note;

public class SaplingNotePlaintextEncryptionResult {

    public SaplingNoteEncryption sne;
    public byte[] secbyte;

    public SaplingNotePlaintextEncryptionResult(byte[] secbyte, SaplingNoteEncryption sne) {
        this.secbyte = secbyte;
        this.sne = sne;
    }
}
