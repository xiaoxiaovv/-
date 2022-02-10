package com.istar.mediabroken.utils

import com.google.common.net.InternetDomainName
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.message.BasicNameValuePair
import org.apache.http.protocol.HTTP
import org.apache.http.util.EntityUtils

import javax.servlet.http.HttpServletRequest

class UrlUtils {
    static String stripUrl(String url) {
        if (url.indexOf("https://") > -1) {
            url = url.substring("https://".length())
        } else if (url.indexOf("http://") > -1) {
            url = url.substring("http://".length())
        }

        if (url.endsWith('/')) {
            url = url.substring(0, url.length() - 1)
        }

        return url
    }

    static String wrapGetUrl(String url, Map<String, Object> params) {
        List<NameValuePair> postPara = new ArrayList<NameValuePair>();
        params.each { key, value ->
            postPara.add(new BasicNameValuePair(key as String, value as String));
        }
        def paramStr = EntityUtils.toString(new UrlEncodedFormEntity(postPara, HTTP.UTF_8));
        return url + (paramStr ? '?' + paramStr : "")
    }

    static String getDomainFromUrl(String url) {
        if (url.indexOf("https://") > -1) {
            url = url.substring("https://".length())
        } else if (url.indexOf("http://") > -1) {
            url = url.substring("http://".length())
        }

        if (url.indexOf("/") > -1) {
            url = url.substring(0, url.indexOf("/"))
        }
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"))
        }
        return url
    }

    static String getReverseDomainFromUrl(String url) {
        String tail = ""
        String domain = ""
        if (url.indexOf("https://") > -1) {
            url = url.substring("https://".length())
        } else if (url.indexOf("http://") > -1) {
            url = url.substring("http://".length())
        }

        if (url.indexOf("/") > -1) {
            domain = url.substring(0, url.indexOf("/"))
            tail = url.substring(url.indexOf("/"))
        } else if (url.indexOf("?") > -1) {
            domain = url.substring(0, url.indexOf("?"))
            tail = url.substring(url.indexOf("?"))
        } else {
            domain = url
        }
        def splitWords = (domain.split("\\.")).reverse()
        String reverseDomain = splitWords.join(".")
        if (tail.equals("")) {
            return reverseDomain
        } else {
            return reverseDomain + tail
        }
    }

    static String webSiteDomainUrl(String url) {
        if (url.indexOf("https://") > -1) {
            url = url.substring("https://".length())
        } else if (url.indexOf("http://") > -1) {
            url = url.substring("http://".length())
        }

        return url
    }

    static String getWeiboUidFromUrl(String url) {
        String result = ""
        def sepUrlList = []
        if (url.indexOf("https://") > -1) {
            url = url.substring("https://".length())
        } else if (url.indexOf("http://") > -1) {
            url = url.substring("http://".length())
        }
        if (url){
            sepUrlList = url.split("/")
            if (sepUrlList && sepUrlList.size() >= 2){
                result = sepUrlList[1]
                if ("u".equals(result) && sepUrlList.size()>=3){
                    result = sepUrlList[2]
                }
            }
        }
        return result
    }

    static String getBjjHost(HttpServletRequest request) {
        return request.getRequestURL().toString().split('\\/api')[0]
    }

    static String getBjjDomain(HttpServletRequest request) {
        return request.getServerName().toString()
    }

    static String wrapNotifyUrl(HttpServletRequest request, String url){
        return url.replaceAll('\\{bjj_domain\\}', getBjjDomain(request))
    }

    static boolean imgFilter(String url) {
        def img = new ImageInfo(url)
        if (img && img.width >= 200 && img.height >= 200) {
            return true
        }
        return false
    }

    static String getDomainFromUrlWithoutWWW(String url) {
        if(url.startsWith(".")){
            url = url.substring(1)
        }
        if (url.indexOf("https://") > -1) {
            url = url.substring("https://".length())
        } else if (url.indexOf("http://") > -1) {
            url = url.substring("http://".length())
        }
        if (url.startsWith("www.") || url.startsWith("WWW.")) {
            url = url.substring("www.".length())
        }
        if (url.indexOf("/") > -1) {
            url = url.substring(0, url.indexOf("/"))
        }
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"))
        }
        return url
    }
    static String getTopDomain(String url) {

        String host = getDomainFromUrlWithoutWWW(url)
        InternetDomainName owner = null
        try {
            owner =
                    InternetDomainName.from(host).topPrivateDomain()
            host = owner.toString()
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            return host
        }

    }
    static String getYqmsImgUrl(String url) {
        def base64str = Base64.encoder.encodeToString(url as Byte[])
//                .encodeBase64String(url as Byte[])
        if (null == base64str) {
            return null
        }
        return "https://yqms3.zhxgimg.com/download/img/" + base64str.replaceAll("/", "-")
    }
}
