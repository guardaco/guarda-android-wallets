package com.bitshares.bitshareswallet.wallet.graphene.chain;

import com.bitshares.bitshareswallet.wallet.*;
import com.bitshares.bitshareswallet.wallet.fc.crypto.sha256_object;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mrd.bitlib.bitcoinj.Base58;

import org.spongycastle.crypto.digests.RIPEMD160Digest;
import org.spongycastle.crypto.digests.SHA256Digest;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;

import static com.bitshares.bitshareswallet.wallet.graphene.chain.config.GRAPHENE_ADDRESS_PREFIX;


public class types {
    enum reserved_spaces
    {
        relative_protocol_ids, // = 0
        protocol_ids,          // = 1
        implementation_ids     // = 2
    };
    /**
     *  List all object types from all namespaces here so they can
     *  be easily reflected and displayed in debug output.  If a 3rd party
     *  wants to extend the core code then they will have to change the
     *  packed_object::type field from enum_type to uint16 to avoid
     *  warnings when converting packed_objects to/from json.
     */
    public static class object_type {
        public static final int null_object_type = 0;
        public static final int base_object_type = 1;
        public static final int account_object_type = 2;
        public static final int asset_object_type = 3;
        public static final int limit_order_object_type = 7;
        public static final int operation_history_object_type = 11;
    }
    /*public enum object_type
    {
        null_object_type,
        base_object_type,
        account_object_type,
        asset_object_type,
        force_settlement_object_type,
        committee_member_object_type,
        witness_object_type,
        limit_order_object_type,
        call_order_object_type,
        custom_object_type,
        proposal_object_type,
        operation_history_object_type,
        withdraw_permission_object_type,
        vesting_balance_object_type,
        worker_object_type,
        balance_object_type,
        OBJECT_TYPE_COUNT ///< Sentry value which contains the number of different object types
    };*/

    enum impl_object_type
    {
        impl_global_property_object_type,
        impl_dynamic_global_property_object_type,
        impl_reserved0_object_type,      // formerly index_meta_object_type, TODO: delete me
        impl_asset_dynamic_data_type,
        impl_asset_bitasset_data_type,
        impl_account_balance_object_type,
        impl_account_statistics_object_type,
        impl_transaction_object_type,
        impl_block_summary_object_type,
        impl_account_transaction_history_object_type,
        impl_blinded_balance_object_type,
        impl_chain_property_object_type,
        impl_witness_schedule_object_type,
        impl_budget_record_object_type,
        impl_special_authority_object_type,
        impl_buyback_object_type,
        impl_fba_accumulator_object_type
    };

    /*enum asset_issuer_permission_flags
    {
        charge_market_fee    = 0x01, // < an issuer-specified percentage of all market trades in this asset is paid to the issuer
        white_list           = 0x02, // < accounts must be whitelisted in order to hold this asset
        override_authority   = 0x04, // < issuer may transfer asset back to himself
        transfer_restricted  = 0x08, // < require the issuer to be one party to every transfer
        disable_force_settle = 0x10, // < disable force settling
        global_settle        = 0x20, // < allow the bitasset issuer to force a global settling -- this may be set in permissions, but not flags
        disable_confidential = 0x40, // < allow the asset to be used with confidential transactions
        witness_fed_asset    = 0x80, // < allow the asset to be fed by witnesses
        committee_fed_asset  = 0x100 // < allow the asset to be fed by the committee
    };*/

    public final static int charge_market_fee    = 0x01; /**< an issuer-specified percentage of all market trades in this asset is paid to the issuer */
    public final static int white_list           = 0x02; /**< accounts must be whitelisted in order to hold this asset */
    public final static int override_authority   = 0x04; /**< issuer may transfer asset back to himself */
    public final static int transfer_restricted  = 0x08; /**< require the issuer to be one party to every transfer */
    public final static int disable_force_settle = 0x10; /**< disable force settling */
    public final static int global_settle        = 0x20; /**< allow the bitasset issuer to force a global settling -- this may be set in permissions, but not flags */
    public final static int disable_confidential = 0x40; /**< allow the asset to be used with confidential transactions */
    public final static int witness_fed_asset    = 0x80; /**< allow the asset to be fed by witnesses */
    public final static int committee_fed_asset  = 0x100; /**< allow the asset to be fed by the committee */

    public final static int ASSET_ISSUER_PERMISSION_MASK = charge_market_fee|white_list|override_authority|transfer_restricted|disable_force_settle|global_settle|disable_confidential
            |witness_fed_asset|committee_fed_asset;
    public final static int UIA_ASSET_ISSUER_PERMISSION_MASK = charge_market_fee|white_list|override_authority|transfer_restricted|disable_confidential;


    public static class binary_key {
        public byte[] data = new byte[33];
        public int check;
        public binary_key(byte[] key) {
            System.arraycopy(key, 0, data, 0, data.length);
            byte[] byteCheck = new byte[4];
            System.arraycopy(key, data.length, byteCheck, 0, 4);
            check = ByteBuffer.wrap(byteCheck).getInt();
        }
    }

    public static class public_key_type {
        public byte[] key_data = new byte[33];

        public public_key_type() {

        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(key_data);
        }

        @Override
        public boolean equals(Object obj) {
            public_key_type publicKeyType = (public_key_type)obj;
            return Arrays.equals(key_data, publicKeyType.key_data);
        }

        public public_key_type(public_key publicKey) {
            key_data = publicKey.getKeyByte();
        }

