package com.istar.mediabroken.service.weibo

import com.istar.mediabroken.entity.app.Agent
import com.istar.mediabroken.repo.WeiboAccessTokenRepo
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
 * Date   : 2017-03-28
 * Email  : liyancai1986@163.com
 */
@Slf4j
@Service
class WeiboOAuthService extends ChannelOAuthService<WeiboOAuthConfig> {

    @Value('${weibo.open.app.key}')
    public String appKey

    @Value('${weibo.open.app.secret}')
    public String appSecret

    @Value('${weibo.open.oauth.notify.url}')
    public String oauthNotifyUrl

    @Value('${weibo.api.url}')
    public String apiUrl

    @Autowired
    private WeiboAccessTokenRepo accessTokenRepo


    String getOAuthRequestUrl(HttpServletRequest request){

        config = getOAuthConfig(request)

        String url = apiUrl + '/oauth2/authorize'

        def params = [
                client_id       : config.clientId,
                redirect_uri    : UrlUtils.wrapNotifyUrl(request, oauthNotifyUrl),//授权回调地址，站外应用需与设置的回调地址一致，站内应用需填写canvas page的地址。
                scope           : 'all',        //申请scope权限所需参数，可一次申请多个scope权限，用逗号分隔。
                state           : 'demo',       //用于保持请求和回调的状态，在回调时，会在Query Parameter中回传该参数。开发者可以用这个参数验证请求有效性，也可以记录用户请求授权页前的位置。这个参数可用于防止跨站请求伪造（CSRF）攻击
                display         : 'default',    //授权页面的终端类型
                forcelogin      : true         //是否强制用户重新登录，true：是，false：否。默认false。
        ]

        return UrlUtils.wrapGetUrl(url, params)
    }

    /**
     *
     * @param code
     * @return
     *  {"access_token":"2.00GBOQlDSWmmcD4964080f9a_s_nbD","uid":"3446753420","expires_in":157679999}
     */
    def getAccessToken(HttpServletRequest request, String code){

        config = getOAuthConfig(request)

        String url = apiUrl + '/oauth2/access_token'

        def params = [
                client_id       : config.clientId,
                client_secret   : config.clientSecret,
                grant_type      : 'authorization_code',
                code            : code,
                redirect_uri    : UrlUtils.wrapNotifyUrl(request, oauthNotifyUrl)
        ]

        def res = Unirest.post(url).fields(params as Map<String, Object>).asJson()

        def result = res.body.object
        if(!(result?.isNull('access_token'))){
            //更新库
            accessTokenRepo.update(config.clientId, result.uid as String, result.access_token as String, result.expires_in as long)
            //返回结果
            return [
                    status  : 1,
                    msg     : '获取授权成功',
                    data    : result
            ]
        } else {
            return [
                    status  : 0,
                    msg     : result.error,
                    code    : result.error_code
            ]
        }

    }

    def getAccessTokenFromMongo(HttpServletRequest request, String uid){

        config = getOAuthConfig(request)

        def accessTokenObj = accessTokenRepo.get(config.clientId, uid)
        if(accessTokenObj && accessTokenIsValid(accessTokenObj)) {
            return accessTokenObj.accessToken
        }else {
            return ''
        }
    }

    def getWeiboUser(String accessToken, String uid){

        if(!uid || !accessToken) {
            return null
        }

        String url = apiUrl + '/2/users/show.json'

        def res = Unirest.get(url)
                .queryString('access_token', accessToken)
                .queryString('uid', uid)
                .asJson()

        def result = res.body.object
        log.info("微博授权，查询用户信息：{}", result)
        return res.statusCode == 200 ? result : null
    }

    def cancel(String accessToken){

        String url = apiUrl + '/oauth2/revokeoauth2'

        def res = Unirest.get(url).queryString('access_token', accessToken).asJson()

        def result = res.body.object

        if(result.isNull('result')){
            return false
        }
        return result?.getString('result')?.equals('true')
    }

    boolean accessTokenIsValid (def accessTokenObj) {
        if(!accessTokenObj){
            return false
        }
        return (new Date().time - accessTokenObj.updateTime.time) < accessTokenObj.expiresIn * 1000
    }

    @Override
    WeiboOAuthConfig getOAuthConfig(HttpServletRequest request) {

        Agent agent = agentService.getAgent(request)

        config = configMap.get(agent.agentKey)

        if(!config) {
            def obj = getShareChannelOAuthConfig(agent)

            config = new WeiboOAuthConfig([
                    clientId    : obj.weibo.appKey,
                    clientSecret: obj.weibo.appSecret
            ])
            configMap.put(agent.agentKey, config)
        }
        return config
    }

}
