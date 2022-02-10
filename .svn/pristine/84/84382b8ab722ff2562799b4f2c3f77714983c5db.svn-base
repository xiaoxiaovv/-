package com.istar.mediabroken.repo

import com.istar.mediabroken.utils.MongoHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

@Repository
class SpreadEvluationRepo {
    @Autowired
    MongoHolder mongo

    void modifyAnalysisSites(Map analysisSites) {
        def collection = mongo.getCollection("analysisSites")
        def userId = analysisSites.userId
        analysisSites.remove('userId')
        collection.update(
                toObj([_id: userId]),
                toObj(['$set': analysisSites]),
                true,
                false)
    }

    Map getAnalysisSites(long userId) {
        def collection = mongo.getCollection("analysisSites")
        def res = collection.findOne(toObj([_id: userId]))
        return res
    }
}
