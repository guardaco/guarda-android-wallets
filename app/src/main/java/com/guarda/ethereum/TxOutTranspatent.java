package com.guarda.ethereum;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.common.primitives.Bytes;
import com.guarda.ethereum.crypto.Utils;

import java.util.Arrays;

public class TxOutTranspatent {

    private static final byte[] addressPrefixT1 = new byte[] { (byte) 0x1c, (byte) 0xb8 };
    private static final byte[] addressPrefixT3 = new byte[] { (byte) 0x1c, (byte) 0xbd };

    private static final byte[] OP_DUP = new byte[] { (byte) 0x76 };
    private static final byte[] OP_HASH160 = new byte[] { (byte) 0xa9, (byte) 0x14 };
    private static final byte[] OP_EQUALVERIFY = new byte[] { (byte) 0x88 };
    private static final byte[] OP_CHECKSIG = new byte[] { (byte) 0xac };
    private static final byte[] OP_EQUAL = new byte[] { (byte) 0x87 };

    // t1 address (standard)
    private static final byte[] prefixScriptT1 = ArrayUtils.concatByteArrays(OP_DUP, OP_HASH160);
    private static final byte[] postfixScriptT1 = ArrayUtils.concatByteArrays(OP_EQUALVERIFY, OP_CHECKSIG);
    // t3 address (Multisig)
    private static final byte[] prefixScriptT3 = OP_HASH160;
    private static final byte[] postfixScriptT3 = OP_EQUAL;

    long value;
    byte[] script;

    TxOutTranspatent(byte[] pubKeyHash, long value) {
        if (pubKeyHash.length != 22) throw new IllegalArgumentException("TxOutTranspatent pubKeyHash length != 22 length=" + pubKeyHash.length);
        byte[] firstTwoBytesPubKeyHash = Arrays.copyOfRange(pubKeyHash, 0, 2);
        byte[] croppedPubKeyHash = Arrays.copyOfRange(pubKeyHash, 2, 22);
        this.value = value;

        /**
         * separate two addresses:
         * t1 - normal transparent address and
         * t3 - Multisig address
         */

        // build T1 address script
        if (Arrays.equals(firstTwoBytesPubKeyHash, addressPrefixT1)) {
            script = Bytes.concat(prefixScriptT1, croppedPubKeyHash, postfixScriptT1);
        } else if (Arrays.equals(firstTwoBytesPubKeyHash, addressPrefixT3)) {
            script = Bytes.concat(prefixScriptT3, croppedPubKeyHash, postfixScriptT3);
        } else {
            throw new IllegalArgumentException("TxOutTranspatent pubKeyHash first two bytes wrong, pubKeyHash=" + Arrays.toString(pubKeyHash));
        }
    }

    byte[] getBytes() {
        return Bytes.concat(Utils.int64BytesLE(value), Utils.compactSizeIntLE(script.length), script);
    }

}
