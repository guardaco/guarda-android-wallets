package com.bitshares.bitshareswallet.wallet;

import com.bitshares.bitshareswallet.wallet.graphene.chain.object_id;
import com.bitshares.bitshareswallet.wallet.graphene.chain.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class authority {
    private Integer weight_threshold;
    public HashMap<object_id<account_object>, Integer> account_auths = new HashMap<>();
    private HashMap<types.public_key_type, Integer> key_auths = new HashMap<>();
    private HashMap<address, Integer> address_auths = new HashMap<>();

    public authority(int nWeightThreshold, types.public_key_type publicKeyType, int nWeightType) {
        weight_threshold = nWeightThreshold;
        key_auths.put(publicKeyType, nWeightType);
    }

    public boolean is_public_key_type_exist(types.public_key_type publicKeyType) {
        return key_auths.containsKey(publicKeyType);
    }

    public List<types.public_key_type> get_keys() {
        List<types.public_key_type> listKeyType = new ArrayList<>();
        listKeyType.addAll(key_auths.keySet());
        return listKeyType;
    }
}
