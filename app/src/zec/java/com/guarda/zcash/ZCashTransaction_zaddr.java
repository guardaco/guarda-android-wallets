package com.guarda.zcash;

import com.google.common.primitives.Bytes;
import com.guarda.zcash.crypto.Utils;
import com.guarda.zcash.globals.TypeConvert;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.db.model.ReceivedNotesRoom;
import com.guarda.zcash.sapling.db.model.SaplingWitnessesRoom;
import com.guarda.zcash.sapling.db.model.TxOutRoom;
import com.guarda.zcash.sapling.key.SaplingCustomFullKey;
import com.guarda.zcash.sapling.note.OutputDescription;
import com.guarda.zcash.sapling.note.ProofAndCv;
import com.guarda.zcash.sapling.note.SaplingNoteEncryption;
import com.guarda.zcash.sapling.note.SaplingNotePlaintext;
import com.guarda.zcash.sapling.note.SaplingNotePlaintextEncryptionResult;
import com.guarda.zcash.sapling.note.SaplingOutgoingPlaintext;
import com.guarda.zcash.sapling.note.SpendProof;
import com.guarda.zcash.sapling.tree.IncrementalWitness;
import com.guarda.zcash.sapling.tree.MerklePath;

import org.spongycastle.crypto.digests.Blake2bDigest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

import static com.guarda.zcash.crypto.Utils.bytesToHex;
import static com.guarda.zcash.crypto.Utils.hexToBytes;
import static com.guarda.zcash.crypto.Utils.revHex;
import static com.guarda.zcash.crypto.Utils.reverseByteArray;

public class ZCashTransaction_zaddr implements ZcashTransaction {

    private static final byte[] ZCASH_PREVOUTS_HASH_PERSONALIZATION = {'Z', 'c', 'a', 's', 'h', 'P', 'r', 'e', 'v', 'o', 'u', 't', 'H', 'a', 's', 'h'}; //ZcashPrevoutHash
    private static final byte[] ZCASH_SEQUENCE_HASH_PERSONALIZATION = {'Z', 'c', 'a', 's', 'h', 'S', 'e', 'q', 'u', 'e', 'n', 'c', 'H', 'a', 's', 'h'}; //ZcashSequencHash
    private static final byte[] ZCASH_OUTPUTS_HASH_PERSONALIZATION = {'Z', 'c', 'a', 's', 'h', 'O', 'u', 't', 'p', 'u', 't', 's', 'H', 'a', 's', 'h'};  //ZcashOutputsHash
    private static final byte[] ZCASH_SIGNATURE_HASH_PERSONALIZATION = {'Z', 'c', 'a', 's', 'h', 'S', 'i', 'g', 'H', 'a', 's', 'h'}; // ZcashSigHash (12 bytes)
    private static final byte[] ZCASH_SHIELDED_SPENDS_HASH_PERSONALIZATION = {'Z','c','a','s','h','S','S','p','e','n','d','s','H','a','s','h'}; // ZcashSSpendsHash
    private static final byte[] ZCASH_SHIELDED_OUTPUTS_HASH_PERSONALIZATION = {'Z','c','a','s','h','S','O','u','t','p','u','t','H','a','s','h'}; // ZcashSOutputHash

    private static final int VERSION_BRANCH_ID_OVERWINTER = 0x03C48270;
    private static final int VERSION_BRANCH_ID_SAPLING = 0x892F2085;
    private static final int CONSENSUS_BRANCH_ID_OVERWINTER = 0x5ba81b19;
    private static final int CONSENSUS_BRANCH_ID_SAPLING = 0x76b809bb;
    private static final int header = 0x80000004; //version=4, fooverwintered=1
    private static final int versionGroupId = VERSION_BRANCH_ID_SAPLING;
    private static final int consensusBranchId = CONSENSUS_BRANCH_ID_SAPLING;
    private static final int SIGHASH_ALL = 1;

    private byte[] tx_sig_bytes;
    private byte[] tx_bytes;
    private List<SpendProof>  spendProofList = new ArrayList<>();
    private long locktime = 0;
    private int nExpiryHeight;
    private SaplingCustomFullKey privKey;
    private String zAddr;
    private byte[] bytesShieldedOutputs = new byte[0];
    private byte[] shieldedOutputsBlake;
    private int outputsSize = 0;
    private byte[] bytesShieldedSpends = new byte[0];
    private byte[] shieldedSpendsBlake;
    private DbManager dbManager;
    private Long fee;

