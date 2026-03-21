package com.ShortStuff.util;

public class Base62 {
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final int BASE = ALPHABET.length();

    public static String encode(long id) {
        StringBuilder sb = new StringBuilder();
        while(id > 0) {
            sb.append(ALPHABET.charAt((int)(id % BASE)));
            id /= BASE;
        }

        return sb.reverse().toString();
    }

    private Base62() {}

}
