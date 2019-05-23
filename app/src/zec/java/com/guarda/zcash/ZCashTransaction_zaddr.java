package com.guarda.zcash;

import com.google.common.primitives.Bytes;
import com.guarda.zcash.crypto.Base58;
import com.guarda.zcash.crypto.ECKey;
import com.guarda.zcash.crypto.Sha256Hash;
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

import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.DERSequenceGenerator;
import org.spongycastle.crypto.digests.Blake2bDigest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import timber.log.Timber;

import static com.guarda.zcash.RustAPI.newAsk;
import static com.guarda.zcash.crypto.Utils.bytesToHex;
import static com.guarda.zcash.crypto.Utils.hexToBytes;
import static com.guarda.zcash.crypto.Utils.revHex;
import static com.guarda.zcash.crypto.Utils.reverseByteArray;

public class ZCashTransaction_zaddr {

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
    //  private static final int header = 0x80000003; //version=3, fooverwintered=1
    private static final int header = 0x80000004; //version=4, fooverwintered=1
    private static final int versionGroupId = VERSION_BRANCH_ID_SAPLING;
    private static final int consensusBranchId = CONSENSUS_BRANCH_ID_SAPLING;
    private static final int SIGHASH_ALL = 1;

    private byte[] tx_sig_bytes;
    private byte[] tx_bytes;
//    private Vector<Tx_in> inputs = new Vector<>();
//    private Vector<Tx_out> outputs = new Vector<>();
    private List<SpendProof>  spendProofList = new ArrayList<>();
    private long locktime = 0;
    private int nExpiryHeight;
    private SaplingCustomFullKey privKey;
    private String zAddr;
    private byte[] bytesShieldedOutputs;
    private byte[] shieldedOutputsBlake;
    private int outputsSize = 0;
    private byte[] bytesShieldedSpends = new byte[0];
    private byte[] bytesSpendAuthSig;
    private byte[] shieldedSpendsBlake;
    private RustAPI rustAPI;
    private SpendProof sprf;
    private byte[] revNewAsk;
    private DbManager dbManager;
    private String toAddress;
    private Long value;
    private Long fee;
    private Long valueBalance;

    public ZCashTransaction_zaddr(SaplingCustomFullKey privKey, String toAddress, Long value, Long fee, int expiryHeight,
                                  List<ReceivedNotesRoom> unspents, DbManager dbManager) throws IllegalArgumentException {

        this.privKey = privKey;
        this.nExpiryHeight = expiryHeight;
        this.dbManager = dbManager;
        this.toAddress = toAddress;
        this.value = value;
        this.fee = fee;

        long valuePool = 0;

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
            bytesShieldedOutputs = Bytes.concat(bytesShieldedOutputs, getUotput(dTo, pkdTo, valuePool - value - fee));
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
//        /**
//         * Shielded spend
//         */
//        byte[] cv = sprf.getCv();
//        byte[] anchor = sprf.getAnchor();
//        byte[] nf = sprf.getNullifier();
//        byte[] rk = sprf.getRk();
//        byte[] spProof = sprf.getZkproof();
//
//        bytesShieldedSpends = Bytes.concat(cv, anchor, nf, rk, spProof); // cv + anchor + nullifier + rk + zkproof;
//
//        // in Blake2 add all parameters but without spendAuthSig
//        shieldedSpendsBlake = new byte[32];
//        Blake2bDigest spendsDigest = new Blake2bDigest(null, 32, null, ZCASH_SHIELDED_SPENDS_HASH_PERSONALIZATION);
//        spendsDigest.update(bytesShieldedSpends, 0, bytesShieldedSpends.length);
//        spendsDigest.doFinal(shieldedSpendsBlake, 0);
//        Timber.d("shieldedSpendsBlake=%s %d", Arrays.toString(shieldedSpendsBlake), shieldedSpendsBlake.length);
    }

