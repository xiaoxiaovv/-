package com.istar.mediabroken.repo.copyright

import com.istar.mediabroken.entity.CopyrightMonitor
import com.istar.mediabroken.entity.CopyrightMonitorLog
import com.istar.mediabroken.entity.copyright.CopyRightFilter
import com.istar.mediabroken.entity.copyright.Monitor
import com.istar.mediabroken.entity.copyright.MonitorNews
import com.istar.mediabroken.utils.BDMongoHolder
import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.AggregationOutput
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.QueryBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj


@Repository
class MonitorRepo {
    @Autowired
    MongoHolder mongo
    @Autowired
    BDMongoHolder bdMongoHolder

    String addCopyrightMonitor(Monitor monitor) {
        def collection = mongo.getCollection("copyrightMonitor")
        collection.insert(toObj(monitor.toMap()))
        return monitor.monitorId
    }

    int getCopyrightMonitorCount(long userId) {
        def collection = mongo.getCollection("copyrightMonitor")
        def query = [userId: userId]
        return collection.find(toObj(query)).count()
    }

    List<Monitor> getCopyrightMonitors(long userId) {
        def collection = mongo.getCollection("copyrightMonitor")
        def query = [userId: userId]
        def cursor = collection.find(toObj(query))
        def list = []
        while (cursor.hasNext()) {
            def obj = cursor.next()
            list << new Monitor(obj)
        }
        cursor.close()
        return list
    }

    Map getNewsTortCounts(Long userId, List monitorIds) {
        def collection = mongo.getCollection("copyrightMonitorNews")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('isTort').is(true)
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('monitorId').in(monitorIds)
        DBObject match = new BasicDBObject("\$match", queryBuilder.get());
        DBObject groupFields = new BasicDBObject("_id", "\$monitorId");
        groupFields.put("count", new BasicDBObject("\$sum", 1));
        DBObject group = new BasicDBObject("\$group", groupFields);
        AggregationOutput output = collection.aggregate(match, group);
        def result = [:]
        Iterable<DBObject> list = output.results();
        for (DBObject dbObject : list) {
            result.put(dbObject.get("_id"), dbObject.get("count"))
        }
        return result;
    }

    Long getMonitorTortCounts(Long userId, String monitorId) {
        def collection = mongo.getCollection("copyrightMonitorNews")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('monitorId').is(monitorId)
        queryBuilder.put('isTort').is(true)
        def count = collection.count(queryBuilder.get());
        return count;
    }

    Monitor getCopyrightMonitor(long userId, String monitorId) {
        def collection = mongo.getCollection("copyrightMonitor")
        def query = [userId: userId, _id: monitorId]
        def obj = collection.findOne(toObj(query))
        return obj != null ? new Monitor(
                monitorId: obj._id,
                userId: obj.userId,
                title: obj.title,
                url: obj.url,
                author: obj.author,
                startDate: obj.startDate,
                media: obj.media,
                contentAbstract: obj.contentAbstract,
                whiteList: obj.whiteList,
                blackList: obj.blackList
        ) : null
    }

    Monitor getCopyrightMonitorById(String monitorId) {
        def collection = mongo.getCollection("copyrightMonitor")
        def query = [_id: monitorId]
        def obj = collection.findOne(toObj(query))
        return obj != null ? new Monitor(
                monitorId: obj._id,
                userId: obj.userId,
                title: obj.title,
                url: obj.url,
                author: obj.author,
                startDate: obj.startDate,
                media: obj.media,
                contentAbstract: obj.contentAbstract,
                whiteList: obj.whiteList,
                blackList: obj.blackList
        ) : null
    }

    MonitorNews getCopyrightMonitorNewsById(String copyrightMonitorNewsId) {
        def collection = mongo.getCollection("copyrightMonitorNews")
        def query = [_id: copyrightMonitorNewsId]
        def obj = collection.findOne(toObj(query))
        return obj != null ? new MonitorNews(
                _id: obj._id,
                userId: obj.userId,
                monitorId: obj.monitorId,
                newsId: obj.newsId,
                isTort: obj.isTort,
                isBlack: obj.isBlack,
                isWhite: obj.isWhite,
                title: obj.title,
                source: obj.source,
                author: obj.author,
                url: obj.url,
                contentAbstract: obj.contentAbstract,
                site: obj.site,
                newsType: obj.newsType,
                Ctime: obj.Ctime,
                DkTime: obj.DkTime,
                createTime: obj.createTime,
                isKeyChannel: obj.isKeyChannel
        ) : null
    }

