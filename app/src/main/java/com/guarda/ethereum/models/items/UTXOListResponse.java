package com.guarda.ethereum.models.items;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UTXOListResponse {

    @SerializedName("unspent_outputs")
    @Expose
    private List<UTXOItem> UTXOList;

    public List<UTXOItem> getUTXOList() {
        return UTXOList;
    }

    public class UTXOItem{

        @SerializedName("tx_hash_big_endian")
        @Expose
        private String txHash;
        @SerializedName("tx_output_n")
        @Expose
        private long txOutputN;
        @SerializedName("value")
        @Expose
        private long satoshiValue;

        @SerializedName("confirmations")
        @Expose
        private String confirmations;

        public String getTxHash() {
            return txHash;
        }

        public long getTxOutputN() {
            return txOutputN;
        }

        public long getSatoshiValue() {
            return satoshiValue;
        }

        public String getConfirmations() {
            return confirmations;
        }
    }

}
