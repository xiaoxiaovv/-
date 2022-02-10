package com.istar.mediabroken.utils

import cn.hutool.core.codec.Base64Decoder
import org.apache.xmlbeans.impl.util.Base64
import org.springframework.util.Base64Utils
import sun.misc.BASE64Decoder

/**
 * @author YCSnail
 * @date 2019-01-18
 * @email liyancai1986@163.com
 */
class ImageUtil {

    static String convertZhxgImage2WeiboLarge(String url) {

        String res = url

        try {
            if(url.contains("zhxgimg.com") && url.contains("download/img")) {
                res = url.split("download/img/")[1]
                res = Base64Decoder.decodeStr(res).replaceAll("thumbnail", "large")
            }
        } catch (Exception e) {
            return res
        }

        return res
    }

    public static void main(String[] args) {

        String url = "https://yqms3.zhxgimg.com/download/img/aHR0cDovL3d4NC5zaW5haW1nLmNuL3RodW1ibmFpbC83ZTM2OWU3ZWx5MWZ6YWg5Z3owbDNqMjBwMDBnbWpzcS5qcGc="


        println(convertZhxgImage2WeiboLarge(url))


    }
}
