package com.istar.mediabroken.service.ecloud

import com.mashape.unirest.http.Unirest
import org.springframework.beans.factory.annotation.Value

/**
 * Author : YCSnail
 * Date   : 2018-04-19
 * Email  : liyancai1986@163.com
 */
class EcloudMessageService {

    @Value('${ecloud.gateway}')
    String apiHost

    /**
     * 统一消息发送接口 (SaaS2004)
     * @param code
     * @return
     */
    def sendMessage(){

        String accessToken = "xxxxxxxx",
        refreshToken = "xxxxxxxxxxx"

        String url = apiHost + "/services/message/send?access_token=${accessToken}&refresh_token=${refreshToken}"
        def params = [
                userid      : 1234,
                priority    : 1,
                sendtime    : System.currentTimeMillis(),
                receivers   : '2345,3456',
                msglevel    : 0,
                appcode     : 'xxx'
        ]

        def res = Unirest.post(url).body(params as Map<String, Object>).asJson()

        def result = res.body.object

        println result

        return result.response
    }

}
