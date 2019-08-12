package com.guarda.zcash;

import android.content.Context;
import android.content.res.AssetManager;

import com.guarda.zcash.crypto.Bech32;
import com.guarda.zcash.crypto.BitcoinCashBitArrayConverter;

import java.util.Arrays;

import timber.log.Timber;

import static com.guarda.zcash.crypto.Utils.bytesToHex;

public class RustAPI {

    //region NATIVE METHODS

    private static native String initModel(AssetManager assetManager,
                                           String outputPath,
                                           String spendPath);

    public static native byte[] greeting(final String esk,
                                          final String d,
                                          final String pkd,
                                          final String rcm,
                                          final String value);

    public static native String genr();

    public static native String epk(final String d,
                                    final String esk);

    public static native String cm(final String d,
                                   final String pkd,
                                   final String value,
                                   final String r);

    public static native byte[] bsig(final String value,
                                     final String data);

    public static native String kagree(final String epk,
                                       final String ivk);

    public static native String ivkToPdk(final String ivk,
                                         final String d);

    public static native String checkout(final String cv,
                                         final String cm,
                                         final String ephemeralKey,
                                         final String zkproof);

    public static native String merkelHash(final int depth,
                                           final String l,
                                           final String r);

    public static native String uncommitted();

    public static native String computeNf(final String d,
                                          final String pkd,
                                          final String value,
                                          final String r,
                                          final String ak,
                                          final String nk,
                                          final String position);

    public static native byte[] encryptNp(final String key,
                                          final String cipher);

    public static native String proveContextInit();

    public static native byte[] spendProof(final String ak,
                                           final String nsk,
                                           final String d,
                                           final String r,
                                           final String alpha,
                                           final String value,
                                           final String anchor,
                                           final boolean[][] authPathsArr,
                                           final boolean[] indexesArr);

    public static native byte toByteMerklePath(final byte pathByte,
                                                 final boolean authPathBool,
                                                 final int p);

    public static native byte[] vectorToInt(final boolean[][] authPathsArr,
                                            final boolean[] indexesArr);

    public static native byte[] spendSig(final String ask,
                                         final String alpha,
                                         final String dataToBeSigned);

    public static native String testVerify(final String spendHash,
                                           final String sigHash,
                                           final String bsigHash);

    public static native String testUint256(final String str);

    public static native byte[] testToArr(final String str);

    //WALLET
    public static native void initWallet(final byte[] data);
    public static native byte[] dPart(final byte[] data);
    public static native String zAddrFromWif(final byte[] data);
    public static native String getExtsk(final byte[] data);
    public static native byte[] compactDecrypt(final byte[] key,
                                                   final byte[] cipher);

    //endregion

    public static String checkInit(Context context) {
        Timber.d("checkInit started");
        String oPath = "sapling-output.params";
        String sPath = "sapling-spend.params";
        String instr = initModel(context.getAssets(), oPath, sPath);
        if (instr.contains("nullptr")) Timber.e("checkInit=initialization error");
        Timber.d("checkInit=" + instr);
        Timber.d("checkInit done");

        return instr;
    }

    public static byte[] getBsig(String value, byte[] data) {
        byte[] bsigStr = bsig(value, bytesToHex(data));
        Timber.d("getBsig=" + Arrays.toString(bsigStr) + " s=" + bsigStr.length);
        return bsigStr;
    }

    public static byte[] checkConvertAddr(String address) {
        Bech32.Bech32Data ddd = Bech32.decodeWithoutVeryfy(address);
        Timber.d("checkConvertAddr ddd=%s", Arrays.toString(ddd.data));
        byte[] bytesConverted = BitcoinCashBitArrayConverter.convertBits(ddd.data, 5, 8, true);
        Timber.d("checkConvertAddr ddd=%s", Arrays.toString(bytesConverted));
        return bytesConverted;
    }

}
