package com.istar.mediabroken.utils;

import com.alibaba.fastjson.JSONObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.util.Map;

/**
 * @author zxj
 * @create 2018/11/20
 */
public class PmsApiUtils {

    public static Map<String, Object> get(String url, String appKey, String appSecret, Map<String, Object> parameters) {
        return getMethod(url, appKey, appSecret, parameters);
    }

    public static Map<String, Object> postBody(String url, String appKey, String appSecret, Map parameters) {
        return postBodyMethod(url, appKey, appSecret, parameters);
    }

    /**
     *
     * @param url 访问路径 域名+接口
     * @param appKey
     * @param appSecret
     * @param parameters
     * @return
     */
    public static Map<String, Object> getMethod(String url, String appKey, String appSecret, Map<String, Object> parameters) {
        long timestamp = System.currentTimeMillis();
        String str = appKey + appSecret + timestamp;
        String sign = Md5Util.md5(str);
        HttpResponse<JsonNode> response = null;
        JSONObject object = null;
        try {
            response = Unirest.get(url).queryString("appKey", appKey).
                    queryString("sign", sign)
                    .queryString("timestamp", timestamp)
                    .queryString(parameters).asJson();
            if (response.getStatus() == 200){
                object = JSONObject.parseObject(response.getBody().toString());
            }
        } catch (UnirestException e) {
            System.out.println("访问api异常::" + e);
            e.printStackTrace();
        }
        return object;
    }

    /**
     * @param url        访问路径 域名+接口
     * @param appKey
     * @param appSecret
     * @param parameters
     * @return
     */
    public static Map<String, Object> postBodyMethod(String url, String appKey, String appSecret, Map parameters) {
        long timestamp = System.currentTimeMillis();
        String str = appKey + appSecret + timestamp;
        String sign = Md5Util.md5(str);
        HttpResponse response = null;
        JSONObject object = null;
        String string = JSONObject.toJSONString(parameters);
        try {
            response = Unirest.post(url)
                    .queryString("appKey", appKey)
                    .queryString("sign", sign)
                    .queryString("timestamp", timestamp)
                    .body(string).asJson();
            if (response.getStatus() == 200) {
                object = JSONObject.parseObject(response.getBody().toString());
            }
        } catch (UnirestException e) {
            System.out.println("访问api异常::" + e);
            e.printStackTrace();
        }
        return object;
    }
}
