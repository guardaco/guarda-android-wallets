package com.guarda.ethereum.utils.sha3;

public enum Parameters {

    KECCAK_224(1152, 28, "01"),
    KECCAK_256(1088, 32, "01"),
    KECCAK_384(832, 48, "01"),
    KECCAK_512(576, 64, "01"),
    SHA3_224(1152, 28, "06"),
    SHA3_256(1088, 32, "06"),
    SHA3_384(832, 48, "06"),
    SHA3_512(576, 64, "06"),
    SHAKE128(1344, 32, "1F"),
    SHAKE256(1088, 64, "1F");

    private final int r;

    private final int outputLength;

    private final String d;

    Parameters(int r, int outputLength, String d) {
        this.r = r;
        this.outputLength = outputLength;
        this.d = d;
    }

    public int getR() {
        return r;
    }

    public int getOutputLength() {
        return outputLength;
    }

    public String getD() {
        return d;
    }
}
