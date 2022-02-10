package com.istar.mediabroken.utils

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.mashape.unirest.http.Unirest

class SubscriptionApiUtils {
    static def post(String url, String appId, String secret, def data) {
        Map resultMap = this.createSign(appId, secret, data)
        def unirest = Unirest.post(url)
                .field("appId", appId)
                .field("data", resultMap.data)
                .field("time", resultMap.time)
                .field("rand", resultMap.rand)
                .field("sign", resultMap.sign).asJson()
        def result = JSONObject.parseObject(unirest.body.object.toString())
        changeErrorMsg(result)
        return result
    }

    static def get(String url, String appId, String secret, def data) {
        Map resultMap = this.createSign(appId, secret, data)
        def unirest = Unirest.get(url).queryString("appId", appId)
                .queryString("appId", appId)
                .queryString("data", resultMap.data)
                .queryString("time", resultMap.time)
                .queryString("rand", resultMap.rand)
                .queryString("sign", resultMap.sign).asJson()
        def result = JSONObject.parseObject(unirest.body.object.toString())
        changeErrorMsg(result)
        return result
    }

    static def delete(String url, String appId, String secret, def data) {
        Map resultMap = this.createSign(appId, secret, data)
        def unirest = Unirest.delete(url)
                .queryString("appId", appId)
                .queryString("data", resultMap.data)
                .queryString("time", resultMap.time)
                .queryString("rand", resultMap.rand)
                .queryString("sign", resultMap.sign).asJson()
        def result = JSONObject.parseObject(unirest.body.object.toString())
        changeErrorMsg(result)
        return result
    }

    static def put(String url, String appId, String secret, def data) {
        Map resultMap = this.createSign(appId, secret, data)
        def unirest = Unirest.put(url)
                .queryString("appId", appId)
                .queryString("data", resultMap.data)
                .queryString("time", resultMap.time)
                .queryString("rand", resultMap.rand)
                .queryString("sign", resultMap.sign).asJson()
        def result = JSONObject.parseObject(unirest.body.object.toString())
        changeErrorMsg(result)
        return result
    }

    static Map createSign(String appId, String secret, def data) {
        String rand = UUID.randomUUID().toString()
        String time = new Date().getTime()
        data = URLEncoder.encode(JSON.toJSONString(data),"utf-8")
        String sign = Md5Util.md5(appId + secret + rand + time + data)
        Map result = [:]
        result.put("data", data)
        result.put("rand", rand)
        result.put("time", time)
        result.put("sign", sign)
        return result
    }
    private static void changeErrorMsg(JSONObject result) {
        if (result.get("errorMsg")) {
            result.put("msg", result.get("errorMsg"))
            //result.remove("errorMsg")
        }
    }
    public static void main(String[] args) {
        def data = [
                newsId: "1626cdd5e1965809ba0526bbd03b381b",
                subjectId : "cfe30001-44c7-463f-a439-3ecfdb20fcd9"
        ]
        def appId = "chinanews"
        def secret = "d9b3d7f3-09d9-4418-bbe5-c621176a6117"
        def result = this.get("/openapi/newsDetail", appId, secret, data)
        println("=====返回结果======"+result)
    }
}
