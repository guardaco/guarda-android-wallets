package com.guarda.ethereum.sapling.tree;

import java.util.LinkedList;

import static com.guarda.ethereum.sapling.tree.SaplingMerkleTree.SAPLING_INCREMENTAL_MERKLE_TREE_DEPTH;

public class PathFiller {
    private LinkedList<String> queue;
    private static EmptyMerkleRoots emptyroots = new EmptyMerkleRoots(SAPLING_INCREMENTAL_MERKLE_TREE_DEPTH);
//    private static EmptyMerkleRoots emptyroots = new EmptyMerkleRoots(INCREMENTAL_MERKLE_TREE_DEPTH_TESTING);

    public PathFiller() {
        queue = new LinkedList<>();
    }

    public PathFiller(LinkedList<String> queue) {
        this.queue = queue;
    }

    public String next(int depth) {
        if (queue.size() > 0) {
            return queue.pop();
        } else {
            return emptyroots.empty_root(depth);
        }
    }

    @Override
    public String toString() {
        return "PathFiller{" +
                "queue=" + queue +
                '}';
    }
}
