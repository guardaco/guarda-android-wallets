
package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public class AddressInfoResp {

    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("tokens")
    @Expose
    private List<Token> tokens;

    public String getAddress() {
        return address;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public class Token {
        @SerializedName("tokenInfo")
        @Expose
        private TokenInfo tokenInfo;
        @SerializedName("balance")
        @Expose
        private BigDecimal balance;

        public TokenInfo getTokenInfo() {
            return tokenInfo;
        }

        public BigDecimal getBalance() {
            return balance;
        }
    }

    public class TokenInfo {
        @SerializedName("address")
        @Expose
        private String address;
        @SerializedName("decimals")
        @Expose
        private int decimals;
        @SerializedName("symbol")
        @Expose
        private String symbol;

        public String getAddress() {
            return address;
        }

        public int getDecimals() {
            return decimals;
        }

        public String getSymbol() {
            return symbol;
        }
    }
}