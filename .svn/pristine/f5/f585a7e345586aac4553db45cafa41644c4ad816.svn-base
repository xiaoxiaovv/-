package com.istar.mediabroken.repo.statistics

import com.istar.mediabroken.entity.statistics.AccountDataStatistics
import com.istar.mediabroken.utils.MongoHolder
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * @author hanhui
 * @date 2018/4/2 15:40
 * */
@Repository
@Slf4j
class AccountDataStatisticsRepo {
    @Autowired
    MongoHolder mongo

    void addAccountDataStatistics(AccountDataStatistics accountDataStatistics) {
        def collection = mongo.getCollection("accountDataStatistics")
        collection.insert(toObj(accountDataStatistics.toMap()))
    }
}
