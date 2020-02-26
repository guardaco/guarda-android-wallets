package com.guarda.zcash.sapling.note;

import com.goterl.lazycode.lazysodium.LazySodiumAndroid;
import com.guarda.zcash.crypto.Utils;
import com.guarda.zcash.sapling.LsaSingle;

import java.util.Arrays;

import timber.log.Timber;
import work.samosudov.rustlib.RustAPI;

import static com.guarda.zcash.crypto.Utils.bytesToHex;
import static com.guarda.zcash.crypto.Utils.revHex;

public class SaplingNoteEncryption {

    public byte[] epkbP;
    public byte[] eskbS;
    public String eskstr;
    private static final byte[] ZCASH_SAPLING_KDF = {'Z','c','a','s','h','_','S','a','p','l','i','n','g','K','D','F'}; //Zcash_SaplingKDF

    public SaplingNoteEncryption(byte[] epkbP, byte[] eskbS, String eskstr) {
        this.epkbP = epkbP;
        this.eskbS = eskbS;
        this.eskstr = eskstr;
    }

    public byte[] encryptToRecipient(byte[] pk_d, byte[] dFromNote) {
        byte[] dhsecret = new byte[0];
        Timber.d("encryptToRecipient pk_d=%s", Arrays.toString(pk_d));
        Timber.d("encryptToRecipient eskbS=%s", Arrays.toString(this.eskbS));
        dhsecret = Utils.reverseByteArray(Utils.hexToBytes(RustAPI.kagree(bytesToHex(Utils.reverseByteArray(pk_d)), this.eskstr)));

        byte[] K = new byte[32];
        K = KDFSapling(dhsecret, this.epkbP);
        Timber.d("encryptToRecipient K=%s, s=%s", Arrays.toString(K), K.length);

        byte[] sec2 = new byte[0];
        sec2 = RustAPI.encryptNp(K, dFromNote);
        Timber.d("encryptToRecipient sec2=%s %d", Arrays.toString(sec2), sec2.length);
        return sec2;
    }

    public static byte[] KDFSapling(byte[] dhsecret, byte[] epk) {
        byte[] block = new byte[64];
        System.arraycopy(dhsecret, 0, block, 0, 32);
        System.arraycopy(epk, 0, block, 32, 32);

        byte[] K = new byte[32];
        LazySodiumAndroid lazySodium = LsaSingle.getInstance();
        if (lazySodium.getSodium().crypto_generichash_blake2b_salt_personal(K, 32, block, 64, null, 0, 0L, ZCASH_SAPLING_KDF) != 0) {
            Timber.e("KDFSapling - lazySodium.getSodium().crypto_generichash_blake2b_salt_personal() != 0  ");
        }

        return K;
    }

    @Override
    public String toString() {
        return "SaplingNoteEncryption{" +
                "epkbP=" + Arrays.toString(epkbP) +
                ", eskbS=" + Arrays.toString(eskbS) +
                '}';
    }
}
