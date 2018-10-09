package com.guarda.ethereum.models.items;


import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;

public class TokenBodyItem implements Parcelable {

    private String tokenName;
    private BigDecimal tokenNum;
    private String tokenSum;
    private Double otherSum = 0.0;
    private int decimal;

    public TokenBodyItem(String tokenName, BigDecimal tokenNum, String tokenSum, int decimal) {
        this.tokenName = tokenName;
        this.tokenNum = tokenNum;
        this.tokenSum = tokenSum;
        this.decimal = decimal;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public BigDecimal getTokenNum() {
        return tokenNum;
    }

    public void setTokenNum(BigDecimal tokenNum) {
        this.tokenNum = tokenNum;
    }

    public String getTokenSum() {
        return tokenSum;
    }

    public void setTokenSum(String tokenSum) {
        this.tokenSum = tokenSum;
    }

    public int getDecimal() {
        return decimal;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tokenName);
        dest.writeString(tokenNum.toString());
        dest.writeString(tokenSum);
    }

    public TokenBodyItem(Parcel in) {
        tokenName = in.readString();
        tokenNum = new BigDecimal(in.readString());
        tokenSum = in.readString();
    }

    public static final Creator<TokenBodyItem> CREATOR = new Creator<TokenBodyItem>() {
        @Override
        public TokenBodyItem createFromParcel(Parcel in) {
            return new TokenBodyItem(in);
        }

        @Override
        public TokenBodyItem[] newArray(int size) {
            return new TokenBodyItem[size];
        }
    };

    public Double getOtherSum() {
        return otherSum;
    }

    public void setOtherSum(Double otherSum) {
        this.otherSum = otherSum;
    }
}
