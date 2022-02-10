package com.istar.mediabroken.repo.evaluate

import com.istar.mediabroken.entity.evaluate.EvaluateChannel
import com.istar.mediabroken.utils.MongoHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * @author hanhui
 * @date 2018/6/20 10:07
 * @desc 测评相关
 * */

@Repository
class EvaluateRepo {
    @Autowired
    MongoHolder mongo


    EvaluateChannel getDailyEvaluateChannelByTime(int siteType, String siteDomain, Date startTime, Date endTime) {
        def collection = mongo.getCollection("dailyFourPower")
        def find = collection.find(toObj([siteType: siteType, siteDomain: siteDomain, time: [$gte: startTime, $lte: endTime]])).sort(toObj([createTime: -1]))
        if (find.hasNext()) {
            def evaluateChannel = find.next()
            return new EvaluateChannel(evaluateChannel)
        } else {
            return null
        }
    }

    EvaluateChannel getWeeklyEvaluateChannelByTime(int siteType, String siteDomain, Date startTime, Date endTime) {
        def collection = mongo.getCollection("weeklyFourPower")
        def find = collection.find(toObj([siteType: siteType, siteDomain: siteDomain, startTime: startTime])).sort(toObj([createTime: -1])).hint('siteDomain_1_siteType_1_startTime_1')
        if (find.hasNext()) {
            def evaluateChannel = find.next()
            return new EvaluateChannel(evaluateChannel)
        } else {
            return null
        }
    }

    EvaluateChannel getWeeklyEvaluateChannel(int siteType, String siteDomain, Date startTime, Date endTime) {
        def collection = mongo.getCollection("weeklyFourPower")
        def find = collection.findOne(toObj([siteType: siteType, siteDomain: siteDomain, startTime: [$gte: startTime], endTime: [$lte: endTime]]))
        return find

    }

    List<EvaluateChannel> getDailyEvaluateChannel(int siteType, String siteDomain, Date startTime, Date endTime) {
        def collection = mongo.getCollection("dailyFourPower")
        def find = collection.find(toObj([siteType: siteType, siteDomain: siteDomain, time: [$gte: startTime, $lte: endTime]]))
        def result = []
        while (find.hasNext()) {
            def evaluateChannel = find.next()
            result << new EvaluateChannel(evaluateChannel)
        }
        find.close()
        return result
    }
}
