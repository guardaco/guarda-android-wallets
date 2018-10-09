package com.bitshares.bitshareswallet.wallet.common;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.json.JSONObject;

import java.lang.reflect.Type;

public class unsigned_number_deserializer {
    public static class UnsignedLongDeserialize implements JsonDeserializer<UnsignedLong> {

        @Override
        public UnsignedLong deserialize(JsonElement json,
                                        Type typeOfT,
                                        JsonDeserializationContext context) throws JsonParseException {
            UnsignedLong uLongObject = UnsignedLong.valueOf(json.getAsString());

            return uLongObject;
        }
    }

    public static class UnsignedIntegerDeserialize implements JsonDeserializer<UnsignedInteger> {

        @Override
        public UnsignedInteger deserialize(JsonElement json,
                                        Type typeOfT,
                                        JsonDeserializationContext context) throws JsonParseException {
            UnsignedInteger uIntegerObject = UnsignedInteger.valueOf(json.getAsString());

            return uIntegerObject;
        }
    }
}
