package com.guarda.zcash;

import android.content.Context;
import android.content.res.AssetManager;

import com.guarda.zcash.crypto.Bech32;
import com.guarda.zcash.crypto.BitcoinCashBitArrayConverter;
import com.guarda.zcash.crypto.Utils;

import java.util.Arrays;

import timber.log.Timber;

import static com.guarda.zcash.crypto.Utils.bytesToHex;
import static com.guarda.zcash.crypto.Utils.revHex;

public class RustAPI {

    //April 12 2019
    //from python test-vectors (for i in range(116, 117):)
    public static String NEW_TESTNET_KEY = "L2QU1padaoHK5dMSbK6qqkZHjAdCxKtYWR4ZqmzsQUipdYGc2ADC";
    public static String NEW_TESTNET_ADDR = "tmEFo1fzEFPZGgEHzpVc5ecc6Qej6DjXUux";

    public static byte[] newOvk = {(byte)0x6e, (byte)0xdd, (byte)0x8e, (byte)0x9f, (byte)0xc3, (byte)0x48, (byte)0x26, (byte)0x38, (byte)0x2e, (byte)0x92, (byte)0xee, (byte)0xf0, (byte)0x77, (byte)0xa2, (byte)0x37, (byte)0x60, (byte)0xba, (byte)0x0c, (byte)0x41, (byte)0x78, (byte)0x6a, (byte)0x14, (byte)0x4e, (byte)0xd0, (byte)0xb7, (byte)0xcc, (byte)0x41, (byte)0x33, (byte)0xc2, (byte)0xce, (byte)0x48, (byte)0xac};
    public static byte[] newIvk = {(byte)0x5c, (byte)0xed, (byte)0x37, (byte)0xcc, (byte)0x27, (byte)0x29, (byte)0xc9, (byte)0xca, (byte)0x97, (byte)0x59, (byte)0xf9, (byte)0x72, (byte)0xf3, (byte)0x74, (byte)0xed, (byte)0x8a, (byte)0x91, (byte)0x44, (byte)0xaa, (byte)0xa0, (byte)0x0c, (byte)0xbe, (byte)0x09, (byte)0x38, (byte)0x90, (byte)0x3b, (byte)0x04, (byte)0xb2, (byte)0x85, (byte)0x80, (byte)0xb1, (byte)0x04};
    public static byte[] newD = {(byte)0xb6, (byte)0x1e, (byte)0x63, (byte)0x77, (byte)0x6c, (byte)0x8b, (byte)0x91, (byte)0xfe, (byte)0x6f, (byte)0x2e, (byte)0xa6};
    public static byte[] newPkd = {(byte)0xf6, (byte)0xe7, (byte)0x0c, (byte)0x51, (byte)0xa6, (byte)0xfb, (byte)0xd1, (byte)0xcf, (byte)0x9d, (byte)0x9f, (byte)0xfd, (byte)0x6e, (byte)0xfa, (byte)0x22, (byte)0x84, (byte)0xfd, (byte)0x96, (byte)0x14, (byte)0x36, (byte)0xe9, (byte)0xa3, (byte)0x8b, (byte)0xfd, (byte)0x59, (byte)0x58, (byte)0x15, (byte)0xea, (byte)0x8d, (byte)0x2e, (byte)0xee, (byte)0xa2, (byte)0xdf};
    public static byte[] newAk = {(byte)0xfc, (byte)0x4e, (byte)0x9b, (byte)0x2c, (byte)0xe6, (byte)0x0e, (byte)0x9b, (byte)0x18, (byte)0x24, (byte)0xe8, (byte)0x6f, (byte)0x51, (byte)0xa1, (byte)0xd4, (byte)0xf6, (byte)0x7b, (byte)0x14, (byte)0x91, (byte)0x0a, (byte)0x24, (byte)0x23, (byte)0xdc, (byte)0xa6, (byte)0xe5, (byte)0x4a, (byte)0xa5, (byte)0x00, (byte)0x92, (byte)0xfc, (byte)0x3f, (byte)0x2d, (byte)0x50};
    public static byte[] newNk = {(byte)0x18, (byte)0x95, (byte)0xa7, (byte)0x79, (byte)0xaa, (byte)0x54, (byte)0x46, (byte)0x0b, (byte)0x73, (byte)0x01, (byte)0x76, (byte)0x94, (byte)0xe6, (byte)0xf7, (byte)0xa5, (byte)0xc5, (byte)0xdd, (byte)0x02, (byte)0x4a, (byte)0xee, (byte)0x0a, (byte)0x8d, (byte)0xcb, (byte)0x79, (byte)0xb0, (byte)0xf8, (byte)0xc5, (byte)0x60, (byte)0xca, (byte)0xfa, (byte)0x9e, (byte)0xbc};
    public static byte[] newNsk = {(byte)0x72, (byte)0x8b, (byte)0xb5, (byte)0x6d, (byte)0x90, (byte)0x84, (byte)0x87, (byte)0x6d, (byte)0xfb, (byte)0xcd, (byte)0x0d, (byte)0x98, (byte)0x46, (byte)0xf8, (byte)0xdb, (byte)0xc7, (byte)0x6f, (byte)0x16, (byte)0x52, (byte)0x7c, (byte)0x3a, (byte)0x01, (byte)0x78, (byte)0xc3, (byte)0x8f, (byte)0x6e, (byte)0xbf, (byte)0xe1, (byte)0xfb, (byte)0x4f, (byte)0x76, (byte)0x0d};
    public static byte[] newAsk = {(byte)0xcf, (byte)0x5a, (byte)0xc1, (byte)0xc1, (byte)0x11, (byte)0xf9, (byte)0xe5, (byte)0x9f, (byte)0x0c, (byte)0x45, (byte)0x32, (byte)0x33, (byte)0x77, (byte)0x5c, (byte)0xc0, (byte)0x8c, (byte)0xaa, (byte)0x4b, (byte)0xf8, (byte)0x86, (byte)0xf5, (byte)0xd3, (byte)0x0f, (byte)0x00, (byte)0xa1, (byte)0x53, (byte)0x04, (byte)0xc7, (byte)0xb8, (byte)0x9f, (byte)0x04, (byte)0x0e};

