package com.projects.zonetwyn.carla.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptoUtils {

    private static final String SHA = "SHA-256";

    public static String hashWithSHA256(String data) {
        String hashedData = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(SHA);
            byte[] encodedData = messageDigest.digest(data.getBytes(StandardCharsets.UTF_8));
            hashedData = bytesToHex(encodedData);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hashedData;
    }

    private static String bytesToHex(byte[] bytes) {
        String hex = null;
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String part = Integer.toHexString(0xff & bytes[i]);
            if (part.length() == 1) {
                hexString.append('0');
            }
            hexString.append(part);
        }
        hex = hexString.toString();

        return hex;
    }
}
