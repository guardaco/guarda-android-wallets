package com.bitshares.bitshareswallet.wallet.graphene.chain;

import android.util.Log;

import com.bitshares.bitshareswallet.wallet.fc.crypto.sha256_object;

import java.util.ArrayList;
import java.util.List;


public class signed_transaction extends transaction {
    List<compact_signature> signatures = new ArrayList<>();

    public void sign(types.private_key_type privateKeyType, sha256_object chain_id) {
        sha256_object digest = sig_digest(chain_id);
        signatures.add(privateKeyType.getPrivateKey().sign_compact(digest, true));
    }
}
