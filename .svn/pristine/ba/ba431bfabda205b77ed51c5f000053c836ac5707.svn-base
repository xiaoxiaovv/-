package com.istar.mediabroken.utils

import groovy.util.logging.Slf4j;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Author : YCSnail
 * Date   : 2018-04-24
 * Email  : liyancai1986@163.com
 */
@Slf4j
class CookieUtil {

    private static final String DEFAULT_PATH = "/";
    private static final boolean DEFAULT_HTTP_ONLY = false;
    private static final int TEMPORARY_MAX_AGE = -1;
    private static final int OVERDUE_MAX_AGE = 0;

    public static String readCookie(HttpServletRequest request, String key) {
        String value = "";

        request?.getCookies()?.each { cookie ->
            if(cookie.name?.equals(key)){
                value = cookie.value
                return
            }
        }

        return decodeStr(value)
    }

    public static void writeCookie(HttpServletResponse response, String domain, String key, String value) {
        writeCookie(response, domain, key, value, DEFAULT_HTTP_ONLY);
    }

    public static void writeCookie(HttpServletResponse response, String domain, String key, String value, boolean isHttpOnly) {
        addCookie(response, domain, DEFAULT_PATH, key, value, TEMPORARY_MAX_AGE, isHttpOnly);
    }

    public static void writeCookie(HttpServletResponse response, String domain, String key, String value, int maxAge) {
        addCookie(response, domain, DEFAULT_PATH, key, value, maxAge, DEFAULT_HTTP_ONLY);
    }

    public static void delCookie(HttpServletResponse response, String domain, String key) {
        addCookie(response, domain, DEFAULT_PATH, key, "", OVERDUE_MAX_AGE, DEFAULT_HTTP_ONLY);
    }

    private static void addCookie(HttpServletResponse response, String domain, String path,
                                  String key, String value, int maxAge, boolean isHttpOnly) {
        try {
            Cookie cookie = new Cookie(key, encodeStr(value));
//            cookie.setDomain(domain);
            cookie.setPath(path);
            cookie.setMaxAge(maxAge);
//            cookie.setHttpOnly(isHttpOnly);
            cookie.setVersion(0)
            response.addCookie(cookie);
        } catch (UnsupportedEncodingException e) {
            log.error(e);
            return;
        }
    }

    /**
     * 批量写入cookie
     * @param response
     * @param map
     */
    public static void addCookies(HttpServletResponse response, HttpServletRequest request, Map<String, String> map) {

        String domain = request.getServerName();
        map.each { k, v ->
            this.writeCookie(response, domain, k, v)
        }
    }

    public static String decodeStr(String source) {
        def resultStr = ""
        if (source) {
            if (source.contains("%")) {
                try {
                    resultStr = URLDecoder.decode(source, "UTF-8")
                } catch (UnsupportedEncodingException e) {
                    log.error("decode exception ::: {}", e)
                }
            } else {
                resultStr = source
            }
        }
        return resultStr
    }

    public static String encodeStr(String source) {
        try {
            if(source){
                return URLEncoder.encode(source, "UTF-8")
            }else{
                return ''
            }
        } catch (UnsupportedEncodingException e) {
            log.error("encode exception ::: {}", e)
        }
    }

}
