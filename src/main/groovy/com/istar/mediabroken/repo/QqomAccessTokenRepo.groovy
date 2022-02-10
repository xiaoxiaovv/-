package com.istar.mediabroken.repo

import com.istar.mediabroken.utils.MongoHolder
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Author : YCSnail
 * Date   : 2017-09-11
 * Email  : liyancai1986@163.com
 */
@Repository
@Slf4j
class QqomAccessTokenRepo {

    @Autowired
    MongoHolder mongo

    def get(String appKey, String uid) {
        def collection = mongo.getCollection("qqomAccessToken")
        def obj = collection.findOne(toObj([appKey: appKey, uid: uid]))
        return obj != null ? [
                id          : obj._id,
                appKey      : obj.appKey,
                accessToken : obj.accessToken,
                refreshToken: obj.refreshToken,
                expiresIn   : obj.expiresIn as long,
                uid         : obj.uid,
                updateTime  : obj.updateTime
        ] : null
    }


    def update(String appKey, String uid, String token, String refreshToken, long expiresIn) {
        def collection = mongo.getCollection("qqomAccessToken")

        def res = this.get(appKey, uid)
        if(res) {
            collection.update(
                    toObj([_id: res.id, appKey: appKey, uid: uid]),
                    toObj(['$set': [
                            accessToken : token,
                            refreshToken: refreshToken,
                            expiresIn   : expiresIn,
                            updateTime  : new Date()
                    ]]), false, true
            )
        }else {
            collection.insert(toObj([
                    _id         : UUID.randomUUID().toString(),
                    appKey      : appKey,
                    uid         : uid,
                    accessToken : token,
                    refreshToken: refreshToken,
                    expiresIn   : expiresIn,
                    updateTime  : new Date()
            ]))
        }
    }

}
