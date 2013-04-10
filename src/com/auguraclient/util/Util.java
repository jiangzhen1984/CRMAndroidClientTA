
package com.auguraclient.util;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {

    private static MessageDigest digest = null;

    public static String encryptMD5(String sum) {
        if (sum == null || sum.isEmpty()) {
            Log.w("AUGURACLIENT", "can't encrype empty string ");
            return null;
        }
        if (digest == null) {
            try {
                digest = java.security.MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                Log.e("AUGURACLIENT", "can't get md5 instance  ", e);
                throw new RuntimeException(" can't get md5 instance ");
            }
        }
        digest.update(sum.getBytes());
        byte messageDigest[] = digest.digest();

        // Create Hex String
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < messageDigest.length; i++) {
            String h = Integer.toHexString(0xFF & messageDigest[i]);
            while (h.length() < 2)
                h = "0" + h;
            hexString.append(h);
        }
        return hexString.toString();
    }



    public static String getLoginUrl() {
        return Constants.API_TABLE[Constants.LOGIN_URL_INDEX];
    }

}
