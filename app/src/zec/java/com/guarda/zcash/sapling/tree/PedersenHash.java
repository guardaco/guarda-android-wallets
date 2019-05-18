package com.guarda.zcash.sapling.tree;

import com.guarda.zcash.RustAPI;

import static com.guarda.zcash.crypto.Utils.revHex;

public class PedersenHash {
    private String hash;

    public PedersenHash(String hash) {
        this.hash = hash;
    }

    public static PedersenHash empty() {
        return new PedersenHash("");
    }

    public static PedersenHash combine(PedersenHash l, PedersenHash r, int depth) {
        //for after converting the hash to bytes you should do Utils.reverseByteArray()
        String mHash = RustAPI.merkelHash(depth, revHex(l.getHash()), revHex(r.getHash()));
        return new PedersenHash(revHex(mHash));
    }

    public static PedersenHash uncommitted() {
        //should be reversed after converting the hash to byte array
        String ph = RustAPI.uncommitted();
        return new PedersenHash(revHex(ph));
    }

    public String getHash() {
        return hash;
    }
}
