package com.istar.mediabroken.repo.account

import com.istar.mediabroken.utils.MongoHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

@Repository
class WechatSessionRepo {
    @Autowired
    MongoHolder mongo;

    def getByUserId(long userId) {
        def collection = mongo.getCollection("wechatSession")
        def rep = collection.findOne(toObj([userId: userId, enable: true]))
        def res = null
        if (rep) {
            res = [
                    userId      : rep.userId,
                    enable      : rep.enable,
                    sid         : rep._id,
                    updateTime  : rep.updateTime
            ]
        }
        return res
    }


    String addSession(Long userId) {
        def collection = mongo.getCollection("wechatSession")
        def sid = UUID.randomUUID().toString()
        // todo 需要验证是否插入成功
        collection.insert(toObj([_id: sid, userId: userId, createTime: new Date(), enable: true, updateTime: new Date()]))
        return sid
    }

    Map getUserBySessionId(String sid) {
        def collection = mongo.getCollection("wechatSession")
        def rep = collection.findOne(toObj([_id: sid]))
        def userId = null;
        if (rep) {
            return [
                    userId    : rep.userId,
                    enable    : rep.enable,
                    updateTime: rep.updateTime]
        } else {
            null
        }
        return userId
    }

    void deleteSession(String sid) {
        def collection = mongo.getCollection("wechatSession")
        collection.remove(toObj([_id: sid]))
    }

    void disableSession(long userId) {
        def collection = mongo.getCollection("wechatSession")
        collection.update(toObj([userId: userId]), toObj(['$set': [enable: false, updateTime: new Date()]]), false, true)

    }

}
