package com.istar.mediabroken.service.wechat

import com.mashape.unirest.http.Unirest
import org.springframework.stereotype.Service

/**
 * @author YCSnail
 * @date 2018-06-01
 * @email liyancai1986@163.com
 * @company SnailMart
 */
@Service
class WechatMinaService {

    private final static String CODE_2_SESSION_URL = 'https://api.weixin.qq.com/sns/jscode2session'
    private final static String appId = 'wx30226ddd87330318'
    private final static String appSecret = '030c9d48d9a48e61d9b8f71b346feb1c'

    def getOpenInfo(String code) {
        def result = Unirest.get(CODE_2_SESSION_URL)
                .queryString('appid', appId)
                .queryString('secret', appSecret)
                .queryString('js_code', code)
                .queryString('grant_type', 'authorization_code')
                .asJson()

        def res = result.statusCode == 200 ? result.body.object : [:]

        return res
    }

}
