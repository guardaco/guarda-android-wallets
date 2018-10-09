package com.bitshares.bitshareswallet.wallet.graphene.chain;


import com.bitshares.bitshareswallet.wallet.account_object;
import com.bitshares.bitshareswallet.wallet.authority;
import com.bitshares.bitshareswallet.wallet.common.UnsignedShort;
import com.bitshares.bitshareswallet.wallet.fc.bitutil;
import com.bitshares.bitshareswallet.wallet.fc.crypto.ripemd160_object;
import com.bitshares.bitshareswallet.wallet.fc.crypto.sha256_object;
import com.bitshares.bitshareswallet.wallet.fc.io.raw_type;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLongs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.bitshares.bitshareswallet.wallet.graphene.chain.operations.*;


public class transaction {
    public class required_authorities {
        public List<object_id<account_object>> active;
        public List<object_id<account_object>> owner;
        public List<authority> other;
    }

    /**
     * Least significant 16 bits from the reference block number. If @ref relative_expiration is zero, this field
     * must be zero as well.
     */
    public UnsignedShort ref_block_num    = UnsignedShort.ZERO;
    /**
     * The first non-block-number 32-bits of the reference block ID. Recall that block IDs have 32 bits of block
     * number followed by the actual block hash, so this field should be set using the second 32 bits in the
     * @ref block_id_type
     */
    public UnsignedInteger ref_block_prefix = UnsignedInteger.ZERO;

    /**
     * This field specifies the absolute expiration for this transaction.
     */
    public Date expiration;
    public List<operations.operation_type> operations;
    public Set<types.void_t> extensions;

    public ripemd160_object id() {
        return null;
    }

    public void set_reference_block(ripemd160_object reference_block) {
        ref_block_num = new UnsignedShort((short)bitutil.endian_reverse_u32(reference_block.hash[0]));

        //ref_block_prefix = new UnsignedInteger(reference_block.hash[1]);
        ref_block_prefix = UnsignedInteger.fromIntBits(reference_block.hash[1]);
    }

    public void set_expiration(Date expiration_time) {
        expiration = expiration_time;
    }

    public required_authorities get_required_authorities() {
        required_authorities requiredAuthorities = new required_authorities();
        requiredAuthorities.active = new ArrayList<>();
        requiredAuthorities.owner = new ArrayList<>();
        requiredAuthorities.other = new ArrayList<>();

        for (operation_type operationType : operations) {
            base_operation baseOperation = (base_operation) operationType.operationContent;
            requiredAuthorities.active.addAll(baseOperation.get_required_active_authorities());
            requiredAuthorities.owner.addAll(baseOperation.get_required_owner_authorities());
            requiredAuthorities.other.addAll(baseOperation.get_required_authorities());
        }

        return requiredAuthorities;
    }

    public sha256_object sig_digest(sha256_object chain_id) {
        // // TODO: 07/09/2017 这里还未处理
        sha256_object.encoder enc = new sha256_object.encoder();

        enc.write(chain_id.hash, 0, chain_id.hash.length);
        raw_type rawTypeObject = new raw_type();
        enc.write(rawTypeObject.get_byte_array(ref_block_num.shortValue()));
        enc.write(rawTypeObject.get_byte_array(ref_block_prefix.intValue()));
        enc.write(rawTypeObject.get_byte_array(expiration));

        //enc.write(rawTypeObject.get_byte_array(operations.size()));
        rawTypeObject.pack(enc, UnsignedInteger.fromIntBits(operations.size()));
        for (operation_type operationType : operations) {
            //enc.write(rawTypeObject.get_byte_array(operationType.nOperationType));
            rawTypeObject.pack(enc, UnsignedInteger.fromIntBits(operationType.nOperationType));
            base_operation baseOperation = (base_operation) operationType.operationContent;
            baseOperation.write_to_encoder(enc);
        }
        //enc.write(rawTypeObject.get_byte_array(extensions.size()));
        rawTypeObject.pack(enc, UnsignedInteger.fromIntBits(extensions.size()));



        return enc.result();
    }
}