    boolean modifyCopyrightMonitor(Monitor monitor) {
        def collection = mongo.getCollection("copyrightMonitor")
        collection.update(toObj([userId: monitor.userId, _id: monitor.monitorId]),
                toObj(['$set': [title          : monitor.title,
                                url            : monitor.url,
                                author         : monitor.author,
                                startDate      : monitor.startDate,
                                endDate        : monitor.endDate,
                                media          : monitor.media,
                                contentAbstract: monitor.contentAbstract,
                                whiteList      : monitor.whiteList,
                                blackList      : monitor.blackList,
                                updateTime     : monitor.updateTime
                ]]))
        return true;
    }

    boolean removeCopyrightMonitorLog(String monitorId) {
        def collection = mongo.getCollection("copyrightMonitorLog")
        collection.remove(toObj([_id: monitorId]))
        return true;
    }

    boolean removeCopyrightMonitorNews(Long userId, String monitorId) {
        def collection = mongo.getCollection("copyrightMonitorNews")
        collection.remove(toObj([userId: userId, monitorId: monitorId]))
        return true;
    }

    boolean removeCopyrightMonitor(long userId, String monitorId) {
        def collection = mongo.getCollection("copyrightMonitor")
        collection.remove(toObj([userId: userId, _id: monitorId]))
        return true;
    }

    boolean removeCopyrightMonitorNews(long userId, String monitorId, List newsList) {
        def collection = mongo.getCollection("copyrightMonitorNews")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('monitorId').is(monitorId)
        queryBuilder.put('_id').in(newsList)
        collection.remove(queryBuilder.get())
        return true;
    }

    boolean isMonitoringInWhiteList(Long userId, String websiteDomain) {
        def collection = mongo.getCollection("copyrightMonitor")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('whiteList').in([websiteDomain])
        def result = collection.findOne(queryBuilder.get())
        if (result) {
            return true
        }
        return false
    }

    boolean isMonitoringInBlackList(Long userId, String websiteDomain) {
        def collection = mongo.getCollection("copyrightMonitor")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('blackList').in([websiteDomain])
        def result = collection.findOne(queryBuilder.get())
        if (result) {
            return true
        }
        return false
    }

    CopyRightFilter getCopyRightFilter(Long userId) {
        def collection = mongo.getCollection("copyRightFilter")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('userId').is(userId)
        def result = collection.findOne(queryBuilder.get())
        if (result) {
            CopyRightFilter resu = new CopyRightFilter(result)
            return resu;//new CopyRightFilter(result)
        }
        return null
    }

    long getReprintCount(long userId, String monitorId) {
        def collection = mongo.getCollection("copyrightMonitorNews")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('monitorId').is(monitorId)
        def result = collection.count(queryBuilder.get())
        return result
    }

    long getReprintMediaCount(Long userId, String monitorId) {
        def collection = mongo.getCollection("copyrightMonitorNews")
        def result = collection.distinct("site", toObj([userId: userId, monitorId: monitorId]))
        return result ? result.size() : 0
    }

    long getReprintCounts(Long userId, String monitorId) {
        def collection = mongo.getCollection("copyrightMonitorNews")
        def result = collection.count(toObj([userId: userId, monitorId: monitorId]))
        return result ? result : 0
    }

    long getCommentCount(Long userId, String monitorId) {
        long result = 0
        def collection = mongo.getCollection("copyrightMonitorNews")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('monitorId').is(monitorId)

        DBObject match = new BasicDBObject("\$match", queryBuilder.get());
        DBObject groupFields = new BasicDBObject("_id", new BasicDBObject("userId","\$userId").put("monitorId","\$monitorId"));
        groupFields.put("count", new BasicDBObject("\$sum", "\$commentCount"));
        DBObject group = new BasicDBObject("\$group", groupFields);
        AggregationOutput output = collection.aggregate(Arrays.asList(match, group))
        Iterable<DBObject> list = output.results();
        for (DBObject dbObject : list) {
            result = dbObject.get("count")
        }
        return result

    }

    long getLikeCount(Long userId, String monitorId) {
        long result = 0
        def collection = mongo.getCollection("copyrightMonitorNews")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('monitorId').is(monitorId)

        DBObject match = new BasicDBObject("\$match", queryBuilder.get());
        DBObject groupFields = new BasicDBObject("_id", new BasicDBObject("userId","\$userId").put("monitorId","\$monitorId"));
        groupFields.put("count", new BasicDBObject("\$sum", "\$likeCount"));
        DBObject group = new BasicDBObject("\$group", groupFields);
        AggregationOutput output = collection.aggregate(Arrays.asList(match, group))
        Iterable<DBObject> list = output.results();
        for (DBObject dbObject : list) {
            result = dbObject.get("count")
        }
        return result

    }

    long getKeyChannelCount(Long userId, String monitorId) {
        def collection = mongo.getCollection("copyrightMonitorNews")
        def result = collection.count(toObj([userId: userId, monitorId: monitorId, isKeyChannel: true]))
        return result ? result : 0
    }

