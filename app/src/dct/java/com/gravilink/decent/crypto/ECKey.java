package com.gravilink.decent.crypto;

import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.asn1.x9.X9IntegerConverter;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.params.*;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.spongycastle.math.ec.ECAlgorithms;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.math.ec.FixedPointCombMultiplier;
import org.spongycastle.math.ec.FixedPointUtil;
import org.spongycastle.math.ec.custom.sec.SecP256K1Curve;

import java.math.BigInteger;

public class ECKey {
  private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");

  private static final ECDomainParameters CURVE;

  private static final BigInteger HALF_CURVE_ORDER;

  static {
    FixedPointUtil.precompute(CURVE_PARAMS.getG(), 12);
    CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(),
            CURVE_PARAMS.getH());
    HALF_CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);
  }

  private final BigInteger priv;
  private final LazyECPoint pub;

  private ECKey(BigInteger priv, ECPoint pub) {
    this.priv = priv;
    this.pub = new LazyECPoint(pub);
  }

  private static ECPoint decompressPoint(ECPoint point) {
    return getPointWithCompression(point, false);
  }

  private static ECPoint getPointWithCompression(ECPoint point, boolean compressed) {
    if (point.isCompressed() == compressed)
      return point;
    point = point.normalize();
    BigInteger x = point.getAffineXCoord().toBigInteger();
    BigInteger y = point.getAffineYCoord().toBigInteger();
    return CURVE.getCurve().createPoint(x, y, compressed);
  }

  public static ECKey fromPrivate(byte[] privKeyBytes) {
    BigInteger privKey = new BigInteger(1, privKeyBytes);
    ECPoint point = publicPointFromPrivate(privKey);
    return new ECKey(privKey, getPointWithCompression(point, true));
  }

  private static ECKey fromPublicOnly(byte[] pub) {
    return new ECKey(null, CURVE.getCurve().decodePoint(pub));
  }

  public ECKey decompress() {
    if (!pub.isCompressed())
      return this;
    else
      return new ECKey(priv, decompressPoint(pub.get()));
  }

  private static ECPoint publicPointFromPrivate(BigInteger privKey) {
    if (privKey.bitLength() > CURVE.getN().bitLength()) {
      privKey = privKey.mod(CURVE.getN());
    }
    return new FixedPointCombMultiplier().multiply(CURVE.getG(), privKey);
  }

  public ECPoint getPubKeyPoint() {
    return pub.get();
  }

  public byte[] getPrivKeyBytes() {
    return Utils.bigIntegerToBytes(priv, 32);
  }

  public DumpedPrivateKey getPrivateKeyEncoded() {
    return new DumpedPrivateKey(getPrivKeyBytes(), isCompressed());
  }
  public boolean isCompressed() {
    return pub.isCompressed();
  }

  public static class ECDSASignature {
    public final BigInteger r, s;

    public ECDSASignature(BigInteger r, BigInteger s) {
      this.r = r;
      this.s = s;
    }

    public boolean isCanonical() {
      return s.compareTo(HALF_CURVE_ORDER) <= 0;
    }

    public ECDSASignature toCanonicalised() {
      if (!isCanonical()) {
        // The order of the curve is the number of valid points that exist on that curve. If S is in the upper
        // half of the number of valid points, then bring it back to the lower half. Otherwise, imagine that
        //    N = 10
        //    s = 8, so (-8 % 10 == 2) thus both (r, 8) and (r, 2) are valid solutions.
        //    10 - 8 == 2, giving us always the latter solution, which is canonical.
        return new ECDSASignature(r, CURVE.getN().subtract(s));
      } else {
        return this;
      }
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ECDSASignature other = (ECDSASignature) o;
      return r.equals(other.r) && s.equals(other.s);
    }

  }

  public ECDSASignature sign(Sha256Hash input) {
    ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
    ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(priv, CURVE);
    signer.init(true, privKey);
    BigInteger[] components = signer.generateSignature(input.getBytes());
    return new ECDSASignature(components[0], components[1]).toCanonicalised();
  }

  public static ECKey recoverFromSignature(int recId, ECDSASignature sig, Sha256Hash message, boolean compressed) {
    BigInteger n = CURVE.getN();  // Curve order.
    BigInteger i = BigInteger.valueOf((long) recId / 2);
    BigInteger x = sig.r.add(i.multiply(n));
    BigInteger prime = SecP256K1Curve.q;
    if (x.compareTo(prime) >= 0) {
      return null;
    }
    ECPoint R = decompressKey(x, (recId & 1) == 1);
    if (!R.multiply(n).isInfinity())
      return null;
    BigInteger e = message.toBigInteger();
    BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
    BigInteger rInv = sig.r.modInverse(n);
    BigInteger srInv = rInv.multiply(sig.s).mod(n);
    BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
    ECPoint q = ECAlgorithms.sumOfTwoMultiplies(CURVE.getG(), eInvrInv, R, srInv);
    return ECKey.fromPublicOnly(q.getEncoded(compressed));
  }

  private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
    X9IntegerConverter x9 = new X9IntegerConverter();
    byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));
    compEnc[0] = (byte)(yBit ? 0x03 : 0x02);
    return CURVE.getCurve().decodePoint(compEnc);
  }

}
