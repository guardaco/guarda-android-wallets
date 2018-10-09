package com.bitshares.bitshareswallet.wallet.fc.crypto;

import android.provider.Settings;

import com.google.common.io.BaseEncoding;

import org.spongycastle.crypto.digests.SHA512Digest;

import java.util.Arrays;


public class sha512_object {
    public byte[] hash = new byte[64];

    @Override
    public String toString() {
        BaseEncoding encoding = BaseEncoding.base16().lowerCase();
        return encoding.encode(hash);
    }

    @Override
    public boolean equals(Object obj) {
        sha512_object sha512Object = (sha512_object)obj;
        return Arrays.equals(hash, sha512Object.hash);
    }

    public static sha512_object create_from_string(String strContent) {
        SHA512Digest digest = new SHA512Digest();
        byte[] bytePassword = strContent.getBytes();
        digest.update(bytePassword, 0, bytePassword.length);

        byte[] byteHash = new byte[64];
        digest.doFinal(byteHash, 0);

        sha512_object sha512Object = new sha512_object();
        System.arraycopy(byteHash, 0, sha512Object.hash, 0, byteHash.length);

        return sha512Object;
    }

    public static sha512_object create_from_byte_array(byte[] byteArray, int offset, int length) {
        SHA512Digest digest = new SHA512Digest();
        digest.update(byteArray, offset, length);

        byte[] byteHash = new byte[64];
        digest.doFinal(byteHash, 0);

        sha512_object sha512Object = new sha512_object();
        System.arraycopy(byteHash, 0, sha512Object.hash, 0, byteHash.length);

        return sha512Object;
    }
}
