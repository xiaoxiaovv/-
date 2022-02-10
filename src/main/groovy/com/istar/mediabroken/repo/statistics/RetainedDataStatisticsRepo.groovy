package com.istar.mediabroken.repo.statistics

import com.istar.mediabroken.entity.statistics.RetainedDataStatistics
import com.istar.mediabroken.utils.MongoHolder
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj
/**
 * @author  hanhui
 * @date  2018/4/8 9:31
 * @desc 
 **/
@Repository
@Slf4j
class RetainedDataStatisticsRepo {
    @Autowired
    MongoHolder mongo

    void addRetainedDataStatistics(RetainedDataStatistics retainedDataStatistics) {
        def collection = mongo.getCollection("retainedDataStatistics")
        collection.insert(toObj(retainedDataStatistics.toMap()))
    }

    List<RetainedDataStatistics> getRetainedDataStatistics(Date startTime, Date endTime) {
        def collection = mongo.getCollection("retainedDataStatistics")
        def cursor = collection.find(toObj("statisticTime": [$gte: startTime, $lt: endTime]))
        def result = []
        while (cursor.hasNext()) {
            def data = cursor.next()
            result << new RetainedDataStatistics(data)
        }
        return result
    }
}