    long getWeiboReprintCount(Long userId, String monitorId) {
        long result = 0
        def collection = mongo.getCollection("copyrightMonitorNews")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('monitorId').is(monitorId)

        DBObject match = new BasicDBObject("\$match", queryBuilder.get());
        DBObject groupFields = new BasicDBObject("_id", new BasicDBObject("userId","\$userId").put("monitorId","\$monitorId"));
        groupFields.put("count", new BasicDBObject("\$sum", "\$reprintCount"));
        DBObject group = new BasicDBObject("\$group", groupFields);
        AggregationOutput output = collection.aggregate(Arrays.asList(match, group))
        Iterable<DBObject> list = output.results();
        for (DBObject dbObject : list) {
            result = dbObject.get("count")
        }
        return result
    }

    long getWeMediaReadCount(Long userId, String monitorId) {
        long result = 0
        def collection = mongo.getCollection("copyrightMonitorNews")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('monitorId').is(monitorId)

        DBObject match = new BasicDBObject("\$match", queryBuilder.get());
        DBObject groupFields = new BasicDBObject("_id", new BasicDBObject("userId","\$userId").put("monitorId","\$monitorId"));
        groupFields.put("count", new BasicDBObject("\$sum", "\$visitCount"));
        DBObject group = new BasicDBObject("\$group", groupFields);
        AggregationOutput output = collection.aggregate(Arrays.asList(match, group))
        Iterable<DBObject> list = output.results();
        for (DBObject dbObject : list) {
            result = dbObject.get("count")
        }
        return result
    }

    Map getAnalysisReprintTrend(Long userId, String monitorId) {
        def collection = mongo.getCollection("copyrightMonitorNews")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('monitorId').is(monitorId)
        DBObject match = new BasicDBObject("\$match", queryBuilder.get());
        BasicDBObject projection = new BasicDBObject("\$project", new BasicDBObject("_id", 1)
                .append("ymd", new BasicDBObject("\$dateToString":
                (new BasicDBObject("format", "%Y-%m-%d").append("date", "\$Ctime")))))
        DBObject groupFields = new BasicDBObject("_id", "\$ymd");
        groupFields.put("count", new BasicDBObject("\$sum", 1));
        DBObject group = new BasicDBObject("\$group", groupFields);
        DBObject sort = new BasicDBObject("\$sort", new BasicDBObject("_id", 1));
        AggregationOutput output = collection.aggregate(Arrays.asList(match, projection, group, sort))
        Map result = new HashMap()
        Iterable<DBObject> list = output.results();
        for (DBObject dbObject : list) {
            result.put(dbObject.get("_id"), dbObject.get("count"))
        }
        return result
    }

    List<Map> getAnalysisChannelReprintSummary(Long userId, String monitorId) {
        def collection = mongo.getCollection("copyrightMonitorNews")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('monitorId').is(monitorId)
        DBObject match = new BasicDBObject("\$match", queryBuilder.get());
        DBObject groupFields = new BasicDBObject("_id", "\$newsType");
        groupFields.put("count", new BasicDBObject("\$sum", 1));
        DBObject group = new BasicDBObject("\$group", groupFields);
        AggregationOutput output = collection.aggregate(match, group);
        List<Map> result = new ArrayList<Map>()
        Iterable<DBObject> list = output.results();
        for (DBObject dbObject : list) {
            Map map = new HashMap()
            map.put("channelName", dbObject.get("_id"))
            map.put("count", dbObject.get("count"))
            result.add(map)
        }
        return result
    }

    boolean modifyCopyRightFilter(CopyRightFilter copyRightFilter) {
        def collection = mongo.getCollection("copyRightFilter")
        collection.save(toObj(copyRightFilter.toMap()))
        return true
    }

    List getCopyrightMonitorNews(long userId, String monitorId, int queryType, int pageNo, int pageSize) {
        def collection = mongo.getCollection("copyrightMonitorNews")
        def queryObj = [userId: userId, monitorId: monitorId]
        def sortQuery = [Ctime: -1]
        switch (queryType) {
            case 0:
                queryObj = [userId: userId, monitorId: monitorId]
                break;
            case 1:
                queryObj = [userId: userId, monitorId: monitorId, isWhite: true, isTort: true]
                break;
            case 2:
                queryObj = [userId: userId, monitorId: monitorId, isBlack: true, isTort: true]
                break;
            case 3:
                queryObj = [userId: userId, monitorId: monitorId, isWhite: false, isBlack: false, isTort: true]
                break;
            default:
                queryObj = [userId: userId, monitorId: monitorId]
                break;
        }

        def cursor = collection.find(toObj(queryObj)).sort(toObj(sortQuery)).skip(pageSize * (pageNo - 1)).limit(pageSize)
        def result = []
        while (cursor.hasNext()) {
            def monitorNews = cursor.next()
            result << new MonitorNews(monitorNews)
        }
        cursor.close()
        return result
    }

