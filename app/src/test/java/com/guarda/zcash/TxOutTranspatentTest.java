package com.guarda.zcash;

import com.guarda.zcash.crypto.Base58;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


public class TxOutTranspatentTest {

    String tOne = "t1Lb7pwVomWgXjfJuAGZMaouifcWymXs6Du";
    String tOne1 = "t1Rxy4hTQXpfSwqkA1CoFd6zz8G3mnRa2Xx";
    String tOne2 = "t1gynHkGVxofEmfZk7ywxM7w36TcqoHyWwP";
    String tThree = "t3MEXDF9Wsi63KwpPuQdD6by32Mw2bNTbEa";
    String tThree1 = "t3JcMe1E5UkFsUtVb7k17eJwXX5FYUewMBy";
    String tThree2 = "t3WGhZh3QV9Wgj9Xb6MrrhLZhpdyVa8QqLG";

    byte[] tOneBytes = Base58.decodeChecked(tOne);
    byte[] tOneBytes1 = Base58.decodeChecked(tOne1);
    byte[] tOneBytes2 = Base58.decodeChecked(tOne2);
    byte[] tThreeBytes = Base58.decodeChecked(tThree);
    byte[] tThreeBytes1 = Base58.decodeChecked(tThree1);
    byte[] tThreeBytes2 = Base58.decodeChecked(tThree2);

    byte[] tOneBytesScript = new byte[] { 118, -87, 20, 29, -54, 50, -6, 24, 43, -125, 17, -64, 125, 103, -100, -108, 100, -66, 63, -8, 79, 83, -40, -120, -84 };
    byte[] tOneBytes1Script = new byte[] { 118, -87, 20, 88, -60, -86, 21, -67, -80, -90, 126, 84, -16, 111, 97, 61, 14, -16, -22, 113, -28, 34, 14, -120, -84 };
    byte[] tOneBytes2Script = new byte[] { 118, -87, 20, -3, 117, -8, -6, -104, -37, -1, 121, 96, -121, 15, 19, 12, -51, -103, -44, -48, 56, 102, 70, -120, -84 };
    byte[] tThreeBytesScript = new byte[] { -87, 20, 29, 75, -77, 121, -35, -44, 4, 58, -24, 108, -117, -106, -25, 24, 55, -124, -7, -19, -33, 73, -121 };
    byte[] tThreeBytes1Script = new byte[] { -87, 20, 0, -124, 123, -47, 64, 36, 43, -47, -55, -63, -112, 36, -65, -32, 10, 74, -50, -5, -69, -123, -121 };
    byte[] tThreeBytes2Script = new byte[] { -87, 20, -128, 110, 78, -111, -111, 8, 85, -51, -83, -98, 59, -86, -58, 20, -105, -14, 121, 14, 57, 19, -121 };

