package com.bitshares.bitshareswallet.wallet.common;

import com.google.common.io.BaseEncoding;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class gson_common_deserializer {
    public static class DateDeserializer implements JsonDeserializer<Date> {

        @Override
        public Date deserialize(JsonElement json,
                                Type typeOfT,
                                JsonDeserializationContext context) throws JsonParseException {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            try {
                Date dateResult = simpleDateFormat.parse(json.getAsString());

                return dateResult;
            } catch (ParseException e) {
                e.printStackTrace();
                throw new JsonParseException(e.getMessage() + json.getAsString());
            }
        }
    }

    public static class ByteBufferDeserializer implements JsonDeserializer<ByteBuffer> {

        @Override
        public ByteBuffer deserialize(JsonElement json,
                                      Type typeOfT,
                                      JsonDeserializationContext context) throws JsonParseException {
            BaseEncoding encoding = BaseEncoding.base16().lowerCase();
            byte[] byteResult = encoding.decode(json.getAsString());
            ByteBuffer byteBuffer = ByteBuffer.wrap(byteResult);

            return byteBuffer;
        }
    }
}
