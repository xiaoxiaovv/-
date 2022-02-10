package com.istar.mediabroken.repo

import com.istar.mediabroken.utils.MongoHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Author : YCSnail
 * Date   : 2017-05-18
 * Email  : liyancai1986@163.com
 */
@Repository
class WechatAuthorizationInfoRepo {

    @Autowired
    MongoHolder mongo

    def get(String appId, String authorizerAppId) {
        def collection = mongo.getCollection("wechatAuthorizationInfo")
        def obj = collection.findOne(toObj([appId : appId, authorizerAppId: authorizerAppId]))
        return obj != null ? [
                id                      : obj._id,
                appId                   : obj.appId,
                authorizerAppId         : obj.authorizerAppId,
                authorizerAccessToken   : obj.authorizerAccessToken,
                authorizerRefreshToken  : obj.authorizerRefreshToken,
                expiresIn               : obj.expiresIn as long,
                funcInfo                : obj.funcInfo,
                updateTime              : obj.updateTime
        ] : null
    }


    def update(String appId, def authorizationInfo) {
        def collection = mongo.getCollection("wechatAuthorizationInfo")

        def res = this.get(appId, authorizationInfo.authorizer_appid as String)
        if(res) {
            collection.update(
                    toObj([_id: res.id, appId: appId, authorizerAppId: authorizationInfo.authorizer_appid as String]),
                    toObj(['$set': [
                            authorizerAccessToken   : authorizationInfo.authorizer_access_token,
                            authorizerRefreshToken  : authorizationInfo.authorizer_refresh_token,
                            expiresIn               : authorizationInfo.expires_in as long,
                            funcInfo                : authorizationInfo.func_info,
                            updateTime              : new Date()
                    ]]), false, true
            )
        }else {
            collection.insert(toObj([
                    _id                     : UUID.randomUUID().toString(),
                    appId                   : appId,
                    authorizerAppId         : authorizationInfo.authorizer_appid,
                    authorizerAccessToken   : authorizationInfo.authorizer_access_token,
                    authorizerRefreshToken  : authorizationInfo.authorizer_refresh_token,
                    expiresIn               : authorizationInfo.expires_in as long,
                    funcInfo                : authorizationInfo.func_info,
                    updateTime              : new Date()
            ]))
        }
    }

    def updateToken(String appId, def authorizationInfo) {
        def collection = mongo.getCollection("wechatAuthorizationInfo")
        collection.update(
                toObj([appId: appId, authorizerAppId: authorizationInfo.authorizer_appid as String]),
                toObj(['$set': [
                        authorizerAccessToken   : authorizationInfo.authorizer_access_token,
                        authorizerRefreshToken  : authorizationInfo.authorizer_refresh_token,
                        expiresIn               : authorizationInfo.expires_in as long,
                        updateTime              : new Date()
                ]]), false, true
        )
    }
}
