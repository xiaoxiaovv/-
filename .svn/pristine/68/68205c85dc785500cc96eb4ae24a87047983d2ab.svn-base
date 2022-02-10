package com.istar.mediabroken.repo.system

import com.istar.mediabroken.entity.SystemSetting
import com.istar.mediabroken.entity.system.Message
import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.QueryBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

@Repository
class MessageRepo {
    @Autowired
    MongoHolder mongo

    void addMessage(long userId, String title, String content) {
        def collection = mongo.getCollection("message")
        collection.insert(toObj([
                _id       : UUID.randomUUID().toString(),
                userId    : userId,
                title     : title ?: "",
                content   : content ?: "",
                createTime: new Date()
        ]))
    }

    Date getLastTimeByUserId(long userId) {
        Date lastTime = null
        def collection = mongo.getCollection("messageStatus")
        def obj = collection.findOne(toObj([_id: userId]))
        if (obj) {
            lastTime = obj.lastTime
        }
        return lastTime
    }

    void addMessageStatus(long userId, Date lastTime) {
        def collection = mongo.getCollection("messageStatus")
        collection.insert(toObj([
                _id       : userId,
                lastTime  : lastTime,
                updateTime: new Date()
        ]))
    }

    int getNewMesageCount(long userId, Date lastTime) {
        int count = 0
        def collection = mongo.getCollection("message")
        count = collection.count(toObj([userId: userId, createTime: [$gt: lastTime]]))
        return count
    }

    List<Message> getMessage(long userId, Date prevTime, int pageSize) {
        def collection = mongo.getCollection("message")
        def cursor = collection.find(toObj([userId: userId, createTime: [$lt: prevTime]])).sort(toObj([createTime: -1])).limit(pageSize)
        if (!prevTime){
            cursor = collection.find(toObj([userId: userId])).sort(toObj([createTime: -1])).limit(pageSize)
        }
        def list = []
        while (cursor.hasNext()) {
            def obj = cursor.next()
            list << new Message(obj)
        }
        cursor.close()
        return list
    }

    Date getMinCreateTime(long userId){
        def collection = mongo.getCollection("message")
        def cursor = collection.find(toObj([userId: userId])).sort(toObj([createTime: 1])).limit(1)
        Date minCreateTime
        while (cursor.hasNext()) {
            def obj = cursor.next()
            minCreateTime = obj.createTime
        }
        cursor.close()
        return minCreateTime
    }

    boolean modifyMessageStatus(long userId, Date lastTime) {
        def collection = mongo.getCollection("messageStatus")
        collection.update(toObj([_id: userId]), toObj([lastTime: lastTime, updateTime: new Date()]), true, false)
        return true;
    }

}
