package com.guarda.zcash;

import com.google.common.primitives.Bytes;
import com.guarda.zcash.crypto.Base58;
import com.guarda.zcash.crypto.Utils;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.db.model.ReceivedNotesRoom;
import com.guarda.zcash.sapling.db.model.SaplingWitnessesRoom;
import com.guarda.zcash.sapling.db.model.TxOutRoom;
import com.guarda.zcash.sapling.key.SaplingCustomFullKey;
import com.guarda.zcash.sapling.note.SaplingNotePlaintext;
import com.guarda.zcash.sapling.note.SpendProof;
import com.guarda.zcash.sapling.tree.IncrementalWitness;
import com.guarda.zcash.sapling.tree.MerklePath;

import org.spongycastle.crypto.digests.Blake2bDigest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import timber.log.Timber;
import work.samosudov.rustlib.RustAPI;

import static com.guarda.zcash.crypto.Utils.bytesToHex;
import static com.guarda.zcash.crypto.Utils.hexToBytes;
import static com.guarda.zcash.crypto.Utils.revHex;

public class ZCashTransaction_ztot implements ZcashTransaction {

    private static final byte[] ZCASH_PREVOUTS_HASH_PERSONALIZATION = {'Z', 'c', 'a', 's', 'h', 'P', 'r', 'e', 'v', 'o', 'u', 't', 'H', 'a', 's', 'h'}; //ZcashPrevoutHash
    private static final byte[] ZCASH_SEQUENCE_HASH_PERSONALIZATION = {'Z', 'c', 'a', 's', 'h', 'S', 'e', 'q', 'u', 'e', 'n', 'c', 'H', 'a', 's', 'h'}; //ZcashSequencHash
    private static final byte[] ZCASH_OUTPUTS_HASH_PERSONALIZATION = {'Z', 'c', 'a', 's', 'h', 'O', 'u', 't', 'p', 'u', 't', 's', 'H', 'a', 's', 'h'};  //ZcashOutputsHash
    private static final byte[] ZCASH_SIGNATURE_HASH_PERSONALIZATION = {'Z', 'c', 'a', 's', 'h', 'S', 'i', 'g', 'H', 'a', 's', 'h'}; // ZcashSigHash (12 bytes)
    private static final byte[] ZCASH_SHIELDED_SPENDS_HASH_PERSONALIZATION = {'Z','c','a','s','h','S','S','p','e','n','d','s','H','a','s','h'}; // ZcashSSpendsHash
    private static final byte[] ZCASH_SHIELDED_OUTPUTS_HASH_PERSONALIZATION = {'Z','c','a','s','h','S','O','u','t','p','u','t','H','a','s','h'}; // ZcashSOutputHash

    private static final int VERSION_BRANCH_ID_BLOSSOM = 0x892F2085;
    private static final int CONSENSUS_BRANCH_ID_BLOSSOM = 0x2BB40E60;
    private static final int header = 0x80000004; //version=4, fooverwintered=1
    private static final int versionGroupId = VERSION_BRANCH_ID_BLOSSOM;
    private static final int consensusBranchId = CONSENSUS_BRANCH_ID_BLOSSOM;
    private static final int SIGHASH_ALL = 1;

    private byte[] tx_sig_bytes;
    private byte[] tx_bytes;
    private List<SpendProof>  spendProofList = new ArrayList<>();
    private Vector<TxOutTranspatent> outputs = new Vector<>();
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
    private Long valueBalance;

    public ZCashTransaction_ztot(SaplingCustomFullKey privKey, String toAddress, Long value, Long fee, int expiryHeight,
                                 List<ReceivedNotesRoom> unspents, DbManager dbManager) throws IllegalArgumentException {

        this.privKey = privKey;
        this.nExpiryHeight = expiryHeight;
        this.dbManager = dbManager;
        this.fee = fee;

        valueBalance = value + fee;

        byte[] toKeyHash = Arrays.copyOfRange(Base58.decodeChecked(toAddress), 2, 22);
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
         * Transpatent output
         */
        this.outputs.add(new TxOutTranspatent(toKeyHash, value));

        if (valuePool - value - fee > 0) {
            bytesShieldedOutputs = Bytes.concat(bytesShieldedOutputs, ZcashTransactionHelper.buildOutDesc(privKey.getD(), privKey.getPkd(), privKey, valuePool - value - fee));
            outputsSize++;
        } else if (valuePool - value - fee < 0) {
            throw new IllegalArgumentException("Found sapling unspents cannot fund this transaction.");
        }

        Timber.d("bytesShieldedOutputs (bytes)=%s %d", Arrays.toString(bytesShieldedOutputs), bytesShieldedOutputs.length); // 948 bytes for an output
        shieldedOutputsBlake = new byte[32];
        if (outputsSize > 0) {
            Blake2bDigest prevoutsDigest = new Blake2bDigest(null, 32, null, ZCASH_SHIELDED_OUTPUTS_HASH_PERSONALIZATION);
            prevoutsDigest.update(bytesShieldedOutputs, 0, bytesShieldedOutputs.length);
            prevoutsDigest.doFinal(shieldedOutputsBlake, 0);
        }
        Timber.d("shieldedOutputsBlake=%s %d", Arrays.toString(shieldedOutputsBlake), shieldedOutputsBlake.length);
    }

    public byte[] getBytes() {

        calcSigBytes();
        tx_bytes = Bytes.concat(
                Utils.int32BytesLE(header),
                Utils.int32BytesLE(versionGroupId),
                Utils.compactSizeIntLE(0) //transparent inputs size
        );

        tx_bytes = Bytes.concat(tx_bytes, Utils.compactSizeIntLE(outputs.size()));
        for (int i = 0; i < outputs.size(); i++) {
            tx_bytes = Bytes.concat(tx_bytes, outputs.get(i).getBytes());
        }

        byte[] bindingSig = RustAPI.getBsig(valueBalance.toString(), getSignatureHash());
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
                Utils.int64BytesLE(valueBalance), //valuebalance (The net value of Sapling Spend transfers minus Output transfers)
                Utils.compactSizeIntLE(spendProofList.size()), //nShieldedSpend (size)
                bytesShieldedSpendsAndAuthSig, //TODO: append bytesSpendAuthSig for every spend
                Utils.compactSizeIntLE(outputsSize) //nShieldedOutput (size)
        );

        if (outputsSize > 0) {
            tx_bytes = Bytes.concat(tx_bytes,
                    bytesShieldedOutputs //hashShieldedOutputs
            );
        }

        tx_bytes = Bytes.concat(
                tx_bytes,
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
        for (int i = 0; i < outputs.size(); i++) {
            TxOutTranspatent out = outputs.get(i);
            outputs_ser = Bytes.concat(
                    outputs_ser,
                    Utils.int64BytesLE(out.value),
                    Utils.compactSizeIntLE(out.script.length),
                    out.script
            );
        }

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
                Utils.int64BytesLE(valueBalance),
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
        Timber.d("addSpendS ReceivedNotesRoom cm=%s", in.getCm());
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

}
