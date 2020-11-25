package com.guarda.ethereum.crypto;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.SecureRandom;
import java.util.ArrayList;

public class BrainKeyDict {
  public static final int DICT_WORD_COUNT = 49744;
  public static final int BRAINKEY_WORD_COUNT = 12;
  private static String words;

  public static void init(AssetManager assetManager) throws IOException {
    InputStream stream = null;
    stream = assetManager.open("brainkeydict.txt");

    final int bufferSize = 1024;
    final char[] buffer = new char[bufferSize];
    final StringBuilder out = new StringBuilder();
    Reader in = new InputStreamReader(stream, "UTF-8");
      for (; ; ) {
        int rsz = in.read(buffer, 0, buffer.length);
        if (rsz < 0)
          break;
        out.append(buffer, 0, rsz);
      }

      words = out.toString();
  }

  public static void initFromContext(Context context) throws IOException {
    init(context.getAssets());
  }

  @NonNull
  public static String suggestBrainKey() {
    String[] wordArray = BrainKeyDict.words.split(",");
    ArrayList<String> suggestedBrainKey = new ArrayList<String>();
    assert (wordArray.length == DICT_WORD_COUNT);
    SecureRandom secureRandom = SecureRandomGenerator.getSecureRandom();
    int index;
    for (int i = 0; i < BRAINKEY_WORD_COUNT; i++) {
      index = secureRandom.nextInt(DICT_WORD_COUNT - 1);
      suggestedBrainKey.add(wordArray[index].toUpperCase());
    }
    StringBuilder stringBuilder = new StringBuilder();
    for (String word : suggestedBrainKey) {
      stringBuilder.append(word);
      stringBuilder.append(" ");
    }

    return stringBuilder.toString().trim();
  }

}
