package com.guarda.ethereum.crypto;

import java.nio.ByteBuffer;

public interface EntropySource {
  ByteBuffer provideEntropy();
}