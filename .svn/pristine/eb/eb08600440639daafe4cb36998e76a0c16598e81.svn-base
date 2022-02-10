package com.istar.mediabroken.repo

import com.istar.mediabroken.utils.MongoHolder
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Author : YCSnail
 * Date   : 2018-04-18
 * Email  : liyancai1986@163.com
 */
@Repository
@Slf4j
class EcloudAccessTokenRepo {

    @Autowired
    MongoHolder mongo

    private static final String collName = "ecloudAccessToken"

    def get(String appId) {
        def collection = mongo.getCollection(collName)
        def obj = collection.findOne(toObj([appId : appId]))
        return obj != null ? [
                id              : obj._id,
                appId           : obj.appId,
                access_token    : obj.access_token,
                refresh_token   : obj.refresh_token,
                expires         : obj.expires as long,
                uid             : obj.uid as int,
                username        : obj.username as String,
                updateTime      : obj.updateTime
        ] : null
    }


    def update(String appId, String token, long expires, String refreshToken, int uid, String username) {
        def collection = mongo.getCollection(collName)

        def res = this.get(appId)
        if(res) {
            collection.update(
                    toObj([_id: res.id, appId: appId]),
                    toObj(['$set': [
                            access_token    : token,
                            refresh_token   : refreshToken,
                            expires         : expires,
                            uid             : uid,
                            username        : username,
                            updateTime      : new Date()
                    ]]), false, true
            )
        }else {
            collection.insert(toObj([
                    _id             : UUID.randomUUID().toString(),
                    appId           : appId,
                    access_token    : token,
                    refresh_token   : refreshToken,
                    expires         : expires,
                    uid             : uid,
                    username        : username,
                    updateTime      : new Date()
            ]))
        }
    }
}
