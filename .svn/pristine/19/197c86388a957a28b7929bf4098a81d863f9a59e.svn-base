package com.istar.mediabroken.repo

import com.istar.mediabroken.utils.MongoHolder
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Author : YCSnail
 * Date   : 2017-05-18
 * Email  : liyancai1986@163.com
 */
@Repository
@Slf4j
class WechatAccessTokenRepo {

    @Autowired
    MongoHolder mongo

    def get(String appId) {
        def collection = mongo.getCollection("wechatAccessToken")
        def obj = collection.findOne(toObj([appId : appId]))
        return obj != null ? [
                id          : obj._id,
                appId       : obj.appId,
                accessToken : obj.accessToken,
                expiresIn   : obj.expiresIn as long,
                updateTime  : obj.updateTime
        ] : null
    }


    def update(String appId, String token, long expiresIn) {
        def collection = mongo.getCollection("wechatAccessToken")

        def res = this.get(appId)
        if(res) {
            collection.update(
                    toObj([_id: res.id, appId: appId]),
                    toObj(['$set': [
                            accessToken : token,
                            expiresIn   : expiresIn,
                            updateTime  : new Date()
                    ]]), false, true
            )
        }else {
            collection.insert(toObj([
                    _id         : UUID.randomUUID().toString(),
                    appId       : appId,
                    accessToken : token,
                    expiresIn   : expiresIn,
                    updateTime  : new Date()
            ]))
        }
    }
}
