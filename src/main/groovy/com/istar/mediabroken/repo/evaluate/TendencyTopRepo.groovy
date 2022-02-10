package com.istar.mediabroken.repo.evaluate

import com.istar.mediabroken.entity.evaluate.TendencyTop
import com.istar.mediabroken.utils.MongoHolder
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * @author hanhui
 * @date 2018/6/25 10:23
 * @desc 趋势统计
 * */
@Repository
@Slf4j
class TendencyTopRepo {
    @Autowired
    MongoHolder mongo

    TendencyTop findTendencyTop(String evaluateId) {
        def collection = mongo.getCollection("tendencyTop")
        def result = collection.findOne(toObj(["evaluateId": evaluateId]))
        return result
    }

    void addTendencyTop(TendencyTop tendencyTop) {
        def collection = mongo.getCollection("tendencyTop")
        def result = collection.insert(toObj([
                _id         : UUID.randomUUID().toString(),
                evaluateId  : tendencyTop.evaluateId,
                publishCount: tendencyTop.publishCount,
                multiple    : tendencyTop.multiple,
                psi         : tendencyTop.psi,
                mii         : tendencyTop.mii,
                tsi         : tendencyTop.tsi,
                bsi         : tendencyTop.bsi,
                createTime  : new Date(),
                updateTime  : new Date()
        ]))
    }

    void updateTendencyTop(TendencyTop tendencyTop) {
        def collection = mongo.getCollection("tendencyTop")
        def result = collection.update(
                toObj([evaluateId: tendencyTop.evaluateId]),
                toObj(['$set': [
                        publishCount: tendencyTop.publishCount,
                        multiple    : tendencyTop.multiple,
                        psi         : tendencyTop.psi,
                        mii         : tendencyTop.mii,
                        bsi         : tendencyTop.bsi,
                        tsi         : tendencyTop.tsi,
                        updateTime  : new Date()
                ]])
        )
    }
}
