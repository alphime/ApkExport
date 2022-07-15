package com.alphi.myapplication.Utils;
/*
  IDEA 2022/03/11
 */

import android.content.pm.Signature;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {

    public static String getFileMD5(String filePath){
        try (FileChannel channel = new FileInputStream(filePath).getChannel()) {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            MappedByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            md5.update(byteBuffer);
            byte[] digest = md5.digest();
            BigInteger bigInteger = new BigInteger(1, digest);
            String result = bigInteger.toString(16);
            if (result.length() < 16){
                result = "0" + result;
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSignaturesMD5(Signature signature) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(signature.toByteArray());
            byte[] digest = md5.digest();
            return new BigInteger(1, digest).toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
