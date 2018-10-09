package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


public class CoinifySignUpResponse {

    @SerializedName("trader")
    @Expose
    private TraidetCoinify trader;
    @SerializedName("offlineToken")
    @Expose
    private String offlineToken;


    public class TraidetCoinify {
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("email")
        @Expose
        private String email;
        @SerializedName("password")
        @Expose
        private String password;



        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public TraidetCoinify getTrader() {
        return trader;
    }

    public void setTrader(TraidetCoinify trader) {
        this.trader = trader;
    }

    public String getOfflineToken() {
        return offlineToken;
    }

    public void setOfflineToken(String offlineToken) {
        this.offlineToken = offlineToken;
    }
}
