package com.guarda.ethereum.sapling.note;

import com.google.common.primitives.Bytes;
import com.guarda.ethereum.ZCashException;
import com.guarda.ethereum.crypto.Utils;
import com.guarda.ethereum.globals.TypeConvert;
import com.guarda.ethereum.sapling.db.model.TxOutRoom;
import com.guarda.ethereum.sapling.key.SaplingCustomFullKey;

import java.util.Arrays;

import timber.log.Timber;
import work.samosudov.rustlib.RustAPI;
import work.samosudov.zecrustlib.ZecLibRustApi;

import static com.guarda.ethereum.crypto.Utils.bytesToHex;
import static com.guarda.ethereum.crypto.Utils.hexToBytes;
import static com.guarda.ethereum.crypto.Utils.revHex;
import static com.guarda.ethereum.crypto.Utils.reverseByteArray;

public class SaplingNotePlaintext {

    byte[] d;
    public byte[] vbytes;
    byte[] rcmbytes;
    String rcmStr;
    byte[] memobytes = new byte[512];

    byte[] pkdbytes;

    SaplingNote sn;
    byte[] memo = new byte[512];

    private static final int ZC_NOTEPLAINTEXT_LEADING = 1;
    private static final int ZC_V_SIZE = 8;
    private static final int ZC_R_SIZE = 32;
    private static final int ZC_MEMO_SIZE = 512;
    private static final int ZC_DIVERSIFIER_SIZE = 11;
    private static final int ZC_SAPLING_ENCPLAINTEXT_SIZE = (ZC_NOTEPLAINTEXT_LEADING + ZC_DIVERSIFIER_SIZE + ZC_V_SIZE + ZC_R_SIZE + ZC_MEMO_SIZE);
    private static final int ENC_CIPHERTEXT_SIZE = ZC_SAPLING_ENCPLAINTEXT_SIZE + 16;
    private static final int COMPACT_NOTE_SIZE = 1 + 11 + 8 + 32; // version + diversifier + value + rcv
    private static final byte[] T01 = {(byte) 0x01};
    private static final byte[] T02 = {(byte) 0x02};

    public SaplingNotePlaintext(byte[] d, byte[] vbytes, byte[] rcmbytes, byte[] memobytes, String rHex) {
        this.d = d;
        this.vbytes = vbytes;
        this.rcmbytes = rcmbytes;
        this.memobytes = memobytes;
        this.rcmStr = rHex;
    }

    public SaplingNotePlaintext(byte[] d, byte[] vbytes, byte[] rcmbytes, byte[] memobytes) {
        this.d = d;
        this.vbytes = vbytes;
        this.rcmbytes = rcmbytes;
        this.memobytes = memobytes;
    }

    /**
     * leadingByte - 1 byte (0x01)
     * d - 11 bytes
     * value - 8 bytes
     * rcm - 32 bytes
     * memo - 512 bytes
     */

    public static SaplingNotePlaintext fromBytes(byte[] fromByteArray) throws ZCashException {
        if (fromByteArray[0] != 0x01 && fromByteArray[0] != 0x02)
            throw new ZCashException("SaplingNotePlaintext first byte is not 0x01 or 0x02 = " + fromByteArray[0]);

        byte[] d = new byte[11];
        byte[] vbytes = new byte[8];
        byte[] rcm = new byte[32];
        byte[] memobytes = new byte[512];

        System.arraycopy(fromByteArray, 1, d, 0, 11);
        System.arraycopy(fromByteArray, 11 + 1, vbytes, 0, 8);
        System.arraycopy(fromByteArray, 8 + 11 + 1, rcm, 0, 32);
        // don't need for Note from Compact ciphertext (which length is 52)
        if (fromByteArray.length == ZC_SAPLING_ENCPLAINTEXT_SIZE) {
            System.arraycopy(fromByteArray, 32 + 8 + 11 + 1, memobytes, 0, 512);
        } else {
            memobytes = new byte[0];
        }

        return new SaplingNotePlaintext(d, reverseByteArray(vbytes), reverseByteArray(rcm), memobytes);
    }

