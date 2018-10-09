
package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TransactionResponse {

    @SerializedName("blockNumber")
    @Expose
    private String blockNumber;
    @SerializedName("timeStamp")
    @Expose
    private long timeStamp;
    @SerializedName("hash")
    @Expose
    private String hash;
    @SerializedName("from")
    @Expose
    private String from;
    @SerializedName("to")
    @Expose
    private String to;
    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("confirmations")
    @Expose
    private String confirmations;
    @SerializedName("isError")
    @Expose
    private int isError;

    private RawTransactionResponse rawResponse;

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

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
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

    public boolean isError() {
        return isError != 0;
    }


    @Override
    public String toString() {
        return "from - " + from
                + "\nto - " + to
                + "\ntime stamp = " + timeStamp
                + "\n";
    }
}