package com.guarda.zcash.crypto;

import java.nio.ByteBuffer;

public interface EntropySource {
  ByteBuffer provideEntropy();
}