package com.bitshares.bitshareswallet.wallet.faucet;

import com.bitshares.bitshareswallet.wallet.graphene.chain.types;

import java.util.List;
import java.util.Map;

/**
 * Created by lorne on 20/09/2017.
 */

public class create_account_object {
    public static class response_error {
        public List<String> base;
    }

    public static class response_fail_error {
        public Map<String, List<String>> error;
    }

    public static class create_account_response {
        public Object account;
        public response_error error;
    };

    public String name;
    public types.public_key_type owner_key;
    public types.public_key_type active_key;
    public types.public_key_type memo_key;
    public String refcode;
    public String referrer;
}
