package com.guarda.zcash.globals;


import org.junit.Test;

public class TypeConvertTest {

    @Test
    public void longToBytes() {
        byte[] firstArr = { 0, 0, 0, 0, 0, -87, -51, 52 };

        byte[] secondArr = { 0, 0, 0, 0, 0, -55, 21, -101 };

        long firstLong = TypeConvert.bytesToLong(firstArr);
        long secondLong = TypeConvert.bytesToLong(secondArr);

        System.out.println("firstLong=" + firstLong + " secondLong=" + secondLong);
    }
}