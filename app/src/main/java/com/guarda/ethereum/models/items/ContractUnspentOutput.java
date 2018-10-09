package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class ContractUnspentOutput {
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("amount")
    @Expose
    private BigDecimal amount;
    @SerializedName("vout")
    @Expose
    private Integer vout;
    @SerializedName("scriptPubKey")
    @Expose
    private String txoutScriptPubKey;
    @SerializedName("txid")
    @Expose
    private String txHash;
    @SerializedName("confirmations")
    @Expose
    private Integer confirmations;
    @SerializedName("isStake")
    private boolean isStake;

    public boolean isOutputAvailableToPay() {
        if (isStake) {
            return confirmations > 500;
        }
        return true;
    }

    public ContractUnspentOutput() {
    }

    /**
     * Constructor for unit testing
     */
    public ContractUnspentOutput(Integer confirmations, boolean isStake, BigDecimal amount) {
        this.confirmations = confirmations;
        this.isStake = isStake;
        this.amount = amount;
    }

    public String getAddress() {
        return address;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getVout() {
        return vout;
    }

    public void setVout(Integer vout) {
        this.vout = vout;
    }

    public String getTxoutScriptPubKey() {
        return txoutScriptPubKey;
    }

    public void setTxoutScriptPubKey(String txoutScriptPubKey) {
        this.txoutScriptPubKey = txoutScriptPubKey;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public Integer getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(Integer confirmations) {
        this.confirmations = confirmations;
    }

    @Override
    public String toString() {
        return "address - " + address + "\n"
                + "amount - " + amount + "\n"
                + "vout - " + vout + "\n"
                + "scriptpubkey - " + txoutScriptPubKey + "\n"
                + "txId - " + txHash + "\n"
                + "confirmations - " + confirmations + "\n";
    }
}

