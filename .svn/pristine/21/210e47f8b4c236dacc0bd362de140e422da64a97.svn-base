package com.istar.mediabroken.repo.evaluate

import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.entity.evaluate.EvaluateNews
import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.BasicDBObject
import com.mongodb.DBCursor
import com.mongodb.QueryBuilder
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * @author zxj
 * @create 2018/6/25
 */
@Repository
@Slf4j
class EvaluateNewsRepo {
    @Autowired
    MongoHolder mongo


    def getEvaluateNewsList(String siteName, String siteDomain, int siteType, Date star, Date end, String value, int pageSize, int pageNo) {
        def collection = mongo.getCollection("evaluateNews")
        QueryBuilder queryBuilder = QueryBuilder.start()
        if (siteType == Site.SITE_TYPE_WEBSITE) {
            queryBuilder.put("siteDomain").is(siteDomain)
        }
        if (siteType == Site.SITE_TYPE_WECHAT) {
            queryBuilder.put("siteDomain").is(siteName)
        }
        if (siteType == Site.SITE_TYPE_WEIBO) {
            queryBuilder.put("siteDomain").is(siteName)
        }
        queryBuilder.put("siteType").is(siteType)
        queryBuilder.put("publishTime").greaterThanEquals(star)
        queryBuilder.put("publishTime").lessThanEquals(end)
        QueryBuilder sort = QueryBuilder.start()

        if (value) {
            sort.put(value).is(-1)
        }
        sort.put("publishTime").is(-1)
        def cursor = collection.find(queryBuilder.get()).sort(sort.get()).skip(pageSize * (pageNo - 1)).limit(pageSize)
        def result = []
        while (cursor.hasNext()) {
            def site = cursor.next()
            result << new EvaluateNews(site)
        }
        cursor.close()
        return result
    }

    List<EvaluateNews> getNewsRankByChannel(List<String> webSite, List<String> weChat, List<String> weibo,
                                            Date startTime, Date endTime, String rankType) {
        def collection = mongo.getCollection("evaluateNews")
        DBCursor cursor
        def result = []
        BasicDBObject query = new BasicDBObject("publishTime", new BasicDBObject("\$gte", startTime).append("\$lt", endTime))
        query.append("\$or", [new BasicDBObject("siteType", Site.SITE_TYPE_WECHAT).append("siteDomain", new BasicDBObject("\$in", weChat)),
                              new BasicDBObject("siteType", Site.SITE_TYPE_WEIBO).append("siteDomain", new BasicDBObject("\$in", weibo)),
                              new BasicDBObject("siteType", Site.SITE_TYPE_WEBSITE).append("siteDomain", new BasicDBObject("\$in", webSite))])
        BasicDBObject sort = new BasicDBObject(rankType,-1)
        cursor = collection.find(query).sort(toObj(["reprintCount": -1])).limit(10).sort(sort)
        while (cursor.hasNext()) {
            def it = cursor.next()
            result << new EvaluateNews(it as Map)
        }
        return result
    }
}
