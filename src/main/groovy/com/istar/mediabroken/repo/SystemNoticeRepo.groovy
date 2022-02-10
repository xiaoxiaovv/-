package com.istar.mediabroken.repo

import com.istar.mediabroken.utils.MongoHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Author : YCSnail
 * Date   : 2017-07-06
 * Email  : liyancai1986@163.com
 */
@Repository
class SystemNoticeRepo {

    @Autowired
    MongoHolder mongo

    void add(String cont, long expireTime) {
        def collection = mongo.getCollection("systemNotice")
        def query = [ expireTime : [$gt: new Date().time]]
        collection.update(toObj(query), toObj(['$set': [expireTime : 0]]))
        collection.insert(toObj([
                _id         : UUID.randomUUID().toString(),
                cont        : cont,
                createTime  : new Date(),
                expireTime  : expireTime
        ]))
    }

    void update(String id) {
        def collection = mongo.getCollection("systemNotice")
        collection.update(
                toObj([_id: id]),
                toObj(['$set': [expireTime : 0]])
        )
    }

    List getNoticeList() {
        def collection = mongo.getCollection("systemNotice")
        def query = [ expireTime : [$gt: new Date().time]]
        def cursor = collection.find(toObj(query)).sort(toObj([createTime: -1]))
        def list = []
        while (cursor.hasNext()) {
            def res = cursor.next()
            list << [
                    id          : res._id,
                    cont        : res.cont,
                    createTime  : res.createTime.time,
                    expireTime  : res.expireTime
            ]
        }
        cursor.close()

        return list
    }

}