    public static final String iwSer = "{\"cursor\":{\"deserIndex\":0,\"left\":\"299166965aad1d2ec4aaee5a9af1d145c4296f95bc9b4eb77d115e260f65a964\",\"parents\":[\"a90fc24705376927a91b74cc5b29ee14384c29dc514514a5c407011a092bc43d\",\"b58131e2218c168f249dda440bd0054a2c14bec3ae48d6072fc9704c1da01818\",\"cb688334edee6131310a52c1a29d7d0934e22e980fa184006109fd351acd4f5f\",\"a4c333730595ab5756ae2f788c02f9252468e686a118d8abba4d80d91e8efe5e\"],\"right\":\"65db9fc6a86cc91e812bb0c334010909e408ae7cbc419cd4e9784f380463aa55\"},\"cursor_depth\":7,\"filled\":[\"5674827a3244a4ef98ca255682f333bb8392c3047fc1aea272f00e1aac425020\",\"deb919870348525f35fe52f0fa02bc986a1b37e41bcc3ff20fdc1247aa3c8a22\",\"687266111cd9d76bc291010e642151e5350ddf76f3734d690d543602dd2a5f60\"],\"tree\":{\"deserIndex\":0,\"left\":\"7fc8e26c3c4711edf8590f9460dede975dfbe378795f05d459482037edb7bc0b\",\"parents\":[\"4d1667793fdd5baf0921a9f5b8126bd2950054b0ba84abb88848a3b19b72d41c\",\"\",\"\",\"abe2c95064f844c3205f837feda9db62b5f3ac2004f80d6378288b2065671459\",\"45acf64822a1af93fdcd2333997d2871b91673eb3f4c67e7bcd921600b3a5335\",\"\",\"\",\"\",\"\",\"f8116f03abcd02bec0862b99c84c572b0cf4b1bc4d0f4d4b7ff9179a2f900101\",\"\",\"\",\"\",\"\",\"\",\"383f5abdcb2ab13d05228205d736ec6d7b92a428fc45e7ada62328e803ad8523\"],\"right\":\"b733e839b5f844287a6a491409a991ec70277f39a50c99163ed378d23a829a07\"}}";


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

    public static native byte[] compactDecrypt(final String key,
                                               final String cipher);

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

    //endregion

