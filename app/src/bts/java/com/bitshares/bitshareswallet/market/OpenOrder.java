package com.bitshares.bitshareswallet.market;


import com.bitshares.bitshareswallet.wallet.graphene.chain.asset_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.limit_order_object;


public class OpenOrder {
    public limit_order_object limitOrder;
    public asset_object base;
    public asset_object quote;
    public double price;
}
