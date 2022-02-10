package com.istar.mediabroken.repo.evaluate

import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.entity.evaluate.EvaluateChannel
import com.istar.mediabroken.entity.evaluate.EvaluateChannelDetail
import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.QueryBuilder
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import java.util.regex.Pattern

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * @author zxj
 * @create 2018/6/20
 */
@Repository
@Slf4j
class EvaluateChannelRepo {

    @Autowired
    MongoHolder mongo


    def addChannelForStat(String siteName, String siteDomain, int siteType, int statFlag) {
        def collection = mongo.getCollection("channelForStat")
        collection.insert(toObj(
                [_id       : UUID.randomUUID().toString(),
                 siteName  : siteName,
                 siteDomain: siteDomain,
                 siteType  : siteType,
                 statFlag  : statFlag,
                 updateTime: new Date(),
                 createTime: new Date()
                ]))
    }

    def getChannelForStat(String siteName, String siteDomain, int siteType, int statFlag) {
        def collection = mongo.getCollection("channelForStat")
        return collection.findOne(toObj([siteName: siteName, siteDomain: siteDomain, siteType: siteType, statFlag: statFlag]))

    }

    List getUserAllChannel(long userId) {
        def collection = mongo.getCollection("evaluateChannel")
        def find = collection.find(toObj([userId: userId])).sort(toObj([createTime: -1]))
        def result = []
        while (find.hasNext()) {
            def evaluateChannel = find.next()
            result << new EvaluateChannel(evaluateChannel)
        }
        find.close()
        return result

    }
    def modifyChannelTeamId(long userId, String teamId, List channelIds) {
        def collection = mongo.getCollection("evaluateChannel")
        collection.update(toObj([_id: ['$in': channelIds], userId: userId]), toObj(['$set': [evaluateTeamId: teamId]]), false, true)
    }

    def modifyChannel(long userId, String channelId, int siteType, String siteName, String siteDomain, String evaluateTeamId) {
        def collection = mongo.getCollection("evaluateChannel")
        def update = collection.update(toObj([_id: channelId, userId: userId]), toObj([$set: [siteType: siteType, siteName: siteName, siteDomain: siteDomain, evaluateTeamId: evaluateTeamId]]))
        return update.updateOfExisting
    }

    def delChannelForStatById(int type, String siteName, String siteDomain, List list) {
        def collection = mongo.getCollection("channelForStat")
        def remove = collection.remove(toObj([siteType: type, siteName: siteName, siteDomain: siteDomain, statFlag: [$in: list]]))
        return remove.updateOfExisting
    }

    def delChannelById(long userId, String channelId) {
        def collection = mongo.getCollection("evaluateChannel")
        def remove = collection.remove(toObj([_id: channelId, userId: userId]))
        return remove.updateOfExisting
    }

    List getChannelListByTypeAndName(int type, String siteName, String siteDomain) {
        def collection = mongo.getCollection("evaluateChannel")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("siteType").is(type)
        if (type == Site.SITE_TYPE_WEBSITE) {
            queryBuilder.put("siteDomain").is(siteDomain)
        }
        if (type == Site.SITE_TYPE_WECHAT || type == Site.SITE_TYPE_WEIBO) {
            queryBuilder.put("siteName").is(siteName)
        }
        def find = collection.find(queryBuilder.get()).sort(toObj([createTime: -1]))
        def result = []
        while (find.hasNext()) {
            def evaluateChannel = find.next()
            result << new EvaluateChannel(evaluateChannel)
        }
        find.close()
        return result

    }

    def addEvaluateChannel(EvaluateChannel evaluateChannel) {
        def collection = mongo.getCollection("evaluateChannel")
        def insert = collection.insert(toObj([
                _id           : evaluateChannel.id,
                userId        : evaluateChannel.userId,
                siteName      : evaluateChannel.siteName,
                siteType      : evaluateChannel.siteType,
                siteDomain    : evaluateChannel.siteDomain,
                evaluateTeamId: evaluateChannel.evaluateTeamId,
                createTime    : evaluateChannel.createTime,
                updateTime    : evaluateChannel.updateTime
        ]))
        return evaluateChannel.id
    }

