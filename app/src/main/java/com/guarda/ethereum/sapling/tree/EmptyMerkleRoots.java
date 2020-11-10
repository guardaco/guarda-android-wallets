package com.guarda.ethereum.sapling.tree;

import java.util.ArrayList;
import java.util.List;


public class EmptyMerkleRoots {
    //empty_roots should have size depth + 1;
    //so in for cycle inside constructor we have "d <= depth"
    private List<String> empty_roots = new ArrayList<>();

    public EmptyMerkleRoots(int depth) {
        empty_roots.add(0, PedersenHash.uncommitted().getHash());
        for (int d = 1; d <= depth; d++) {
            PedersenHash ph = PedersenHash.combine(
                    new PedersenHash(empty_roots.get(d - 1)),
                    new PedersenHash(empty_roots.get(d - 1)),
                    d - 1);
            String h = ph.getHash();
            empty_roots.add(d, h);
        }
    }

    public String empty_root(int depth) {
        return empty_roots.get(depth);
    }
}
