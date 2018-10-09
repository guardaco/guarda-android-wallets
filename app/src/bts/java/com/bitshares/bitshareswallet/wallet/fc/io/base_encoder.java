package com.bitshares.bitshareswallet.wallet.fc.io;


public interface base_encoder {
    void write(byte[] data);
    void write(byte[] data, int off, int len);
    void write(byte data);
}
