package com.guarda.ethereum;

import com.google.common.primitives.Bytes;
import com.guarda.ethereum.crypto.Utils;

import java.util.Collections;
import java.util.List;

public class TxInTranspatent {

    byte[] txid;
    long index;
    byte[] script;
    int sequence = 0xffffffff;
    long value;

    TxInTranspatent(ZCashTransactionOutput base) {
        List<Byte> txbytes = Bytes.asList(Utils.hexToBytes(base.txid));
        Collections.reverse(txbytes);
        txid = Bytes.toArray(txbytes);
        index = base.n;
        script = Utils.hexToBytes(base.hex);
        this.value = base.value;
    }

}
