package com.istar.mediabroken.service.toutiao

import com.istar.mediabroken.entity.app.Agent
import com.istar.mediabroken.repo.ToutiaoAccessTokenRepo
import com.istar.mediabroken.service.shareChannel.ChannelOAuthService
import com.istar.mediabroken.utils.Md5Util
import com.istar.mediabroken.utils.UrlUtils
import com.mashape.unirest.http.Unirest
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest

/**
 * Author : YCSnail
 * Date   : 2017-05-31
 * Email  : liyancai1986@163.com
 */
@Service
@Slf4j
class ToutiaoOAuthService extends ChannelOAuthService<ToutiaoOAuthConfig> {

    @Value('${toutiao.open.client.key}')
    public String clientKey

    @Value('${toutiao.open.client.secret}')
    public String clientSecret

    @Value('${toutiao.open.oauth.callback.url}')
    public String oauthCallbackUrl

    @Autowired
    private ToutiaoAccessTokenRepo accessTokenRepo


    String getOAuthRequestUrl(HttpServletRequest request) {

        config = getOAuthConfig(request)

        String url = 'https://open.snssdk.com/auth/authorize/'

        def params = [
                response_type   : 'code',
                auth_only       : 1,
                client_key      : config.clientKey,
                redirect_uri    : UrlUtils.wrapNotifyUrl(request, oauthCallbackUrl),
                display         : 0,     //  0/1/2
                timestamp       : System.currentTimeMillis()
        ]

        return UrlUtils.wrapGetUrl(url, params)
    }

    /**
     *
     * @param code
     * @return
     *  {
         "ret": 0,
         "data": {
             "access_token": "830fbcc88c90b767bcf5487d34d73bde",
             "expires_in": 1408122918,
             "uid": 18052,
             "uid_type": 12,
             "open_id": 10000
         }
     }
     */
    def getAccessToken(HttpServletRequest request, String code){

        config = getOAuthConfig(request)

        String url = 'https://open.snssdk.com/auth/token/'

        long timestamp = new Date().time
        def params = [
                client_key   : config.clientKey,
                client_secret: config.clientSecret,
                grant_type   : 'authorize_code',
                code         : code,
                atimestamp   : timestamp
        ]
        def signArr = [timestamp as String, code, config.clientSecret]
        signArr.sort()
        params.signature = Md5Util.md5(signArr.join('') as String)

        def res = Unirest.get(url).queryString(params as Map<String, Object>).asJson()

        def result = res.body.object
        if(result.ret == 0) {
            def data = result.data
            //更新库
            accessTokenRepo.update(config.clientKey as String, data.uid as String, data.access_token as String, data.expires_in as long)
            //返回结果
            return [
                    status: 1,
                    msg   : '获取授权成功',
                    data  : result.data
            ]
        }else {
            return [
                    status: 0,
                    msg: '接口请求发生错误',
                    code: result.ret
            ]
        }
    }

    def getAccessTokenFromMongo(HttpServletRequest request, String uid){

        config = getOAuthConfig(request)

        def accessTokenObj = accessTokenRepo.get(config.clientKey as String, uid)
        if(accessTokenObj && accessTokenIsValid(accessTokenObj)) {
            return accessTokenObj.accessToken
        }else {
            return ''
        }
    }

    def getToutiaoUser(HttpServletRequest request, String accessToken, String uid){

        if(!uid || !accessToken) {
            return null
        }

        config = getOAuthConfig(request)

        String url = 'https://open.snssdk.com/data/user_profile/'

        def res = Unirest.get(url)
                .queryString('access_token', accessToken)
                .queryString('client_key', config.clientKey)
                .asJson()

        def result = res.body.object
        log.info(['toutiao', '查询用户信息', result].join(':::') as String)
        return res.statusCode == 200 ? result : null
    }

    boolean accessTokenIsValid (def accessTokenObj) {
        if(!accessTokenObj){
            return false
        }
        return (new Date().time) < (accessTokenObj.expiresIn * 1000)
    }

    @Override
    ToutiaoOAuthConfig getOAuthConfig(HttpServletRequest request) {

        Agent agent = agentService.getAgent(request)

        config = configMap.get(agent.agentKey)

        if(!config) {
            def obj = getShareChannelOAuthConfig(agent)

            config = new ToutiaoOAuthConfig([
                    clientKey       : obj.toutiao.appKey,
                    clientSecret    : obj.toutiao.appSecret
            ])
            configMap.put(agent.agentKey, config)
        }
        return config
    }

    public static void main(String[] args) {

        long timestamp = new Date().time
        String code = '13943433'
        String clientSecret = '85d21f3eaab20ea641f8baabec1d2e80'


        String sign = [timestamp, code, clientSecret].sort()

//        String signature = Md5Util.md5([timestamp, code, clientSecret].sort())


        println sign
        println ''.join(sign)

    }

}

enum ToutiaoApiErrorEnum {

    SUCCESS_0(0, '成功'),
    AUTHORIZE_CODE_EXPIRED_7(7, 'authorize_code过期')
//    0	正常
//    1	没有传client_key
//    2	错误的client_key
//    3	没有授权信息
//    4	响应类型错误
//    5	授权类型错误
//    6	client_secret错误
//    7	authorize_code过期
//    8	指定url的scheme不是https
//    9	内部错误	请联系头条技术
//    10	access_token过期
//    11	缺少access_token
//    12	参数缺失
//    13	url错误
//    21	域名与登记域名不匹配
//    999	未知错误

    private int index
    private String value

    ToutiaoApiErrorEnum(int index, String value) {
        this.index = index
        this.value = value
    }

}
