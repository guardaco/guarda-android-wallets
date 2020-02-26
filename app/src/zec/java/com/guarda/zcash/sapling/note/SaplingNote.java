package com.guarda.zcash.sapling.note;

import com.guarda.zcash.crypto.Utils;
import com.guarda.zcash.globals.TypeConvert;
import com.guarda.zcash.sapling.key.SaplingFullViewingKey;

import java.math.BigInteger;
import java.util.Arrays;

import timber.log.Timber;
import work.samosudov.rustlib.RustAPI;

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
                Utils.bytesToHex(this.d), //TODO: check d when you get from decrypt
                Utils.bytesToHex(Utils.reverseByteArray(this.pk)), // reverse because this pk from native method ivkToPdk
                String.valueOf(TypeConvert.bytesToLong(this.vbytes)),
                Utils.bytesToHex(this.r),
                ak,
                nk,
                String.valueOf(position));
        Timber.d("SaplingNote nullifier result=%s", result);

        return result;
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
