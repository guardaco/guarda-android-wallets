package com.guarda.zcash.sapling.note;

import com.google.common.primitives.Bytes;
import com.goterl.lazycode.lazysodium.LazySodiumAndroid;
import com.goterl.lazycode.lazysodium.interfaces.AEAD;
import com.goterl.lazycode.lazysodium.utils.Key;
import com.guarda.zcash.sapling.LsaSingle;

import timber.log.Timber;


public class SaplingOutgoingPlaintext {

    private static final byte[] ZCASH_DERIVE_OCK = {'Z','c','a','s','h','_','D','e','r','i','v','e','_','o','c','k'}; //Zcash_Derive_ock

    public byte[] pk_d;
    public byte[] esk;

    public SaplingOutgoingPlaintext(byte[] pk_d, byte[] esk) {
        this.pk_d = pk_d;
        this.esk = esk;
    }

    public static byte[] encryptToOurselves(byte[] ovk, byte[] cv, byte[] cm,  byte[] epk, byte[] message) {
        byte[] K = new byte[32];
        K = PRF_ock(ovk, cv, cm, epk);

        byte[] sec = new byte[0];
        byte[] nPub = new byte[AEAD.CHACHA20POLY1305_IETF_NPUBBYTES]; // should be 12 bytes
        LazySodiumAndroid lazySodium = LsaSingle.getInstance();
        sec = lazySodium.toBinary(lazySodium.encrypt(lazySodium.str(message),null, nPub, Key.fromBytes(K), AEAD.Method.CHACHA20_POLY1305_IETF));

        return sec;
    }

    public static byte[] PRF_ock(byte[] ovk, byte[] cv, byte[] cm, byte[] epk) {
        byte[] K = new byte[32];

        byte[] block = new byte[128];
        System.arraycopy(ovk, 0, block, 0, 32);
        System.arraycopy(cv, 0, block, 32, 32);
        System.arraycopy(cm, 0, block, 64, 32);
        System.arraycopy(epk, 0, block, 96, 32);

        LazySodiumAndroid lazySodium = LsaSingle.getInstance();
        if (lazySodium.getSodium().crypto_generichash_blake2b_salt_personal(K, 32, block, 128, null, 0, 0L, ZCASH_DERIVE_OCK) != 0) {
            Timber.d("PRF_ock - lazySodium.getSodium().crypto_generichash_blake2b_salt_personal() != 0  ");
        }

        return K;
    }

    /**
     * pk_d - 8 bytes
     * esk - 8 bytes
     */
    public byte[] toByte() {
        byte[] bytes = new byte[0];
        return Bytes.concat(bytes, this.pk_d, this.esk);
    }
}
