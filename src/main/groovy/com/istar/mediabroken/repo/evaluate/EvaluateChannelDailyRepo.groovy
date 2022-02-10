package com.istar.mediabroken.repo.evaluate

import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.entity.evaluate.EvaluateChannelDetail
import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.BasicDBObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * @author hanhui
 * @date 2018/6/20 10:07
 * @desc
 * */

@Repository
class EvaluateChannelDailyRepo {
    @Autowired
    MongoHolder mongo

    List<EvaluateChannelDetail> getIndexTop(String evaluateId, String rankIndex) {
        def collection = mongo.getCollection("evaluateChannelDaily")
        BasicDBObject query = new BasicDBObject("evaluateId", evaluateId)
        BasicDBObject sort = new BasicDBObject("\$sort", new BasicDBObject("indexCount", -1))
        BasicDBObject limit = new BasicDBObject("\$limit", 10)
        BasicDBObject match = new BasicDBObject("\$match", query)
        BasicDBObject group = new BasicDBObject("\$group",
                new BasicDBObject("_id", new BasicDBObject("siteDomain", "\$siteDomain").append("siteName", "\$siteName")
                        .append("siteType", "\$siteType")).append("indexCount", new BasicDBObject("\$sum", "\$" + rankIndex + "")))
        def aggregate = collection.aggregate(Arrays.asList(match, group, sort, limit))
        def list = aggregate.results()
        def result = []
        for (BasicDBObject dbObject : list) {
            result << new EvaluateChannelDetail((dbObject.get("_id")) as Map)
        }
        return result
    }

    List<EvaluateChannelDetail> getDetailByDomain(Integer siteType, String evaluateId,
                                                  String siteDomain, String siteName) {
        def collection = mongo.getCollection("evaluateChannelDaily")
        BasicDBObject query = new BasicDBObject("siteType", siteType).append("evaluateId",evaluateId)
        if (siteType == Site.SITE_TYPE_WEBSITE) {
            query.append("siteDomain", siteDomain)
        } else if (siteType in [Site.SITE_TYPE_WECHAT, Site.SITE_TYPE_WEIBO]) {
            query.append("siteName", siteName)
        }
        def cursor = collection.find(query).sort(toObj(["time":-1]))
        def result = []
        while (cursor.hasNext()) {
            def detail = cursor.next()
            result << new EvaluateChannelDetail(detail as Map)
        }
        return result
    }
}
