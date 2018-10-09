package com.bitshares.bitshareswallet.wallet.fc.io;

import com.google.common.primitives.UnsignedBytes;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;


public class raw_type {
    public byte[] get_byte_array(short value) {
        byte[] byteValue = new byte[2];
        byteValue[1] = (byte) ((value >> 8) & 0xff);
        byteValue[0] = (byte) (value & 0xff);

        return byteValue;
    }

    public byte[] get_byte_array(int value) {
        byte[] byteValue = new byte[4];
        byteValue[3] = (byte) ((value >> 24) & 0xff);
        byteValue[2] = (byte) ((value >> 16) & 0xff);
        byteValue[1] = (byte) ((value >> 8) & 0xff);
        byteValue[0] = (byte) ( value & 0xff);

        return byteValue;
    }

    public byte get_byte(boolean value) {
        byte byteValue = 0;
        if (value == true) {
            byteValue = 1;
        }

        return byteValue;
    }

    public byte[] get_byte_array(long value) {
        byte[] byteValue = new byte[8];
        byteValue[7] = (byte) ((value >> 56) & 0xff);
        byteValue[6] = (byte) ((value >> 48) & 0xff);
        byteValue[5] = (byte) ((value >> 40) & 0xff);
        byteValue[4] = (byte) ((value >> 32) & 0xff);
        byteValue[3] = (byte) ((value >> 24) & 0xff);
        byteValue[2] = (byte) ((value >> 16) & 0xff);
        byteValue[1] = (byte) ((value >> 8) & 0xff);
        byteValue[0] = (byte) (value & 0xff);

        return byteValue;
    }

    public byte[] get_byte_array(UnsignedLong value) {
        return get_byte_array(value.longValue());
    }

    public byte[] get_byte_array(Date date) {
        byte[] byteValue = new byte[4];
        long lTime = date.getTime() / 1000;
        byteValue[3] = (byte) ((lTime >> 24) & 0xff);
        byteValue[2] = (byte) ((lTime >> 16) & 0xff);
        byteValue[1] = (byte) ((lTime >> 8) & 0xff);
        byteValue[0] = (byte) ( lTime & 0xff);

        return byteValue;
    }

    public int byte_array_to_int(byte[] bytes) {
        assert(bytes.length == 4);
        int nValue = 0;
        nValue = (bytes[0] & 0xff);
        nValue |= ((bytes[1] & 0xff) << 8);
        nValue |= ((bytes[2] & 0xff) << 16);
        nValue |= ((bytes[3] & 0xff) << 24);

        return nValue;
    }

    public void pack(base_encoder encoder, UnsignedInteger value) {
        long lValue = value.longValue();
        do {
            byte b = (byte)(lValue & 0x7f);
            lValue >>= 7;
            if (lValue > 0) {
                b |= (1 << 7);
            }
            encoder.write(b);
        } while (lValue > 0);
    }

    public UnsignedInteger unpack(InputStream inputStream) {
        long value = 0;
        int b = 0;
        int by = 0;

        try {
            do {
                b = inputStream.read();
                value |= (b & 0x7f) << by;
                by += 7;
            } while ((b & 0x80) > 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return UnsignedInteger.valueOf(value);
    }
}
