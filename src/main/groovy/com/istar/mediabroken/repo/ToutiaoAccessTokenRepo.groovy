package com.istar.mediabroken.repo

import com.istar.mediabroken.utils.MongoHolder
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Author : YCSnail
 * Date   : 2017-05-31
 * Email  : liyancai1986@163.com
 */
@Repository
@Slf4j
class ToutiaoAccessTokenRepo {

    @Autowired
    MongoHolder mongo

    def get(String clientKey, String uid) {
        def collection = mongo.getCollection("toutiaoAccessToken")
        def obj = collection.findOne(toObj([clientKey: clientKey, uid: uid]))
        return obj != null ? [
                id          : obj._id,
                clientKey   : obj.clientKey,
                accessToken : obj.accessToken,
                expiresIn   : obj.expiresIn as long,
                uid         : obj.uid,      //头条的用户类型   12：注册用户 14：匿名用户
                updateTime  : obj.updateTime
        ] : null
    }


    def update(String clientKey, String uid, String token, long expiresIn) {
        def collection = mongo.getCollection("toutiaoAccessToken")

        def res = this.get(clientKey, uid)
        if(res) {
            collection.update(
                    toObj([_id: res.id, clientKey: clientKey, uid: uid]),
                    toObj(['$set': [
                            accessToken : token,
                            expiresIn   : expiresIn,
                            updateTime  : new Date()
                    ]]), false, true
            )
        }else {
            collection.insert(toObj([
                    _id         : UUID.randomUUID().toString(),
                    clientKey   : clientKey,
                    uid         : uid,
                    accessToken : token,
                    expiresIn   : expiresIn,
                    updateTime  : new Date()
            ]))
        }
    }
}
