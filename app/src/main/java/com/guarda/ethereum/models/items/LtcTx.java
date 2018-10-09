package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by psd on 13.02.2018.
 */

public class LtcTx {

    @SerializedName("tx_hash")
    @Expose
    private String tx_hash;

    @SerializedName("block_height")
    @Expose
    private String block_height;

    @SerializedName("tx_input_n")
    @Expose
    private String tx_input_n;

    @SerializedName("tx_output_n")
    @Expose
    private String tx_output_n;

    @SerializedName("value")
    @Expose
    private String value;

    @SerializedName("ref_balance")
    @Expose
    private String ref_balance;

    @SerializedName("spent")
    @Expose
    private String spent;

    @SerializedName("spent_by")
    @Expose
    private String spent_by;

    @SerializedName("confirmations")
    @Expose
    private String confirmations;
    @SerializedName("confirmed")
    @Expose
    private String confirmed;

    @SerializedName("double_spend")
    @Expose
    private String double_spend;

    public String getTx_hash() {
        return tx_hash;
    }

    public void setTx_hash(String tx_hash) {
        this.tx_hash = tx_hash;
    }

    public String getBlock_height() {
        return block_height;
    }

    public void setBlock_height(String block_height) {
        this.block_height = block_height;
    }

    public String getTx_input_n() {
        return tx_input_n;
    }

    public void setTx_input_n(String tx_input_n) {
        this.tx_input_n = tx_input_n;
    }

    public String getTx_output_n() {
        return tx_output_n;
    }

    public void setTx_output_n(String tx_output_n) {
        this.tx_output_n = tx_output_n;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRef_balance() {
        return ref_balance;
    }

    public void setRef_balance(String ref_balance) {
        this.ref_balance = ref_balance;
    }

    public String getSpent() {
        return spent;
    }

    public void setSpent(String spent) {
        this.spent = spent;
    }

    public String getSpent_by() {
        return spent_by;
    }

    public void setSpent_by(String spent_by) {
        this.spent_by = spent_by;
    }

    public String getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(String confirmations) {
        this.confirmations = confirmations;
    }

    public String getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(String confirmed) {
        this.confirmed = confirmed;
    }

    public String getDouble_spend() {
        return double_spend;
    }

    public void setDouble_spend(String double_spend) {
        this.double_spend = double_spend;
    }
}