    public ZCashTransaction_zaddr(SaplingCustomFullKey privKey, String toAddress, Long value, Long fee, int expiryHeight,
                                  List<ReceivedNotesRoom> unspents, DbManager dbManager) throws IllegalArgumentException {

        this.privKey = privKey;
        this.nExpiryHeight = expiryHeight;
        this.dbManager = dbManager;
        this.fee = fee;

        long valuePool = 0;

        //Initialization sapling proving context (it's free after fail or completed)
        String res = RustAPI.proveContextInit();
        Timber.d("proveContextInit res=%s", res);

        /**
         * Shielded spends
         */

        for (ReceivedNotesRoom r : unspents) {
            spendProofList.add(addSpendS(r));
            valuePool += r.getValue();
        }

        for (SpendProof sp : spendProofList) {
            bytesShieldedSpends = Bytes.concat(
                    bytesShieldedSpends,
                    sp.getCv(),
                    sp.getAnchor(),
                    sp.getNullifier(),
                    sp.getRk(),
                    sp.getZkproof());
        }

        // in Blake2 add all parameters but without spendAuthSig
        shieldedSpendsBlake = new byte[32];
        Blake2bDigest spendsDigest = new Blake2bDigest(null, 32, null, ZCASH_SHIELDED_SPENDS_HASH_PERSONALIZATION);
        spendsDigest.update(bytesShieldedSpends, 0, bytesShieldedSpends.length);
        spendsDigest.doFinal(shieldedSpendsBlake, 0);
        Timber.d("shieldedSpendsBlake=%s %d", Arrays.toString(shieldedSpendsBlake), shieldedSpendsBlake.length);

        /**
         * Shielded output
         */
        byte[] addressToBytes = RustAPI.checkConvertAddr(toAddress);
        byte[] dTo = new byte[11];
        byte[] pkdTo = new byte[32];
        System.arraycopy(addressToBytes, 0, dTo, 0, 11);
        System.arraycopy(addressToBytes, 11, pkdTo, 0, 32);

        bytesShieldedOutputs = Bytes.concat(bytesShieldedOutputs, getUotput(dTo, pkdTo, value));
        outputsSize++;
        if (valuePool - value - fee > 0) {
            bytesShieldedOutputs = Bytes.concat(bytesShieldedOutputs, getUotput(privKey.getD(), privKey.getPkd(), valuePool - value - fee));
            outputsSize++;
        } else if (valuePool - value - fee < 0) {
            throw new IllegalArgumentException("Found sapling unspents cannot fund this transaction.");
        }

        Timber.d("bytesShieldedOutputs (bytes)=%s %d",Arrays.toString(bytesShieldedOutputs), bytesShieldedOutputs.length); // 948 bytes for an output
        shieldedOutputsBlake = new byte[32];
        Blake2bDigest prevoutsDigest = new Blake2bDigest(null, 32, null, ZCASH_SHIELDED_OUTPUTS_HASH_PERSONALIZATION);
        prevoutsDigest.update(bytesShieldedOutputs, 0, bytesShieldedOutputs.length);
        prevoutsDigest.doFinal(shieldedOutputsBlake, 0);
        Timber.d("shieldedOutputsBlake=%s %d", Arrays.toString(shieldedOutputsBlake), shieldedOutputsBlake.length);

    }

    public byte[] getBytes() {

        calcSigBytes();
        tx_bytes = Bytes.concat(
                Utils.int32BytesLE(header),
                Utils.int32BytesLE(versionGroupId),
                Utils.compactSizeIntLE(0) //transparent inputs size
        );

        tx_bytes = Bytes.concat(tx_bytes, Utils.compactSizeIntLE(0)); //transparent outputs size

        byte[] bindingSig = RustAPI.getBsig(fee.toString(), getSignatureHash());
        Timber.d("tx geBytes() bindingSig=" + Arrays.toString(bindingSig) + " s=" + bindingSig.length);

        byte[] bytesShieldedSpendsAndAuthSig = new byte[0];
        for (SpendProof sp : spendProofList) {
            bytesShieldedSpendsAndAuthSig = Bytes.concat(
                    bytesShieldedSpendsAndAuthSig, sp.getCv(),
                    sp.getAnchor(),
                    sp.getNullifier(),
                    sp.getRk(),
                    sp.getZkproof(),
                    calcSpendAuthSig(sp.getAlpha()));
        }

        tx_bytes = Bytes.concat(
                tx_bytes,
                Utils.int32BytesLE(locktime),
                Utils.int32BytesLE(nExpiryHeight),
                Utils.int64BytesLE(fee), //valuebalance (The net value of Sapling Spend transfers minus Output transfers)
                Utils.compactSizeIntLE(spendProofList.size()), //nShieldedSpend (size)
                bytesShieldedSpendsAndAuthSig, //TODO: append bytesSpendAuthSig for every spend
                Utils.compactSizeIntLE(outputsSize), //nShieldedOutput (size)
                bytesShieldedOutputs, //hashShieldedOutputs
                Utils.compactSizeIntLE(0), //nJoinSplit (size)
                bindingSig
        );


        return tx_bytes;
    }