    byte[] tOneBytesOutputBytes = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 25, 118, -87, 20, 29, -54, 50, -6, 24, 43, -125, 17, -64, 125, 103, -100, -108, 100, -66, 63, -8, 79, 83, -40, -120, -84 };
    byte[] tOneBytes1OutputBytes = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 25, 118, -87, 20, 88, -60, -86, 21, -67, -80, -90, 126, 84, -16, 111, 97, 61, 14, -16, -22, 113, -28, 34, 14, -120, -84 };
    byte[] tOneBytes2OutputBytes = new byte[] { 2, 0, 0, 0, 0, 0, 0, 0, 25, 118, -87, 20, -3, 117, -8, -6, -104, -37, -1, 121, 96, -121, 15, 19, 12, -51, -103, -44, -48, 56, 102, 70, -120, -84 };
    byte[] tThreeBytesOutputBytes = new byte[] { 3, 0, 0, 0, 0, 0, 0, 0, 23, -87, 20, 29, 75, -77, 121, -35, -44, 4, 58, -24, 108, -117, -106, -25, 24, 55, -124, -7, -19, -33, 73, -121 };
    byte[] tThreeBytes1OutputBytes = new byte[] { 4, 0, 0, 0, 0, 0, 0, 0, 23, -87, 20, 0, -124, 123, -47, 64, 36, 43, -47, -55, -63, -112, 36, -65, -32, 10, 74, -50, -5, -69, -123, -121 };
    byte[] tThreeBytes2OutputBytes = new byte[] { 5, 0, 0, 0, 0, 0, 0, 0, 23, -87, 20, -128, 110, 78, -111, -111, 8, 85, -51, -83, -98, 59, -86, -58, 20, -105, -14, 121, 14, 57, 19, -121 };

    @Test
    public void positiveTestTxOutTranspatent() {
        TxOutTranspatent txOutTranspatent0 = new TxOutTranspatent(tOneBytes, 0L);
        TxOutTranspatent txOutTranspatent1 = new TxOutTranspatent(tOneBytes1, 1L);
        TxOutTranspatent txOutTranspatent2 = new TxOutTranspatent(tOneBytes2, 2L);
        TxOutTranspatent txOutTranspatent3 = new TxOutTranspatent(tThreeBytes, 3L);
        TxOutTranspatent txOutTranspatent4 = new TxOutTranspatent(tThreeBytes1, 4L);
        TxOutTranspatent txOutTranspatent5 = new TxOutTranspatent(tThreeBytes2, 5L);

        assertEquals(txOutTranspatent0.value, 0L);
        assertEquals(txOutTranspatent1.value, 1L);
        assertEquals(txOutTranspatent2.value, 2L);
        assertEquals(txOutTranspatent3.value, 3L);
        assertEquals(txOutTranspatent4.value, 4L);
        assertEquals(txOutTranspatent5.value, 5L);

        assertArrayEquals(txOutTranspatent0.script, tOneBytesScript);
        assertArrayEquals(txOutTranspatent1.script, tOneBytes1Script);
        assertArrayEquals(txOutTranspatent2.script, tOneBytes2Script);
        assertArrayEquals(txOutTranspatent3.script, tThreeBytesScript);
        assertArrayEquals(txOutTranspatent4.script, tThreeBytes1Script);
        assertArrayEquals(txOutTranspatent5.script, tThreeBytes2Script);

        assertArrayEquals(txOutTranspatent0.getBytes(), tOneBytesOutputBytes);
        assertArrayEquals(txOutTranspatent1.getBytes(), tOneBytes1OutputBytes);
        assertArrayEquals(txOutTranspatent2.getBytes(), tOneBytes2OutputBytes);
        assertArrayEquals(txOutTranspatent3.getBytes(), tThreeBytesOutputBytes);
        assertArrayEquals(txOutTranspatent4.getBytes(), tThreeBytes1OutputBytes);
        assertArrayEquals(txOutTranspatent5.getBytes(), tThreeBytes2OutputBytes);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void negativeTestTxOutTranspatent() {
        thrown.expect(IllegalArgumentException.class);

        new TxOutTranspatent(new byte[] { 5 }, 0L);
        new TxOutTranspatent(new byte[] { 118, -87, 20, 29, -54, 50, -6, 24, 43, -125, 17, -64, 125, 103, -100, -108, 100, -66, 63, -8, 79, 83, -40, -120 }, 1L);
        new TxOutTranspatent(new byte[] { 118, -87, 20, 29, -54, 50, -6, 24, 43, -125, 17, -64, 125, 103, -100, -108, 100, -66, 63, -8, 79, 83, -40, -120, 0 }, 2L);

        byte[] wrongAddressBytes = new byte[] { 117, -86, 20, 29, -54, 50, -6, 24, 43, -125, 17, -64, 125, 103, -100, -108, 100, -66, 63, -8, 79, 83, -40, -120, -84 };
        new TxOutTranspatent(wrongAddressBytes, 1L);
    }
}