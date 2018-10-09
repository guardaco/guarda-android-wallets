package com.gravilink.decent.crypto;

import java.nio.ByteBuffer;

public interface EntropySource {
  ByteBuffer provideEntropy();
}