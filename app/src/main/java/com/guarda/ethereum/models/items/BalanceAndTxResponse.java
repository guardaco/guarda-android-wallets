package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


public class BalanceAndTxResponse {

    @SerializedName("hash160")
    @Expose
    private String hash;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("n_tx")
    @Expose
    private String numTransactions;
    @SerializedName("total_received")
    @Expose
    private String totalReceived;
    @SerializedName("total_sent")
    @Expose
    private String totalSend;

    @SerializedName("final_balance")
    @Expose
    private Long finalBalance = 0L;

    @SerializedName("txs")
    @Expose
    private List<BtcTransaction> transactions;

    public Long getFinalBalance() {
        return finalBalance;
    }


    public List<BtcTransaction> getTransactions() {
        return transactions;
    }


    public class BtcTransaction {
        @SerializedName("inputs")
        @Expose
        private ArrayList<Inputs> inputs;
        @SerializedName("block_height")
        @Expose
        private Long blockHeight;
        @SerializedName("out")
        @Expose
        private ArrayList<BtcOutTx> outs;
        @SerializedName("hash")
        @Expose
        private String hash;
        @SerializedName("time")
        @Expose
        private long time;
        @SerializedName("result")
        @Expose
        private long result;

        @SerializedName("confirmations")
        @Expose
        private long confirmations;

        public long getConfirmations() {
            return confirmations;
        }

        public ArrayList<Inputs> getInputs() {
            return inputs;
        }

        public Long getBlockHeight() {
            return blockHeight;
        }

        public ArrayList<BtcOutTx> getOuts() {
            return outs;
        }

        public String getHash() {
            return hash;
        }

        public long getTime() {
            return time;
        }

        public long getResult() {
            return result;
        }

    }

    public class Inputs {
        @SerializedName("prev_out")
        @Expose
        private BtcOutTx prevOut;

        public BtcOutTx getOut() {
            return prevOut;
        }

    }

    public class BtcOutTx {
        @SerializedName("spent")
        @Expose
        private boolean spent;
        @SerializedName("addr")
        @Expose
        private String address;
        @SerializedName("value")
        @Expose
        private Long value;

        public boolean isSpent() {
            return spent;
        }

        public String getAddress() {
            return address;
        }

        public Long getValue() {
            return value;
        }
    }

}
