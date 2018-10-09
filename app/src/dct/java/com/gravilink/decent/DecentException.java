package com.gravilink.decent;

import java.util.Locale;

public class DecentException extends Exception {

  public DecentException(String error) {
    super(String.format(Locale.ENGLISH, "Response error: %s", error.replace("\\n", "\n").replace("\\\"", "\"")));
  }
}
