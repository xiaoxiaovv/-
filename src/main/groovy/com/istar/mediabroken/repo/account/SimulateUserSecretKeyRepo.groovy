package com.istar.mediabroken.repo.account

import com.istar.mediabroken.utils.MongoHolder
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Created by zc on   2018/7/19
 */
@Repository
@Slf4j
class SimulateUserSecretKeyRepo {

    @Autowired
    MongoHolder mongo

    def getInfoByUserIdAndSecretKey(long userId, String secretKey) {
        def collection = mongo.getCollection("simulateUserSecretKey")
        def info = collection.findOne(toObj([userId: userId, secretKey: secretKey]))
        return info ? info : null
    }

    void removeSecretKey(long userId) {
        def collection = mongo.getCollection("simulateUserSecretKey")
        collection.remove(toObj([userId: userId]))
    }
}
