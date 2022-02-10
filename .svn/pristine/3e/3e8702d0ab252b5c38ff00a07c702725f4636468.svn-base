package com.istar.mediabroken.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Util {
    private static ThreadLocal<MessageDigest> messageDigestHolder = new ThreadLocal();
    private static final char[] hexDigits =
            { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    public static final int HTTP_RETRIES = 3;

    static {
        try {
            MessageDigest message = MessageDigest.getInstance("MD5");
            messageDigestHolder.set(message);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String md5(String data)
    {
        try
        {
            MessageDigest message = (MessageDigest)messageDigestHolder.get();
            if (message == null) {
                message = MessageDigest.getInstance("MD5");
                messageDigestHolder.set(message);
            }

            message.update(data.getBytes("utf-8"));
            byte[] b = message.digest();

            String digestHexStr = "";
            for (int i = 0; i < 16; i++) {
                digestHexStr = digestHexStr + byte2hexstr(b[i]);
            }

            return digestHexStr;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String byte2hexstr(byte ib) {
        char[] ob = new char[2];
        ob[0] = hexDigits[(ib >>> 4 & 0xF)];
        ob[1] = hexDigits[(ib & 0xF)];

        return new String(ob);
    }

    //新增方法，待验证
    public static String getStreamMD5(FileInputStream fis) {
        BigInteger bi = null;
        try {
            byte[] buffer = new byte[8192];
            int len = 0;
            MessageDigest md = MessageDigest.getInstance("MD5");
            while ((len = fis.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            fis.close();
            byte[] b = md.digest();
            bi = new BigInteger(1, b);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bi.toString(16);
    }

    //新增方法，待验证
    public static String getMd5ByFile(File file) throws FileNotFoundException {
        String value = null;
        FileInputStream in = new FileInputStream(file);
        try {
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            BigInteger bi = new BigInteger(1, md5.digest());
            value = bi.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }

}
