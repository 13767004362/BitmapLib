package com.xingen.bitmaplib.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Author by ${xinGen},  Date on 2018/5/28.
 *
 *
 */
public class MD5Utils {
    //md5加密路径，生成新的缓存需要用的key
    public static String hashImageUrlForDisk(String imageUrl) {
        String cacheImageUrl;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(imageUrl.getBytes());
            cacheImageUrl = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheImageUrl = String.valueOf(imageUrl.hashCode());
        }
        return cacheImageUrl;
    }
    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
