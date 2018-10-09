package com.bitshares.bitshareswallet.wallet.graphene.chain;

import java.util.List;


public class global_property_object {
    public object_id id;

    //static const uint8_t space_id = implementation_ids;
    //static const uint8_t type_id  = impl_global_property_object_type;

    public chain_parameters parameters;
    public int             next_available_vote_id = 0;
    public List<object_id> active_committee_members;
    public List<object_id> active_witnesses;

    //vector<committee_member_id_type>   active_committee_members; // updated once per maintenance interval
    //flat_set<witness_id_type>          active_witnesses; // updated once per maintenance interval
}
