package com.bitshares.bitshareswallet.wallet;

import com.bitshares.bitshareswallet.wallet.graphene.chain.limit_order_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.operations;

import java.util.List;

/**
 * Created by lorne on 22/09/2017.
 */
public class BitsharesNoticeMessage {
    int nSubscriptionId;

    // market_notice
    List<operations.operation_type> listFillOrder;
    List<limit_order_object> listOrderObject;

    // account
    boolean bAccountChanged;
}
