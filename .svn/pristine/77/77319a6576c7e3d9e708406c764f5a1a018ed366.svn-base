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
class WechatPreAuthCodeRepo {

    @Autowired
    MongoHolder mongo

    def get(String appId) {
        def collection = mongo.getCollection("wechatPreAuthCode")
        def obj = collection.findOne(toObj([appId : appId]))
        return obj != null ? [
                id          : obj._id,
                appId       : obj.appId,
                preAuthCode : obj.preAuthCode,
                expiresIn   : obj.expiresIn as long,
                updateTime  : obj.updateTime
        ] : null
    }


    def update(String appId, String preAuthCode, long expiresIn) {
        def collection = mongo.getCollection("wechatPreAuthCode")

        def res = this.get(appId)
        if(res) {
            collection.update(
                    toObj([_id: res.id, appId: appId]),
                    toObj(['$set': [
                            preAuthCode : preAuthCode,
                            expiresIn   : expiresIn,
                            updateTime  : new Date()
                    ]]), false, true
            )
        }else {
            collection.insert(toObj([
                    _id         : UUID.randomUUID().toString(),
                    appId       : appId,
                    preAuthCode : preAuthCode,
                    expiresIn   : expiresIn,
                    updateTime  : new Date()
            ]))
        }
    }
}
