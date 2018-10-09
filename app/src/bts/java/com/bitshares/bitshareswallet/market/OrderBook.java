package com.bitshares.bitshareswallet.market;


import java.util.List;


public class OrderBook {
    public String base;
    public String quote;
    public List<Order> bids;
    public List<Order> asks;
}
