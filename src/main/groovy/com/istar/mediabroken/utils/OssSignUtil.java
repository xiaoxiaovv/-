package com.istar.mediabroken.utils;

import org.apache.commons.codec.binary.Base64;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.crypto.dsig.SignatureMethod;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @description: aliyunOssApi请求工具类
 * @author: hexushuai
 * @date: 2019/1/22 15:30
 */
public class OssSignUtil {

    private static final String ENCODE_TYPE = "UTF-8";
    private static final String ALGORITHM = "HmacSHA1";
    private static final String HTTP_METHOD = "GET";
    private static final String SEPARATOR = "&";
    private static final String EQUAL = "=";
    private static final String ISO8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public static String sign(String keySecret, Map<String, String> parameterMap)
            throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
        String normalQueryString = buildNormalizedQueryString(parameterMap);
        String stringToSign = buildStringToSign(normalQueryString);
        String signature = buildSignature(keySecret, stringToSign);
        String requestURL = buildRequestURL(signature, parameterMap);
        return requestURL;
    }

    /**
     * 获得阿里云接口规范化的请求参数
     */
    private static String buildNormalizedQueryString(Map<String, String> parameterMap) throws UnsupportedEncodingException {
        // 对参数进行排序
        List<String> sortedKeys = new ArrayList<String>(parameterMap.keySet());
        Collections.sort(sortedKeys);

        StringBuilder temp = new StringBuilder();
        for (String key : sortedKeys) {
            // 此处需要对key和value进行编码
            String value = parameterMap.get(key);
            temp.append(SEPARATOR).append(percentEncode(key)).append(EQUAL).append(percentEncode(value));
        }
        return temp.toString().substring(1);
    }

    /**
     * 生成加密参数
     */
    private static String buildStringToSign(String normalQueryString) throws UnsupportedEncodingException {
        // 生成stringToSign字符
        StringBuilder temp = new StringBuilder();
        temp.append(HTTP_METHOD).append(SEPARATOR);
        temp.append(percentEncode("/")).append(SEPARATOR);
        temp.append(percentEncode(normalQueryString));
        return temp.toString();
    }

    /**
     * 利用HmacSHA1算法进行加密
     */
    private static String buildSignature(String keySecret, String stringToSign) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
        SecretKey key = new SecretKeySpec((keySecret + SEPARATOR).getBytes(ENCODE_TYPE), SignatureMethod.HMAC_SHA1);
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(key);
        byte[] hashBytes = mac.doFinal(stringToSign.toString().getBytes(ENCODE_TYPE));
        byte[] base64Bytes = new Base64().encode(hashBytes);
        String base64UTF8String = new String(base64Bytes, "utf-8");
        return URLEncoder.encode(base64UTF8String, ENCODE_TYPE);
    }

    /**
     * 生成最终的请求url
     */
    private static String buildRequestURL(String signature, Map<String, String> parameterMap) throws UnsupportedEncodingException {
        // 生成请求URL
        StringBuilder temp = new StringBuilder("http://mts.cn-beijing.aliyuncs.com/?");
        temp.append(URLEncoder.encode("Signature", ENCODE_TYPE)).append("=").append(signature);
        for (Map.Entry<String, String> e : parameterMap.entrySet()) {
            temp.append("&").append(percentEncode(e.getKey())).append("=").append(percentEncode(e.getValue()));
        }
        return temp.toString();
    }

    /**
     * 字符转码
     */
    private static String percentEncode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, ENCODE_TYPE).replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
    }

    /**
     * 获取当前国际时间
     */
    public static String formatIso8601Date(Date date) {
        SimpleDateFormat df = new SimpleDateFormat(ISO8601_DATE_FORMAT);
        df.setTimeZone(new SimpleTimeZone(0, "GMT"));
        return df.format(date);
    }
}
