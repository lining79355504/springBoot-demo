package com.demo.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public class TokenUtils {

    public static String generateHashToken(int length) throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstanceStrong();
        byte[] salt = new byte[16]; // 使用 Salt 增加安全性
        random.nextBytes(salt);

        String dataToHash = System.currentTimeMillis() + "-" + Base64.getEncoder().encodeToString(salt); // 种子信息：时间戳 + Salt

        MessageDigest digest = MessageDigest.getInstance("SHA-256"); // 使用 SHA-256 哈希算法
        byte[] hashBytes = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0'); // 补 0，保证两位十六进制
            }
            hexString.append(hex);
        }

        return hexString.substring(0, length); // 截取指定长度
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        int tokenLength = 32;
        String token = generateHashToken(tokenLength);
        System.out.println("哈希 Token (SHA-256, 固定长度 " + tokenLength + " 字符): " + token);
        UUID uuid = UUID.randomUUID();
        System.out.println("UUID: " + uuid.toString());
    }

}
