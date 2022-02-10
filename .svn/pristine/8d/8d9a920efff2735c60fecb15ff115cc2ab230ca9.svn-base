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
class WechatVerifyTicketRepo {

    @Autowired
    MongoHolder mongo

    def getNewest(String appId) {
        def collection = mongo.getCollection("wechatVerifyTicket")

        def ticket = null
        def cursor = collection.find(toObj([appId : appId])).sort(toObj([createTime: -1])).limit(1)
        while (cursor.hasNext()) {
            def res = cursor.next()
            ticket = [
                    id          : res._id,
                    appId       : res.appId,
                    infoType    : res.infoType,
                    createTime  : res.createTime,
                    ticket      : res.ticket,
                    updateTime  : res.cTime
            ]
        }
        cursor.close()

        return ticket
    }


    def add(String appId, String infoType, String createTime, String ticket) {
        if(!ticket) return

        def collection = mongo.getCollection("wechatVerifyTicket")
        def res = this.getNewest(appId)
        if(res) {
            collection.update(
                    toObj([_id: res.id, appId: appId]),
                    toObj(['$set': [
                            infoType    : infoType,
                            createTime  : createTime,
                            ticket      : ticket,
                            updateTime  : new Date()
                    ]]), false, true
            )
        }else {
            collection.insert(toObj([
                    _id         : UUID.randomUUID().toString(),
                    appId       : appId,
                    infoType    : infoType,
                    createTime  : createTime,
                    ticket      : ticket,
                    updateTime  : new Date()
            ]))
        }
    }

}
