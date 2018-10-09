package com.gravilink.decent.crypto;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha256Hash {
  public static final int LENGTH = 32;
  private final byte[] bytes;

  public Sha256Hash(byte[] rawHashBytes) {
    this.bytes = rawHashBytes;
  }

  public static Sha256Hash wrap(byte[] rawHashBytes) {
    return new Sha256Hash(rawHashBytes);
  }

  public static MessageDigest newDigest() {
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);  // Can't happen.
    }
  }

  public static byte[] hash(byte[] input) {
    return hash(input, 0, input.length);
  }

  public static byte[] hash(byte[] input, int offset, int length) {
    MessageDigest digest = newDigest();
    digest.update(input, offset, length);
    return digest.digest();
  }

  public static byte[] hashTwice(byte[] input) {
    MessageDigest digest = newDigest();
    digest.update(input, 0, input.length);
    return digest.digest(digest.digest());
  }

  public static byte[] hashTwice(byte[] input, int offset, int length) {
    MessageDigest digest = newDigest();
    digest.update(input, offset, length);
    return digest.digest(digest.digest());
  }

  public BigInteger toBigInteger() {
    return new BigInteger(1, bytes);
  }

  public byte[] getBytes() {
    return bytes;
  }

}
