package com.bitshares.bitshareswallet.wallet.graphene.chain;

import com.google.common.io.BaseEncoding;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;


public class compact_signature {
    public byte data[] = new byte[65];

    public compact_signature(byte[] signature) {
        assert(signature.length == 65);
        System.arraycopy(signature, 0, data, 0, signature.length);
    }

    static class compact_signature_serializer implements JsonSerializer<compact_signature> {

        @Override
        public JsonElement serialize(compact_signature src,
                                     Type typeOfSrc,
                                     JsonSerializationContext context) {
            BaseEncoding encoding = BaseEncoding.base16().lowerCase();

            return new JsonPrimitive(encoding.encode(src.data));
        }
    }
}
