package com.istar.mediabroken.service.qqom

import com.istar.mediabroken.entity.app.Agent
import com.istar.mediabroken.repo.QqomAccessTokenRepo
import com.istar.mediabroken.service.shareChannel.ChannelOAuthService
import com.istar.mediabroken.utils.UrlUtils
import com.mashape.unirest.http.Unirest
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest

/**
 * Author : YCSnail
 * Date   : 2017-09-11
 * Email  : liyancai1986@163.com
 */
@Service
@Slf4j
class QqomOAuthService extends ChannelOAuthService<QqomOAuthConfig> {

    @Value('${qqom.open.client.id}')
    public String appKey

    @Value('${qqom.open.client.secret}')
    public String appSecret

    @Value('${qqom.open.oauth.callback.url}')
    public String oauthCallbackUrl

    @Autowired
    private QqomAccessTokenRepo accessTokenRepo


    String getOAuthRequestUrl(HttpServletRequest request){

        config = getOAuthConfig(request)

        String url = "https://auth.om.qq.com/omoauth2/authorize"

        def params = [
                client_id       : config.appKey,
                redirect_uri    : UrlUtils.wrapNotifyUrl(request, oauthCallbackUrl),//授权回调地址，站外应用需与设置的回调地址一致，站内应用需填写canvas page的地址。
                response_type   : 'code',        //申请scope权限所需参数，可一次申请多个scope权限，用逗号分隔。
                state           : 'demo'         //用于保持请求和回调的状态，在回调时，会在Query Parameter中回传该参数。开发者可以用这个参数验证请求有效性，也可以记录用户请求授权页前的位置。这个参数可用于防止跨站请求伪造（CSRF）攻击
        ]

        return UrlUtils.wrapGetUrl(url, params)
    }

    /**
     *
     * @param code
     * @return
     *  { "code":"0", "data": {
     *  "access_token":"ACCESS_TOKEN",
     *  "expires_in":7200,
     *  "refresh_toekn":"REFRESH_TOKEN",
     *  "openid":OPENID,
     *  "scope":"SCOPE", }
     *  }
     */
    def getAccessToken(HttpServletRequest request, String code){

        config = getOAuthConfig(request)

        String url = "https://auth.om.qq.com/omoauth2/accesstoken"

        def params = [
                client_id       : config.appKey,
                client_secret   : config.appSecret,
                grant_type      : 'authorization_code',
                code            : code
        ]

        def res = Unirest.post(url).fields(params as Map<String, Object>).asJson()

        def result = res.body.object
        if((result.code as int) == 0){
            def resultData = result.data
            //更新库
            accessTokenRepo.update(config.appKey as String, resultData.openid as String, resultData.access_token as String, resultData.refresh_token as String, resultData.expires_in as long)
            //返回结果
            return [
                    status  : 1,
                    msg     : '获取授权成功',
                    data    : resultData
            ]
        } else {
            log.error(['qqom', '获取token信息', result].join(':::') as String)
            return [
                    status  : 0,
                    msg     : result.msg,
                    code    : result.code as int
            ]
        }
    }

    def refreshAccessToken(HttpServletRequest request, String refreshToken){

        config = getOAuthConfig(request)

        String url = "https://auth.om.qq.com/omoauth2/refreshtoken"

        def params = [
                client_id       : config.appKey,
                grant_type      : 'refreshtoken',
                refresh_token   : refreshToken
        ]

        def res = Unirest.post(url).fields(params as Map<String, Object>).asJson()

        def result = res.body.object
        if((result.code as int) == 0){
            def resultData = result.data
            //更新库
            accessTokenRepo.update(config.appKey as String, resultData.openid as String, resultData.access_token as String, resultData.refresh_token as String, resultData.expires_in as long)
            //返回结果
            return [
                    status  : 1,
                    msg     : '刷新授权成功',
                    data    : resultData
            ]
        } else {
            log.error(['qqom', '刷新token信息', result].join(':::') as String)
            return [
                    status  : 0,
                    msg     : result.msg,
                    code    : result.code as int
            ]
        }
    }

    def getAccessTokenFromMongo(HttpServletRequest request, String uid){

        config = getOAuthConfig(request)

        def accessTokenObj = accessTokenRepo.get(config.appKey as String, uid)
        if(accessTokenObj && accessTokenIsValid(accessTokenObj)) {
            return accessTokenObj.accessToken
        }else {
            def res = this.refreshAccessToken(request, accessTokenObj.refreshToken as String)
            return res.status == 1 ? res.data.access_token : ''
        }
    }

    boolean accessTokenIsValid (def accessTokenObj) {
        if(!accessTokenObj){
            return false
        }
        return (new Date().time - accessTokenObj.updateTime.time) < (accessTokenObj.expiresIn) * 1000
    }

    def getQqomUser(String accessToken, String uid) {

        if(!uid || !accessToken) {
            return null
        }

        String url = "https://api.om.qq.com/media/basicinfoauth"

        def res = Unirest.get(url)
                .queryString('access_token', accessToken)
                .queryString('openid', uid)
                .asJson()

        def result = res.body.object
        log.info("企鹅号授权，查询用户信息：{}", result)

//    {
//        "code":"0",
//        "msg": "success",
//        "data": {
//            "header": "http://inews.gtimg.com/newsapp_ls/0/183849551_100100/0",
//            "nick": "测试"
//        }
//    }
        if((result.code as int) == 0) {
            def resultData = result.data
            //异常处理，企鹅号返回来的头像是http地址，但实际无法访问，https可以，故做特殊处理
            resultData.put('header', resultData.header.replaceAll('^http:', 'https:'))
            return resultData
        } else {
            return null
        }
    }

    @Override
    QqomOAuthConfig getOAuthConfig(HttpServletRequest request) {

        Agent agent = agentService.getAgent(request)

        config = configMap.get(agent.agentKey)

        if(!config) {
            def obj = getShareChannelOAuthConfig(agent)

            config = new QqomOAuthConfig([
                    appKey      : obj.qqom.appKey,
                    appSecret   : obj.qqom.appSecret
            ])
            configMap.put(agent.agentKey, config)
        }
        return config
    }

}
