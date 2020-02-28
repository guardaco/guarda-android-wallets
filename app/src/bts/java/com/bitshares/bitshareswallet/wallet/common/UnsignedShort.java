package com.bitshares.bitshareswallet.wallet.common;

import androidx.annotation.NonNull;

import com.google.common.primitives.UnsignedInteger;



public class UnsignedShort extends Number {
    public  static final UnsignedShort ZERO = new UnsignedShort((short)0);

    private short value;
    public UnsignedShort(short value) {
        this.value = value;
    }

    @Override
    public int intValue() {
        return toInt();
    }

    @Override
    public short shortValue() {
        return value;
    }

    @Override
    public long longValue() {
        return toInt();
    }

    @Override
    public float floatValue() {
        return toInt();
    }

    @Override
    public double doubleValue() {
        return toInt();
    }


    private int toInt() {
        return value & 0xffff;
    }
}
