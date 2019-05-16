package com.guarda.zcash.crypto;

import java.nio.ByteBuffer;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;

public class SecureRandomStrengthener {
  private static final String DEFAULT_PSEUDO_RANDOM_NUMBER_GENERATOR = "SHA1PRNG";

  private static final EntropySource mTimeEntropySource = new EntropySource() {

    final ByteBuffer timeBuffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE
      * 2);

    @Override
    public ByteBuffer provideEntropy() {
      this.timeBuffer.clear();
      this.timeBuffer.putLong(System.currentTimeMillis());
      this.timeBuffer.putLong(System.nanoTime());
      this.timeBuffer.flip();
      return this.timeBuffer;
    }
  };

  private final String algorithm;
  private final List<EntropySource> entropySources = new LinkedList<EntropySource>();
  private final MessageDigest digest;
  private final ByteBuffer seedBuffer;

  public static SecureRandomStrengthener getInstance() {
    return new SecureRandomStrengthener(
      DEFAULT_PSEUDO_RANDOM_NUMBER_GENERATOR);
  }

  public static SecureRandomStrengthener getInstance(final String algorithm) {
    return new SecureRandomStrengthener(algorithm);
  }

  private SecureRandomStrengthener(final String algorithm) {
    if (algorithm == null || algorithm.length() == 0) {
      throw new IllegalArgumentException(
        "Please provide a PRNG algorithm string such as SHA1PRNG");
    }

    this.algorithm = algorithm;
    try {
      this.digest = MessageDigest.getInstance("SHA1");
    } catch (final NoSuchAlgorithmException e) {
      throw new IllegalStateException(
        "MessageDigest to create seed not available", e);
    }
    this.seedBuffer = ByteBuffer.allocate(this.digest.getDigestLength());
  }

  public void addEntropySource(final EntropySource source) {
    if (source == null) {
      throw new IllegalArgumentException(
        "EntropySource should not be null");
    }
    this.entropySources.add(source);
  }

  public SecureRandom generateAndSeedRandomNumberGenerator() {
    final SecureRandom secureRandom;
    try {
      secureRandom = SecureRandom.getInstance(this.algorithm);
    } catch (final NoSuchAlgorithmException e) {
      throw new IllegalStateException("PRNG is not available", e);
    }

    reseed(secureRandom);
    return secureRandom;
  }

  public void reseed(final SecureRandom secureRandom) {
    this.seedBuffer.clear();
    secureRandom.nextBytes(this.seedBuffer.array());

    for (final EntropySource source : this.entropySources) {
      final ByteBuffer entropy = source.provideEntropy();
      if (entropy == null) {
        continue;
      }

      final ByteBuffer wipeBuffer = entropy.duplicate();
      this.digest.update(entropy);
      wipe(wipeBuffer);
    }

    this.digest.update(mTimeEntropySource.provideEntropy());
    this.digest.update(this.seedBuffer);
    this.seedBuffer.clear();
    // remove data from seedBuffer so it won't be retrievable

    // reuse

    try {
      this.digest.digest(this.seedBuffer.array(), 0,
        this.seedBuffer.capacity());
    } catch (final DigestException e) {
      throw new IllegalStateException(
        "DigestException should not be thrown", e);
    }
    secureRandom.setSeed(this.seedBuffer.array());

    wipe(this.seedBuffer);
  }

  private void wipe(final ByteBuffer buf) {
    while (buf.hasRemaining()) {
      buf.put((byte) 0);
    }
  }
}