    boolean modifyStatFlag(long userId, List channelIds) {
        def collection = mongo.getCollection("evaluateChannel")
        collection.update(
                toObj([
                        userId: userId, _id: [$in: channelIds]
                ]),
                toObj(['$set': [
                        tatFlag: false,
                ]]))
        return true
    }

    List findChannelByTypeAndName(long userId, int type, String name, String teamId) {
        def collection = mongo.getCollection("evaluateChannel")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("userId").is(userId)
        if (type == Site.SITE_TYPE_WEBSITE || type == Site.SITE_TYPE_WECHAT || type == Site.SITE_TYPE_WEIBO) {
            queryBuilder.put("siteType").is(type)
        }
        if (StringUtils.isNoneBlank(name)) {
            Pattern pattern = Pattern.compile(".*?" + name + ".*")
            queryBuilder.put("siteName").regex(pattern)
        }
        if (teamId) {
            queryBuilder.put("evaluateTeamId").is(teamId)
        }
        QueryBuilder filed = QueryBuilder.start()
        filed.put("_id").is(1)
        filed.put("userId").is(1)
        filed.put("siteName").is(1)
        filed.put("siteDomain").is(1)
        filed.put("siteType").is(1)
        filed.put("evaluateTeamId").is(1)
        filed.put("updateTime").is(1)
        filed.put("createTime").is(1)
        def find = collection.find(queryBuilder.get(), filed.get()).sort(toObj([createTime: -1]))
        def result = []
        while (find.hasNext()) {
            def evaluateChannel = find.next()
            result << new EvaluateChannel(evaluateChannel)
        }
        find.close()
        return result

    }

    EvaluateChannel findById(long userId, String id) {
        def collection = mongo.getCollection("evaluateChannel")
        def one = collection.findOne(toObj([_id: id, userId: userId]))
        return one
    }

    List findByTeamId(long userId, String teamId) {
        def collection = mongo.getCollection("evaluateChannel")
        def find = collection.find(toObj([evaluateTeamId: teamId, userId: userId]))
        def result = []
        while (find.hasNext()) {
            def evaluateChannel = find.next()
            result << new EvaluateChannel(evaluateChannel)
        }
        find.close()
        return result
    }

    EvaluateChannel findChannel(long userId, int siteType, String siteName, String siteDomain, String teamId) {
        def collection = mongo.getCollection("evaluateChannel")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("userId").is(userId)
        queryBuilder.put("siteType").is(siteType)
        if (teamId) {
            queryBuilder.put("evaluateTeamId").is(teamId)
        }
        if (siteType == Site.SITE_TYPE_WEBSITE) {
            queryBuilder.put("siteDomain").is(siteDomain)
        }
        if (siteType == Site.SITE_TYPE_WECHAT || siteType == Site.SITE_TYPE_WEIBO) {
            queryBuilder.put("siteName").is(siteName)
        }
        def cursor = collection.findOne(queryBuilder.get())
        return cursor
    }

    List<EvaluateChannel> findByUserId(Long userId, Integer siteType) {
        def collection = mongo.getCollection("evaluateChannel")
        BasicDBObject query = new BasicDBObject("userId", userId)
        if (siteType) {
            query.append("siteType", siteType)
        }
        def cursor = collection.find(query)
        def result = []
        while (cursor.hasNext()) {
            def it = cursor.next()
            result << new EvaluateChannel(it)
        }
        return result
    }

    boolean setChannelMultipleWeight(long userId, String channelId, def weight) {
        def collection = mongo.getCollection("evaluateChannel")
        def update = collection.update(toObj([_id: channelId, userId: userId]), toObj(['$set': [multipleWeight: weight]]), true, true)
        update.updateOfExisting
    }

