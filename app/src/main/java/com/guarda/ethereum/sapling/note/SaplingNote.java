package com.guarda.ethereum.sapling.note;

import com.guarda.ethereum.globals.TypeConvert;
import com.guarda.ethereum.sapling.key.SaplingCustomFullKey;
import com.guarda.ethereum.sapling.key.SaplingFullViewingKey;

import java.math.BigInteger;
import java.util.Arrays;

import timber.log.Timber;
import work.samosudov.rustlib.RustAPI;
import work.samosudov.zecrustlib.ZecLibRustApi;

import static com.guarda.ethereum.crypto.Utils.bytesToHex;
import static com.guarda.ethereum.crypto.Utils.reverseByteArray;

public class SaplingNote {
    public byte[] d;
    public byte[] pk;
    public byte[] vbytes;
    BigInteger value;
    public byte[] r;
    public String rStr;

    //TODO: one constructor for both - r bytes and r hex (convert inside)
    public SaplingNote(byte[] d, byte[] pk, byte[] vbytes, byte[] r) {
        this.d = d;
        this.pk = pk;
        this.vbytes = vbytes;
        this.r = r;
    }

    public SaplingNote(byte[] d, byte[] pk, BigInteger value, byte[] r, String rStr) {
        this.d = d;
        this.pk = pk;
        this.value = value;
        this.r = r;
        this.rStr = rStr;
    }

    public String nullifier(SaplingFullViewingKey fvk, int position) {
        String ak = fvk.getAk();
        String nk = fvk.getNk();

        String result = RustAPI.computeNf(
                bytesToHex(this.d), //TODO: check d when you get from decrypt
                bytesToHex(reverseByteArray(this.pk)), // reverse because this pk from native method ivkToPdk
                String.valueOf(TypeConvert.bytesToLong(this.vbytes)),
                bytesToHex(this.r),
                ak,
                nk,
                String.valueOf(position));
        Timber.d("SaplingNote nullifier result=%s", result);

        return result;
    }

    public String nullifierCanopy(SaplingCustomFullKey fullKey, SaplingNotePlaintext snp, int position) {
        byte[] ivkBytes = fullKey.getIvk().clone();
        byte[] plainTextBytes = snp.toBytesCompactV2();
        byte[] akBytes = fullKey.getAk().clone();
        byte[] nkBytes = fullKey.getNk().clone();

        byte[] nfBytes = ZecLibRustApi.nullifier(
                ivkBytes,
                plainTextBytes,
                akBytes,
                nkBytes,
                position
        );
        String nfString = bytesToHex(nfBytes);
        Timber.d("SaplingNote nullifier nfString=%s", nfString);

        return nfString;
    }

    @Override
    public String toString() {
        return "SaplingNote{" +
                "d=" + Arrays.toString(d) +
                ", pk=" + Arrays.toString(pk) +
                ", vbytes=" + Arrays.toString(vbytes) +
                ", value=" + value +
                ", r=" + Arrays.toString(r) +
                ", rStr='" + rStr + '\'' +
                '}';
    }
}