    public byte[] getBytes() throws ZCashException {

        calcSigBytes();
//        calcSpendAuthSig();
        tx_bytes = Bytes.concat(
                Utils.int32BytesLE(header),
                Utils.int32BytesLE(versionGroupId),
                Utils.compactSizeIntLE(0) //transparent inputs size
        );

//        for (int i = 0; i < inputs.size(); i++) {
//            tx_bytes = Bytes.concat(tx_bytes, getSignedInputBytes(i));
//        }

        tx_bytes = Bytes.concat(tx_bytes, Utils.compactSizeIntLE(0)); //transparent outputs size
//        for (int i = 0; i < outputs.size(); i++) {
//            tx_bytes = Bytes.concat(tx_bytes, outputs.get(i).getBytes());
//        }
        byte[] bindingSig = RustAPI.getBsig(value.toString(), getSignatureHash());
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
//                tx_bytes,
//                Utils.int32BytesLE(locktime),
//                Utils.int32BytesLE(nExpiryHeight),
//                Utils.int64BytesLE(- 54321), // valueBalance
////                Utils.compactSizeIntLE(0), //nShieldedSpend (size)
//                new byte[32], //hashShieldedSpends, zeros for us (384 bytes * nShieldedSpend)
////                Utils.compactSizeIntLE(1), //nShieldedOutput (size) TODO: change size of shielded outputs
//                shieldedOutputsBlake, //hashShieldedOutputs (948 bytes * nShieldedOutput)
////                Utils.compactSizeIntLE(0), //nJoinSplit (size)
//                new byte[32], //nJoinSplits, zero
//                bindingSig
                //////////// TODO: check the order of parameters
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
//        for (int i = 0; i < inputs.size(); i++) {
//            ZCashTransaction_zaddr.Tx_in input = inputs.get(i);
//            prevouts_ser = Bytes.concat(prevouts_ser, input.txid, Utils.int32BytesLE(input.index));
//        }

        prevoutsDigest.update(prevouts_ser, 0, prevouts_ser.length);
        prevoutsDigest.doFinal(hashPrevouts, 0);

        Blake2bDigest sequenceDigest = new Blake2bDigest(null, 32, null, ZCASH_SEQUENCE_HASH_PERSONALIZATION);

        byte[] sequence_ser = new byte[0];
//        for (int i = 0; i < inputs.size(); i++) {
//            sequence_ser = Bytes.concat(sequence_ser, Utils.int32BytesLE(inputs.get(i).sequence));
//        }

        sequenceDigest.update(sequence_ser, 0, sequence_ser.length);
        sequenceDigest.doFinal(hashSequence, 0);

        Blake2bDigest outputsDigest = new Blake2bDigest(null, 32, null, ZCASH_OUTPUTS_HASH_PERSONALIZATION);
        byte[] outputs_ser = new byte[0];
//        for (int i = 0; i < outputs.size(); i++) {
//            ZCashTransaction_zaddr.Tx_out out = outputs.get(i);
//            outputs_ser = Bytes.concat(
//                    outputs_ser,
//                    Utils.int64BytesLE(out.value),
//                    Utils.compactSizeIntLE(out.script.length),
//                    out.script
//            );
//        }

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
//        String alpha = "09a344a7d78aa4e7c6f985b85e9eb94dcc0f1db17dbc7e54ab987a7f836789af"; // uint256
//        byte[] alpha = sprf.getAlpha();

        //TODO: check revNewAsk
//        bytesSpendAuthSig = RustAPI.spendSig(bytesToHex(revNewAsk), bytesToHex(alpha), bytesToHex(getSignatureHash()));
        return RustAPI.spendSig(bytesToHex(privKey.getAsk()), bytesToHex(alpha), bytesToHex(getSignatureHash()));
    }

//    private byte[] getSignedInputBytes(int index) throws ZCashException {
//        ZCashTransaction_zaddr.Tx_in input = inputs.get(index);
//        byte[] sign = Bytes.concat(getInputSignature(input), new byte[]{1});
//        byte[] pubKey = privKey.getPubKeyPoint().getEncoded(true);
//        return Bytes.concat(
//                input.txid,
//                Utils.int32BytesLE(input.index),
//                Utils.compactSizeIntLE(sign.length + pubKey.length + Utils.compactSizeIntLE(sign.length).length + Utils.compactSizeIntLE(pubKey.length).length),
//                Utils.compactSizeIntLE(sign.length),
//                sign,
//                Utils.compactSizeIntLE(pubKey.length),
//                pubKey,
//                Utils.int32BytesLE(input.sequence)
//        );
//    }

//    private byte[] getInputSignature(ZCashTransaction_zaddr.Tx_in input) throws ZCashException {
//        byte[] personalization = Bytes.concat(ZCASH_SIGNATURE_HASH_PERSONALIZATION, Utils.int32BytesLE(consensusBranchId));
//        Blake2bDigest tx_digest = new Blake2bDigest(null, 32, null, personalization);
//        byte[] preimage = Bytes.concat(
//                tx_sig_bytes,
//                input.txid,
//                Utils.int32BytesLE(input.index),
//                Utils.compactSizeIntLE(input.script.length),
//                input.script,
//                Utils.int64BytesLE(input.value),
//                Utils.int32BytesLE(input.sequence)
//        );
//
//        byte[] hash = new byte[32];
//        tx_digest.update(preimage, 0, preimage.length);
//        tx_digest.doFinal(hash, 0);
//        Sha256Hash sha256Hash = new Sha256Hash(hash);
//        ECKey.ECDSASignature sig = privKey.sign(sha256Hash);
//        sig = sig.toCanonicalised();
//        ByteArrayOutputStream bos = new ByteArrayOutputStream(72);
//        try {
//            DERSequenceGenerator seq = new DERSequenceGenerator(bos);
//            seq.addObject(new ASN1Integer(sig.r));
//            seq.addObject(new ASN1Integer(sig.s));
//            seq.close();
//        } catch (IOException e) {
//            throw new ZCashException("Cannot encode signature into transaction in ZCashTransaction_zaddr.getInputSignature", e);
//        }
//
//        return bos.toByteArray();
//    }

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