    //设置传播力参
    boolean setChannelPsiWeight(long userId, String channelId, def weight) {
        def collection = mongo.getCollection("evaluateChannel")
        def update = collection.update(toObj([_id: channelId, userId: userId]), toObj(['$set': [psiWeight: weight]]), true, true)
        update.updateOfExisting
    }
    //设置影响力参
    boolean setChannelMiiWeight(long userId, String channelId, def weight) {
        def collection = mongo.getCollection("evaluateChannel")
        def update = collection.update(toObj([_id: channelId, userId: userId]), toObj(['$set': [miiWeight: weight]]), true, true)
        update.updateOfExisting
    }
    //设置公信力参
    boolean setChannelTsiWeight(long userId, String channelId, def weight) {
        def collection = mongo.getCollection("evaluateChannel")
        def update = collection.update(toObj([_id: channelId, userId: userId]), toObj(['$set': [tsiWeight: weight]]), true, true)
        update.updateOfExisting
    }
    //设置引导力参
    boolean setChannelBsiWeight(long userId, String channelId, def weight) {
        def collection = mongo.getCollection("evaluateChannel")
        def update = collection.update(toObj([_id: channelId, userId: userId]), toObj(['$set': [bsiWeight: weight]]), true, true)
        update.updateOfExisting
    }

    boolean updateEvaluateChannelDetail(String id, long userId,
                                        double psi, double mii, double tsi, double bsi, double multiple,
                                        double psiRate, double miiRate, double tsiRate, double bsiRate, double multipleRate) {
        def collection = mongo.getCollection("evaluateChannelDetail")
        def update = collection.update(toObj([_id: id, userId: userId]),
                toObj(['$set': [
                        psi         : psi,
                        mii         : mii,
                        tsi         : tsi,
                        bsi         : bsi,
                        multiple    : multiple,
                        psiRate     : psiRate,
                        miiRate     : miiRate,
                        tsiRate     : tsiRate,
                        bsiRate     : bsiRate,
                        multipleRate: multipleRate,
                        updateTime  : new Date()
                ]])
        )
        return update.updateOfExisting
    }

    boolean updateEvaluateChannelDetailFourPowerRate(String id, long userId,
                                                     double psiRate, double miiRate, double tsiRate, double bsiRate, double multipleRate) {
        def collection = mongo.getCollection("evaluateChannelDetail")
        def update = collection.update(toObj([_id: id, userId: userId]),
                toObj(['$set': [
                        psiRate     : psiRate,
                        miiRate     : miiRate,
                        tsiRate     : tsiRate,
                        bsiRate     : bsiRate,
                        multipleRate: multipleRate,
                        updateTime  : new Date()
                ]])
        )
        return update.updateOfExisting
    }
    boolean modifyEvaluateChannelDetail(String id, long userId,
                                        double psi, double mii, double tsi, double bsi, double multiple,
                                                     double psiRate, double miiRate, double tsiRate, double bsiRate, double multipleRate) {
        def collection = mongo.getCollection("evaluateChannelDetail")
        def update = collection.update(toObj([_id: id, userId: userId]),
                toObj(['$set': [
                        psi         : psi,
                        mii         : mii,
                        tsi         : tsi,
                        bsi         : bsi,
                        multiple    : multiple,
                        psiRate     : psiRate,
                        miiRate     : miiRate,
                        tsiRate     : tsiRate,
                        bsiRate     : bsiRate,
                        multipleRate: multipleRate,
                        updateTime  : new Date()
                ]])
        )
        return update.updateOfExisting
    }


    EvaluateChannelDetail getEvaluateChannelDetail(long userId, String evaluateId, String channelId, Date startTime, Date endTime) {
        def collection = mongo.getCollection("evaluateChannelDetail")
        def find = collection.findOne(toObj([userId: userId, evaluateId: evaluateId, channelId: channelId, startTime: [$gte: startTime], endTime: [$lte: endTime]]))
        return find
    }

    EvaluateChannelDetail findEvaluateChannelDetail(long userId, String channelId, Date startTime, Date endTime) {
        def collection = mongo.getCollection("evaluateChannelDetail")
        def find = collection.findOne(toObj([userId: userId, channelId: channelId, startTime: [$gte: startTime], endTime: [$lte: endTime]]))
        return find
    }

