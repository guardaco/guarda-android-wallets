package com.guarda.zcash.sapling.tree;

import com.guarda.zcash.ZCashException;
import com.guarda.zcash.crypto.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class SaplingMerkleTree {
    private String left;
    private String right;
    private List<String> parents;
    public static final int SAPLING_INCREMENTAL_MERKLE_TREE_DEPTH = 32;
    private static EmptyMerkleRoots emptyroots = new EmptyMerkleRoots(SAPLING_INCREMENTAL_MERKLE_TREE_DEPTH);
//    private static EmptyMerkleRoots emptyroots = new EmptyMerkleRoots(INCREMENTAL_MERKLE_TREE_DEPTH_TESTING);
    private int deserIndex = 0;

    public SaplingMerkleTree() {
        this.left = "";
        this.right = "";
        this.parents = new ArrayList<>();
    }

    private SaplingMerkleTree(SaplingMerkleTree from) {
        this.left = from.left;
        this.right = from.right;
        this.parents = new ArrayList<>(from.parents);
        try {
            wfcheck();
        } catch (ZCashException e) {
            Timber.e("serialize wfcheck() e=%s", e.getMessage());
        }
    }

    public SaplingMerkleTree(String serializedTree) {
        left = deserializeStep(serializedTree);
        right = deserializeStep(serializedTree);

        parents = new ArrayList<>();

        int size = Integer.parseInt(serializedTree.substring(deserIndex, deserIndex + 2), 16);
        if (size == 0) return;

        deserIndex += 2;

        for (int i = 0; i < size; i++)
            parents.add(i, deserializeStep(serializedTree));
    }

    private String deserializeStep(String serStr) {
        String res = "";
        if (serStr.substring(deserIndex, deserIndex + 2).equals("00")) {
            deserIndex += 2;
        } else {
            res = serStr.substring(deserIndex + 2, deserIndex + 2 + 64);
            deserIndex += 2 + 64;
        }
        return res;
    }

    public int size() {
        int ret = 0;
        if (!left.isEmpty()) {
            ret++;
        }
        if (!right.isEmpty()) {
            ret++;
        }

        for (int i = 0; i < parents.size(); i++) {
            if (parents.get(i) != null && !parents.get(i).isEmpty()) {
                ret += (1 << (i+1));
            }
        }
        return ret;
    }

    public void append(String hash) throws ZCashException {
//        hash = Utils.revHex(hash);
        if (isComplete(SAPLING_INCREMENTAL_MERKLE_TREE_DEPTH)) {
//        if (isComplete(INCREMENTAL_MERKLE_TREE_DEPTH_TESTING)) {
            throw new ZCashException("SaplingMerkleTree append() tree is completed");
        }

        if (left.isEmpty()) {
            left = hash;
        } else if (right.isEmpty()) {
            right = hash;
        } else {
            // Combine the leaves and propagate it up the tree
            PedersenHash ph = PedersenHash.combine(new PedersenHash(left), new PedersenHash(right), 0);
            // Set the "left" leaf to the object and make the "right" leaf none
            left = hash;
            right = "";

            for (int i = 0; i < SAPLING_INCREMENTAL_MERKLE_TREE_DEPTH; i++) {
//            for (int i = 0; i < INCREMENTAL_MERKLE_TREE_DEPTH_TESTING; i++) {
                if (i < parents.size()) {
                    if (parents.get(i) != null && !parents.get(i).isEmpty()) {
                        ph = PedersenHash.combine(new PedersenHash(parents.get(i)), ph, i + 1);
                        parents.set(i, "");
                    } else {
                        parents.set(i, ph.getHash());
                        break;
                    }
                } else {
                    parents.add(ph.getHash());
                    break;
                }
            }
        }

    }

    public IncrementalWitness witness() {
        return new IncrementalWitness(new SaplingMerkleTree(this));
    }


    public boolean isComplete(long depth) {
        if (left.isEmpty() || right.isEmpty()) {
            return false;
        }

        if (parents.size() != (depth - 1)) {
            return false;
        }

        for (String p : parents) {
            if (p == null || p.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public int nextDepth(Integer skip) {
        if (left.isEmpty()) {
            if (skip > 0) {
                skip--;
            } else {
                return 0;
            }
        }

        if (right.isEmpty()) {
            if (skip > 0) {
                skip--;
            } else {
                return 0;
            }
        }

        int d = 1;

        for (String parent : parents) {
            if (parent == null || parent.isEmpty()) {
                if (skip > 0) {
                    skip--;
                } else {
                    return d;
                }
            }

            d++;
        }

        return d + skip;
    }

    public String root() throws ZCashException {
        return root_inner(SAPLING_INCREMENTAL_MERKLE_TREE_DEPTH, new PathFiller());
//        return root_inner(INCREMENTAL_MERKLE_TREE_DEPTH_TESTING, new PathFiller());
    }

    //maybe the method is redundant
    public String root(int depth, PathFiller partialPath) throws ZCashException {
        return root_inner(depth, partialPath);
    }

    String root_inner(int depth, PathFiller filler) throws ZCashException {
        if (depth <= 0) throw new ZCashException("SaplingMerkleTree root_inner depth <= 0 =" + depth);
        Timber.d("root_inner filler=%s", filler);
        Timber.d("root_inner left=%s", left);
        Timber.d("root_inner right=%s", right);
        String combineLeft = !left.isEmpty() ? left : filler.next(0);
        String combineRight = !right.isEmpty() ? right : filler.next(0);

        PedersenHash root = PedersenHash.combine(new PedersenHash(combineLeft), new PedersenHash(combineRight), 0);
        Timber.d("root_inner 1root=%s", root.getHash());
        int d = 1;
        Timber.d("root_inner parents=%s", parents);
        for (String parent : parents) {
            if (!parent.isEmpty()) {
                root = PedersenHash.combine(new PedersenHash(parent), root, d);
            } else {
                root = PedersenHash.combine(root, new PedersenHash(filler.next(d)), d);
            }

            d++;
        }

        while (d < depth) {
            root = PedersenHash.combine(root, new PedersenHash(filler.next(d)), d);
            Timber.d("root_inner root=%s, d=%d", root.getHash(), d);
            d++;
        }

        return root.getHash();
    }

    public MerklePath path(PathFiller filler) throws ZCashException {
        if (left.isEmpty()) {
            throw new ZCashException("can't create an authentication path for the beginning of the tree");
        }

        List<String> path = new ArrayList<>();
        List<Boolean> index = new ArrayList<>();

        if (!right.isEmpty()) {
            index.add(true);
            path.add(left);
        } else {
            index.add(false);
            path.add(filler.next(0));
        }

        int d = 1;

        for (String parent : parents) {
            if (!parent.isEmpty()) {
                index.add(true);
                path.add(parent);
            } else {
                index.add(false);
                path.add(filler.next(d));
            }

            d++;
        }

        while (d < SAPLING_INCREMENTAL_MERKLE_TREE_DEPTH) {
//        while (d < INCREMENTAL_MERKLE_TREE_DEPTH_TESTING) {
            index.add(false);
            path.add(filler.next(d));
            d++;
        }

        //TODO: maybe byte[][]
        List<List<Boolean>> merkelPath = new ArrayList<>();
        for (String b : path) {
//            byte[] hashv = Utils.reverseByteArray(Utils.hexToBytes(b));
            byte[] hashv = Utils.hexToBytes(b);
            merkelPath.add(Utils.byteArray2BitArray(hashv));
        }

        Collections.reverse(merkelPath);
        Collections.reverse(index);

        return new MerklePath(merkelPath, index);
    }

    public String empty_root() {
        return emptyroots.empty_root(SAPLING_INCREMENTAL_MERKLE_TREE_DEPTH);
//        return emptyroots.empty_root(INCREMENTAL_MERKLE_TREE_DEPTH_TESTING);
    }

    public String last() throws ZCashException {
        if (!right.isEmpty()) {
            return right;
        } else if (!left.isEmpty()) {
            return left;
        } else {
            throw new ZCashException("last() - tree has no cursor");
        }
    }

    @Override
    public String toString() {
        return "SaplingMerkleTree{" +
                "left='" + left + '\'' +
                ", right='" + right + '\'' +
                ", parents=" + parents +
                '}';
    }

    public String serialize() {
        try {
            wfcheck();
        } catch (ZCashException e) {
            Timber.e("serialize wfcheck() e=%s", e.getMessage());
        }

        //serialize to hex like in original code
        //when left or right is empty - add 00
        String res = "";
        res += left.isEmpty() ? "00" : "01" + left;
        res += right.isEmpty() ? "00" : "01" + right;
        //before parents - add size of the array in hex
        //before every parent - add 01
        //when parent is empty - add 00
        if (parents.isEmpty()) {
            res += "00";
        } else {
            String strSize = String.format("%x", parents.size());
            res += strSize.length() == 1 ? "0" + strSize : strSize;
            for (String p : parents) {
                res += p.isEmpty() ? "00" : "01" + p;
            }
        }
        return res;
    }

    private void wfcheck() throws ZCashException {
        if (parents.size() >= SAPLING_INCREMENTAL_MERKLE_TREE_DEPTH) throw new ZCashException("tree has too many parents");
        if (!parents.isEmpty() && parents.get(parents.size()-1).isEmpty()) throw new ZCashException("tree has non-canonical representation of parent");
        if (left.isEmpty() && !right.isEmpty()) throw new ZCashException("tree has non-canonical representation; right should not exist");
        if (left.isEmpty() && parents.size() > 0) throw new ZCashException("tree has non-canonical representation; parents should not be unempty");
    }
}
