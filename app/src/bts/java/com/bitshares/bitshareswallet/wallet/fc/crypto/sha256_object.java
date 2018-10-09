package com.bitshares.bitshareswallet.wallet.fc.crypto;

import com.bitshares.bitshareswallet.wallet.fc.io.base_encoder;
import com.google.common.io.BaseEncoding;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.digests.SHA512Digest;

import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class sha256_object {
    public byte[] hash = new byte[32];

    @Override
    public String toString() {
        BaseEncoding encoding = BaseEncoding.base16().lowerCase();
        return encoding.encode(hash);
    }

    @Override
    public boolean equals(Object obj) {
        sha256_object sha256Object = (sha256_object)obj;
        return Arrays.equals(hash, sha256Object.hash);
    }

    public static sha256_object create_from_string(String strContent) {
        SHA256Digest digest = new SHA256Digest();
        byte[] bytePassword = strContent.getBytes();
        digest.update(bytePassword, 0, bytePassword.length);

        byte[] byteHash = new byte[32];
        digest.doFinal(byteHash, 0);

        sha256_object sha256Object = new sha256_object();
        System.arraycopy(byteHash, 0, sha256Object.hash, 0, byteHash.length);

        return sha256Object;
    }

    public static class sha256_object_deserializer implements JsonDeserializer<sha256_object> {

        @Override
        public sha256_object deserialize(JsonElement json,
                                            Type typeOfT,
                                            JsonDeserializationContext context) throws JsonParseException {
            sha256_object sha256ObjectObject = new sha256_object();
            BaseEncoding encoding = BaseEncoding.base16().lowerCase();
            byte[] byteContent = encoding.decode(json.getAsString());
            if (byteContent.length != 32) {
                throw new JsonParseException("sha256_object size not correct.");
            }
            System.arraycopy(byteContent, 0, sha256ObjectObject.hash, 0, sha256ObjectObject.hash.length);
            sha256ObjectObject.hash = byteContent;
            return sha256ObjectObject;
        }
    }

    public static class sha256_object_serializer implements JsonSerializer<sha256_object> {

        @Override
        public JsonElement serialize(sha256_object src,
                                     Type typeOfSrc,
                                     JsonSerializationContext context) {

            BaseEncoding encoding = BaseEncoding.base16().lowerCase();

            return new JsonPrimitive(encoding.encode(src.hash));
        }
    }

    public static class encoder implements base_encoder {
        SHA256Digest digest = new SHA256Digest();
        MessageDigest messageDigest;
        public encoder() {
            try {
                messageDigest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        public void reset() {
            digest.reset();
        }

        @Override
        public void write(byte[] data) {
            digest.update(data, 0, data.length);
            messageDigest.update(data);
        }

        @Override
        public void write(byte[] data, int off, int len) {
            digest.update(data, off, len);
            messageDigest.update(data, off, len);
        }

        @Override
        public void write(byte data) {
            digest.update(data);
            messageDigest.update(data);
        }

        public sha256_object result() {
            sha256_object sha256Object = new sha256_object();
            digest.doFinal(sha256Object.hash, 0);

            return sha256Object;
        }
    }
}
