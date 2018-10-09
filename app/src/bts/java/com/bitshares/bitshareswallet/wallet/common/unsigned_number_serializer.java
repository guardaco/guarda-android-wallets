package com.bitshares.bitshareswallet.wallet.common;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.math.BigInteger;


public class unsigned_number_serializer {
    public static class UnsigendIntegerSerializer implements JsonSerializer<UnsignedInteger> {

        @Override
        public JsonElement serialize(UnsignedInteger src,
                                     Type typeOfSrc,
                                     JsonSerializationContext context) {
            return new JsonPrimitive(src.longValue());
        }
    }

    public static class UnsigendShortSerializer implements JsonSerializer<UnsignedShort> {

        @Override
        public JsonElement serialize(UnsignedShort src,
                                     Type typeOfSrc,
                                     JsonSerializationContext context) {
            return new JsonPrimitive(src.intValue());
        }
    }

    public static class UnsignedLongSerializer implements JsonSerializer<UnsignedLong> {

        @Override
        public JsonElement serialize(UnsignedLong src,
                                     Type typeOfSrc,
                                     JsonSerializationContext context) {
            return new JsonPrimitive(src.bigIntegerValue());
        }
    }
}
