package com.istar.mediabroken.repo.InformationStatistics

import com.istar.mediabroken.utils.MongoHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Created by zxj on   2018/3/16
 */
@Repository
class StatisticsNewsRepo {
    @Autowired
    MongoHolder mongo

    void addDataToInfluxes(Map map) {
        def collection = mongo.getCollection("statisticsNews")
        collection.insert(toObj([
                webCount       : map.webCount,
                weChatCount    : map.weChatCount,
                weiboCount     : map.weiboCount,
                amount         : map.amount,
                statisticalTime: map.statisticalTime,
                createTime     : new Date(),
                updateTime     : new Date()
        ]))
    }


    def getDataByTypeAndTime(int operationType, int pushType, Date starTime, Date endTime) {
        def collection = mongo.getCollection("newsOperation")
        def count = collection.count(toObj(
                [operationType: operationType,
                 pushType     : pushType,
                 createTime   : [$gte: starTime, $lte: endTime]
                ]
        ))
        return count
    }

    def getDataByTypeAndTimeAndNewsType(int operationType, int pushType, List newsType, Date starTime, Date endTime) {
        def collection = mongo.getCollection("newsOperation")
        def count = collection.count(toObj(
                [operationType  : operationType,
                 pushType       : pushType,
                 "news.newsType": [$in: newsType],
                 createTime     : [$gte: starTime, $lte: endTime]
                ]
        ))
        return count
    }

    void addDataToStatisticsOperation(Map map) {
        def collection = mongo.getCollection("statisticsOperation")
        def count = collection.insert(toObj(
                [webCount       : map.webCount,
                 weChatCount    : map.weChatCount,
                 weiBoCount     : map.weiBoCount,
                 articlePush    : map.articlePush,
                 articleSyc     : map.articleSyc,
                 amount         : map.amount,
                 statisticalTime: map.statisticalTime,
                 createTime     : new Date(),
                 updateTime     : new Date()
                ]
        ))
    }

    def getArticleSycSum(int operationType, int pushType, Date starTime, Date endTime) {
        def collection = mongo.getCollection("newsOperation")
        def aggregate = collection.aggregate(
                toObj([$match: [operationType: operationType,
                                pushType     : pushType,
                                createTime   : [$gte: starTime, $lte: endTime]]]),
                toObj([$group: [_id: "", count: [$sum: "\$shareChannelCount"]]]))
        if (aggregate.results().size() == 0) {
            return 0
        }
        return (long) aggregate.results().getAt(0).toMap().get("count")
    }
}

