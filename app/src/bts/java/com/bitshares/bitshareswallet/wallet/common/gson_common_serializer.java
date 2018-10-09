package com.bitshares.bitshareswallet.wallet.common;

import com.google.common.io.BaseEncoding;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class gson_common_serializer {
    public static class DateSerializer implements JsonSerializer<Date> {

        @Override
        public JsonElement serialize(Date src,
                                     Type typeOfSrc,
                                     JsonSerializationContext context) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            String strResult = simpleDateFormat.format(src);

            return new JsonPrimitive(strResult);
        }
    }

    public static class ByteBufferSerializer implements JsonSerializer<ByteBuffer> {

        @Override
        public JsonElement serialize(ByteBuffer src,
                                     Type typeOfSrc,
                                     JsonSerializationContext context) {
            BaseEncoding encoding = BaseEncoding.base16().lowerCase();

            return new JsonPrimitive(encoding.encode(src.array()));
        }
    }


}