    //添加一周四力和综合指数
    def addEvaluateChannelDetail(String id, long userId, String evaluateId, String channelId,
                                 def count, String siteDomain, int siteType, String siteName,
                                 String evaluateName, Date startTime, Date endTime,
                                 double psi, double mii, double tsi, double bsi, double multiple,
                                 double psiRate, double miiRate, double tsiRate, double bsiRate, double multipleRate) {
        def collection = mongo.getCollection("evaluateChannelDetail")
        def insert = collection.insert(toObj([
                _id         : id,
                userId      : userId,
                evaluateId  : evaluateId,
                channelId   : channelId,
                contentCount: count,
                siteDomain  : siteDomain,
                siteType    : siteType,
                siteName    : siteName,
                evaluateName: evaluateName,
                startTime   : startTime,
                endTime     : endTime,
                psi         : psi,
                mii         : mii,
                tsi         : tsi,
                bsi         : bsi,
                multiple    : multiple,
                psiRate     : psiRate,
                miiRate     : miiRate,
                tsiRate     : tsiRate,
                bsiRate     : bsiRate,
                multipleRate: multipleRate,
                updateTime  : new Date(),
                createTime  : new Date()
        ]))
        return insert.updateOfExisting
    }


    List getEvaluateChannelDetailList(long userId, String evaluateId, String channelId, Date startTime, Date endTime) {
        def collection = mongo.getCollection("evaluateChannelDaily")
        def find = collection.find(toObj([userId: userId, evaluateId: evaluateId, channelId: channelId, time: [$gte: startTime, $lte: endTime]]))
        def result = []
        while (find.hasNext()) {
            def evaluateChannel = find.next()
            result << new EvaluateChannelDetail(evaluateChannel)
        }
        find.close()
        return result
    }

    def getEvaluateChannelDaily(long userId, String evaluateId, String channelId, Date startTime, Date endTime) {
        def collection = mongo.getCollection("evaluateChannelDaily")
        def find = collection.findOne(toObj([userId: userId, evaluateId: evaluateId, channelId: channelId, time: [$gte: startTime, $lte: endTime]]))

        return find
    }


    boolean updateEvaluateChannelDaily(String id, long userId,
                                       double psi, double mii, double tsi, double bsi, double multiple,
                                       long publishCount, long sumReprint, double avgReprint, long sumRead, double avgRead, long sumLike, double avgLike) {
        def collection = mongo.getCollection("evaluateChannelDaily")
        def update = collection.update(toObj([_id: id, userId: userId]),
                toObj(['$set': [
                        psi         : psi,
                        mii         : mii,
                        tsi         : tsi,
                        bsi         : bsi,
                        multiple    : multiple,
                        publishCount: publishCount,
                        sumReprint  : sumReprint,
                        avgReprint  : avgReprint,
                        sumRead     : sumRead,
                        avgRead     : avgRead,
                        sumLike     : sumLike,
                        avgLike     : avgLike,
                        updateTime  : new Date()
                ]])
        )
        return update.updateOfExisting
    }

    //微信微博的siteName存在了siteDomain里面
//    List getWeeklyWordCloud(String siteDomain,int siteType, Date startTime,Date endTime){
//        def keywordList = []
//        if (!siteDomain) {
//            return null
//        }
//        def collection = mongo.getCollection("keywordsDaily")
//        QueryBuilder queryBuilder = QueryBuilder.start()
//        def querys = new ArrayList<DBObject>()
//        QueryBuilder qb = QueryBuilder.start()
//        qb.put("siteType").is(siteType)
//        qb.put("siteDomain").is(siteDomain)
//        querys.add(qb.get())
//        BasicDBObject match = new BasicDBObject("\$match", queryBuilder.get())
//        QueryBuilder timeFilter = QueryBuilder.start()
//        timeFilter.put("time").greaterThanEquals(startTime).lessThan(endTime)
//        BasicDBObject filter = new BasicDBObject("\$match", timeFilter.get())
//        def project = new BasicDBObject("\$project", new BasicDBObject("wordCloud", 1).append("time", 1).append("_id", 0))
//        def unwind = new BasicDBObject("\$unwind", "\$wordCloud")
//        BasicDBObject group = new BasicDBObject("\$group", new BasicDBObject("_id", "\$wordCloud.word").
//                append("count", new BasicDBObject("\$sum", "\$wordCloud.count")))
//        def sort = new BasicDBObject("\$sort", new BasicDBObject("count", -1))
//        def limit = new BasicDBObject("\$limit", 30)
//        def siteWord = collection.aggregate(Arrays.asList(match, project, unwind, group, sort, limit))
//        for (BasicDBObject dbObject : siteWord.results()) {
//            keywordList << ["word": dbObject._id, "count": dbObject.count]
//        }
//        return keywordList
//    }

