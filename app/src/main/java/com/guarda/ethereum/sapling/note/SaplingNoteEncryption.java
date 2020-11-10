package com.guarda.ethereum.sapling.note;

import com.guarda.ethereum.crypto.Utils;

import java.util.Arrays;

import timber.log.Timber;
import work.samosudov.rustlib.RustAPI;

import static com.guarda.ethereum.crypto.Utils.bytesToHex;

public class SaplingNoteEncryption {

    public byte[] epkbP;
    public byte[] eskbS;
    public String eskstr;

    public SaplingNoteEncryption(byte[] epkbP, byte[] eskbS, String eskstr) {
        this.epkbP = epkbP;
        this.eskbS = eskbS;
        this.eskstr = eskstr;
    }

    public byte[] encryptToRecipient(byte[] pk_d, byte[] dFromNote) {
        Timber.d("encryptToRecipient pk_d=%s", Arrays.toString(pk_d));
        Timber.d("encryptToRecipient eskbS=%s", Arrays.toString(this.eskbS));
        byte[] dhsecret = Utils.reverseByteArray(Utils.hexToBytes(RustAPI.kagree(bytesToHex(Utils.reverseByteArray(pk_d)), this.eskstr)));

        byte[] K = RustAPI.kdfSapling(dhsecret, this.epkbP);
        Timber.d("encryptToRecipient K=%s, s=%s", Arrays.toString(K), K.length);

        byte[] sec2 = RustAPI.encryptNp(K, dFromNote);
        Timber.d("encryptToRecipient sec2=%s %d", Arrays.toString(sec2), sec2.length);
        return sec2;
    }

    @Override
    public String toString() {
        return "SaplingNoteEncryption{" +
                "epkbP=" + Arrays.toString(epkbP) +
                ", eskbS=" + Arrays.toString(eskbS) +
                '}';
    }
}
