package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class CoinifyKYCResponse {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("traderId")
    @Expose
    private int traderId;
    @SerializedName("state")
    @Expose
    private String state;
    @SerializedName("returnUrl")
    @Expose
    private String returnUrl;
    @SerializedName("redirectUrl")
    @Expose
    private String redirectUrl;
    @SerializedName("externalId")
    @Expose
    private String externalId;
    @SerializedName("updateTime")
    @Expose
    private String updateTime;
    @SerializedName("createTime")
    @Expose
    private String createTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTraderId() {
        return traderId;
    }

    public void setTraderId(int traderId) {
        this.traderId = traderId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
