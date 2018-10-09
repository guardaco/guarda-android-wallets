package com.bitshares.bitshareswallet.wallet;

import com.bitshares.bitshareswallet.wallet.graphene.chain.compact_signature;


public class public_key {
    private byte[] key_data = new byte[33];

    public public_key(byte[] key) {
        System.arraycopy(key, 0, key_data, 0, key_data.length);
    }

    public byte[] getKeyByte() {
        return key_data;
    }

    public static boolean is_canonical(compact_signature c) {
        /*return !(c.data[1] & 0x80)
                && !(c.data[1] == 0 && !(c.data[2] & 0x80))
                && !(c.data[33] & 0x80)
                && !(c.data[33] == 0 && !(c.data[34] & 0x80));*/

        boolean bCompareOne = ((c.data[1] & 0x80) == 0);
        boolean bCompareTwo = ((c.data[1] == 0) && ((c.data[2] & 0x80) == 0)) == false;
        boolean bCompareThree = ((c.data[33] & 0x80) == 0);
        boolean bCompareFour = ((c.data[33] == 0) && ((c.data[34] & 0x80) ==0)) == false;

        return bCompareOne && bCompareTwo && bCompareThree && bCompareFour;
    }
}
