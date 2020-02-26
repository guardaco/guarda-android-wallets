package com.guarda.zcash.sapling.tree;

import com.guarda.zcash.crypto.Utils;

import java.util.Arrays;
import java.util.List;

import timber.log.Timber;
import work.samosudov.rustlib.RustAPI;

public class MerklePath {

    private List<List<Boolean>> authenticationPath;
    private List<Boolean> index;

    public MerklePath(List<List<Boolean>> authenticationPath, List<Boolean> index) {
        this.authenticationPath = authenticationPath;
        this.index = index;
    }

    public String serialize() {
        boolean[][] auPathArr = new boolean[authenticationPath.size()][];
        for (int i = 0; i < authenticationPath.size(); i++) {
            auPathArr[i] = new boolean[authenticationPath.get(i).size()];
            for (int j = 0; j < authenticationPath.get(i).size(); j++) {
                auPathArr[i][j] = authenticationPath.get(i).get(j);
            }
        }

        boolean[] inArr = new boolean[index.size()];
        for (int i = 0; i < index.size(); i++) {
            inArr[i] = index.get(i);
        }

        byte[] res = RustAPI.vectorToInt(auPathArr, inArr);

        Timber.d("toByte() vectorToInt=%s", Arrays.toString(res));
        Timber.d("toByte() vectorToInt=%s", Utils.bytesToHex(res));

        return Utils.bytesToHex(res);
    }

    public boolean[][] getAuthPathPrimitive() {
        boolean[][] auPathArr = new boolean[authenticationPath.size()][];
        for (int i = 0; i < authenticationPath.size(); i++) {
            auPathArr[i] = new boolean[authenticationPath.get(i).size()];
            for (int j = 0; j < authenticationPath.get(i).size(); j++) {
                auPathArr[i][j] = authenticationPath.get(i).get(j);
            }
        }
        return auPathArr;
    }

    public boolean[] getIndexPrimitive() {
        boolean[] inArr = new boolean[index.size()];
        for (int i = 0; i < index.size(); i++) {
            inArr[i] = index.get(i);
        }
        return inArr;
    }

    @Override
    public String toString() {
        return "MerklePath{" +
                "authenticationPath=" + authenticationPath +
                ", index=" + index +
                '}';
    }
}
