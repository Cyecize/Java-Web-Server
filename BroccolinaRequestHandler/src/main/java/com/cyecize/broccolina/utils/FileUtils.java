package com.cyecize.broccolina.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {

    public static boolean filesMatch(InputStream firstFile, InputStream secondFile) {
        try {
            String md50 = streamMD5(firstFile);
            String md51 = streamMD5(secondFile);
            if (md50 == null || md51 == null) return false;
            return md50.equals(md51);
        } catch (Exception e) {
            return false;
        }
    }

    public static String byteArrayToHex(byte[] byteArray) {
        StringBuilder hex = new StringBuilder();
        for (byte aByteArray : byteArray) {
            String stmp = (Integer.toHexString(aByteArray & 0XFF));
            if (stmp.length() == 1)
                hex.append('0');
            hex.append(stmp);
        }
        return hex.toString();
    }

    public static String streamMD5(InputStream is) throws IOException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            try (DigestInputStream digestInputStream = new DigestInputStream(is, messageDigest)) {
                byte[] buffer = new byte[4 * 1024];
                while (digestInputStream.read(buffer) > 0) ;
                messageDigest = digestInputStream.getMessageDigest();
                byte[] resultByteArray = messageDigest.digest();
                return byteArrayToHex(resultByteArray);
            }
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
