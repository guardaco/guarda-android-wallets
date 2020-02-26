package work.samosudov.rustlib;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.getkeepsafe.relinker.ReLinker;

import java.util.Arrays;


import work.samosudov.rustlib.crypto.Bech32;
import work.samosudov.rustlib.crypto.BitcoinCashBitArrayConverter;

import static work.samosudov.rustlib.crypto.Utils.bytesToHex;

public class RustAPI {

    public static void init(Context context) {
        ReLinker.Logger logcatLogger = new ReLinker.Logger() {
            @Override
            public void log(String message) {
                Log.d(RUST_INDEPENDENT_TAG,"ReLinker " + message);
            }
        };
        ReLinker.log(logcatLogger).loadLibrary(context, "native-lib");
    }

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

    public static native byte[] encryptNp(final byte[] key,
                                               final byte[] cipher);

    //endregion

    public static String checkInit(Context context) {
        Log.d(RUST_INDEPENDENT_TAG, "checkInit started");
        String oPath = "sapling-output.params";
        String sPath = "sapling-spend.params";
        String instr = initModel(context.getAssets(), oPath, sPath);
        if (instr.contains("nullptr")) Log.e("rust-independent","checkInit=initialization error");
        Log.d(RUST_INDEPENDENT_TAG,"checkInit=" + instr);
        Log.d(RUST_INDEPENDENT_TAG, "checkInit done");

        return instr;
    }

    public static byte[] getBsig(String value, byte[] data) {
        byte[] bsigStr = bsig(value, bytesToHex(data));
        Log.d(RUST_INDEPENDENT_TAG,"getBsig=" + Arrays.toString(bsigStr) + " s=" + bsigStr.length);
        return bsigStr;
    }

    public static byte[] checkConvertAddr(String address) {
        Bech32.Bech32Data ddd = Bech32.decodeWithoutVeryfy(address);
        Log.d(RUST_INDEPENDENT_TAG, "checkConvertAddr ddd=" + Arrays.toString(ddd.data));
        byte[] bytesConverted = BitcoinCashBitArrayConverter.convertBits(ddd.data, 5, 8, true);
        Log.d(RUST_INDEPENDENT_TAG,"checkConvertAddr bytesConverted=" + Arrays.toString(bytesConverted));
        return bytesConverted;
    }

    private final static String RUST_INDEPENDENT_TAG = "rust-independent";

}
