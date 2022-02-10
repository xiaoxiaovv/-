package com.istar.mediabroken.service.baidu

import com.istar.mediabroken.utils.UrlUtils
import com.mashape.unirest.http.Unirest
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service

/**
 * @author YCSnail
 * @date 2018-07-06
 * @email liyancai1986@163.com
 * @company SnailTech
 */
@Service
@Slf4j
class BaiduOAuthService {


    private static final String appKey = "LiR15bIZzDOkGKxc6aO5LCRy"
    private static final String appSecret = "zhxFd6vkZdy2ETqlx5777BrGQH84qWH3"



    def getAccessToken() {

        String url = "https://openapi.baidu.com/oauth/2.0/token"

        def params = [
                grant_type   : 'client_credentials',
                client_id    : appKey,
                client_secret: appSecret
        ]

        def res = Unirest.post(url).fields(params as Map<String, Object>).asJson()

//                {
//                    "access_token":"24.8487207f1e1de69be4371f96feacb366.2592000.1533967079.282335-11494107",
//                    "refresh_token":"25.a8497600f7edee7889c145cb9c1929ea.315360000.1846735079.282335-11494107",
//                    "scope":"public brain_all_scope audio_voice_assistant_get audio_tts_post wise_adapt lebo_resource_base lightservice_public hetu_basic lightcms_map_poi kaidian_kaidian ApsMisTest_Test权限 vis-classify_flower lpq_开放 cop_helloScope ApsMis_fangdi_permission smartapp_snsapi_base iop_autocar",
//                    "session_key":"9mzdA54kdyxNK9FkNSp4K76fn5w6nvhlZsYEWAh4puuRbq0F1mTQh31I9068W0066tGKJRTlFezhbLTcgPVVzhMLQxpehg==",
//                    "session_secret":"30477f2bddf7e45ce850a0051441d02e",
//                    "expires_in":2592000
//                }


        def result = res.body.object


        return result
    }

    String getVideoUrl (String content, String token) {

        String url = "http://tsn.baidu.com/text2audio"

        def params = [
                lan: 'zh',
                ctp: 1,
                cuid: 'bjj',
                tok: token,
                tex: content,
                vol: 9,
                per: 3,
                spd: 5,
                pit: 5
        ]

        def res = UrlUtils.wrapGetUrl(url, params as Map<String, Object>)

        return res


    }

}
