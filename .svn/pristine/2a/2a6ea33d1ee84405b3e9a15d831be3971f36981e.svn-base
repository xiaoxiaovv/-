package com.istar.mediabroken.service.wechat

import com.google.common.base.Charsets
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.hash.Hasher
import com.google.common.hash.Hashing
import com.mashape.unirest.http.Unirest
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

/**
 * Author : YCSnail
 * Date   : 2017-11-22
 * Email  : liyancai1986@163.com
 */
@Service
class WechatMPService {

    public static final String appId = "wx9383d74844fbd6f2"
    public static final String appSecret = "0b679326c01277b3ae96f5b87b3da9fb"
    public static final String tokenUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s"
    public static final String ticketUrl= "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=%s&type=jsapi"

    private static final Cache<String, String> cache = CacheBuilder.newBuilder()
            .maximumSize(1000)    // 最多可以缓存1000个key
            .expireAfterWrite(7200, TimeUnit.SECONDS)  // 过期时间
            .build()


    def getWechatConfig(HttpServletRequest request){

        def token = cache.get("WX_APPID_${appId}", new Callable<String>() {
            @Override
            String call() throws Exception {
                return Unirest.get(String.format(tokenUrl, appId, appSecret)).asJson().body.object.getString('access_token')
            }
        })

        def ticket = cache.get("WX_TOKEN_${token}",new Callable<String>() {
            @Override
            String call() throws Exception {
                return Unirest.get(String.format(ticketUrl, token)).asJson().body.object.get('ticket')
            }
        })

        def noncestr = RandomStringUtils.randomAlphanumeric(15)
        def timestamp = (System.currentTimeMillis()/1000).longValue()
        def signatureContent = "jsapi_ticket=${ticket}&noncestr=${noncestr}&timestamp=${timestamp}&url=${request.getHeader('Referer')}"
        Hasher sha1 = Hashing.sha1().newHasher();
        def signature = sha1.putString(signatureContent.toString(),Charsets.UTF_8).hash().toString();

        return [
                appId       : appId,
                timestamp   : timestamp,
                noncestr    : noncestr,
                signature   : signature
        ]
    }
}
