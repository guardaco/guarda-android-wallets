package com.bitshares.bitshareswallet.wallet.graphene.chain;

import com.bitshares.bitshareswallet.wallet.fc.crypto.ripemd160_object;

import java.math.BigInteger;
import java.util.Date;

public class dynamic_global_property_object {
    //static const uint8_t space_id = implementation_ids;
    //static const uint8_t type_id  = impl_dynamic_global_property_object_type;
    object_id id;
    int       head_block_number = 0;
    public ripemd160_object head_block_id;         //block_id_type     head_block_id;
    public Date time;                  //time_point_sec    time;
    object_id current_witness;       // witness_id_type   current_witness;
    String    next_maintenance_time; // time_point_sec    next_maintenance_time;
    String    last_budget_time;      // time_point_sec    last_budget_time;
    long      witness_budget;
    int       accounts_registered_this_interval = 0;
    /**
     *  Every time a block is missed this increases by
     *  RECENTLY_MISSED_COUNT_INCREMENT,
     *  every time a block is found it decreases by
     *  RECENTLY_MISSED_COUNT_DECREMENT.  It is
     *  never less than 0.
     *
     *  If the recently_missed_count hits 2*UNDO_HISTORY then no new blocks may be pushed.
     */
    int          recently_missed_count = 0;

    /**
     * The current absolute slot number.  Equal to the total
     * number of slots since genesis.  Also equal to the total
     * number of missed slots plus head_block_number.
     */
    long         current_aslot = 0;

    /**
     * used to compute witness participation.
     */

    BigInteger recent_slots_filled;

    /**
     * dynamic_flags specifies chain state properties that can be
     * expressed in one bit.
     */
    int dynamic_flags = 0;

    int last_irreversible_block_num = 0;

    enum dynamic_flag_bits
    {
        /**
         * If maintenance_flag is set, then the head block is a
         * maintenance block.  This means
         * get_time_slot(1) - head_block_time() will have a gap
         * due to maintenance duration.
         *
         * This flag answers the question, "Was maintenance
         * performed in the last call to apply_block()?"
         */
        //maintenance_flag = 0x01
    };
}
