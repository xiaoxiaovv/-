package com.istar.mediabroken.repo.evaluate

import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.BasicDBObject
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * @author hanhui
 * @date 2018/6/29 16:12
 * @desc 词云
 * */
@Repository
@Slf4j
class KeywordsWeeklyRepo {
    @Autowired
    MongoHolder mongo

    List getKeywordsWeeklyBySite(List<String> webSite, List<String> weChat, List<String> weibo, Date startTime, Date endTime) {
        def collection = mongo.getCollection("keywordsWeekly")
        def keywordList = []
        def webSiteArray = []
        if (weChat) {
            webSiteArray.add(new BasicDBObject("siteType", Site.SITE_TYPE_WECHAT).append("siteDomain", new BasicDBObject("\$in", weChat)))
        }
        if (weibo) {
            webSiteArray.add(new BasicDBObject("siteType", Site.SITE_TYPE_WEIBO).append("siteDomain", new BasicDBObject("\$in", weibo)))
        }
        if (webSite) {
            webSiteArray.add(new BasicDBObject("siteType", Site.SITE_TYPE_WEBSITE).append("siteDomain", new BasicDBObject("\$in", webSite)))
        }
        BasicDBObject match = new BasicDBObject("\$match", new BasicDBObject("startTime", startTime)
                .append("endTime", endTime).append("\$or", webSiteArray))
        BasicDBObject unwind = new BasicDBObject("\$unwind", "\$wordCloud")
        BasicDBObject group = new BasicDBObject("\$group", new BasicDBObject("_id", "\$wordCloud.word")
                .append("count", new BasicDBObject("\$sum", "\$wordCloud.count")))
        BasicDBObject sort = new BasicDBObject("\$sort", new BasicDBObject("count", -1))
        BasicDBObject limit = new BasicDBObject("\$limit", 30)
        def keywords = collection.aggregate(Arrays.asList(match, unwind, group, sort, limit))
        for (BasicDBObject dbObject : keywords.results()) {
            keywordList << ["word": dbObject._id, "count": dbObject.count]
        }
        return keywordList
    }
}
