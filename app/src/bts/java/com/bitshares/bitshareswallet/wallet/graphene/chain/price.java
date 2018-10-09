package com.bitshares.bitshareswallet.wallet.graphene.chain;

import com.bitshares.bitshareswallet.wallet.asset;

public class price {
    public asset base;
    public asset quote;

    public price(asset assetBase, asset assetQuote) {
        base = assetBase;
        quote = assetQuote;
    }

    public static price unit_price(object_id<asset_object> assetObjectobjectId) {
        return new price(new asset(1, assetObjectobjectId), new asset(1, assetObjectobjectId));
    }
}
