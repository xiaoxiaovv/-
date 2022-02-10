package com.istar.mediabroken.service.ecloud

import com.alibaba.fastjson.JSONObject
import com.mashape.unirest.http.Unirest
import groovy.util.logging.Slf4j
import org.apache.http.entity.ContentType
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Author : YCSnail
 * Date   : 2018-04-19
 * Email  : liyancai1986@163.com
 */
@Service
@Slf4j
class EcloudUserService {

    @Value('${ecloud.gateway}')
    String apiHost

    /**
     * 获取用户基本信息接口 (SaaS2001)
     * @param userId
     * @return
     */
    def getEcloudUserInfo(String accessToken, int userId){


        String url = apiHost + "/services/user/info?access_token=${accessToken}"
        def params = [
                userid          : userId,
                expandkeys      : 'headphoto'
        ]

        def res = Unirest.get(url).headers([
                "Content-type": ContentType.APPLICATION_JSON.toString()
        ]).queryString(params as Map<String, Object>).asJson()

        def result = res.body.object
        log.info(['ecloud', '请求用户信息SaaS2001', result, result.isNull('code')].join(':::') as String)

        //授权接口返回错误
        if(!result.isNull('code')) {
            return [
                    userid      : 0,
                    username    : '',
                    code        : result.getInt('code'),
                    msg         : result.getString('msg')
            ]
        }

        return result
    }


}
