package com.guarda.zcash.crypto;

import org.spongycastle.crypto.digests.RIPEMD160Digest;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Utils {
  public static byte[] bigIntegerToBytes(BigInteger b, int numBytes) {
    if (b == null) {
      return null;
    }
    byte[] bytes = new byte[numBytes];
    byte[] biBytes = b.toByteArray();
    int start = (biBytes.length == numBytes + 1) ? 1 : 0;
    int length = Math.min(biBytes.length, numBytes);
    System.arraycopy(biBytes, start, bytes, numBytes - length, length);
    return bytes;
  }

  public static final char[] hexArray = "0123456789abcdef".toCharArray();

  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }

  public static byte[] hexToBytes(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
              + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  public static byte[] hexlify(String data) {
    ByteBuffer buffer = ByteBuffer.allocate(data.length());
    for (char letter : data.toCharArray()) {
      buffer.put((byte) letter);
    }
    return buffer.array();
  }

  public static byte[] calculateChecksum(byte[] data) {
    byte[] checksum = new byte[160 / 8];
    RIPEMD160Digest ripemd160Digest = new RIPEMD160Digest();
    ripemd160Digest.update(data, 0, data.length);
    ripemd160Digest.doFinal(checksum, 0);
    return Arrays.copyOfRange(checksum, 0, 4);
  }

  public static byte[] revertLong(Long input) {
    return ByteBuffer.allocate(Long.SIZE / 8).putLong(Long.reverseBytes(input)).array();
  }

  public static byte[] compactSizeIntLE(long val) {
    byte[] result;
    if (val < 253) {
      result = new byte[1];
      result[0] = (byte) val;
    } else if (val < 0x10000) {
      result = new byte[3];
      result[0] = (byte) 253;
      result[1] = (byte) (0xff & (val));
      result[2] = (byte) (0xff & (val >> 8));
    } else if (val < 0x1000000) {
      result = new byte[5];
      result[0] = (byte) 254;
      result[1] = (byte) (0xff & (val));
      result[2] = (byte) (0xff & (val >> 8));
      result[3] = (byte) (0xff & (val >> 16));
      result[4] = (byte) (0xff & (val >> 24));
    } else {
      result = new byte[9];
      result[0] = (byte) 255;
      result[1] = (byte) (0xff & (val));
      result[2] = (byte) (0xff & (val >> 8));
      result[3] = (byte) (0xff & (val >> 16));
      result[4] = (byte) (0xff & (val >> 24));
      result[5] = (byte) (0xff & (val >> 32));
      result[6] = (byte) (0xff & (val >> 40));
      result[7] = (byte) (0xff & (val >> 48));
      result[8] = (byte) (0xff & (val >> 56));
    }

    return result;
  }

  public static byte[] int32BytesLE(long val) {
    byte[] buf = new byte[4];
    buf[0] = (byte) (0xff & (val));
    buf[1] = (byte) (0xff & (val >> 8));
    buf[2] = (byte) (0xff & (val >> 16));
    buf[3] = (byte) (0xff & (val >> 24));
    return buf;
  }

  public static byte[] int64BytesLE(long val) {
    byte[] buf = new byte[8];
    buf[0] = (byte) (0xff & (val));
    buf[1] = (byte) (0xff & (val >> 8));
    buf[2] = (byte) (0xff & (val >> 16));
    buf[3] = (byte) (0xff & (val >> 24));
    buf[4] = (byte) (0xff & (val >> 32));
    buf[5] = (byte) (0xff & (val >> 40));
    buf[6] = (byte) (0xff & (val >> 48));
    buf[7] = (byte) (0xff & (val >> 56));
    return buf;
  }

}
