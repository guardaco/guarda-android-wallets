package com.guarda.zcash.globals;

import java.nio.ByteBuffer;

public class TypeConvert {

    public static byte[] longToBytes(long l) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(l);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return buffer.getLong();
    }
}
