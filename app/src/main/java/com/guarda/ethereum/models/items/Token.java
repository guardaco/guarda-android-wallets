package com.guarda.ethereum.models.items;

import java.math.BigDecimal;

/**
 * Created by SV on 06.10.2017.
 */

public class Token {

    private String name;
    private BigDecimal balance;

    public Token(String name, BigDecimal balance) {
        this.name = name;
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
