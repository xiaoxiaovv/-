package com.istar.mediabroken.repo.evaluate

import com.istar.mediabroken.entity.evaluate.SiteInfoWeekly
import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.BasicDBObject
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * @author hanhui
 * @date 2018/6/25 10:23
 * @desc
 * */
@Repository
@Slf4j
class SiteInfoWeeklyRepo {
    @Autowired
    MongoHolder mongo

    Long getDistributeCount(int siteType, List siteDomain, Date startTime, Date endTime) {
        def distributeCount = 0
        def collection = mongo.getCollection("siteInfoWeekly")
        BasicDBObject query = new BasicDBObject("siteDomain", new BasicDBObject("\$in", siteDomain))
                .append("siteType", siteType)
                .append("startTime", startTime).append("endTime", endTime)
        BasicDBObject match = new BasicDBObject("\$match", query)
        BasicDBObject group = new BasicDBObject("\$group",
                new BasicDBObject("_id", null).append("distributeCount", new BasicDBObject("\$sum", "\$publishCount")))
        def aggregate = collection.aggregate(Arrays.asList(match, group))
        def list = aggregate.results()
        for (BasicDBObject dbObject : list) {
            distributeCount = dbObject.get("distributeCount") != null ? dbObject.get("distributeCount") : 0
        }
        return distributeCount
    }

    List<SiteInfoWeekly> getIndexRankBySite(Integer siteType, Date startTime, Date endTime, List siteDomain,
                                            String rankIndex, Integer pageNo, Integer pageSize) {
        def collection = mongo.getCollection("siteInfoWeekly")
        BasicDBObject sort = new BasicDBObject(rankIndex, -1)
        def cursor = collection.find(toObj([siteType  : siteType, startTime: startTime, endTime: endTime,
                                            siteDomain: ["\$in": siteDomain]])).sort(sort)
        if (pageNo && pageSize) {
            cursor.skip(pageSize * (pageNo - 1)).limit(pageSize)
        }
        def result = []
        while (cursor.hasNext()) {
            def it = cursor.next()
            result << new SiteInfoWeekly(it)
        }
        return result
    }

    SiteInfoWeekly getSiteInfoBySite(String siteDomain, Date startTime, Date endTime) {
        def collection = mongo.getCollection("siteInfoWeekly")
        def result = collection.findOne(toObj(["siteDomain": siteDomain, "startTime": startTime, "endTime": endTime]))
        return result
    }

    Map getAvgReadAndLike(List<String> siteDomain, Date startTime, Date endTime, Integer siteType) {
        Map resultMap = ["avgRead": 0, "avgLike": 0]
        def collection = mongo.getCollection("siteInfoWeekly")
        BasicDBObject query = new BasicDBObject("siteDomain", new BasicDBObject("\$in", siteDomain))
                .append("siteType", siteType)
                .append("startTime", startTime).append("endTime", endTime)
        BasicDBObject match = new BasicDBObject("\$match", query)
        BasicDBObject group = new BasicDBObject("\$group",
                new BasicDBObject("_id", null).append("avgRead", new BasicDBObject("\$avg", "\$avgRead"))
                        .append("avgLike", new BasicDBObject("\$avg", "\$avgLike")))
        def aggregate = collection.aggregate(Arrays.asList(match, group))
        def result = aggregate.results()
        result.each {
            resultMap.put("avgRead", it.get("avgRead") as int)
            resultMap.put("avgLike", it.get("avgLike") as int)
        }
        return resultMap
    }


    long getTotalCount(Integer siteType, Date startTime, Date endTime, List siteDomain) {
        long count = 0L
        def collection = mongo.getCollection("siteInfoWeekly")
        count = collection.count(toObj([siteType  : siteType, startTime: startTime, endTime: endTime,
                                             siteDomain: ["\$in": siteDomain]]))
        return count
    }
}
