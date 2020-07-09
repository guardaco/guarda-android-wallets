package com.guarda.zcash.crypto;

import org.junit.Test;

import java.util.Arrays;

import static com.guarda.zcash.crypto.Utils.bytesToHex;

public class Base58Test {

    @Test
    public void decodeChecked() {
        String tOne = "t1Lb7pwVomWgXjfJuAGZMaouifcWymXs6Du";
        String tOne1 = "t1Rxy4hTQXpfSwqkA1CoFd6zz8G3mnRa2Xx";
        String tOne2 = "t1gynHkGVxofEmfZk7ywxM7w36TcqoHyWwP";
        String tThree = "t3MEXDF9Wsi63KwpPuQdD6by32Mw2bNTbEa";
        String tThree1 = "t3JcMe1E5UkFsUtVb7k17eJwXX5FYUewMBy";
        String tThree2 = "t3WGhZh3QV9Wgj9Xb6MrrhLZhpdyVa8QqLG";

        byte[] tOneBytes = Base58.decodeChecked(tOne);
        byte[] tOneBytes1 = Base58.decodeChecked(tOne1);
        byte[] tOneBytes2 = Base58.decodeChecked(tOne2);
        byte[] tThreeBytes = Base58.decodeChecked(tThree);
        byte[] tThreeBytes1 = Base58.decodeChecked(tThree1);
        byte[] tThreeBytes2 = Base58.decodeChecked(tThree2);

        System.out.println("tOneBytes=" + Arrays.toString(tOneBytes));
        System.out.println("tOneBytes1=" + Arrays.toString(tOneBytes1));
        System.out.println("tOneBytes2=" + Arrays.toString(tOneBytes2));
        System.out.println("tThreeBytes=" + Arrays.toString(tThreeBytes));
        System.out.println("tThreeBytes1=" + Arrays.toString(tThreeBytes1));
        System.out.println("tThreeBytes2=" + Arrays.toString(tThreeBytes2));

        System.out.println("byte=" + bytesToHex(new byte[] { (byte) 28, (byte) -72 }));
        System.out.println("byte=" + bytesToHex(new byte[] { (byte) 28, (byte) -67 }));
    }
}