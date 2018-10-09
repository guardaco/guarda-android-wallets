package com.guarda.ethereum.models;

import android.graphics.drawable.Drawable;

public class ExchangeSpinnerRowModel {
    public Drawable icon;
    public String text = "";
    public String symbol = "";
    public String url = "";

    public ExchangeSpinnerRowModel(Drawable icon, String text, String symbol) {
        this.icon = icon;
        this.text = text;
        this.symbol = symbol;
    }

    public ExchangeSpinnerRowModel(Drawable icon, String text, String symbol, String url) {
        this.icon = icon;
        this.text = text;
        this.symbol = symbol;
        this.url = url;
    }

}