    public SaplingNotePlaintextEncryptionResult encrypt(byte[] pkd, SaplingNoteEncryption sne) {
        if (sne.epkbP == null || sne.eskbS == null) {
            Timber.d("SaplingNotePlaintext encrypt sne.epkb == null || sne.eskb == null");
            return null;
        }

        byte[] saplingEncPlaintext = this.toBytes();
        Timber.d("SaplingNotePlaintextEncryptionResult encrypt saplingEncPlaintext=" + Arrays.toString(saplingEncPlaintext) + " s=" + saplingEncPlaintext.length);
        byte[] encciphertext = sne.encryptToRecipient(pkd, saplingEncPlaintext);
        if (encciphertext == null) {
            Timber.d("SaplingNotePlaintext encrypt (encciphertext == null)");
            return null;
        }

        return new SaplingNotePlaintextEncryptionResult(encciphertext, sne);
    }

    public static SaplingNotePlaintext decrypt(String ciphertextHex, String epkHex, String cmuHex, SaplingCustomFullKey saplingKey) throws ZCashException {
        String ivkHex = revHex(bytesToHex(saplingKey.getIvk()));
        Timber.d("decrypt: ciphertextHex=%s ivkHex=%s epkHex=%s cmuHex=%s", ciphertextHex, ivkHex, epkHex, cmuHex);
        byte[] pt = attemptSaplingEncDecryption(ciphertextHex, ivkHex, epkHex);
        Timber.d("decrypt pt=%s, %s", Arrays.toString(pt), pt.length);
        if (pt.length != ZC_SAPLING_ENCPLAINTEXT_SIZE && pt.length != COMPACT_NOTE_SIZE)
            throw new ZCashException("SaplingNotePlaintext.decrypt() pt incorrect, length=" + pt.length);

        SaplingNotePlaintext snp = fromBytes(pt);
        Timber.d("SaplingNotePlaintext decrypt snp= " + snp);

        String dHex = bytesToHex(saplingKey.getD());
        Timber.d("snp.decrypt() dHex=%s", dHex);
        String pkdHex = RustAPI.ivkToPdk(ivkHex, dHex);
        snp.setPkdbytes(reverseByteArray(Utils.hexToBytes(pkdHex)));
        Timber.d("snp.decrypt() pkdHex=%s", pkdHex);

        String cmhexExpected;
        if (pt[0] == 0x01) {
            cmhexExpected = RustAPI.cm(dHex, pkdHex, String.valueOf(TypeConvert.bytesToLong(snp.vbytes)), bytesToHex(snp.rcmbytes));
        } else if (pt[0] == 0x02) {
            byte[] cmRseedBytes = ZecLibRustApi.cmRseed(saplingKey.getIvk(), pt);
            cmhexExpected = bytesToHex(reverseByteArray(cmRseedBytes));
        } else {
            throw new ZCashException("SaplingNotePlaintext: plaintext start byte is not 0x01 or 0x02");
        }

        Timber.d("snp.decrypt() cmuHex=%s", cmuHex);
        Timber.d("snp.decrypt() cmhexExpected=%s", cmhexExpected);
        return cmuHex.equals(cmhexExpected) ? snp : null;
    }

    private static byte[] attemptSaplingEncDecryption(String ciphertextHex, String ivkHex, String epkHex) {
        byte[] K = epkIvkToKey(ivkHex, epkHex);
        Timber.d("attemptSaplingEncDecryption K=%s s=%d", Arrays.toString(K), K.length);

        switch (ciphertextHex.length()) {
            // multiply 2 because cipherText is hex string, size constants for bytes
            case ENC_CIPHERTEXT_SIZE * 2:
                return decryptFullCipher(ciphertextHex, K);
            case COMPACT_NOTE_SIZE * 2:
                return decryptCompactCipher(ciphertextHex, K);
            default:
                Timber.e("attemptSaplingEncDecryption - size incorrect ciphertextHex=%s s=%d", ciphertextHex, ciphertextHex.length());
                return new byte[0];
        }
    }