    List<CopyrightMonitor> getActiveCopyrightMonitors(int pageNo, int pageSize) {
        def collection = mongo.getCollection("copyrightMonitor")
        QueryBuilder key = QueryBuilder.start()
        key.put("status").notEquals(3)
        def cursor = collection.find(key.get()).sort(toObj([createTime: -1])).skip((pageNo - 1) * pageSize).limit(pageSize)
        def list = []
        while (cursor.hasNext()) {
            def obj = cursor.next()
            list << new Monitor(obj)
        }
        cursor.close()
        return list
    }

    void updateCopyrightMonitorToComplete(String id) {
        def collection = mongo.getCollection("copyrightMonitor")
        collection.update(toObj(["_id": id]), toObj(["\$set": ["status": 3]]))
    }

    CopyrightMonitorLog getCopyrightMonitorLogById(String id) {
        CopyrightMonitorLog copyrightMonitorLog = new CopyrightMonitorLog();
        def collection = mongo.getCollection("copyrightMonitorLog")
        QueryBuilder LogQuery = QueryBuilder.start()
        LogQuery.put('_id').is(id)
        def monitorLog = collection.findOne(LogQuery.get())
        if (monitorLog) {
            copyrightMonitorLog.monitorId = monitorLog._id
            copyrightMonitorLog.prevStartTime = monitorLog.prevStartTime
            copyrightMonitorLog.prevEndTime = monitorLog.prevEndTime
            copyrightMonitorLog.updateTime = monitorLog.updateTime
            copyrightMonitorLog.createTime = monitorLog.createTime
        }
        return copyrightMonitorLog
    }

    String addCopyrightMonitorLog(CopyrightMonitorLog copyrightMonitorLog) {
        def collection = mongo.getCollection("copyrightMonitorLog")
        collection.insert(toObj([
                _id          : copyrightMonitorLog.monitorId,
                prevStartTime: copyrightMonitorLog.prevStartTime,
                prevEndTime  : copyrightMonitorLog.prevEndTime,
                updateTime   : copyrightMonitorLog.updateTime,
                createTime   : copyrightMonitorLog.createTime,
        ]))
        return copyrightMonitorLog.monitorId
    }

    void addCopyrightMonitorNews(List copyrightMonitors) {
        def collection = mongo.getCollection("copyrightMonitorNews")
        List<DBObject> insertDataList = new ArrayList<DBObject>();
        copyrightMonitors.each {
            DBObject insertData = new BasicDBObject();
            insertData.put("_id", it._id);
            insertData.put("userId", it.userId);
            insertData.put("monitorId", it.monitorId);
            insertData.put("newsId", it.newsId);
            insertData.put("isTort", it.isTort);
            insertData.put("title", it.title);
            insertData.put("source", it.source);
            insertData.put("author", it.author);
            insertData.put("url", it.url);
            insertData.put("contentAbstract", it.contentAbstract);
            insertData.put("site", it.site);
            insertData.put("newsType", it.newsType);
            insertData.put("Ctime", it.Ctime);
            insertData.put("DkTime", it.DkTime);
            insertData.put("createTime", it.createTime);
            insertData.put("isWhite", it.isWhite);
            insertData.put("isBlack", it.isBlack);
            insertData.put("isKeyChannel", it.isKeyChannel);
            insertData.put("commentCount", it.commentCount);
            insertData.put("visitCount", it.visitCount);
            insertData.put("reprintCount", it.reprintCount);
            insertData.put("likeCount", it.likeCount);

            insertDataList.add(insertData)
        }
        collection.insert(insertDataList)
    }

    void modifyCopyrightMonitorLog(CopyrightMonitorLog copyrightMonitorLog) {
        def collection = mongo.getCollection("copyrightMonitorLog")
        collection.update(toObj([_id: copyrightMonitorLog.monitorId]),
                toObj(['$set': [
                        prevStartTime: copyrightMonitorLog.prevStartTime,
                        prevEndTime  : copyrightMonitorLog.prevEndTime,
                        updateTime   : copyrightMonitorLog.updateTime,
                        createTime   : copyrightMonitorLog.createTime
                ]]))
    }
    //查询最后一次更新的所有用户信息在规定时间
   List<Monitor> getCopyrightMonitorInDate(long userId , Date startDate, Date endDate){
       def collection = mongo.getCollection("copyrightMonitor")
       def query = toObj([ 'userId': userId, 'updateTime' : [$gt: startDate, $lt: endDate]])
       def cursor = collection.find(query);
       def result = []
       while (cursor.hasNext()) {
           def Monitor = cursor.next()
           result << new Monitor(toObj(Monitor))
       }
       cursor.close()
       return result
    }
}
