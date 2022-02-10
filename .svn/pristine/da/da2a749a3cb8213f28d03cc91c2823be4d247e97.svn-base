package com.istar.mediabroken.repo.statistics

import com.istar.mediabroken.entity.statistics.OrgDataStatistics
import com.istar.mediabroken.entity.statistics.UserDataStatistics
import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.DBObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj
/**
 * author:hh
 * date:2018/3/15
 */
@Repository
class DataStatisticsRepo {
    @Autowired
    MongoHolder mongo

    void addUserDataStatistics(UserDataStatistics userDataStatistics) {
        def collection = mongo.getCollection("userDataStatistics")
        collection.insert(toObj(userDataStatistics.toMap()))
    }

    void addUserDataStatistics(List<UserDataStatistics> userDataStatisticsList) {
        def collection = mongo.getCollection("userDataStatistics")
        List<DBObject> insertDataList = new ArrayList<DBObject>()
        userDataStatisticsList.each { userDataStatistics ->
            insertDataList.add(toObj(userDataStatistics.toMap()))
        }
        collection.insert(insertDataList)
    }

    void addOrgDataStatistics(List<OrgDataStatistics> orgDataStatisticsList) {
        def collection = mongo.getCollection("orgDataStatistics")
        List<DBObject> insertDataList = new ArrayList<DBObject>()
        orgDataStatisticsList.each { orgDataStatistics ->
            insertDataList.add(toObj(orgDataStatistics.toMap()))
        }
        collection.insert(insertDataList)
    }


}
