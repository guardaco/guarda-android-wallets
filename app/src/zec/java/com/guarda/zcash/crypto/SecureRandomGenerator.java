package com.guarda.zcash.crypto;

import java.security.SecureRandom;

public class SecureRandomGenerator {

  public static SecureRandom getSecureRandom(){
    SecureRandomStrengthener randomStrengthener = SecureRandomStrengthener.getInstance();
    return randomStrengthener.generateAndSeedRandomNumberGenerator();
  }
}