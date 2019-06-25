package com.guarda.zcash;

import com.google.common.primitives.Bytes;
import com.guarda.zcash.crypto.Utils;

public class TxOutTranspatent {

    long value;
    byte[] script;

    TxOutTranspatent(byte[] pubKeyHash, long value) {
        this.value = value;
        script = Bytes.concat(new byte[]{(byte) 0x76, (byte) 0xa9, (byte) 0x14}, pubKeyHash, new byte[]{(byte) 0x88, (byte) 0xac});
        //                                OP_DUP       OP_HASH160  20_bytes     <PubkeyHash>           OP_EQUALVERIFY OP_CHECKSIG
    }

    byte[] getBytes() {
        return Bytes.concat(Utils.int64BytesLE(value), Utils.compactSizeIntLE(script.length), script);
    }

}
