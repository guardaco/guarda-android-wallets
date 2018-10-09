package com.bitshares.bitshareswallet.wallet;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by lorne on 01/11/2017.
 */

public class full_account_object_reply {
    public static class full_account_object_reply_deserializer implements JsonDeserializer<full_account_object_reply> {

        @Override
        public full_account_object_reply deserialize(JsonElement json,
                                                     Type typeOfT,
                                                     JsonDeserializationContext context) throws JsonParseException {
            JsonArray jsonArray = json.getAsJsonArray();
            full_account_object_reply fullAccountObjectReply = new full_account_object_reply();
            fullAccountObjectReply.name = jsonArray.get(0).getAsString();
            fullAccountObjectReply.fullAccountObject = context.deserialize(jsonArray.get(1), full_account_object.class);

            return fullAccountObjectReply;
        }
    }

    String name;
    full_account_object fullAccountObject;
}
