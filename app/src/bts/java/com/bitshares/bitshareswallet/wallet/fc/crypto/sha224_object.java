package com.bitshares.bitshareswallet.wallet.fc.crypto;

import com.google.common.io.BaseEncoding;

import org.spongycastle.crypto.digests.SHA224Digest;
import org.spongycastle.crypto.digests.SHA512Digest;

import java.util.Arrays;

/**
 * Created by lorne on 08/09/2017.
 */

public class sha224_object {
    public byte[] hash = new byte[28];

    @Override
    public String toString() {
        BaseEncoding encoding = BaseEncoding.base16().lowerCase();
        return encoding.encode(hash);
    }

    @Override
    public boolean equals(Object obj) {
        sha224_object sha224Object = (sha224_object)obj;
        return Arrays.equals(hash, sha224Object.hash);
    }

    public static sha224_object create_from_byte_array(byte[] byteArray, int offset, int length) {
        SHA224Digest digest = new SHA224Digest();
        digest.update(byteArray, offset, length);

        byte[] byteHash = new byte[28];
        digest.doFinal(byteHash, 0);

        sha224_object sha224Object = new sha224_object();
        System.arraycopy(byteHash, 0, sha224Object.hash, 0, byteHash.length);

        return sha224Object;
    }
}
