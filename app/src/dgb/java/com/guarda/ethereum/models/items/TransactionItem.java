package com.guarda.ethereum.models.items;


public class TransactionItem {

    private String hash;
    private long time;
    private long sum;
    private boolean isReceived;
    private long confirmations;
    private String from;
    private String to;
    private boolean isOut;


    public String getFrom() {
        return from;
    }

    public boolean isOut() {
        return isOut;
    }

    public void setOut(boolean out) {
        isOut = out;
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

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getValue() {
        return sum;
    }

    public void setSum(long sum) {
        this.sum = sum;
    }

    public boolean isReceived() {
        return isReceived;
    }

    public void setReceived(boolean received) {
        isReceived = received;
    }

    public long getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(long confirmations) {
        this.confirmations = confirmations;
    }

}
