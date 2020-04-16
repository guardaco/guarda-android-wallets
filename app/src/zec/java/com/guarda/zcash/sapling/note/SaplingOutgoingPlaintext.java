package com.guarda.zcash.sapling.note;

import com.google.common.primitives.Bytes;
import com.goterl.lazycode.lazysodium.LazySodiumAndroid;
import com.goterl.lazycode.lazysodium.interfaces.AEAD;
import com.goterl.lazycode.lazysodium.utils.Key;
import com.guarda.zcash.sapling.LsaSingle;

import work.samosudov.rustlib.RustAPI;


public class SaplingOutgoingPlaintext {

    public byte[] pk_d;
    public byte[] esk;

    public SaplingOutgoingPlaintext(byte[] pk_d, byte[] esk) {
        this.pk_d = pk_d;
        this.esk = esk;
    }

    public static byte[] encryptToOurselves(byte[] ovk, byte[] cv, byte[] cm,  byte[] epk, byte[] message) {
        byte[] K = RustAPI.prfOck(ovk, cv, cm, epk);

        byte[] nPub = new byte[AEAD.CHACHA20POLY1305_IETF_NPUBBYTES]; // should be 12 bytes
        LazySodiumAndroid lazySodium = LsaSingle.getInstance();
        byte[] sec = lazySodium.toBinary(lazySodium.encrypt(lazySodium.str(message),null, nPub, Key.fromBytes(K), AEAD.Method.CHACHA20_POLY1305_IETF));

        return sec;
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
