package com.istar.mediabroken.repo

import com.istar.mediabroken.utils.MongoHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Author : YCSnail
 * Date   : 2018-03-15
 * Email  : liyancai1986@163.com
 */
@Repository
class CaptchaRepo {

    @Autowired
    MongoHolder mongo

    private String collectionName = "captcha";

    def get(String id) {
        def collection = mongo.getCollection(collectionName)
        def obj = collection.findOne(toObj([_id : id]))
        return obj != null ? [
                id          : obj._id,
                verifyCode  : obj.verifyCode,
                userAgent   : obj.userAgent
        ] : null
    }

    def update(String id, String verifyCode, String ua) {
        def collection = mongo.getCollection(collectionName)
        collection.update(
                toObj([_id: id]),
                toObj(['$set': [
                        verifyCode: verifyCode,
                        userAgent : ua,
                        createTime: new Date()
                ]]),
                true,
                false
        )
    }

    def remove(String id) {
        def collection = mongo.getCollection(collectionName)
        collection.remove(toObj([_id: id]))
    }



}
