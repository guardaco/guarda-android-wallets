package com.bitshares.bitshareswallet.room;

import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;

import com.bitshares.bitshareswallet.wallet.account_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.asset_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.global_config_object;
import com.bitshares.bitshareswallet.wallet.graphene.chain.object_id;
import com.bitshares.bitshareswallet.wallet.graphene.chain.operations;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by lorne on 31/10/2017.
 */

public class RoomConverters {
    @TypeConverter
    public static String fromAssetObjectId(object_id<asset_object> id) {
        return id.toString();
    }

    @TypeConverter
    public static object_id<asset_object> stringToObjectId(String strId) {
        return object_id.create_from_string(strId);
    }

    @TypeConverter
    public static String fromAccountObjectId(object_id<account_object> id) {
        return id.toString();
    }

    @TypeConverter
    public static object_id<account_object> stringToAccountObjectId(String strId) {
        return object_id.create_from_string(strId);
    }

    @TypeConverter
    public static String fromOperationType2String(operations.operation_type op) {
        Gson gson = global_config_object.getInstance().getGsonBuilder().create();
        return gson.toJson(op);
    }

    @TypeConverter
    public static operations.operation_type fromString2OperationType(String content) {
        Gson gson = global_config_object.getInstance().getGsonBuilder().create();
        return gson.fromJson(content, operations.operation_type.class);
    }
}
