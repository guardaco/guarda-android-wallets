package com.guarda.ethereum.models.items;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class TokenHeaderItem extends ExpandableGroup<TokenBodyItem> {

    private String allTokensSum;

    public TokenHeaderItem(String title, List<TokenBodyItem> items, String allTokensSum) {
        super(title, items);
        this.allTokensSum = allTokensSum;
    }

    public String getAllTokensSum() {
        return allTokensSum;
    }

}

