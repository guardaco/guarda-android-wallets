package com.gravilink.zcash.crypto;

import java.nio.ByteBuffer;

public interface EntropySource {
  ByteBuffer provideEntropy();
}