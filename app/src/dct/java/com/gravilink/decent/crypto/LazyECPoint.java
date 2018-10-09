package com.gravilink.decent.crypto;

import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.math.ec.ECPoint;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

public class LazyECPoint {

  private final ECCurve curve;
  private final byte[] bits;

  private ECPoint point;

  public LazyECPoint(ECPoint point) {
    this.point = checkNotNull(point);
    this.curve = null;
    this.bits = null;
  }

  public ECPoint get() {
    if (point == null)
      point = curve.decodePoint(bits);
    return point;
  }

  public boolean isCompressed() {
    if (bits != null)
      return bits[0] == 2 || bits[0] == 3;
    else
      return get().isCompressed();
  }

  public byte[] getEncoded(boolean compressed) {
    if (compressed == isCompressed() && bits != null)
      return Arrays.copyOf(bits, bits.length);
    else
      return get().getEncoded(compressed);
  }

  public ECPoint add(ECPoint b) {
    return get().add(b);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return Arrays.equals(getCanonicalEncoding(), ((LazyECPoint) o).getCanonicalEncoding());
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(getCanonicalEncoding());
  }

  private byte[] getCanonicalEncoding() {
    return getEncoded(true);
  }
}
