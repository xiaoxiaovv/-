package com.istar.mediabroken.repo.evaluate

import com.istar.mediabroken.entity.evaluate.EvaluateChannel
import com.istar.mediabroken.utils.MongoHolder
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * @author zxj
 * @create 2018/6/26
 */
@Repository
@Slf4j
class SiteInfoDailyRepo {
    @Autowired
    MongoHolder mongo
    List<EvaluateChannel> getSiteInfoDailyList(int siteType, String siteDomain, Date startTime, Date endTime) {
        def collection = mongo.getCollection("siteInfoDaily")
        def find = collection.find(toObj([siteType: siteType, siteDomain: siteDomain, time: [$gte: startTime, $lte: endTime]]))
        def result = []
        while (find.hasNext()) {
            def evaluateChannel = find.next()
            result << new EvaluateChannel(evaluateChannel)
        }
        find.close()
        return result
    }

    EvaluateChannel getSiteInfoDailyEvaluateChannel(int siteType, String siteDomain, Date startTime, Date endTime) {
        def collection = mongo.getCollection("siteInfoDaily")
        def find = collection.findOne(toObj([siteType: siteType, siteDomain: siteDomain, time: [$gte: startTime, $lte: endTime]]))
        return find
    }
}