        @Override
        public String toString() {
            RIPEMD160Digest dig = new RIPEMD160Digest();
            dig.update(key_data, 0, key_data.length);
            byte[] out = new byte[20];
            dig.doFinal(out, 0);

            byte[] byteKeyData = new byte[37];
            System.arraycopy(key_data, 0, byteKeyData, 0, key_data.length);
            System.arraycopy(out, 0, byteKeyData, key_data.length, byteKeyData.length - key_data.length);

            String strResult = GRAPHENE_ADDRESS_PREFIX;
            strResult += Base58.encode(byteKeyData);

            return strResult;
        }

        public public_key_type(String strBase58) throws NoSuchAlgorithmException {
            String strPrefix = GRAPHENE_ADDRESS_PREFIX;
            byte[] byteKeyData = Base58.decode(strBase58.substring(strPrefix.length()));
            binary_key binaryKey = new binary_key(byteKeyData);

            RIPEMD160Digest digest = new RIPEMD160Digest();
            digest.update(binaryKey.data, 0, binaryKey.data.length);
            byte[] out = new byte[20];
            digest.doFinal(out, 0);

            byte[] byteOut = new byte[4];
            System.arraycopy(out, 0, byteOut, 0, byteOut.length);
            int nByteOut = ByteBuffer.wrap(byteOut).getInt();

            if (nByteOut != binaryKey.check) {
                throw new RuntimeException("Public key is not valid");
            }
            key_data = binaryKey.data;
        }

        public public_key getPublicKey() {
            return new public_key(key_data);
        }

        public boolean compare(public_key_type publicKeyType) {
            return Arrays.equals(key_data, publicKeyType.key_data);
        }
    }

    public static class private_key_type {
        private byte[] key_data = new byte[32];

        @Override
        public String toString() {
            byte[] data = new byte[key_data.length + 1 + 4];
            data[0] = (byte)0x80;
            System.arraycopy(key_data, 0, data, 1, key_data.length);

            SHA256Digest digest = new SHA256Digest();
            digest.update(data, 0, key_data.length + 1);
            byte[] out = new byte[32];
            digest.doFinal(out, 0);

            digest.update(out, 0, out.length);
            digest.doFinal(out, 0);

            System.arraycopy(out, 0, data, key_data.length + 1, 4);
            return Base58.encode(data);
        }

        public private_key_type(String strBase58) {
            byte wif_bytes[] = Base58.decode(strBase58);
            if (wif_bytes.length < 5) {
                throw new RuntimeException("Private key is not valid");
            }

            System.arraycopy(wif_bytes, 1, key_data, 0, key_data.length);

            SHA256Digest digest = new SHA256Digest();
            digest.update(wif_bytes, 0, wif_bytes.length - 4);
            byte[] hashCheck = new byte[32];
            digest.doFinal(hashCheck, 0);

            byte[] hashCheck2 = new byte[32];
            digest.update(hashCheck, 0, hashCheck.length);
            digest.doFinal(hashCheck2, 0);

            byte check[] = new byte[4];
            System.arraycopy(wif_bytes, wif_bytes.length - check.length, check, 0, check.length);

            byte[] check1 = new byte[4];
            byte[] check2 = new byte[4];
            System.arraycopy(hashCheck, 0, check1, 0, check1.length);
            System.arraycopy(hashCheck2, 0, check2, 0, check2.length);

            if (Arrays.equals(check1, check) == false &&
                    Arrays.equals(check2, check) == false) {
                throw new RuntimeException("Private key is not valid");
            }
        }

        public private_key_type(private_key privateKey) {
            key_data = privateKey.get_secret();
        }

        public private_key getPrivateKey() {
            return new private_key(key_data);
        }
    }

    public static class vote_id_type {
        int content;

        public vote_id_type(String strSerial) {
            int nIndex = strSerial.indexOf(':');
            if (nIndex == -1) {
                throw new RuntimeException("vote_id_type invalid serial");
            }
            int nType = Integer.valueOf(strSerial.substring(0, nIndex));
            int nInstance = Integer.valueOf(strSerial.substring(nIndex + 1));

            content = (nInstance << 8) | nType;
        }
    }

    public static class vote_id_type_deserializer implements JsonDeserializer<vote_id_type> {

        @Override
        public vote_id_type deserialize(JsonElement json,
                                        Type typeOfT,
                                        JsonDeserializationContext context) throws JsonParseException {
            String strSerial = json.getAsString();

            return new vote_id_type(strSerial);
        }
    }

    public static class account_options {
        public public_key_type memo_key;
        public String voting_account;
        public Integer num_witness;
        public Integer num_committee;
        public HashSet<vote_id_type> votes;
        // 未完成
        public HashSet<String> extensions;  // extension type

    }

    public static class public_key_type_deserializer implements JsonDeserializer<public_key_type> {

        @Override
        public public_key_type deserialize(JsonElement json,
                                           Type typeOfT,
                                           JsonDeserializationContext context) throws JsonParseException {
            String strPublicKey = json.getAsString();

            try {
                return new public_key_type(strPublicKey);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();

                throw new JsonParseException("pubic key is invalid.");
            }
        }
    }

    public static class public_type_serializer implements JsonSerializer<public_key_type> {
        @Override
        public JsonElement serialize(public_key_type src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

    class void_t {

    }
    public static class TestClass {
        public String id;
    }

    public static class TestClass2 {
        public object_id<account_object> id;
    }
}
