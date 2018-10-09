package com.bitshares.bitshareswallet.wallet.graphene.chain;

import com.bitshares.bitshareswallet.wallet.graphene.chain.operation_history_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.operations;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class operation_history_object_deserializer implements JsonDeserializer<operation_history_object> {

    @Override
    public operation_history_object deserialize(JsonElement json,
                                                Type typeOfT,
                                                JsonDeserializationContext context) throws JsonParseException {
        operation_history_object historyObject = new operation_history_object();
        JsonObject jsonObject = json.getAsJsonObject();
        historyObject.id = jsonObject.get("id").getAsString();
        historyObject.block_num = jsonObject.get("block_num").getAsInt();
        historyObject.trx_in_block = jsonObject.get("trx_in_block").getAsInt();
        historyObject.op_in_trx = jsonObject.get("op_in_trx").getAsInt();
        historyObject.virtual_op = jsonObject.get("virtual_op").getAsInt();

        JsonArray jsonArray = jsonObject.get("op").getAsJsonArray();
        int nOp = jsonArray.get(0).getAsInt();

        historyObject.op = new operations.operation_type();
        historyObject.op.nOperationType = nOp;
        // 根据op进行转换
        JsonElement jsonElement = jsonArray.get(1);

        Type type = operations.operations_map.getOperationObjectById(nOp);
        if (type != null) {
            historyObject.op.operationContent = context.deserialize(jsonElement, type);
        } else {
            historyObject.op.operationContent = context.deserialize(jsonElement, type);
        }

        return historyObject;
    }
}