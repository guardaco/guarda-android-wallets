package com.bitshares.bitshareswallet.wallet.fc.io;

import java.nio.ByteBuffer;

public class datastream_encoder implements base_encoder {
    private ByteBuffer mByteBuffer;

    public datastream_encoder(int nSize) {
        mByteBuffer = ByteBuffer.allocate(nSize);
    }
    @Override
    public void write(byte[] data) {
        mByteBuffer.put(data);
    }

    @Override
    public void write(byte[] data, int off, int len) {
        mByteBuffer.put(data, off, len);
    }

    @Override
    public void write(byte data) {
        mByteBuffer.put(data);
    }

    public byte[] getData() {
        return mByteBuffer.array();
    }
}
