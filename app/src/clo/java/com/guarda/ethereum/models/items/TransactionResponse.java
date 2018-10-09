
package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class TransactionResponse {

    @SerializedName("0")
    @Expose
    private String hash;

    @SerializedName("1")
    @Expose
    private String confirmations;

    @SerializedName("2")
    @Expose
    private String from;

    @SerializedName("3")
    @Expose
    private String to;

    @SerializedName("4")
    @Expose
    private String value;

    @SerializedName("5")
    @Expose
    private String gasProvided;

    @SerializedName("6")
    @Expose
    private String timeStamp;

    private String blockNumber;
    private RawTransactionResponse rawResponse;

    public TransactionResponse() {
    }

    public TransactionResponse(ArrayList<String> txs) {
        this.hash = txs.get(0);
        this.confirmations = txs.get(1);
        this.from = txs.get(2);
        this.to = txs.get(3);
        this.value = txs.get(4);
        this.gasProvided = txs.get(5);
        this.timeStamp = txs.get(6);
    }

    public void setRawResponse(RawTransactionResponse response){
        this.rawResponse = response;
    }

    public boolean haveRawResponse(){
        return rawResponse != null;
    }

    public RawTransactionResponse getRawResponse(){
        return rawResponse;
    }

    public String getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(String blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }


    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(String confirmations) {
        this.confirmations = confirmations;
    }

    public String getGasProvided() {
        return gasProvided;
    }

    public void setGasProvided(String gasProvided) {
        this.gasProvided = gasProvided;
    }

    //    public boolean isError() {
//        return isError != 0;
//    }


    @Override
    public String toString() {
        return "from - " + from
                + "\nto - " + to
                + "\ntime stamp = " + timeStamp
                + "\n";
    }
}