package com.istar.mediabroken.utils

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import groovy.util.logging.Slf4j
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.NameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.protocol.HTTP
import org.apache.http.util.EntityUtils
import weibo4j.org.json.JSONArray

@Slf4j
class HttpHelper {
    static def doGet(String url, Map params) {
        String result = ""
        try {
            HttpClient httpClient = new DefaultHttpClient();
            List<NameValuePair> postPara = new ArrayList<NameValuePair>();
            params.each { key, value ->
                if (value == null) {
                    value = ''
                }
                postPara.add(new BasicNameValuePair(key as String, value as String));
            }
            def paramStr = EntityUtils.toString(new UrlEncodedFormEntity(postPara, HTTP.UTF_8));
            HttpGet request = new HttpGet(url + (paramStr ? '?' + paramStr : ""))
            log.debug("request: {}", request)
            HttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = EntityUtils.toString(response.getEntity(), "utf-8");
            }

        } catch (Throwable e) {
            log.error("unexpected error", e)
        }
        def json = result ? JSON.parseObject(result) : new JSONObject()
//        log.debug("response json: {}", json)
        return json
    }

    static List doGetArray(String url, Map params) {
        String result = "";
        try {
            HttpClient httpClient = new DefaultHttpClient();
            List<NameValuePair> postPara = new ArrayList<NameValuePair>();
            params.each { key, value ->
                postPara.add(new BasicNameValuePair(key as String, value as String));
            }
            def paramStr = EntityUtils.toString(new UrlEncodedFormEntity(postPara, HTTP.UTF_8));
            HttpGet request = new HttpGet(url + (paramStr ? '?' + paramStr : ""))
            log.debug("request: {}", request)
            HttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = EntityUtils.toString(response.getEntity(), "utf-8");
            }
        } catch (Throwable e) {
            log.error("unexpected error", e)
        }
        def json = result ? JSON.parseArray(result) : new JSONArray()
        log.debug("response json: {}", json)
        return json
    }
}
