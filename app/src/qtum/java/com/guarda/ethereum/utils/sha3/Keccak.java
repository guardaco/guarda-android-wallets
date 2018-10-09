package com.guarda.ethereum.utils.sha3;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import static org.spongycastle.util.Arrays.reverse;

public class Keccak {

    private static final int DEFAULT_PERMUTATION_WIDTH = 1600;

    /**
     * max unsigned long
     */
    private static BigInteger BIT_64 = new BigInteger("18446744073709551615");

    /**
     * round constants RC[i]
     */
    private BigInteger[] RC = new BigInteger[]{
            new BigInteger("0000000000000001", 16),
            new BigInteger("0000000000008082", 16),
            new BigInteger("800000000000808A", 16),
            new BigInteger("8000000080008000", 16),
            new BigInteger("000000000000808B", 16),
            new BigInteger("0000000080000001", 16),
            new BigInteger("8000000080008081", 16),
            new BigInteger("8000000000008009", 16),
            new BigInteger("000000000000008A", 16),
            new BigInteger("0000000000000088", 16),
            new BigInteger("0000000080008009", 16),
            new BigInteger("000000008000000A", 16),
            new BigInteger("000000008000808B", 16),
            new BigInteger("800000000000008B", 16),
            new BigInteger("8000000000008089", 16),
            new BigInteger("8000000000008003", 16),
            new BigInteger("8000000000008002", 16),
            new BigInteger("8000000000000080", 16),
            new BigInteger("000000000000800A", 16),
            new BigInteger("800000008000000A", 16),
            new BigInteger("8000000080008081", 16),
            new BigInteger("8000000000008080", 16),
            new BigInteger("0000000080000001", 16),
            new BigInteger("8000000080008008", 16)
    };

    //	The rotation offsets r[x,y].
    private int[][] r = new int[][]{
            {0, 36, 3, 41, 18},
            {1, 44, 10, 45, 2},
            {62, 6, 43, 15, 61},
            {28, 55, 25, 21, 56},
            {27, 20, 39, 8, 14}
    };

    private int w;

    private int n;

    public Keccak() {
        initialize(DEFAULT_PERMUTATION_WIDTH);
    }

    /**
     * Constructor
     *
     * @param b {25, 50, 100, 200, 400, 800, 1600} sha-3 -> b = 1600
     */
    public Keccak(int b) {
        initialize(b);
    }

    public String getHash(String message, Parameters parameters) {
        //		Initialization and padding
        BigInteger[][] S = new BigInteger[5][5];

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                S[i][j] = new BigInteger("0", 16);
            }
        }

        BigInteger[][] P = padding(message, parameters);

        //	    Absorbing phase
        for (BigInteger[] Pi : P) {
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    if ((i + j * 5) < (parameters.getR() / w)) {
                        S[i][j] = S[i][j].xor(Pi[i + j * 5]);
                    }
                }
            }

            doKeccackf(S);
        }

        //	    Squeezing phase
        String Z = "";

        do {

            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    if ((5 * i + j) < (parameters.getR() / w)) {
                        Z = Z + addZero(Hex.toHexString(reverse(S[j][i].toByteArray())), 16).substring(0, 16);
                    }
                }
            }

            doKeccackf(S);
        } while (Z.length() < parameters.getOutputLength() * 2);

        return Z.substring(0, parameters.getOutputLength() * 2);
    }

    private BigInteger[][] doKeccackf(BigInteger[][] A) {
        for (int i = 0; i < n; i++) {
            A = roundB(A, RC[i]);
        }
        return A;
    }

    private BigInteger[][] roundB(BigInteger[][] A, BigInteger RC) {
        BigInteger[] C = new BigInteger[5];
        BigInteger[] D = new BigInteger[5];
        BigInteger[][] B = new BigInteger[5][5];

        //θ step
        for (int i = 0; i < 5; i++) {
            C[i] = A[i][0].xor(A[i][1]).xor(A[i][2]).xor(A[i][3]).xor(A[i][4]);
        }

        for (int i = 0; i < 5; i++) {
            D[i] = C[(i + 4) % 5].xor(rot(C[(i + 1) % 5], 1));
        }

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                A[i][j] = A[i][j].xor(D[i]);
            }
        }

        //ρ and π steps
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                B[j][(2 * i + 3 * j) % 5] = rot(A[i][j], r[i][j]);
            }
        }

        //χ step
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                A[i][j] = B[i][j].xor(B[(i + 1) % 5][j].not().and(B[(i + 2) % 5][j]));
            }
        }

        //ι step
        A[0][0] = A[0][0].xor(RC);

        return A;
    }

    private BigInteger rot(BigInteger x, int n) {
        n = n % w;

        BigInteger leftShift = getShiftLeft64(x, n);
        BigInteger rightShift = x.shiftRight(w - n);

        return leftShift.or(rightShift);
    }

    private BigInteger getShiftLeft64(BigInteger value, int shift) {
        BigInteger retValue = value.shiftLeft(shift);
        BigInteger tmpValue = value.shiftLeft(shift);

        if (retValue.compareTo(BIT_64) > 0) {
            for (int i = 64; i < 64 + shift; i++) {
                tmpValue = tmpValue.clearBit(i);
            }

            tmpValue = tmpValue.setBit(64 + shift);
            retValue = tmpValue.and(retValue);
        }
        return retValue;
    }

    private BigInteger[][] padding(String message, Parameters parameters) {
        int size;
        message = message + parameters.getD();

        while (((message.length() / 2) * 8 % parameters.getR()) != ((parameters.getR() - 8))) {
            message = message + "00";
        }

        message = message + "80";
        size = (((message.length() / 2) * 8) / parameters.getR());

        BigInteger[][] arrayM = new BigInteger[size][];
        arrayM[0] = new BigInteger[1600 / w];
        initArray(arrayM[0]);

        int count = 0;
        int j = 0;
        int i = 0;

        for (int _n = 0; _n < message.length(); _n++) {

            if (j > (parameters.getR() / w - 1)) {
                j = 0;
                i++;
                arrayM[i] = new BigInteger[1600 / w];
                initArray(arrayM[i]);
            }

            count++;

            if ((count * 4 % w) == 0) {
                String subString = message.substring((count - w / 4), (w / 4) + (count - w / 4));
                arrayM[i][j] = new BigInteger(subString, 16);
                String revertString = Hex.toHexString(reverse((arrayM[i][j].toByteArray())));
                revertString = addZero(revertString, subString.length());
                arrayM[i][j] = new BigInteger(revertString, 16);
                j++;
            }

        }
        return arrayM;
    }

    private String addZero(String str, int length) {
        String retStr = str;
        for (int i = 0; i < length - str.length(); i++) {
            retStr += "0";
        }
        return retStr;
    }

    private void initArray(BigInteger[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = new BigInteger("0", 16);
        }
    }

    private void initialize(int b) {
        w = b / 25;
        int l = (int) (Math.log(w) / Math.log(2));
        n = 12 + 2 * l;
    }
}
