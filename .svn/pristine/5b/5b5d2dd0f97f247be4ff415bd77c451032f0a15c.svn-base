package com.istar.mediabroken.repo.capture

import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.WriteConcern
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj
import static com.istar.mediabroken.utils.MongoHelper.toObj
import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Author: Luda
 * Time: 2017/9/30
 */
@Repository
@Slf4j
class QuerySessionRepo {
    @Autowired
    MongoHolder mongo

    void addQuerySession(String queryId, String simhash) {
        def collection = mongo.getCollection("querySession")
        collection.insert(toObj([_id: queryId + simhash, queryId: queryId, createTime: new Date()]), WriteConcern.ACKNOWLEDGED)
    }

    void removeQuerySessionByTime(String queryId, Date time) {
        def collection = mongo.getCollection("querySession")
        collection.remove(toObj([queryId: queryId, time: ["\$gt": time]]), WriteConcern.ACKNOWLEDGED)
    }

    boolean isQuerySessionRecordExist(String queryId, String simhash) {
        def collection = mongo.getCollection("querySession")
        if(collection.find(toObj([_id: queryId + simhash]))){
            return true
        }else {
            return false
        }
    }

    void addNewsQuerySession(String queryId, String newsId) {
        def collection = mongo.getCollection("querySession")
        collection.insert(toObj([_id: queryId + newsId, queryId: queryId, createTime: new Date()]), WriteConcern.ACKNOWLEDGED)
    }

    boolean isNewsQuerySessionExist(String queryId, String newsId) {
        def collection = mongo.getCollection("querySession")
        if(collection.find(toObj([_id: queryId + newsId]))){
            return true
        }else {
            return false
        }
    }

}
