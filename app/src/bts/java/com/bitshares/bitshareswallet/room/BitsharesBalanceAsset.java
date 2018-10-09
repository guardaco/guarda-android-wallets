package com.bitshares.bitshareswallet.room;

import com.bitshares.bitshareswallet.wallet.graphene.chain.asset_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.object_id;

/**
 * Created by lorne on 31/10/2017.
 */

public class BitsharesBalanceAsset {
    int id;
    public long amount;
    public String base;
    public long base_precision;
    public String quote;
    public long total;
    public long quote_precision;
    public String currency;
    public long balance;
    public long currency_precision;


}
