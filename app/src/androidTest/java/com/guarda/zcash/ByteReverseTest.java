package com.guarda.zcash;

import org.junit.Test;

import java.util.Arrays;

import timber.log.Timber;

import static com.guarda.ethereum.crypto.Utils.bytesToHex;
import static com.guarda.ethereum.crypto.Utils.revHex;

public class ByteReverseTest {


    private static final byte[] firstArr = { 0, 0, 0, 0, 0, -87, -51, 52 };

    @Test
    public void testByteReverse() {
        byte[] byteArray = firstArr.clone();

        String second = revHex(bytesToHex(byteArray));
        Timber.d("testByteReverse second=%s", second);
        Timber.d("testByteReverse byteArray=%s", Arrays.toString(byteArray));
        Timber.d("testByteReverse firstArr=%s", Arrays.toString(firstArr));

    }


}