    private void calcSigBytes() {
        byte[] hashPrevouts = new byte[32];
        byte[] hashSequence = new byte[32];
        byte[] hashOutputs = new byte[32];

        Blake2bDigest prevoutsDigest = new Blake2bDigest(null, 32, null, ZCASH_PREVOUTS_HASH_PERSONALIZATION);
        byte[] prevouts_ser = new byte[0];

        prevoutsDigest.update(prevouts_ser, 0, prevouts_ser.length);
        prevoutsDigest.doFinal(hashPrevouts, 0);

        Blake2bDigest sequenceDigest = new Blake2bDigest(null, 32, null, ZCASH_SEQUENCE_HASH_PERSONALIZATION);

        byte[] sequence_ser = new byte[0];

        sequenceDigest.update(sequence_ser, 0, sequence_ser.length);
        sequenceDigest.doFinal(hashSequence, 0);

        Blake2bDigest outputsDigest = new Blake2bDigest(null, 32, null, ZCASH_OUTPUTS_HASH_PERSONALIZATION);
        byte[] outputs_ser = new byte[0];

        outputsDigest.update(outputs_ser, 0, outputs_ser.length);
        outputsDigest.doFinal(hashOutputs, 0);

        tx_sig_bytes = Bytes.concat(Utils.int32BytesLE(header),
                Utils.int32BytesLE(versionGroupId),
                hashPrevouts,
                hashSequence,
                hashOutputs,
                new byte[32], //hashJoinSplits, zeros for us
                shieldedSpendsBlake, //hashShieldedSpends
                shieldedOutputsBlake, //shieldedOutputsBlake,
                Utils.int32BytesLE(locktime),
                Utils.int32BytesLE(nExpiryHeight),
                /**
                 * valueBalance:
                 * when tx has ShieldedSpends - valueBalance += spends value
                 * when tx has ShieldedOutputs - valueBalance -= outputs value
                 */
                Utils.int64BytesLE(fee),
                Utils.int32BytesLE(SIGHASH_ALL)
        );
    }

    private byte[] calcSpendAuthSig(byte[] alpha) {
        return RustAPI.spendSig(revHex(bytesToHex(privKey.getAsk())), bytesToHex(alpha), bytesToHex(getSignatureHash()));
    }

    private byte[] getSignatureHash() {
        byte[] personalization = Bytes.concat(ZCASH_SIGNATURE_HASH_PERSONALIZATION, Utils.int32BytesLE(consensusBranchId));
        Blake2bDigest tx_digest = new Blake2bDigest(null, 32, null, personalization);
        byte[] preimage = tx_sig_bytes;

        byte[] hash = new byte[32];
        tx_digest.update(preimage, 0, preimage.length);
        tx_digest.doFinal(hash, 0);
        Timber.d("getSignatureHash hash=" + Arrays.toString(hash) + " s=" + hash.length);
        return hash;
    }