    public SpendProof addSpendS(ReceivedNotesRoom in) {
        SaplingWitnessesRoom witness = dbManager.getAppDb().getSaplingWitnessesDao().getWitness(in.getCm());
        IncrementalWitness iw = IncrementalWitness.fromJson(witness.getWitness());
        String anchor = iw.root();
        MerklePath mp = iw.path();
        Timber.d("addSpendS anchor=%s", anchor);
        TxOutRoom out = dbManager.getAppDb().getTxOutputDao().getOut(in.getCm());
        SaplingNotePlaintext snp = SaplingNotePlaintext.tryNoteDecrypt(out, privKey.getIvk());
        byte[] r = snp.getRcmbytes();
        String alpha = RustAPI.genr();
        Timber.d("addSpendS alpha=%s", alpha);
        String v = in.getValue().toString();


//        RustAPI rapi = new RustAPI(context);
//        rapi.checkInit();

        byte[] spProof = RustAPI.spendProof(
                bytesToHex(privKey.getAk()),//+ //TODO: check reverse bytes
                bytesToHex(privKey.getNsk()),//+ //TODO: check reverse bytes
                bytesToHex(privKey.getD()),//+
                bytesToHex(r),//+
                alpha,//+
                v,//+
                revHex(anchor),//+
                mp.getAuthPathPrimitive(),//+
                mp.getIndexPrimitive()//+
        );
        // cv, rk, zproof, nf
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

        //cv + anchor + nullifier + rk + zkproof

//        String nf = "8719a09298fffc28f042b81fd65faa04c179d85675217f16bd056643f4af794a"; //uint256
        return new SpendProof(cv, hexToBytes(anchor), nullifier, rk, zkproof, al);
    }

    private byte[] getUotput(byte[] dTo, byte[] pkdTo, Long value) {
//        byte[] d = newD;
//        byte[] pkbytes = newPkd;
//        byte[] ovkM = newOvk;
//        print("d=" + Arrays.toString(d) + " size=" + d.length);
//        print("pkbytes=" + Arrays.toString(pkbytes) + " size=" + pkbytes.length);
        //vShieldedOutput
        String rhex = RustAPI.genr();
        Timber.d("getUotputs rhex=%s", rhex);
//        SaplingNote sn = new SaplingNote(d, pkbytes, new BigInteger("54321").longValue(), hexToBytes(rhex), rhex);
//        SaplingNotePlaintext snp = new SaplingNotePlaintext(sn, new byte[512]);
        SaplingNotePlaintext snp = new SaplingNotePlaintext(dTo, TypeConvert.longToBytes(value), hexToBytes(rhex), new byte[512], rhex);

        // SaplingNoteEncryption
        String eskhex = RustAPI.genr();
        String epkstr = RustAPI.epk(Utils.bytesToHex(dTo), eskhex);
        Timber.d("getUotputs epkstr=" + epkstr);
//        if (epkstr.equals("librustzcash_sapling_ka_derivepublic=false")) {
//            print("first time is librustzcash_sapling_ka_derivepublic=false - recall");
//            epkstr = epk(Utils.bytesToHex(d), eskhex);
//        }
//
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
        ProofAndCv pacv = getProof(dTo, pkdTo, snper.sne.eskbS, eskhex, snp.getRcmStr());


        byte[] zkproof = pacv.proof;
        byte[] cv = pacv.cv;

        String cmhex = RustAPI.cm(Utils.bytesToHex(dTo), Utils.bytesToHex(pkdTo), value.toString(), snp.getRcmStr());
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

    private ProofAndCv getProof(byte[] d, byte[] pkd, byte[] esk, String eskhex, String rStr) {
        // TODO:
        //        checkInit();

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
                "54321");
        Timber.d("greeting =" + Arrays.toString(gstr) + " s=" + gstr.length);

        byte[] proof = new byte[192];
        byte[] cv = new byte[32];
        System.arraycopy(gstr, 0, proof, 0, 192);
        System.arraycopy(gstr, 192, cv, 0, 32);

        return new ProofAndCv(proof, cv, new byte[1]);
    }

}
