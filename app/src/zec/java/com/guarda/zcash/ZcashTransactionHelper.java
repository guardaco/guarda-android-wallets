package com.guarda.zcash;

import com.google.common.primitives.Bytes;
import com.guarda.zcash.crypto.Utils;
import com.guarda.zcash.globals.TypeConvert;
import com.guarda.zcash.sapling.key.SaplingCustomFullKey;
import com.guarda.zcash.sapling.note.OutputDescription;
import com.guarda.zcash.sapling.note.ProofAndCv;
import com.guarda.zcash.sapling.note.SaplingNoteEncryption;
import com.guarda.zcash.sapling.note.SaplingNotePlaintext;
import com.guarda.zcash.sapling.note.SaplingNotePlaintextEncryptionResult;
import com.guarda.zcash.sapling.note.SaplingOutgoingPlaintext;

import java.util.Arrays;

import timber.log.Timber;

import static com.guarda.zcash.crypto.Utils.bytesToHex;
import static com.guarda.zcash.crypto.Utils.hexToBytes;
import static com.guarda.zcash.crypto.Utils.reverseByteArray;

public class ZcashTransactionHelper {

    public static byte[] buildOutDesc(byte[] dToAddress, byte[] pkdToAddress, SaplingCustomFullKey privKey, Long value) {
        String rhex = RustAPI.genr();
        Timber.d("getUotputs rhex=%s", rhex);
        SaplingNotePlaintext snp = new SaplingNotePlaintext(dToAddress, TypeConvert.longToBytes(value), hexToBytes(rhex), new byte[0], rhex);

        // SaplingNoteEncryption
        String eskhex = RustAPI.genr();
        String epkstr = RustAPI.epk(Utils.bytesToHex(dToAddress), eskhex);
        Timber.d("getUotputs epkstr=%s", epkstr);
        SaplingNoteEncryption sne = new SaplingNoteEncryption(reverseByteArray(Utils.hexToBytes(epkstr)), reverseByteArray(hexToBytes(eskhex)), eskhex);
        Timber.d("getUotputs sne=%s", sne.toString());
        SaplingNotePlaintextEncryptionResult snper = snp.encrypt(pkdToAddress, sne);

        /**
         * outCiphertext
         * 1. get SaplingOutgoingPlaintext - it's a sequence of  pkd and esk
         * 2. call outPlaintext.encrypt(
         *             output.ovk,
         *             odesc.cv,
         *             odesc.cm,
         *             encryptor);
         *
         *  where encryptor is epk and esk
         * 3. inside encrypt() call encryptor.encrypt_to_ourselves(ovk, cv, cm, pt);
         * where pt is SaplingOutgoingPlaintext (pkd + esk)
         * 4.
         *
         */

        /**
         * zkproof - 192 bytes
         * cv - 32 bytes
         * rcv - 32 bytes
         */
        ProofAndCv pacv = getProof(dToAddress, pkdToAddress, eskhex, snp.getRcmStr(), value);


        byte[] zkproof = pacv.proof;
        byte[] cv = pacv.cv;

        //z2z
        String cmhex = RustAPI.cm(
                bytesToHex(dToAddress),
                bytesToHex(pkdToAddress),
                value.toString(),
                snp.getRcmStr());
        Timber.d("getUotputs cmhex=%s", cmhex);
        byte[] cmrust = reverseByteArray(hexToBytes(cmhex));

        byte[] ephemeralKey = snper.sne.epkbP;
        byte[] encCiphertext = snper.secbyte;

        SaplingOutgoingPlaintext sop = new SaplingOutgoingPlaintext(pkdToAddress, snper.sne.eskbS);
        byte[] outCiphertext = SaplingOutgoingPlaintext.encryptToOurselves(privKey.getOvk(), cv, cmrust, snper.sne.epkbP, sop.toByte());

        OutputDescription od = new OutputDescription(cv, cmrust, ephemeralKey, encCiphertext, outCiphertext, zkproof);   //cv, cmu, ephemeralKey, encCiphertext, outCiphertext, zkproof
        Timber.d(od.toString());

        return Bytes.concat(cv, cmrust, ephemeralKey, encCiphertext, outCiphertext, zkproof);
    }

    private static ProofAndCv getProof(byte[] dToAddress, byte[] pkdToAddress, String eskhex, String rStr, Long value) {
        /**
         * expected 256 bytes (proof + cv + rcv)
         */
        Timber.d("greeting eskhex=%s s=%s", eskhex, eskhex.length());
        Timber.d("greeting d=%s s=%s", Arrays.toString(dToAddress), dToAddress.length);
        Timber.d("greeting pkd=%s s=%s", Arrays.toString(pkdToAddress), pkdToAddress.length);

        byte[] gstr = RustAPI.greeting(eskhex,
                Utils.bytesToHex(dToAddress),
                Utils.bytesToHex(pkdToAddress),
                rStr,
                value.toString());
        Timber.d("greeting =" + Arrays.toString(gstr) + " s=" + gstr.length);

        byte[] proof = new byte[192];
        byte[] cv = new byte[32];
        System.arraycopy(gstr, 0, proof, 0, 192);
        System.arraycopy(gstr, 192, cv, 0, 32);

        return new ProofAndCv(proof, cv, new byte[1]);
    }
}