    private SpendProof addSpendS(ReceivedNotesRoom in) {
        SaplingWitnessesRoom witness = dbManager.getAppDb().getSaplingWitnessesDao().getWitness(in.getCm());
        IncrementalWitness iw = IncrementalWitness.fromJson(witness.getWitness());

        String anchor = iw.root();
        MerklePath mp = iw.path();
        Timber.d("addSpendS anchor=%s", anchor);
        TxOutRoom out = dbManager.getAppDb().getTxOutputDao().getOut(in.getCm());
        SaplingNotePlaintext snp = SaplingNotePlaintext.tryNoteDecrypt(out, privKey);
        byte[] r = snp.getRcmbytes();
        String alpha = RustAPI.genr();
        Timber.d("addSpendS alpha=%s", alpha);
        String v = in.getValue().toString();

        byte[] spProof = RustAPI.spendProof(
                revHex(bytesToHex(privKey.getAk())),
                revHex(bytesToHex(privKey.getNsk())),
                bytesToHex(privKey.getD()),
                (bytesToHex(r)),
                alpha,
                v,
                revHex(anchor),
                mp.getAuthPathPrimitive(),
                mp.getIndexPrimitive()
        );

        Timber.d("addSpendS spProof=%s %d", Arrays.toString(spProof), spProof.length);

        byte[] cv = new byte[32];
        byte[] rk = new byte[32];
        byte[] zkproof = new byte[192];
        byte[] nullifier = new byte[32];
        byte[] al = hexToBytes(alpha);

        System.arraycopy(spProof, 0, cv, 0, 32);
        System.arraycopy(spProof, 32, rk, 0, 32);
        System.arraycopy(spProof, 64, zkproof, 0, 192);
        System.arraycopy(spProof, 64 + 192, nullifier, 0, 32);

        Timber.d("addSpendS nf=%s, %s", in.getNf(), bytesToHex(nullifier));
        return new SpendProof(cv, hexToBytes(anchor), nullifier, rk, zkproof, al);
    }

    private byte[] getUotput(byte[] dTo, byte[] pkdTo, Long value) {
        String rhex = RustAPI.genr();
        Timber.d("getUotputs rhex=%s", rhex);
        SaplingNotePlaintext snp = new SaplingNotePlaintext(dTo, TypeConvert.longToBytes(value), hexToBytes(rhex), new byte[0], rhex);

        // SaplingNoteEncryption
        String eskhex = RustAPI.genr();
        String epkstr = RustAPI.epk(Utils.bytesToHex(dTo), eskhex);
        Timber.d("getUotputs epkstr=" + epkstr);
        SaplingNoteEncryption sne = new SaplingNoteEncryption(reverseByteArray(Utils.hexToBytes(epkstr)), reverseByteArray(hexToBytes(eskhex)), eskhex);
        Timber.d("getUotputs " + sne.toString());
        SaplingNotePlaintextEncryptionResult snper = snp.encrypt(pkdTo, sne);

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
        ProofAndCv pacv = getProof(dTo, pkdTo, snper.sne.eskbS, eskhex, snp.getRcmStr(), value);


        byte[] zkproof = pacv.proof;
        byte[] cv = pacv.cv;

        //z2z
        String cmhex = RustAPI.cm(
                bytesToHex(dTo),
                bytesToHex(pkdTo),
                value.toString(),
                snp.getRcmStr());
        Timber.d("getUotputs cmhex =" + cmhex);
        byte[] cmrust = reverseByteArray(hexToBytes(cmhex));

        byte[] ephemeralKey = snper.sne.epkbP;
        byte[] encCiphertext = snper.secbyte;

        SaplingOutgoingPlaintext sop = new SaplingOutgoingPlaintext(pkdTo, snper.sne.eskbS);
        byte[] outCiphertext = SaplingOutgoingPlaintext.encryptToOurselves(privKey.getOvk(), cv, cmrust, snper.sne.epkbP, sop.toByte());

        OutputDescription od = new OutputDescription(cv, cmrust, ephemeralKey, encCiphertext, outCiphertext, zkproof);   //cv, cmu, ephemeralKey, encCiphertext, outCiphertext, zkproof
        Timber.d(od.toString());

        byte[] outBytes = Bytes.concat(cv, cmrust, ephemeralKey, encCiphertext, outCiphertext, zkproof);

        return outBytes;
    }

    private ProofAndCv getProof(byte[] d, byte[] pkd, byte[] esk, String eskhex, String rStr, Long value) {
        /**
         * expected 256 bytes (proof + cv + rcv)
         */
        Timber.d("greeting eskhex=%s s=%s", eskhex, eskhex.length());
        Timber.d("greeting d=%s s=%s", Arrays.toString(d), d.length);
        Timber.d("greeting pkd=%s s=%s", Arrays.toString(pkd), pkd.length);

        byte[] gstr = RustAPI.greeting(eskhex,
                Utils.bytesToHex(d),
                Utils.bytesToHex(pkd),
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