    List getAllEvaluateChannel() {
        def collection = mongo.getCollection("evaluateChannel")
        def find = collection.find(toObj([siteType: [$in: [1, 2, 3]]]))
        def result = []
        while (find.hasNext()) {
            def evaluateChannel = find.next()
            result << new EvaluateChannel(evaluateChannel)
        }
        find.close()
        return result
    }

    //添加一周四力和综合指数
    def addEvaluateChannelDaily(long userId, String evaluateId, String channelId,
                                String siteDomain, int siteType, String siteName,
                                String evaluateName, Date startTime, Date endTime, Date time,
                                def psi, def mii, def tsi, def bsi, def multiple,
                                long publishCount, long sumReprint, double avgReprint, long sumRead, double avgRead, long sumLike, double avgLike) {
        def collection = mongo.getCollection("evaluateChannelDaily")
        def insert = collection.insert(toObj([
                _id         : UUID.randomUUID().toString(),
                userId      : userId,
                evaluateId  : evaluateId,
                channelId   : channelId,
                siteDomain  : siteDomain,
                siteType    : siteType,
                siteName    : siteName,
                evaluateName: evaluateName,
                startTime   : startTime,
                endTime     : endTime,
                time        : time,
                psi         : psi,
                mii         : mii,
                tsi         : tsi,
                bsi         : bsi,
                multiple    : multiple,
                publishCount: publishCount,
                sumReprint  : sumReprint,
                avgReprint  : avgReprint,
                sumRead     : sumRead,
                avgRead     : avgRead,
                sumLike     : sumLike,
                avgLike     : avgLike,
                updateTime  : new Date(),
                createTime  : new Date()
        ]))
        return insert.updateOfExisting
    }

    //微信微博的siteName存在了siteDomain里面
    def getWeeklyWordCloud(String siteDomain, int siteType, Date startTime, Date endTime) {
        def keywordList = []
        if (!siteDomain) {
            return null
        }
        def collection = mongo.getCollection("keywordsWeekly")
        BasicDBObject match = new BasicDBObject("\$match", new BasicDBObject("startTime", startTime).append("endTime", endTime).append("siteType", siteType).append("siteDomain", siteDomain))
        BasicDBObject unwind = new BasicDBObject("\$unwind", "\$wordCloud")
        BasicDBObject group = new BasicDBObject("\$group", new BasicDBObject("_id", "\$wordCloud.word")
                .append("count", new BasicDBObject("\$sum", "\$wordCloud.count")))
        BasicDBObject sort = new BasicDBObject("\$sort", new BasicDBObject("count", -1))
        BasicDBObject limit = new BasicDBObject("\$limit", 30)
        def siteWord = collection.aggregate(Arrays.asList(match, unwind, group, sort, limit))
        for (BasicDBObject dbObject : siteWord.results()) {
            keywordList << ["word": dbObject._id, "count": dbObject.count]
        }
        return keywordList

    }

    def findForbiddenChannel(String siteDomain, int siteType, String siteName) {
        def collection = mongo.getCollection("forbiddenChannel")
        QueryBuilder qb = QueryBuilder.start()
        if (siteType == Site.SITE_TYPE_WEBSITE) {
            qb.put("siteDomain").is(siteDomain)
        }
        if (siteType == Site.SITE_TYPE_WEIBO || siteType == Site.SITE_TYPE_WECHAT) {
            qb.put("siteName").is(siteName)
        }
        return collection.findOne(qb.get())
    }
}
