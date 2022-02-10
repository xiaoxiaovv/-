package com.istar.mediabroken.repo.account

import com.istar.mediabroken.entity.account.SimulateLoginSession
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
class SimulateLoginSessionRepo {

    @Autowired
    MongoHolder mongo

    String addSimulateLoginSession(SimulateLoginSession simulateLoginSession) {
        def collection = mongo.getCollection("simulateLoginSession")
        collection.insert(toObj(simulateLoginSession.toMap()))
        return simulateLoginSession.id
    }

    Map getUserBySimulateSessionId(String simulateSessionId) {
        def collection = mongo.getCollection("simulateLoginSession")
        def session = collection.findOne(toObj([_id: simulateSessionId]))
        return session ? session : null
    }

    void disableSimulateSession(long userId) {
        def collection = mongo.getCollection("simulateLoginSession")
        collection.update(toObj([userId: userId]), toObj(['$set': [enable: false, updateTime: new Date()]]), false, true)
    }

    void disableSimulateSession(String sid) {
        def collection = mongo.getCollection("simulateLoginSession")
        collection.update(toObj([_id: sid]), toObj(['$set': [enable: false, updateTime: new Date()]]), false, true)
    }

    void deleteSimulateSession(String sid) {
        def collection = mongo.getCollection("simulateLoginSession")
        collection.remove(toObj([_id: sid]))
    }
}
