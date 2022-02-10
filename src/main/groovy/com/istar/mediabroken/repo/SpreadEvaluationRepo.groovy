package com.istar.mediabroken.repo

import com.istar.mediabroken.utils.MongoHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class SpreadEvaluationRepo {
    @Autowired
    MongoHolder mongo

    Map getPsiTrend(long userId) {
        null
    }
}