    public String sayHello(String to) {

        int a=8;
        //
        switch (a) {
//            case 8:
//                return createTx();
//            case 9:
//                return checkEPK();
//            case 7:
//                return checkInit(context);
//            case 5:
//                return checkTestOut();
//            case 4:
//                return checkRnEpk();
//            case 3:
//                return checkDecrypt();
//            case 2:
//                return getTxsList();
//            case 1:
//                return checkCmus();
//            case 0:
//                return scanBlock();
//            case -1:
//                return protoBlocks();
//            case -3:
//                return calcWintesses();
//            case -4:
//                return decryptCha();
//            case -5:
//                return cmpDecrypt();
//            case -6:
//                return merkHash();
//            case -7:
//                return checkEmptyRoot();
//            case -8:
//                return checkTree();
//            case -9:
//                return checkMpSer();
//            case -10:
//                return checkUint256();
//            case -11:
//                return deserializeTree();
//            case -12:
//                return checkMyTxs();
//            case -13:
//                return testIvkReverse();
//            case -15:
//                return getNewTestnetAddr();
//            case -16:
//                return checkIvkToPkd();
//            case -17:
//                return checkEncrypt();
//            case -18:
//                return checkSpendSig();
//            case -19:
//                return checkVerify();
//            case -20:
//                return checkBsig();
//            case -21:
//                return checkHexD();
//            case -22:
//                return checkConvertNdkArr();
//            case -23:
//                return checkGenKeys();
            default:
                return "default returned";
        }
    }

//    String zAddr(String wif) {
//        String s = zAddrFromWif(wif.getBytes());
//        Timber.d("checkGenKeys s=%s", s);//ztestsapling1eeeqpt0vs457nf4u5e82jslk9cfsve58vv3vmjl5zp0katmcm4qkf222ajdlkl85k7efudmpjhj
//
//        return "checkGenKeys";
//    }

//    String checkConvertNdkArr() {
//        Timber.d("checkConvertNdkArr started");
//        int i = 0;
//        while (i < 1) {
//            byte[] bytes = new byte[52];
//            new Random().nextBytes(bytes);
////            Timber.d("checkConvertNdkArr bytes=%s", Arrays.toString(bytes));
////            byte[] bytes = hexToBytes();
//            String hex = bytesToHex(bytes);
//            byte[] fromBytes = testToArr(hex);
////            Timber.d("checkConvertNdkArr fromBytes=%s", Arrays.toString(fromBytes));
//            if (!Arrays.equals(bytes, fromBytes)) {
//                Timber.e("checkConvertNdkArr hex=%s from=%s", Arrays.toString(bytes), Arrays.toString(fromBytes));
//            }
//
//            i++;
//        }
//        Timber.d("checkConvertNdkArr completed");
//
//        return "checkConvertNdkArr";
//    }
//
//    String checkHexD() {
//        String d0 = "01442cfffd5f27c7c570f3";
//        String d1 = "01442cda5b5f27c7c570f3";
//
//        Timber.d("checkHexD d0=%s", Arrays.toString(hexToBytes(d0)));
//        Timber.d("checkHexD d1=%s", Arrays.toString(hexToBytes(d1)));
//
//        return "checkHexD";
//    }
//
//    String checkBsig() {
//        byte[] sh = {127, 67, -6, -79, -111, -9, 70, 35, 27, 111, -20, -13, -76, 89, -107, -40, 62, -9, 123, 59, -115, -106, -29, 53, -26, 95, -97, -118, -18, 87, -45, 113};
//        byte[] bsg = getBsig("-54321", sh);
//
//        Timber.d("checkBsig bsg=%s", Arrays.toString(bsg));
//
//        return "checkBsig";
//    }
//
//    String checkVerify() {
//
//        checkInit(context);
//
//        String cvHex = "d5653fd23e74204f10a9bbdad301d995a5edf3756410e1b910dea274f98d3249";
//        String anchorHex = "22931f7a1ae2bcb40551f93f6ba575830be30dcb21b048ae3a3ef7a1054bc52c";
//        String nfHex = "70ca62af9862c20cf8fe307ecccea26633850ed0ea59700f517a0e4d6bbe2ca6";
//        String rkHex = "846309c9983a203e629b051b1e5b5ea3279fc2cddf16cfe79895c7dbaab9a9e5";
//        String spProofHex = "978c53f0b5f0c9fe2d0059f0af400e1b7aef18d21832e7752a7c47285a78b68838233817497d63383a1c53f86b68721eacf01c2bd20eaa6946237e3c4aa2a1b95121f67db12635736c96aa04b9a2901a764ce95901bf78a9630148c197120a3300b5696918993675523f0a32685ff5391c4b3da8ab20de8f9ed0d96e51196e91e11b9f05e1dde84d6310a964000442a5a661873ba93c7d63589784d1aa7a1e6c25df1cbbf883b5814dba83d6f0eeb5a390c564bd33a67edfc1675fbe33d4b1b3";
//
//        String spAuSigHex = "b0ebb9f5f065d4a460a94cea47c4e14ee61ceac1283c8d96df489f9f325cb6e3ce985ae582308320f5985adf4b94997c1f28481b08ebad570bf88dda69edf50d";
//
//        String spendHash = cvHex + anchorHex + nfHex + rkHex + spProofHex + spAuSigHex;
//        Timber.d("checkVerify spendHash=%s", spendHash);
//        byte[] sig = {115, 36, -6, 56, 19, -86, 88, 76, -106, -30, 30, -98, 44, -28, 26, 114, -47, -18, -10, -40, -63, -96, -107, 23, 122, -65, -3, 4, 57, -26, -96, -55};
//        String sigHash = bytesToHex(sig);
//        byte[] bsig = {120, 118, -14, 113, -18, 72, -50, -121, -23, 18, 3, 119, -113, -116, 97, 21, -95, 116, -86, 57, 33, -25, 92, -59, -27, 117, -113, -77, -12, 87, 84, 39, 46, -92, -65, 93, -32, 21, -61, 115, -74, 42, -98, 91, 10, 51, 45, 64, 12, -18, -38, -75, 55, -16, 76, 84, -123, -119, -112, 124, -113, 28, 41, 11};
//        String bsigStr = bytesToHex(bsig);
//
//        // revhex for sigHash only here; check spend is successful with revHex
//        String vrf = testVerify(spendHash, revHex(sigHash), bsigStr);
//
//        Timber.d("checkVerify vrf=%s", vrf);
//
//        return "checkVerify";
//    }
//
//    String checkSpendSig() {
//        String alpha = genr();
//        byte[] sighash = {-116, -4, -28, 69, 110, -75, -80, 85, -32, 105, -98, -98, 112, 94, 15, 107, 71, 76, 103, -38, 11, 125, 50, 76, -100, 78, -88, -50, 108, -126, -56, 124};
//        byte[] spsig = spendSig(bytesToHex(reverseByteArray(newAsk)), alpha, bytesToHex(sighash));
//
//        Timber.d("checkSpendSig spsig=%s", Arrays.toString(spsig));
//
//        return "checkSpendSig";
//    }
//
//    String checkEncrypt() {
//        getUotputs();
//        Timber.d("checkEncrypt cnst=%s", "");
//
//        return "checkEncrypt";
//    }
//
//    String checkIvkToPkd() {
//
//        String i = bytesToHex(reverseByteArray(newIvk));
//        Timber.d("checkIvkToPkd i=%s, %s", i, i.length());
//        String d = bytesToHex(newD);
//        Timber.d("checkIvkToPkd d=%s, %s", d, d.length());
//
//        String p = ivkToPdk(i, d);
//
//        Timber.d("checkIvkToPkd=%s, %s", p, p.length());
//        return "checkIvkToPkd";
////        2019-04-12 17:19:05.221 1917-1917/? D/RustAPI: checkIvkToPkd i=0156b92e6444af532d781781579f67b23bec712a56ae79b31bc6e0d41d1f101b, 64
////        2019-04-12 17:19:05.222 1917-1917/? D/RustAPI: checkIvkToPkd d=f7d9dd2f27f8da18f62b49, 22
////        2019-04-12 17:19:14.332 1917-1917/? D/RustAPI: checkIvkToPkd=4dd45fe61cf2099ea1efb6258ddf05c910c6ae0a5a3b8398784b44e471f03ea9, 64
////        2019-04-12 17:19:14.333 1917-1917/? D/MainActivity: str from rust=checkIvkToPkd
//    }
//
//    String getNewTestnetAddr() {
//        String addr = "";
//        try {
//            addr = ZCashWalletManager
//                    .publicKeyFromPrivateKey_taddr("L2QU1padaoHK5dMSbK6qqkZHjAdCxKtYWR4ZqmzsQUipdYGc2ADC");
//        } catch (ZCashException e) {
//            Timber.d("getNewTestnetAddr err=%s", e.getMessage());
//        }
//
//        Timber.d("getNewTestnetAddr=%s", addr);
//        return "getNewTestnetAddr";
//    }
//
//    String testIvkReverse() {
//        Timber.d("testIvkReverse1=%s", revHex(bytesToHex(ivkt)));
//        Timber.d("testIvkReverse2=%s", revHex(bytesToHex(ivkt)));
//        return "testIvkReverse";
//    }
//
//    String checkMyTxs() {
//        checkInit(context);
//        Observable.fromCallable(new CallCheckMyTxs())
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe((b) -> Timber.d("CallCheckMyTxs accept thread=%s", Thread.currentThread().getName()));
//
//        return "checkMyTxs";
//    }
//
//    String deserializeTree() {
//        SaplingMerkleTree tree = new SaplingMerkleTree(treeOnHeight421720);
//
//        Timber.d("deserializeTree tree=%s", tree);
//
//        return "deserializeTree";
//    }
//
//    String checkUint256() {
//        String str = "6cbbbf93d636409a13b16f0d6e4d3a959a732ac18343bbbd7ef02eef863328d8";
//
//        String res = testUint256(str);
//
//        Timber.d("checkUint256 res=%s", res);
//
//        return "checkUint256";
//    }
//
//    String checkMpSer() {
//        boolean[][] auth = new boolean[0][0];
//        boolean[] index = new boolean[0];
//
//        byte[] res = vectorToInt(auth, index);
//
//        Timber.d("checkMpSer res=%s", Arrays.toString(res));
//        Timber.d("checkMpSer res=%s", Utils.bytesToHex(res));
//
//        return "checkMpSer";
//    }
//
//    String checkTree() {
//        SaplingMerkleTree tree = new SaplingMerkleTree();
//        List<IncrementalWitness> wtnsses = new ArrayList<>();
//
//        try {
//            int witness_ser_i = 0;
//            int path_i = 0;
//
//            String r0 = tree.root();
//            Timber.d("checkTree root=%s", r0);
//            try {
//                tree.last();
//            } catch (ZCashException e) {
//                Timber.d("checkTree last0=%s", e.getMessage());
//            }
//
//            Timber.d("checkTree empty_root=%s", tree.empty_root());
//
//            Timber.d("checkTree tree size 0=%s", tree.size());
//            for (int i = 0; i < 16; i++) {
//                Timber.d("checkTree i=%d", i);
//                String cm = markle_sapling_commitment[i];
//                Timber.d("checkTree cm=%s", cm);
//                wtnsses.add(tree.witness());
//
//                tree.append(cm);
//                Timber.d("checkTree tree size 1=%s", tree.size());
//                Timber.d("checkTree tree last=%s", tree.last()); // expected last appended cm
//
//                String r = tree.root();
//                Timber.d("checkTree tree root=%s", r);
//                if (!r.equals(markle_roots_sapling[i]))
//                    Timber.e("checkTree tree root incorrect=%s", markle_roots_sapling[i]);
//
//                String ser = tree.serialize();
//                Timber.d("checkTree tree serialize=%s", ser);
//                if (!ser.equals(markle_serialization_sapling[i]))
//                    Timber.e("checkTree tree serialize incorrect=%s", markle_serialization_sapling[i]);
//
//                boolean first = true;
//                for (IncrementalWitness w : wtnsses) {
//                    w.append(cm);
//                    Timber.d("checkTree w.append(cm) cm=%s", cm);
//
//                    if (first) {
//                        try {
//                            w.path(); // ZcashException expected
//                            w.element(); // ZcashException expected
//                        } catch (ZCashException e) {
//                            e.printStackTrace();
//                            Timber.d("w.path() or w.element() e=%s", e.getMessage());
//                        }
//
//                        Timber.d("checkTree wtnsses first");
//                    } else {
//                        MerklePath path = w.path();
//                        String p = path.serialize();
//                        Timber.d("checkTree w.path=%s", p);
//                        if (!p.equals(merkle_path_sapling[path_i++]))
//                            Timber.e("checkTree w.path incorrect=%s", merkle_path_sapling[path_i]);
//                    }
//
//                    String wr = w.root();
//                    String tr = tree.root();
//                    Timber.d("checkTree wr=%s, tr=%s", wr, tr);
//                    if (!wr.equals(tr))
//                        Timber.e("checkTree !wr.equals(tr)");
//
//                    first = false;
//                }
//            }
//        } catch (ZCashException e) {
//            Timber.e("checkTree e=%s", e.getMessage());
//        }
//
//        return "checkTree";
//    }
//
//    String checkEmptyRoot() {
////        PedersenHash next = PedersenHash.combine(PedersenHash.empty(), PedersenHash.empty(), 0);
////        Timber.d("checkEmptyRoot next=%s", next.getHash());
//        SaplingMerkleTree tree = new SaplingMerkleTree();
//        String hash = tree.empty_root();
//        Timber.d("checkEmptyRoot empty root=%s", hash);
//        try {
//            String r = tree.root();
//            if (!hash.equals(r)) throw new ZCashException("(!hash.equals(r))");
//            Timber.d("checkEmptyRoot tree.root=%s", r);
//        } catch (ZCashException e) {
//            Timber.e("checkEmptyRoot e=%s", e.getMessage());
//        }
//
//
////        EmptyMerkleRoots roots = new EmptyMerkleRoots(32);
////        for (int i = 0; i <= 32; i++) {
////            Timber.d("checkEmptyRoot empty i=%d root=%s", i, roots.empty_root(i));
////        }
//
//        return "checkEmptyRoot";
//    }
//
//    String merkHash() {
//        String a = "87a086ae7d2252d58729b30263fb7b66308bf94ef59a76c9c86e7ea016536505";
//        String b = "a75b84a125b2353da7e8d96ee2a15efe4de23df9601b9d9564ba59de57130406";
//        String res = merkelHash(25, a, b);
//        //expected = 5bf43b5736c19b714d1f462c9d22ba3492c36e3d9bbd7ca24d94b440550aa561
//        Timber.d("merkHash res=%s", res);
//
//        String a1 = "55a16c35c13ca8e6d0fef6048c29bde0c81978a7bc347607eb7fd5b26ae37d81";
//        String a2 = "344e80dd16698588bcccb227dbaf93d18abbf09f430688996c178bf103fce9ff";
//        String a3 = "6cbbbf93d636409a13b16f0d6e4d3a959a732ac18343bbbd7ef02eef863328d8";
//        String a4 = "491bb32c1b939f4c2bc1646a6475a1be510a4b3ad80baea4deb907c965de10e1";
//
//        Timber.d("merkHash res1=%s", Utils.revHex(a1));
//        Timber.d("merkHash res2=%s", Utils.revHex(a2));
//        Timber.d("merkHash res3=%s", Utils.revHex(a3));
//        Timber.d("merkHash res4=%s", Utils.revHex(a4));
//
//        return "merkHash";
//    }
//
//    String cmpDecrypt() {
//        String epk = "16812422527108f359697259491d3c547557b3b586ab07084566a3a302b2be15";
//        String cipherText = "0750f010d71d79d8dec9d5e4b18b0534209e73dd94bdde5fd317ac8ec8cf96d0f8bb420abd8d2c49f22612d84a726f2833eb11c4";
//        Timber.d("cmpDecrypt before=%s", Arrays.toString(newIvk));
//        String ivkHex = Utils.bytesToHex(reverseByteArray(newIvk));
//        Timber.d("cmpDecrypt after=%s", Arrays.toString(newIvk));
//        String dhsecretHex = RustAPI.kagree(epk, ivkHex);
//        byte[] K = SaplingNoteEncryption.KDFSapling(reverseByteArray(Utils.hexToBytes(dhsecretHex)), reverseByteArray(Utils.hexToBytes(epk)));
//        String kHex = Utils.bytesToHex(reverseByteArray(K));
//
//        byte[] result = compactDecrypt(kHex, cipherText);
//
//        Timber.d("cmpDecrypt result=%s", Arrays.toString(result));
//        return "cmpDecrypt";
//    }
//
//    String decryptCha() {
//        String cmu = "11432d17cb644f175989d5817093036d7ce96a1b4d31670d7f2774303afdab8b";
//        String epk = "a645c14ad23d87e9c1574e92b86a40e4b5989b42378349777453a1edd0cc4192";
//        String cipherText = "db870771d0b56b023c36e5d636024a6a35f828b322c6972067ce0b2d3031c5769a8f97de53a1d511135b2f0d3093d887b2f17ebf";
//        String ivkHex = Utils.bytesToHex(reverseByteArray(ivkt));
//
//        byte[] result = new byte[52];
//        LazySodiumAndroid lazySodium = LsaSingle.getInstance();
//        byte[] nonce = new byte[12]; // ??
//        String dhsecretHex = RustAPI.kagree(epk, ivkHex);
//        byte[] K = SaplingNoteEncryption.KDFSapling(reverseByteArray(Utils.hexToBytes(dhsecretHex)), reverseByteArray(Utils.hexToBytes(epk)));
//
//        byte[] ciphBytes = reverseByteArray(lazySodium.bytes(cipherText));
//
//        lazySodium.cryptoStreamChaCha20Xor(result, ciphBytes, ciphBytes.length, nonce, K);
//        Timber.d("decryptCha result=%s", Arrays.toString(result));
//
//        Stream.Lazy streamLazy = (Stream.Lazy) lazySodium;
//
//        byte[] nonce1 = new byte[12];
////        byte[] nonce1 = lazySodium.nonce(Stream.CHACHA20_NONCEBYTES);
//        String finalMsg = streamLazy.cryptoStreamXorDecrypt(cipherText, nonce1, Key.fromBytes(K), Stream.Method.CHACHA20);
//
//        Timber.d("decryptCha finalMsg=%s", Arrays.toString(lazySodium.bytes(finalMsg)));
//
//        return "decryptCha";
//    }
//
//    String calcWintesses() {
////        checkInit(context);
//
//        Observable.fromCallable(new CallWitnessesRange())
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe((b) -> Timber.d("CallWitnessesRange accept thread=%s", Thread.currentThread().getName()));
//
//        return "calcWintesses";
//    }
//
//    String scanBlock() {
//        AppDb appDb = Room.databaseBuilder(
//                context,
//                AppDb.class,
//                DB_NAME).build();
//        SaplingMerkleTree smr = new SaplingMerkleTree();
//        ProtoApi protoApi = new ProtoApi();
//        PublishProcessor<Long> paginator = PublishProcessor.create();
//
//        long startB = 252500; //testnet
//        long endB = 431075;
//        long step = 1000;
//
//        Observable.just("1").doOnNext(s -> {
//            long end = protoApi.pageNum + step;
//            if (end >= endB)
//                end = endB;
//            Log.d(LOGTAG, "apply thread=" + Thread.currentThread().getName());
//            protoApi.getBlocks(protoApi.pageNum, end);
//        }).observeOn(Schedulers.io()).subscribe(s -> {
//            Log.d(LOGTAG, "accept thread=" + Thread.currentThread().getName());
//            if (s == null) {
//                Log.d(LOGTAG, "paginator.accept(items) null");
//            }
//
//            if (protoApi.pageNum <= endB) {
//                paginator.onNext(0L);
//            } else {
////                            TestScanBlocks.setLocalBlocks(context, protoApi.getList());
//                Log.d(LOGTAG, "protoApi.pageNum >= endB");
//            }
//        });
//
//        Disposable disposable = paginator
//                .onBackpressureDrop()
//                .concatMap(new Function<Long, Publisher<Iterator<CompactFormats.CompactBlock>>>() {
//                    @Override
//                    public Publisher<Iterator<CompactFormats.CompactBlock>> apply(@NonNull Long nextRange) {
//                        long end = protoApi.pageNum + step;
//                        if (end >= endB)
//                            end = endB;
//                        Log.d(LOGTAG, "apply thread=" + Thread.currentThread().getName());
//                        return protoApi.getBlocks(protoApi.pageNum, end);
//                    }
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<Iterator<CompactFormats.CompactBlock>>() {
//                    @Override
//                    public void accept(@NonNull Iterator<CompactFormats.CompactBlock> items) {
////                        try {
////                            protoApi.checkBlocks(items);
////                        } catch (Exception e) {
////                            e.printStackTrace();
////                            Log.d(LOGTAG, "protoApi.checkBlocks(items); e=" + e.getMessage());
////                        }
//                        Log.d(LOGTAG, "accept thread=" + Thread.currentThread().getName());
//                        if (items == null) {
//                            Log.d(LOGTAG, "paginator.accept(items) null");
//                        }
//
//                        if (protoApi.pageNum <= endB) {
//                            paginator.onNext(0L);
//                        } else {
////                            TestScanBlocks.setLocalBlocks(context, protoApi.getList());
//                            Log.d(LOGTAG, "protoApi.pageNum >= endB");
//                        }
//                    }
//                });
//
//        paginator.onNext(0L);
//
////        Observable<Iterator<CompactFormats.CompactBlock>> blocksObservable =
////                Observable.fromCallable(protoApi::getBlocks);
////        Disposable blockSubscription = blocksObservable.
////                subscribeOn(Schedulers.io()).
////                observeOn(AndroidSchedulers.mainThread()).
////                subscribe(n -> {
////                            blocksObservable.
////                            protoApi.checkBlocks(n);
////                        },
////                        e -> Log.d(LOGTAG, "protoApi::checkBlocks subscribe error=" + e.getMessage()),
////                        () -> TestScanBlocks.setLocalBlocks(context, protoApi.getList())
////                        );
//
//        return "scanBlock";
//    }
//
//    private String protoBlocks() {
//        TestScanBlocks tsb = new TestScanBlocks();
//        tsb.getBlocks();
//        return "protoBlocks";
//    }
//
//    String checkCmus() {
//        ArrayList<WalletTx> list = TestScanBlocks.getLocalTxs(context);
//        String ivkHex = Utils.bytesToHex(reverseByteArray(ivkt));
//        Map<String, WalletTx> mapWallet = new HashMap<>();
//
//        mapWallet = WitnessManager.getMyTxs(list, ivkHex);
//        Log.d(LOGTAG, "checkCmu mapWallet=" + mapWallet.size());
//
//        return "checkCmu";
//    }
//
//    String getTxsList() {
//        TestScanBlocks t = new TestScanBlocks();
//        t.getTransactions(context);
//        return "getTxsList";
//    }
//
//    String checkDecrypt() {
////        byte[] encCiphertext = new byte[0];
////        String epkstr = "";
////        try {
////            // SaplingNoteEncryption
////            String eskhex = genr();
////            epkstr = epk(Utils.bytesToHex(newD), eskhex);
////            print("epkstr=" + epkstr);
////            SaplingNoteEncryption sne = new SaplingNoteEncryption(reverseByteArray(Utils.hexToBytes(epkstr)), reverseByteArray(hexToBytes(eskhex)), eskhex);
////            print(sne.toString());
////
////            SaplingNotePlaintext snp = SaplingNotePlaintext.fromBytes(notePt);
////            Timber.d("checkDecrypt from snp=%s", snp);
////            SaplingNotePlaintextEncryptionResult snper = snp.encrypt(newPkd, sne);
////            encCiphertext = snper.secbyte;
////        } catch (ZCashException e) {
////            e.printStackTrace();
////        }
//
//        String cmu = "3cce4a4f399e0e3d7101c0f57405ea86d86a391bf8444ce3eb93733e41b93c99";
//        String epk = "f0416dd13ce99ea6550e61fb2eed7dac0a2c0b7ac2e9f347d06595de3f2beb88";
//        String cipherText = "9131d2ab658a924fd4cf176fa1f14ffd2316f0c1cc9fefcadbe83822839e99c6e10ff6e37b7364eddf25d960d71dd2eec71628e9";
////        byte[] compactCipher = new byte[52];
////        System.arraycopy(encCiphertext, 0, compactCipher, 0, 52);
////        String cipherText = bytesToHex(compactCipher);
////        Timber.d("checkDecrypt cipherText=%s", cipherText);
//        String ivkHex = Utils.bytesToHex(reverseByteArray(newIvk));
//
//        try {
//            //when you get hexs from explorer
////            SaplingNotePlaintext snp = SaplingNotePlaintext.decrypt(cipherText, ivkHex, epkstr, cmu);
//            SaplingNotePlaintext snp = SaplingNotePlaintext.decrypt(cipherText, ivkHex, epk, cmu);
//            Timber.d("checkDecrypt snp=%s", snp);
//
//            //when you get hexs from output.toString();
////                SaplingNotePlaintext snp1 = SaplingNotePlaintext.decrypt(cipherText, ivkHex, revHex(epk), revHex(cmu));
////                Timber.d("checkDecrypt snp1=%s", snp1);
//        } catch (ZCashException e) {
//            e.printStackTrace();
//            Log.d(LOGTAG, "checkDecrypt z=" + e.getMessage());
//        }
//
//        return "checkDecrypt";
//    }
//
//    String checkRnEpk() {
//        checkInit(context);
//
//        byte[] d1 = {-43, 48, 41, -44, -108, -110, 123, 74, 42, -36, 55};
//        byte[] pkd1 = {-74, -114, -98, -32, -64, 103, -115, 123, 48, 54, -109, 28, -125, 26, 37, 37, 95, 126, -28, -121, 56, 90, 48, 49, 110, 21, -10, 72, 43, -121, 79, -38};
//        String sr = genr();
//        String epk = epk(Utils.bytesToHex(d1), sr);
//
//        String rnew = genr();
//        byte[] gstr = greeting(sr,
//                Utils.bytesToHex(d1),
//                Utils.bytesToHex(pkd1),
//                rnew,
//                "54321");
//        print("greeting =" + Arrays.toString(gstr) + " s=" + gstr.length);
//
//        return epk;
//    }
//
//    String checkTestOut() {
////        checkInit();
//
//        String sr = genr();
//        print("srhex = " + sr);
//        byte[] srbytes = Utils.hexToBytes(sr);
//        print("srbytes=" + Arrays.toString(srbytes) + " size=" + srbytes.length);
//
//        String cv = "cd00a025ba1c9235064c1b451555922a321ba0cd9ae26cd7cd8a7102c58bd464";
//        byte[] cvbytes = Utils.hexToBytes(cv);
//        print("cv=" + Arrays.toString(cvbytes) + " size=" + cvbytes.length);
//        String cmu = "cd00a025ba1c9235064c1b451555922a321ba0cd9ae26cd7cd8a7102c58bd464";
//        byte[] cmubytes = Utils.hexToBytes(cmu);
//        print("cmu=" + Arrays.toString(cmubytes) + " size=" + cmubytes.length);
//        String epk = "08e105fbb82da4167c5379794a5e5eea29df3ef11ead50bd18abd523bb37912b";
//        byte[] epkbytes = Utils.hexToBytes(epk);
//        print("epk=" + Arrays.toString(epkbytes) + " size=" + epkbytes.length);
//
//        String zk = "88121c0ea056869155029d27d09066b926756c9b8d45ac53aa15bd911172a2cc7318307b676180f0a3b90d77a1aa011e9643b08cb1924a755180db252f640d9d97a83bac46828642eebdf1942276397682c29086f7593ba0ded967009a91bebd194b58abe95921b23e378e22715a64d36f734c43ea9376b77148f28262374941cb63b0b941a232bd6c429aaad3c47d59aecf0d1e1efed15bd2f853c4e31dfd0f58779f22abd96e297e3a6ea095e3a46340244a0ab9113c5276fa18157dbc37d8";
//        byte[] zkbytes = Utils.hexToBytes(zk);
//        print("zk=" + Arrays.toString(zkbytes) + " size=" + zkbytes.length);
//
//
//        String checkout = checkout(sr, cmu, epk, zk);
//
//        print("checkTestOut=" + checkout);
//
//        return checkout;
//    }
//
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
//
//    String checkEPK() {
//
//        checkInit(context);
//
//        byte[] pk_d = {-74, -114, -98, -32, -64, 103, -115, 123, 48, 54, -109, 28, -125, 26, 37, 37, 95, 126, -28, -121, 56, 90, 48, 49, 110, 21, -10, 72, 43, -121, 79, -38};
//        byte[] eskbS = {2, 6, 94, -32, 34, 71, -86, -65, 68, -4, -67, 124, 92, 44, 103, -64, -59, 37, -56, -91, -98, 21, -111, 100, 81, -36, -4, 75, 31, 20, -19, -91};
//
//        String eskhex1 = "7fd5744ba186288c028da384b8c0d1a7e7804ef7f4016c0c32a216b9862a2e8";
//        byte[] d1 = {-43, 48, 41, -44, -108, -110, 123, 74, 42, -36, 55};
//        byte[] pkd1 = {-74, -114, -98, -32, -64, 103, -115, 123, 48, 54, -109, 28, -125, 26, 37, 37, 95, 126, -28, -121, 56, 90, 48, 49, 110, 21, -10, 72, 43, -121, 79, -38};
//        byte[] r1 = {10, 42, -44, 79, 19, 9, -94, 103, -78, -62, 25, 12, -48, 52, -31, 71, -19, 69, 86, -119, -61, 107, 119, -70, -52, -75, 8, 55, 48, -56, -35, 9};
//
//        byte[] g = greeting(eskhex1,
//                Utils.bytesToHex(d1),
//                Utils.bytesToHex(pkd1),
//                Utils.bytesToHex(r1),
//                "54321");
////
//        print("greeting=" + Arrays.toString(g) + " size=" + g.length);
//
//        byte[] sighash = {-116, -4, -28, 69, 110, -75, -80, 85, -32, 105, -98, -98, 112, 94, 15, 107, 71, 76, 103, -38, 11, 125, 50, 76, -100, 78, -88, -50, 108, -126, -56, 124};
//        byte[] bsighex = bsig("-54321", Utils.bytesToHex(sighash));
//        print("bsighex" + Arrays.toString(bsighex));
//
//        byte[] cv0 = {6, 12, -90, 46, 35, 47, 10, -17, 121, -72, -58, -41, 72, 120, 34, -13, -36, 70, -7, -115, -65, 93, 11, 64, 88, 54, -99, -78, 107, 82, -90, -21};
//        byte[] cmu0 = {7, 18, 111, 78, 11, 78, 2, -23, -20, 125, -54, 5, -79, -100, -84, 49, -103, 8, 122, 53, -20, -4, 83, -41, -86, 22, 53, -123, -90, -66, 121, 29};
//        byte[] ephemeralKey0 = {1, 122, -86, -18, -48, 34, 16, 50, 26, -58, -44, -111, 4, -14, -48, 46, 62, -5, -46, -104, 43, -86, -54, 122, 8, -43, -109, 104, 118, 70, -56, 87};
//        byte[] zk0 = {15, -70, 116, -11, 96, 117, -27, -93, -45, -126, -83, 113, -65, -78, 62, -89, -103, 112, 104, 65, -108, 104, 66, -105, 105, 5, -47, 118, -72, -26, -104, -19, 87, -125, -111, 27, 8, 66, 3, -124, 118, -70, 43, -113, -84, -2, 86, -108, 28, -44, -91, 34, 14, -38, -38, -82, -89, 66, -32, 45, 45, 69, 127, -72, -28, -54, 68, -117, 98, -111, 37, 61, -9, 74, 55, 54, 30, 93, 104, 31, -57, -63, -83, -76, 98, -52, -118, 115, -97, 66, -51, -93, -65, -91, 81, 11, -125, 10, -43, -73, -60, 11, 111, 80, -123, 123, -87, 68, -13, 73, 28, -110, 78, -100, 98, 16, -12, -125, 49, 17, 6, -86, 1, -110, 117, 126, -10, -84, -24, 43, 0, -80, -94, 89, 93, 114, 35, -23, 14, 97, 35, -84, 6, -89, -74, -69, 85, -81, 19, 104, -91, -99, -85, -75, 125, 31, -51, 73, 73, 37, -4, -76, 33, -107, -65, -16, -77, 23, -85, 59, 86, 95, -4, -46, 58, 101, -19, 89, 42, -44, 55, 71, 57, -61, -51, 77, -115, 122, 35, 108, -72, -112};
//
//        String checkout = checkout(Utils.bytesToHex(cv0), Utils.bytesToHex(cmu0), Utils.bytesToHex(ephemeralKey0),
//                Utils.bytesToHex(zk0));
//
//        print("checkout test=" + checkout);
//        return checkout;
//    }
//
//    String createTx() {
//
//        checkInit(context);
//
//        try {
//            ZCashWalletManager.getInstance().createTransaction_zaddr(NEW_TESTNET_ADDR,
//                    NEW_TESTNET_ADDR,
//                    0L,
//                    0L,
//                    NEW_TESTNET_KEY,
//                    1,
//                    this, new WalletCallback<String, ZCashTransaction_zaddr>() {
//                        @Override
//                        public void onResponse(String r1, ZCashTransaction_zaddr r2) {
//                            Log.i("RESPONSE CODE", "onResponse " + r1);
//                            if (r1.equals("ok")) {
//                                try {
//                                    String lastTxhex = Utils.bytesToHex(r2.getBytes());
//                                    Log.i("lastTxhex", lastTxhex);
//
//                                } catch (ZCashException e) {
//                                    Log.i("TX", "Cannot sign transaction");
//                                }
//                            } else {
//                                Log.i("psd", "createTransaction_taddr: RESPONSE CODE is not ok");
//                            }
//                        }
//                    });
//        } catch (ZCashException e) {
//            print("createTx ZCashException=" + e.getMessage());
//        }
//        return "createTxpix";
//    }
//
//    public static ProofAndCv getProof(byte[] d, byte[] pkd, byte[] esk, String eskhex, String rStr) throws ZCashException {
//        // TODO:
//        //
//        ();
//
//        /**
//         * expected 256 bytes (proof + cv + rcv)
//         */
//        Timber.d("greeting eskhex=%s s=%s", eskhex, eskhex.length());
//        Timber.d("greeting d=%s s=%s", Arrays.toString(d), d.length);
//        Timber.d("greeting pkd=%s s=%s", Arrays.toString(pkd), pkd.length);
//
//            byte[] gstr = greeting(eskhex,
//                    Utils.bytesToHex(d),
//                    Utils.bytesToHex(pkd),
//                    rStr,
//                    "54321");
//            print("greeting =" + Arrays.toString(gstr) + " s=" + gstr.length);
//
//        byte[] proof = new byte[192];
//        byte[] cv = new byte[32];
//        System.arraycopy(gstr, 0, proof, 0, 192);
//        System.arraycopy(gstr, 192, cv, 0, 32);
//
//        return new ProofAndCv(proof, cv, new byte[1]);
//    }
//
    public static byte[] getBsig(String value, byte[] data) {
        byte[] bsigStr = bsig(value, bytesToHex(data));
        Timber.d("getBsig=" + Arrays.toString(bsigStr) + " s=" + bsigStr.length);
        return bsigStr;
    }
    //private byte[] d={-50, 114, 0, -83, -20, -123, 105, -23, -90, -68, -90}, pkd=[78, -87, 67, -10, 46, 19, 6, 102, -121, 99, 34, -51, -53, -12, 16, 95, 110, -81, 120, -35, 65, 100, -87, 74, -20, -101, -5, 124, -12, -73, -78, -98]}
    public static byte[] checkConvertAddr(String address) {
        Bech32.Bech32Data ddd = Bech32.decodeWithoutVeryfy(address);
        Timber.d("checkConvertAddr ddd=%s", Arrays.toString(ddd.data));
        byte[] bytesConverted = BitcoinCashBitArrayConverter.convertBits(ddd.data, 5, 8, true);
        Timber.d("checkConvertAddr ddd=%s", Arrays.toString(bytesConverted));
        return bytesConverted;
    }
//
//    public static byte[] getUotputs() {
////        Bech32.Bech32Data ddd = Bech32.decodeWithoutVeryfy(zAddr);
////        print("1=" + ddd.hrp);
////        print("2=" + Arrays.toString(ddd.data) + " size=" + ddd.data.length);
////        print("zzz1=" + Bech32.encode("zs", ddd.data));
////        print("convert5to8=" + Arrays.toString(BitcoinCashBitArrayConverter.convertBits(ddd.data, 5, 8, true)));
//        byte[] d = newD;
//        byte[] pkbytes = newPkd;
//        byte[] ovkM = newOvk;
//        print("d=" + Arrays.toString(d) + " size=" + d.length);
//        print("pkbytes=" + Arrays.toString(pkbytes) + " size=" + pkbytes.length);
//        //vShieldedOutput
//        String rhex = genr();
//        print("rhex=" +  rhex);
////        SaplingNote sn = new SaplingNote(d, pkbytes, new BigInteger("54321").longValue(), hexToBytes(rhex), rhex);
////        SaplingNotePlaintext snp = new SaplingNotePlaintext(sn, new byte[512]);
//        SaplingNotePlaintext snp = new SaplingNotePlaintext(d, TypeConvert.longToBytes(54321), hexToBytes(rhex), new byte[512], rhex);
//
//        // SaplingNoteEncryption
//        String eskhex = genr();
//        String epkstr = epk(Utils.bytesToHex(d), eskhex);
//        print("epkstr=" + epkstr);
//        if (epkstr.equals("librustzcash_sapling_ka_derivepublic=false")) {
//            print("first time is librustzcash_sapling_ka_derivepublic=false - recall");
//            epkstr = epk(Utils.bytesToHex(d), eskhex);
//        }
////
//        print("epkstr=" + epkstr);
//        SaplingNoteEncryption sne = new SaplingNoteEncryption(reverseByteArray(Utils.hexToBytes(epkstr)), reverseByteArray(hexToBytes(eskhex)), eskhex);
//        print(sne.toString());
//        SaplingNotePlaintextEncryptionResult snper = snp.encrypt(pkbytes, sne);
//
//        /**
//         * outCiphertext
//         * 1. get SaplingOutgoingPlaintext - it's a sequence of  pkd and esk
//         * 2. call outPlaintext.encrypt(
//         *             output.ovk,
//         *             odesc.cv,
//         *             odesc.cm,
//         *             encryptor);
//         *
//         *  where encryptor is epk and esk
//         * 3. inside encrypt() call encryptor.encrypt_to_ourselves(ovk, cv, cm, pt);
//         * where pt is SaplingOutgoingPlaintext (pkd + esk)
//         * 4.
//         *
//         */
//
//        /**
//         * zkproof - 192 bytes
//         * cv - 32 bytes
//         * rcv - 32 bytes
//         */
//        ProofAndCv pacv;
//        try {
//            pacv = getProof(d, pkbytes, snper.sne.eskbS, eskhex, snp.getRcmStr());
//        } catch (ZCashException e) {
//            e.printStackTrace();
//            return new byte[0];
//        }
//
//
//        byte[] zkproof = pacv.proof;
//        byte[] cv = pacv.cv;
//
//        byte[] r = new byte[0];
//        String cmhex = cm(Utils.bytesToHex(d), Utils.bytesToHex(pkbytes), "54321", snp.getRcmStr());
//        print("cmhex =" + cmhex);
//        byte[] cmrust = reverseByteArray(hexToBytes(cmhex));
//
//        byte[] ephemeralKey = snper.sne.epkbP;
//        byte[] encCiphertext = snper.secbyte;
//
//        SaplingOutgoingPlaintext sop = new SaplingOutgoingPlaintext(pkbytes, snper.sne.eskbS);
//        byte[] outCiphertext = SaplingOutgoingPlaintext.encryptToOurselves(ovkM, cv, cmrust, snper.sne.epkbP, sop.toByte());
//
//        OutputDescription od = new OutputDescription(cv, cmrust, ephemeralKey, encCiphertext, outCiphertext, zkproof);   //cv, cmu, ephemeralKey, encCiphertext, outCiphertext, zkproof
//        print(od.toString());
//
//        byte[] outBytes = Bytes.concat(cv, cmrust, ephemeralKey, encCiphertext, outCiphertext, zkproof);
//
//        return outBytes;
//    }

}
