package com.guarda.zcash;

import com.google.common.primitives.Bytes;
import com.guarda.zcash.crypto.Base58;
import com.guarda.zcash.crypto.ECKey;
import com.guarda.zcash.crypto.Sha256Hash;
import com.guarda.zcash.crypto.Utils;
import com.guarda.zcash.sapling.note.SpendProof;

import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.DERSequenceGenerator;
import org.spongycastle.crypto.digests.Blake2bDigest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import timber.log.Timber;

import static com.guarda.zcash.RustAPI.newAsk;
import static com.guarda.zcash.crypto.Utils.bytesToHex;
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
    private Vector<Tx_in> inputs = new Vector<>();
    private Vector<Tx_out> outputs = new Vector<>();
    private long locktime = 0;
    private int nExpiryHeight;
    private ECKey privKey;
    private String zAddr;
    private byte[] bytesShieldedOutputs;
    private byte[] shieldedOutputsBlake;
    private byte[] bytesShieldedSpends;
    private byte[] bytesSpendAuthSig;
    private byte[] shieldedSpendsBlake;
    private RustAPI rustAPI;
    private SpendProof sprf;
    private byte[] revNewAsk;

    public ZCashTransaction_zaddr(ECKey privKey, String fromAddr, String toAddr, Long value, Long fee, int expiryHeight,
                                  Iterable<ZCashTransactionOutput> outputs, SpendProof sprf) throws IllegalArgumentException {

        this.privKey = privKey;
        this.nExpiryHeight = expiryHeight;
        this.sprf = sprf;
        revNewAsk = reverseByteArray(newAsk);
        byte[] fromKeyHash = Arrays.copyOfRange(Base58.decodeChecked(fromAddr), 2, 22);
        byte[] toKeyHash = Arrays.copyOfRange(Base58.decodeChecked(toAddr), 2, 22);

        long value_pool = 0;
//        value = 54321L;
        value = 54129L; //54321 - 192
//        fee = 192L;

        //Only when there is transparent inputs
//        for (ZCashTransactionOutput out : outputs) {
//            inputs.add(new Tx_in(out));
//            value_pool += out.value;
//        }


        /**
         * Shielded output
         */
//        bytesShieldedOutputs = RustAPI.getUotputs();
//        Timber.d("bytesShieldedOutputs (bytes)=%s %d",Arrays.toString(bytesShieldedOutputs), bytesShieldedOutputs.length); // 948 bytes for an output
//        shieldedOutputsBlake = new byte[32];
//        Blake2bDigest prevoutsDigest = new Blake2bDigest(null, 32, null, ZCASH_SHIELDED_OUTPUTS_HASH_PERSONALIZATION);
//        prevoutsDigest.update(bytesShieldedOutputs, 0, bytesShieldedOutputs.length);
//        prevoutsDigest.doFinal(shieldedOutputsBlake, 0);
//        Timber.d("shieldedOutputsBlake=%s %d", Arrays.toString(shieldedOutputsBlake), shieldedOutputsBlake.length);
        /**
         * Shielded spend
         */
        byte[] cv = sprf.getCv();
        byte[] anchor = sprf.getAnchor();
        byte[] nf = sprf.getNullifier();
        byte[] rk = sprf.getRk();
        byte[] spProof = sprf.getZkproof();

        bytesShieldedSpends = Bytes.concat(cv, anchor, nf, rk, spProof); // cv + anchor + nullifier + rk + zkproof;

        // in Blake2 add all parameters but without spendAuthSig
        shieldedSpendsBlake = new byte[32];
        Blake2bDigest spendsDigest = new Blake2bDigest(null, 32, null, ZCASH_SHIELDED_SPENDS_HASH_PERSONALIZATION);
        spendsDigest.update(bytesShieldedSpends, 0, bytesShieldedSpends.length);
        spendsDigest.doFinal(shieldedSpendsBlake, 0);
        Timber.d("shieldedSpendsBlake=%s %d", Arrays.toString(shieldedSpendsBlake), shieldedSpendsBlake.length);


//        this.outputs.add(new Tx_out(fromKeyHash, value_pool - 54321L - 192L));
        this.outputs.add(new ZCashTransaction_zaddr.Tx_out(toKeyHash, value));
//        if (value_pool - value - fee > 0) {
//            this.outputs.add(new ZCashTransaction_zaddr.Tx_out(fromKeyHash, value_pool - value - fee));
//        } else if (value_pool - value - fee < 0) {
//            throw new IllegalArgumentException("Found UTXOs cannot fund this transaction.");
//        }
    }

    public byte[] getBytes() throws ZCashException {
//        if (tx_bytes != null) {
//            return tx_bytes;
//        }

        calcSigBytes();
        calcSpendAuthSig();
        tx_bytes = Bytes.concat(
                Utils.int32BytesLE(header),
                Utils.int32BytesLE(versionGroupId),
                Utils.compactSizeIntLE(inputs.size())
        );

        for (int i = 0; i < inputs.size(); i++) {
            tx_bytes = Bytes.concat(tx_bytes, getSignedInputBytes(i));
        }

        tx_bytes = Bytes.concat(tx_bytes, Utils.compactSizeIntLE(outputs.size()));
        for (int i = 0; i < outputs.size(); i++) {
            tx_bytes = Bytes.concat(tx_bytes, outputs.get(i).getBytes());
        }
        byte[] bindingSig = RustAPI.getBsig("54321", getSignatureHash());
        Timber.d("tx geBytes() bindingSig=" + Arrays.toString(bindingSig) + " s=" + bindingSig.length);

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
                Utils.int64BytesLE(54321), //valuebalance (The net value of Sapling Spend transfers minus Output transfers)
                Utils.compactSizeIntLE(1), //nShieldedSpend (size)
                bytesShieldedSpends,
                bytesSpendAuthSig, //TODO: append bytesSpendAuthSig for every spend
                Utils.compactSizeIntLE(0), //nShieldedOutput (size)
//                bytesShieldedOutputs, //hashShieldedOutputs
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
        for (int i = 0; i < inputs.size(); i++) {
            ZCashTransaction_zaddr.Tx_in input = inputs.get(i);
            prevouts_ser = Bytes.concat(prevouts_ser, input.txid, Utils.int32BytesLE(input.index));
        }

        prevoutsDigest.update(prevouts_ser, 0, prevouts_ser.length);
        prevoutsDigest.doFinal(hashPrevouts, 0);

        Blake2bDigest sequenceDigest = new Blake2bDigest(null, 32, null, ZCASH_SEQUENCE_HASH_PERSONALIZATION);

        byte[] sequence_ser = new byte[0];
        for (int i = 0; i < inputs.size(); i++) {
            sequence_ser = Bytes.concat(sequence_ser, Utils.int32BytesLE(inputs.get(i).sequence));
        }

        sequenceDigest.update(sequence_ser, 0, sequence_ser.length);
        sequenceDigest.doFinal(hashSequence, 0);

        Blake2bDigest outputsDigest = new Blake2bDigest(null, 32, null, ZCASH_OUTPUTS_HASH_PERSONALIZATION);
        byte[] outputs_ser = new byte[0];
        for (int i = 0; i < outputs.size(); i++) {
            ZCashTransaction_zaddr.Tx_out out = outputs.get(i);
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
//                new byte[32], //hashShieldedSpends
                new byte[32], //shieldedOutputsBlake,
//                shieldedOutputsBlake, //shieldedOutputsBlake,
                Utils.int32BytesLE(locktime),
                Utils.int32BytesLE(nExpiryHeight),
                /**
                 * valueBalance:
                 * when tx has ShieldedSpends - valueBalance += spends value
                 * when tx has ShieldedOutputs - valueBalance -= outputs value
                 */
                Utils.int64BytesLE(54321),
                Utils.int32BytesLE(SIGHASH_ALL)
        );
    }

    private void calcSpendAuthSig() {
//        String alpha = "09a344a7d78aa4e7c6f985b85e9eb94dcc0f1db17dbc7e54ab987a7f836789af"; // uint256
        byte[] alpha = sprf.getAlpha();

        bytesSpendAuthSig = RustAPI.spendSig(bytesToHex(revNewAsk), bytesToHex(alpha), bytesToHex(getSignatureHash()));
    }

    private byte[] getSignedInputBytes(int index) throws ZCashException {
        ZCashTransaction_zaddr.Tx_in input = inputs.get(index);
        byte[] sign = Bytes.concat(getInputSignature(input), new byte[]{1});
        byte[] pubKey = privKey.getPubKeyPoint().getEncoded(true);
        return Bytes.concat(
                input.txid,
                Utils.int32BytesLE(input.index),
                Utils.compactSizeIntLE(sign.length + pubKey.length + Utils.compactSizeIntLE(sign.length).length + Utils.compactSizeIntLE(pubKey.length).length),
                Utils.compactSizeIntLE(sign.length),
                sign,
                Utils.compactSizeIntLE(pubKey.length),
                pubKey,
                Utils.int32BytesLE(input.sequence)
        );
    }

    private byte[] getInputSignature(ZCashTransaction_zaddr.Tx_in input) throws ZCashException {
        byte[] personalization = Bytes.concat(ZCASH_SIGNATURE_HASH_PERSONALIZATION, Utils.int32BytesLE(consensusBranchId));
        Blake2bDigest tx_digest = new Blake2bDigest(null, 32, null, personalization);
        byte[] preimage = Bytes.concat(
                tx_sig_bytes,
                input.txid,
                Utils.int32BytesLE(input.index),
                Utils.compactSizeIntLE(input.script.length),
                input.script,
                Utils.int64BytesLE(input.value),
                Utils.int32BytesLE(input.sequence)
        );

        byte[] hash = new byte[32];
        tx_digest.update(preimage, 0, preimage.length);
        tx_digest.doFinal(hash, 0);
        Sha256Hash sha256Hash = new Sha256Hash(hash);
        ECKey.ECDSASignature sig = privKey.sign(sha256Hash);
        sig = sig.toCanonicalised();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(72);
        try {
            DERSequenceGenerator seq = new DERSequenceGenerator(bos);
            seq.addObject(new ASN1Integer(sig.r));
            seq.addObject(new ASN1Integer(sig.s));
            seq.close();
        } catch (IOException e) {
            throw new ZCashException("Cannot encode signature into transaction in ZCashTransaction_zaddr.getInputSignature", e);
        }

        return bos.toByteArray();
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

    private class Tx_in {
        byte[] txid;
        long index;
        byte[] script;
        int sequence = 0xffffffff;
        long value;

        Tx_in(ZCashTransactionOutput base) {
            List<Byte> txbytes = Bytes.asList(Utils.hexToBytes(base.txid));
            Collections.reverse(txbytes);
            txid = Bytes.toArray(txbytes);
            index = base.n;
            script = Utils.hexToBytes(base.hex);
            this.value = base.value;
        }
    }

    private class Tx_out {
        long value;
        byte[] script;

        Tx_out(byte[] pubKeyHash, long value) {
            this.value = value;
            script = Bytes.concat(new byte[]{(byte) 0x76, (byte) 0xa9, (byte) 0x14}, pubKeyHash, new byte[]{(byte) 0x88, (byte) 0xac});
            //                                OP_DUP       OP_HASH160  20_bytes     <PubkeyHash>           OP_EQUALVERIFY OP_CHECKSIG
        }

        byte[] getBytes() {
            return Bytes.concat(Utils.int64BytesLE(value), Utils.compactSizeIntLE(script.length), script);
        }
    }
}
