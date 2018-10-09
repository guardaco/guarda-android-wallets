package com.bitshares.bitshareswallet.wallet.fc.crypto;

import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

import java.nio.ByteBuffer;

public class aes {
    public static ByteBuffer encrypt(byte[] key, byte[] iv, byte[] plaintext) {
        assert (key.length == 32 && iv.length == 16);
        try
        {
            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));

            cipher.init(true, new ParametersWithIV(new KeyParameter(key), iv));
            byte[] outBuf   = new byte[cipher.getOutputSize(plaintext.length)];
            int processed = cipher.processBytes(plaintext, 0, plaintext.length, outBuf, 0);
            processed += cipher.doFinal(outBuf, processed);

            ByteBuffer byteBuffer = ByteBuffer.allocate(processed);
            byteBuffer.put(outBuf, 0, processed);
            return byteBuffer;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static ByteBuffer decrypt(byte[] key, byte[] iv, byte[] cipertext) {
        try
        {
            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
            cipher.init(false, new ParametersWithIV(new KeyParameter(key), iv));
            byte[] clear = new byte[cipher.getOutputSize(cipertext.length)];
            int len = cipher.processBytes(cipertext, 0, cipertext.length, clear,0);
            len += cipher.doFinal(clear, len);
            ByteBuffer byteBuffer = ByteBuffer.allocate(len);
            byteBuffer.put(clear, 0, len);
            return byteBuffer;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;

    }
}
