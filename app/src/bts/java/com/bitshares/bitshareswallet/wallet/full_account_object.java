package com.bitshares.bitshareswallet.wallet;


import com.bitshares.bitshareswallet.wallet.graphene.chain.limit_order_object;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class full_account_object {
    public account_object account;
    public List<limit_order_object> limit_orders;
    public List<account_balance_object> balances;
}
