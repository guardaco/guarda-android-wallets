package com.bitshares.bitshareswallet.wallet.graphene.chain;

import com.bitshares.bitshareswallet.wallet.common.UnsignedShort;
import com.bitshares.bitshareswallet.wallet.common.gson_common_deserializer;
import com.bitshares.bitshareswallet.wallet.common.gson_common_serializer;
import com.bitshares.bitshareswallet.wallet.common.unsigned_number_deserializer;
import com.bitshares.bitshareswallet.wallet.common.unsigned_number_serializer;
import com.bitshares.bitshareswallet.wallet.fc.crypto.ripemd160_object;
import com.bitshares.bitshareswallet.wallet.fc.crypto.sha256_object;
import com.bitshares.bitshareswallet.wallet.full_account_object_reply;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.google.gson.GsonBuilder;

import java.nio.ByteBuffer;
import java.util.Date;


public class global_config_object {
    private static global_config_object mConfigObject = new global_config_object();
    private GsonBuilder mGsonBuilder;
    public static global_config_object getInstance()  {
        return mConfigObject;
    }

    private global_config_object() {
        mGsonBuilder = new GsonBuilder();
        mGsonBuilder.registerTypeAdapter(types.public_key_type.class, new types.public_key_type_deserializer());
        mGsonBuilder.registerTypeAdapter(types.public_key_type.class, new types.public_type_serializer());
        mGsonBuilder.registerTypeAdapter(operations.operation_type.class, new operations.operation_type.operation_type_deserializer());
        mGsonBuilder.registerTypeAdapter(object_id.class, new object_id.object_id_deserializer());
        mGsonBuilder.registerTypeAdapter(object_id.class, new object_id.object_id_serializer());
        mGsonBuilder.registerTypeAdapter(fee_schedule.fee_parameters.class, new fee_schedule.fee_parameters_deserializer());
        mGsonBuilder.registerTypeAdapter(types.vote_id_type.class, new types.vote_id_type_deserializer());
        mGsonBuilder.registerTypeAdapter(ripemd160_object.class, new ripemd160_object.ripemd160_object_deserializer());
        mGsonBuilder.registerTypeAdapter(sha256_object.class, new sha256_object.sha256_object_deserializer());
        mGsonBuilder.registerTypeAdapter(UnsignedLong.class, new unsigned_number_deserializer.UnsignedLongDeserialize());
        mGsonBuilder.registerTypeAdapter(Date.class, new gson_common_deserializer.DateDeserializer());
        mGsonBuilder.registerTypeAdapter(ByteBuffer.class, new gson_common_deserializer.ByteBufferDeserializer());
        mGsonBuilder.registerTypeAdapter(full_account_object_reply.class, new full_account_object_reply.full_account_object_reply_deserializer());
        //mGsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        // register serializer
        mGsonBuilder.registerTypeAdapter(operations.operation_type.class, new operations.operation_type.operation_type_serializer());
        mGsonBuilder.registerTypeAdapter(compact_signature.class, new compact_signature.compact_signature_serializer());
        mGsonBuilder.registerTypeAdapter(UnsignedInteger.class, new unsigned_number_serializer.UnsigendIntegerSerializer());
        mGsonBuilder.registerTypeAdapter(UnsignedShort.class, new unsigned_number_serializer.UnsigendShortSerializer());
        mGsonBuilder.registerTypeAdapter(Date.class, new gson_common_serializer.DateSerializer());
        mGsonBuilder.registerTypeAdapter(sha256_object.class, new sha256_object.sha256_object_serializer());
        mGsonBuilder.registerTypeAdapter(ByteBuffer.class, new gson_common_serializer.ByteBufferSerializer());
        mGsonBuilder.registerTypeAdapter(UnsignedLong.class, new unsigned_number_serializer.UnsignedLongSerializer());

    }

    public GsonBuilder getGsonBuilder() {
        return mGsonBuilder;
    }
}
