package com.istar.mediabroken.repo.statistics

import com.istar.mediabroken.entity.capture.NewsOperation
import com.istar.mediabroken.entity.statistics.OrgDataStatistics
import com.istar.mediabroken.entity.statistics.UserDataStatistics
import com.istar.mediabroken.utils.DateUitl
import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.AggregationOutput
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.QueryBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

@Repository
class StatisticsManageRepo {
    @Autowired
    MongoHolder mongo

    long getTodayPublishedCms(String orgId) {
        def collection = mongo.getCollection("newsOperation")
        Date dayBegin = DateUitl.getDayBegin()
        //operationType: 1 表示推送 ooperationType: 3 表示收藏 operationType: 4 表示word导入 operationType: 5 表示同步
        long cmsCount = collection.count(toObj([orgId: orgId, operationType: 1, createTime: [$gt: dayBegin]]))
        return cmsCount
    }

    List<NewsOperation> getTodayPublishedShareChannel(String orgId) {
        def result = []
        def collection = mongo.getCollection("newsOperation")
        Date dayBegin = DateUitl.getDayBegin()

        def cursor = collection.find(toObj([orgId: orgId, operationType: 5, createTime: [$gt: dayBegin]]))
        while (cursor.hasNext()) {
            def newsOperation = cursor.next()
            result << new NewsOperation(newsOperation)
        }
        cursor.close()
        return result
    }

    OrgDataStatistics getYesterdayPublished(String orgId) {
        def collection = mongo.getCollection("orgDataStatistics")
        Date dayEnd = DateUitl.getDayBegin()
        Date dayBegin = DateUitl.addDay(dayEnd, -(1))
        def result = collection.findOne(toObj([orgId: orgId, publishTime: [$gte: dayBegin, $lt: dayEnd]]))
        return result
    }

    def getTeamNameList(String orgId) {
        def collection = mongo.getCollection("newsOperation")
        List teamNameList = collection.distinct("teamName", toObj([orgId: orgId]))
        return teamNameList
    }

    List getPublishTrend(String orgId, int trendType) {
        def collection = mongo.getCollection("orgDataStatistics")
        Date dayEnd = DateUitl.getDayBegin()
        Date dayBegin = DateUitl.addDay(dayEnd, -(trendType))
        def result = []
        def cursor = collection.find(toObj([orgId: orgId, publishTime: [$gte: dayBegin, $lt: dayEnd]]))
        while (cursor.hasNext()) {
            def orgDataStatistics = cursor.next()
            result << new OrgDataStatistics(orgDataStatistics)
        }
        return result
    }

    List<NewsOperation> getPublishDetailInfo(String orgId, Date startDate, Date endDate, String channelType, String teamName, String publisher, int pageNo, int pageSize) {
        def result = []
        def collection = mongo.getCollection("newsOperation")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("orgId").is(orgId)
        queryBuilder.put("createTime").greaterThanEquals(startDate)
        queryBuilder.put("createTime").lessThanEquals(endDate)
        if (channelType) {
            if ("pushChannel".equals(channelType)) {//推送cms的
                queryBuilder.put("operationType").is(1)
            } else {
                queryBuilder.put(channelType).exists(toObj([$ne: 0]))//.notEquals(0)
                queryBuilder.put("operationType").is(5)
            }
        } else {
            queryBuilder.put("operationType").in([1, 5])
        }
        if (teamName) {
            queryBuilder.put("teamName").is(teamName)
        }
        if (publisher) {
            queryBuilder.put("publisher").regex(~/.*(?i)${publisher}.*/)
        }

        def cursor = collection.find(queryBuilder.get(), toObj(["news.title": 1, operationType: 1, teamName: 1, publisher: 1, createTime: 1, weiboChannel: 1, wechatChannel: 1, toutiaoChannel: 1, qqomChannel: 1]),)
                .sort(toObj([createTime: -1]))
                .skip(pageSize * (pageNo - 1)).limit(pageSize)
        while (cursor.hasNext()) {
            def newsOperation = cursor.next()
            result << new NewsOperation(newsOperation)
        }
        cursor.close()
        return result
    }

