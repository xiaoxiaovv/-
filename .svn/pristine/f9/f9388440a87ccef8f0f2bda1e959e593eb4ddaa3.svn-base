package com.istar.mediabroken.repo.rubbish

import com.istar.mediabroken.entity.rubbish.RubbishNews
import com.istar.mediabroken.utils.MongoHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Author : YCSnail
 * Date   : 2017-11-10
 * Email  : liyancai1986@163.com
 */
@Repository
class RubbishNewsRepo {

    @Autowired
    MongoHolder mongo


    def get(String newsId) {
        def collection = mongo.getCollection("rubbishNews")
        def obj = collection.findOne(toObj([newsId : newsId]))
        return obj != null ? RubbishNews.toObject(obj) : null
    }

    def getListBySimhash(String simhash) {
        def collection = mongo.getCollection("rubbishNews")
        def cursor = collection.find(toObj([simhash : simhash]))
        def result = []
        while (cursor.hasNext()) {
            def it = cursor.next()
            result << RubbishNews.toObject(it)
        }
        cursor.close()
        return result
    }

    def add(String newsId, def userIds, String simhash, def news) {
        def collection = mongo.getCollection("rubbishNews")
        collection.insert(toObj([
                _id         : UUID.randomUUID().toString(),
                newsId      : newsId,
                simhash     : simhash,
                submitter   : userIds,
                approved    : RubbishNews.RUBBISH_NEWS_WAITING_DEAL,
                createTime  : new Date(),
                updateTime  : new Date(),
                news        : news
        ]))
    }

    def update(String id, String newsId, def userIds) {
        def collection = mongo.getCollection("rubbishNews")
        collection.update(
                toObj([_id: id, newsId: newsId]),
                toObj(['$set': [
                        submitter   : userIds,
                        updateTime  : new Date()
                ]]), false, true
        )
    }
}
