package com.istar.mediabroken.repo.evaluate

import com.istar.mediabroken.entity.evaluate.ChannelSummary
import com.istar.mediabroken.utils.MongoHolder
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
class ChannelSummaryRepo {
    @Autowired
    MongoHolder mongo

    ChannelSummary getEvaluateInfoByEvaId(String evaluateId) {
        def collection = mongo.getCollection("channelSummary")
        def result = collection.findOne(toObj([evaluateId: evaluateId]))
        return result
    }

    void addEvaluateInfo(ChannelSummary channelSummary) {
        def collection = mongo.getCollection("channelSummary")
        def result = collection.insert(toObj([
                _id         : UUID.randomUUID().toString(),
                evaluateId  : channelSummary.evaluateId,
                evaluateName: channelSummary.evaluateName,
                channelCount: channelSummary.channelCount,
                channelsName: channelSummary.channelsName,
                startTime   : channelSummary.startTime,
                endTime     : channelSummary.endTime,
                createTime  : new Date(),
                updateTime  : new Date()
        ]))
    }

    void updateEvaluateInfo(ChannelSummary channelSummary) {
        def collection = mongo.getCollection("channelSummary")
        def result = collection.update(
                toObj([evaluateId: channelSummary.evaluateId]),
                toObj(['$set': [
                        articleCount: channelSummary.articleCount,
                        multiple    : channelSummary.multiple,
                        psi         : channelSummary.psi,
                        mii         : channelSummary.mii,
                        bsi         : channelSummary.bsi,
                        tsi         : channelSummary.tsi,
                        multipleRate: channelSummary.multipleRate,
                        bsiRate     : channelSummary.bsiRate,
                        tsiRate     : channelSummary.tsiRate,
                        miiRate     : channelSummary.miiRate,
                        psiRate     : channelSummary.psiRate,
                        updateTime  : new Date()
                ]])
        )
    }

    Integer removeEvaluateInfo(String evaluateId) {
        def collection = mongo.getCollection("channelSummary")
        def result = collection.remove(toObj(["evaluateId": evaluateId]))
        return result.getN()
    }
}
