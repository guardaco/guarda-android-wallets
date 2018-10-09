package com.guarda.ethereum.models.items;

/**
 * Created by SV on 31.08.2017.
 */

public class RawTransactionResponse {

    private String hash;
    private String rawTxValue;
    private String blockNumber;

    public RawTransactionResponse(String hash, String rawTxValue) {
        this(hash, rawTxValue, null);
    }

    public RawTransactionResponse(String hash, String rawTxValue, String blockNumber) {
        this.hash = hash;
        this.rawTxValue = rawTxValue;
        this.blockNumber = blockNumber;
    }

    public String getHash() {
        return hash;
    }

    public String getRawTxValue() {
        return rawTxValue;
    }

    public String getBlockNumber() {
        return blockNumber;
    }
}
