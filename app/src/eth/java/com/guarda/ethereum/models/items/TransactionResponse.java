
package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.web3j.utils.Convert;

import java.math.BigDecimal;

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

    private String ticker = "";
    private String dataValue;

    private RawTransactionResponse rawResponse;

    public TransactionResponse() {
    }

    //only for adding token's transaction
    public TransactionResponse(TokenTxResponse ttr, int precision) {
        this.blockNumber = ttr.getBlockNumber();
        this.timeStamp = ttr.getTimeStamp();
        this.hash = ttr.getHash();
        this.from = ttr.getFrom();
        this.to = ttr.getTo();
        BigDecimal v = new BigDecimal(ttr.getValue());
//        this.value = v.multiply(BigDecimal.TEN.pow(precision)).toBigInteger().toString();
        this.value = v.toBigInteger().toString();
        this.confirmations = ttr.getConfirmations();
        this.isError = ttr.getIsError();
        this.ticker = ttr.getTicker();
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

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getDataValue() {
        return dataValue;
    }

    public void setDataValue(String dataValue) {
        this.dataValue = dataValue;
    }

    @Override
    public String toString() {
        return "from - " + from
                + "\nto - " + to
                + "\ntime stamp = " + timeStamp
                + "\n";
    }
}