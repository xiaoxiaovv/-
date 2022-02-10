package com.istar.mediabroken.repo.evaluate

import com.istar.mediabroken.entity.evaluate.EvaluateChannelDetail
import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
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
class EvaluateChannelDetailRepo {
    @Autowired
    MongoHolder mongo

    //获取用户渠道四力指数和
    EvaluateChannelDetail getEvaluateIndexById(String evaluateId) {
        EvaluateChannelDetail evaluateChannelDetail = new EvaluateChannelDetail()
        def collection = mongo.getCollection("evaluateChannelDetail")
        BasicDBObject query = new BasicDBObject("evaluateId", evaluateId)
        BasicDBObject match = new BasicDBObject("\$match", query)
        BasicDBObject group = new BasicDBObject("\$group",
                new BasicDBObject("_id", null).append("psi", new BasicDBObject("\$sum", "\$psi"))
                        .append("multiple", new BasicDBObject("\$sum", "\$multiple"))
                        .append("mii", new BasicDBObject("\$sum", "\$mii"))
                        .append("tsi", new BasicDBObject("\$sum", "\$tsi"))
                        .append("bsi", new BasicDBObject("\$sum", "\$bsi"))
                        .append("channelCount", new BasicDBObject("\$sum", 1)))
        def aggregate = collection.aggregate(Arrays.asList(match, group))
        def list = aggregate.results()
        for (DBObject dbObject : list) {
            evaluateChannelDetail.multiple = dbObject.get("multiple") as double
            evaluateChannelDetail.psi = dbObject.get("psi") as double ?: 0
            evaluateChannelDetail.mii = dbObject.get("mii") as double ?: 0
            evaluateChannelDetail.tsi = dbObject.get("tsi") as double ?: 0
            evaluateChannelDetail.bsi = dbObject.get("bsi") as double ?: 0
            evaluateChannelDetail.channelCount = dbObject.get("channelCount") as long ?: 1
        }
        return evaluateChannelDetail
    }

    List<EvaluateChannelDetail> getIndexRankByEveId(Integer siteType, String evaluateId, String rankIndex,
                                                    Integer pageNo, Integer pageSize) {
        def collection = mongo.getCollection("evaluateChannelDetail")
        BasicDBObject sort = new BasicDBObject(rankIndex, -1)
        def cursor = collection.find(toObj([siteType: siteType, evaluateId: evaluateId])).sort(sort)
                .skip(pageSize * (pageNo - 1)).limit(pageSize)
        def result = []
        while (cursor.hasNext()) {
            def channelDetail = cursor.next()
            result << new EvaluateChannelDetail(channelDetail as Map)
        }
        return result
    }

    long getTotalCount(Integer siteType, String evaluateId) {
        long count = 0L
        def collection = mongo.getCollection("evaluateChannelDetail")
        count = collection.count(toObj([siteType: siteType, evaluateId: evaluateId]))
        return count
    }
}