    def getTeamIds(String teamName, String orgId){
        def result = []
        def collection = mongo.getCollection("userDataStatistics")
        def cursor = collection.find(toObj([teamName: teamName, orgId: orgId]))
        while (cursor.hasNext()) {
            def userDataStatistics = cursor.next()
            if (!result.contains(userDataStatistics.teamId)){
                result << userDataStatistics.teamId
            }
        }
        cursor.close()
        return result
    }

    Map getPublishStatistics(String orgId, Date startDate, Date endDate, String teamName, def teamIds, String publisher) {
        def collection = mongo.getCollection("userDataStatistics")
        BasicDBObject query = new BasicDBObject(toObj([orgId: orgId]))
                .append("publishTime", new BasicDBObject("\$gte", startDate).append("\$lt", endDate))
        if (teamName && !teamIds) {
            query.append("teamName", teamName)
        }
        if (!teamName && teamIds) {
            query.append("teamId", new BasicDBObject("\$in", teamIds))
        }
        if (teamName && teamIds) {
            query.append('$or',[[teamName:teamName], [teamId:['$in': teamIds]]]
            )
        }
        if (publisher) {
            query.append("publisher", new BasicDBObject("\$regex", ~/.*(?i)${publisher}.*/))
        }
        BasicDBObject match = new BasicDBObject("\$match", query)
        BasicDBObject groupObj = new BasicDBObject("_id", new BasicDBObject("teamId", "\$teamId"))
                .append("manualPushCount", new BasicDBObject("\$sum", "\$manualPushCount"))
                .append("autoPushCount", new BasicDBObject("\$sum", "\$autoPushCount"))
                .append("weiboCount", new BasicDBObject("\$sum", "\$weiboCount"))
                .append("wechatCount", new BasicDBObject("\$sum", "\$wechatCount"))
                .append("toutiaoCount", new BasicDBObject("\$sum", "\$toutiaoCount"))
                .append("qqomCount", new BasicDBObject("\$sum", "\$qqomCount"))
        BasicDBObject group = new BasicDBObject("\$group", groupObj)
        def aggregate = collection.aggregate(Arrays.asList(match, group))
        def list = aggregate.results()
        Map userStatistics = list.groupBy { it._id.teamId }
        return userStatistics
    }

    Map getUserPublishStatistics(String orgId, String teamId, Date startDate, Date endDate, String publisher) {
        def collection = mongo.getCollection("userDataStatistics")

        BasicDBObject query = new BasicDBObject(toObj([orgId: orgId, teamId: teamId]))
                .append("publishTime", new BasicDBObject("\$gte", startDate).append("\$lt", endDate))
        if (publisher) {
            query.append("publisher", new BasicDBObject("\$regex", ~/.*(?i)${publisher}.*/))
        }
        BasicDBObject match = new BasicDBObject("\$match", query)
        BasicDBObject groupObj = new BasicDBObject("_id", new BasicDBObject("userId", "\$userId"))
                .append("manualPushCount", new BasicDBObject("\$sum", "\$manualPushCount"))
                .append("autoPushCount", new BasicDBObject("\$sum", "\$autoPushCount"))
                .append("weiboCount", new BasicDBObject("\$sum", "\$weiboCount"))
                .append("wechatCount", new BasicDBObject("\$sum", "\$wechatCount"))
                .append("toutiaoCount", new BasicDBObject("\$sum", "\$toutiaoCount"))
                .append("qqomCount", new BasicDBObject("\$sum", "\$qqomCount"))
        BasicDBObject group = new BasicDBObject("\$group", groupObj)
        def aggregate = collection.aggregate(Arrays.asList(match, group))
        def list = aggregate.results()
        Map userStatistics = list.groupBy { it._id.userId }
        return userStatistics
    }

    String getTeamNameFromStatistics(String orgId, String teamId, Date startDate, Date endDate) {
        String teamName = ""
        def collection = mongo.getCollection("userDataStatistics")
        def cursor = collection.find(toObj([orgId: orgId, teamId: teamId, publishTime: [$gte: startDate, $lt: endDate]]))
                .sort(toObj([publishTime: -1]))
                .limit(1)
        UserDataStatistics userDataStatistics = new UserDataStatistics()
        while (cursor.hasNext()) {
            def map = cursor.next()
            userDataStatistics = new UserDataStatistics(map)
            teamName = userDataStatistics.teamName
            break
        }
        cursor.close()
        return teamName
    }
}
