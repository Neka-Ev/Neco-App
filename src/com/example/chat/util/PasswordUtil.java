package com.example.chat.util;

public class PasswordUtil {
    public static String hash(String plain) {
        return Integer.toHexString(plain.hashCode());
    }
    public static boolean verify(String plain, String hash) {
        return hash(plain).equals(hash);
    }
}