    private static byte[] epkIvkToKey(String ivkHex, String epkHex) {
        String dhsecretHex = RustAPI.kagree(epkHex, ivkHex);

        byte[] K = RustAPI.kdfSapling(reverseByteArray(Utils.hexToBytes(dhsecretHex)), reverseByteArray(Utils.hexToBytes(epkHex)));

        Timber.d("epkIvkToKey K=%s s=%d", Arrays.toString(K), K.length);
        return K;
    }

    private static byte[] decryptFullCipher(String ciphertextHex, byte[] K) {
        byte[] decrypted = RustAPI.fullDecrypt(K, hexToBytes(ciphertextHex));

        Timber.d("decryptFullCipher decrypted=%s size=%d", Arrays.toString(decrypted), decrypted.length);

        byte[] decCut = new byte[564];
        if (decrypted.length == ENC_CIPHERTEXT_SIZE) {
            System.arraycopy(decrypted, 0, decCut, 0, 564);
        }

        return decCut;
    }

    private static byte[] decryptCompactCipher(String ciphertextHex, byte[] K) {
        byte[] result = RustAPI.compactDecrypt(K, hexToBytes(ciphertextHex));
        Timber.d("decryptCompactCipher compactDecrypt result=%s", Arrays.toString(result));

        return result;
    }

    public static SaplingNotePlaintext tryNoteDecrypt(TxOutRoom output, SaplingCustomFullKey saplingKey) {
        try {
            return decrypt(
                    output.getCiphertext(),
                    output.getEpk(),
                    output.getCmu(),
                    saplingKey);
        } catch (ZCashException e) {
            Timber.e("tryNoteDecrypt=%s", e.getMessage());
            return null;
        }
    }

    public SaplingNote getSaplingNote() {
        return new SaplingNote(d, pkdbytes, vbytes, rcmbytes);
    }

    /**
     * d - 11 bytes
     * value - 8 bytes
     * rcm - 32 bytes
     * memo - 512 bytes
     */
    byte[] toByteSn() {
        byte[] bytes = new byte[0];
        byte[] leadingByte = T01;
        return Bytes.concat(bytes, leadingByte, sn.d, TypeConvert.longToBytes(sn.value.longValue()), sn.r, memobytes);
    }

    byte[] toBytes() {
        byte[] bytes = new byte[0];
        byte[] leadingByte = T01;
        String hexValue = bytesToHex(vbytes);
        hexValue = revHex(hexValue);
        String hexR = bytesToHex(rcmbytes);
        hexR = revHex(hexR);
        byte[] fullMemoBytes = Bytes.concat(memobytes, new byte[512 - memobytes.length]);
        return Bytes.concat(bytes, leadingByte, d, hexToBytes(hexValue), hexToBytes(hexR), fullMemoBytes);
    }

    /**
     * Returns compact form of the Note plaintext (52 bytes without memo bytes)
     */
    public byte[] toBytesCompactV2() {
        byte[] bytes = new byte[0];
        byte[] valueBytes = vbytes.clone();
        byte[] rcmBytes = rcmbytes.clone();
        return Bytes.concat(bytes, T02, d, reverseByteArray(valueBytes), reverseByteArray(rcmBytes));
    }

    @Override
    public String toString() {
        return "SaplingNotePlaintext{\n" +
                "d=" + Arrays.toString(d) + "\n" +
                ", vbytes=" + Arrays.toString(vbytes) + "\n" +
                ", rcmbytes=" + Arrays.toString(rcmbytes) + "\n" +
                ", memobytes=" + Arrays.toString(memobytes) +
                '}';
    }

    public void setPkdbytes(byte[] pkdbytes) {
        this.pkdbytes = pkdbytes;
    }

    public byte[] getRcmbytes() {
        return rcmbytes;
    }

    public String getRcmStr() {
        return rcmStr;
    }

    public byte[] getMemobytes() {
        return memobytes;
    }
}
