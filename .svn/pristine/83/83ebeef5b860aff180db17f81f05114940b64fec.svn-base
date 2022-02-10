package com.istar.mediabroken.repo

import com.istar.mediabroken.entity.copyright.CopyRightFilter
import com.istar.mediabroken.entity.CopyrightMonitor
import com.istar.mediabroken.entity.CopyrightMonitorLog
import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.AggregationOutput
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.QueryBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj


@Repository
class CompetitionAnalysRepo {
    @Autowired
    MongoHolder mongo

    void addCopyrightMonitor(CopyrightMonitor copyrightMonitor) {
        def collection = mongo.getCollection("copyrightMonitor")
        collection.insert(toObj([
                _id: copyrightMonitor.monitorId,
                userId: copyrightMonitor.userId,
                title: copyrightMonitor.title,
                url: copyrightMonitor.url,
                author: copyrightMonitor.author,
                date: copyrightMonitor.date,
                media: copyrightMonitor. media,
                contentAbstract: copyrightMonitor.contentAbstract,
                whiteList: copyrightMonitor.whiteList,
                blackList: copyrightMonitor.blackList
        ]))
    }

    void modifyCopyrightMonitor(CopyrightMonitor copyrightMonitor) {
        def collection = mongo.getCollection("copyrightMonitor")
        collection.update(toObj([userId: copyrightMonitor.userId, _id: copyrightMonitor.monitorId]),
                toObj(['$set': [title: copyrightMonitor.title,
                                url: copyrightMonitor.url,
                                author: copyrightMonitor.author,
                                date: copyrightMonitor.date,
                                media: copyrightMonitor. media,
                                contentAbstract: copyrightMonitor.contentAbstract,
                                whiteList: copyrightMonitor.whiteList,
                                blackList: copyrightMonitor.blackList]]))
    }

    void removeCopyrightMonitor(long userId, String monitorId) {
        def collection = mongo.getCollection("copyrightMonitor")
        collection.remove(toObj([userId: userId, _id : monitorId]))
    }

    List<CopyrightMonitor> getCopyrightMonitors(long userId) {
        def collection = mongo.getCollection("copyrightMonitor")
        def query = [userId: userId]
        def cursor = collection.find(toObj(query))
        def list = []
        while (cursor.hasNext()) {
            def obj = cursor.next()
            list << new CopyrightMonitor(
                    monitorId: obj._id,
                    userId: obj.userId,
                    title: obj.title,
                    url: obj.url,
                    author: obj.author,
                    date: obj.date,
                    media: obj.media,
                    whiteList: obj.whiteList,
                    blackList: obj.blackList
            )
        }
        cursor.close()
        return list
    }

    CopyrightMonitor getCopyrightMonitor(long userId, String monitorId) {
        def collection = mongo.getCollection("copyrightMonitor")
        def query = [userId: userId, _id: monitorId]
        def obj = collection.findOne(toObj(query))
        return obj != null ? new CopyrightMonitor(
                    monitorId: obj._id,
                    userId: obj.userId,
                    title: obj.title,
                    url: obj.url,
                    author: obj.author,
                    date: obj.date,
                    media: obj.media,
                    contentAbstract: obj.contentAbstract,
                    whiteList: obj.whiteList,
                    blackList: obj.blackList
            ) : null
    }

    int getCopyrightMonitorCount(long userId) {
        def collection = mongo.getCollection("copyrightMonitor")
        def query = [userId: userId]
        return collection.find(toObj(query)).count()
    }

    void saveSiteComparison(long userId, int siteType, String[] siteNames) {
        def collection = mongo.getCollection("siteComparison")
        collection.update(toObj([userId: userId]),
                toObj(['$set': [siteType: siteType,
                                siteNames: siteNames]]), true, false)
    }

    Map getSiteComparison(long userId) {
        def collection = mongo.getCollection("siteComparison")
        def query = [userId: userId]
        println query
        def obj = collection.findOne(toObj(query))
        println obj
        return (obj) ? [
                siteType: obj.siteType,
                siteNames: obj.siteNames
        ]
         : [:]
    }
    List<CopyrightMonitor> getAllCopyrightMonitors(int pageNo,int pageSize) {
        def collection = mongo.getCollection("copyrightMonitor")
        def cursor = collection.find().sort(toObj([createTime: -1])).skip((pageNo - 1)* pageSize).limit(pageSize)
        def list = []
        while (cursor.hasNext()) {
            def obj = cursor.next()
            list << new CopyrightMonitor(
                    monitorId: obj._id,
                    userId: obj.userId,
                    title: obj.title,
                    url: obj.url,
                    author: obj.author,
                    date: obj.date,
                    media: obj.media,
                    blackList: obj.blackList,
                    whiteList: obj.whiteList
            )
        }
        cursor.close()
        return list
    }

    CopyrightMonitorLog getCopyrightMonitorLogById(String id) {
        CopyrightMonitorLog copyrightMonitorLog = new CopyrightMonitorLog();
        def collection = mongo.getCollection("copyrightMonitorLog")
        QueryBuilder LogQuery = QueryBuilder.start()
        LogQuery.put('_id').is(id)
        def monitorLog = collection.findOne(LogQuery.get())
        if(monitorLog){
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
                _id: copyrightMonitorLog.monitorId,
                prevStartTime: copyrightMonitorLog.prevStartTime,
                prevEndTime  : copyrightMonitorLog.prevEndTime,
                updateTime    : copyrightMonitorLog.updateTime,
                createTime    : copyrightMonitorLog.createTime,
        ]))
        return copyrightMonitorLog.monitorId
    }

    void modifyCopyrightMonitorLog(CopyrightMonitorLog copyrightMonitorLog) {
        def collection = mongo.getCollection("copyrightMonitorLog")
        collection.update(toObj([ _id: copyrightMonitorLog.monitorId]),
                toObj(['$set': [
                                prevStartTime: copyrightMonitorLog.prevStartTime,
                                prevEndTime  : copyrightMonitorLog.prevEndTime,
                                updateTime    : copyrightMonitorLog.updateTime,
                                createTime    : copyrightMonitorLog.createTime
                                ]]))
    }
    void removeCopyrightMonitorLog(String monitorId) {
        def collection = mongo.getCollection("copyrightMonitorLog")
        collection.remove(toObj([_id : monitorId]))
    }
    void addCopyrightMonitorNews(List copyrightMonitors) {
        def collection = mongo.getCollection("copyrightMonitorNews")
        List<DBObject> insertDataList=new ArrayList<DBObject>();
        copyrightMonitors.each {
            DBObject insertData=new BasicDBObject();
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
            insertDataList.add(insertData)
        }
        collection.insert(insertDataList)
    }
    void removeCopyrightMonitorNews(Long userId, String monitorId) {
        def collection = mongo.getCollection("copyrightMonitorNews")
        collection.remove(toObj([userId:userId , monitorId : monitorId]))
    }

    Map getNewsTortCounts(Long userId,List monitorIds ){
        def collection = mongo.getCollection("copyrightMonitorNews")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('isTort').is(true)
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('monitorId').in(monitorIds)
        DBObject match = new BasicDBObject("\$match", queryBuilder.get() );
        DBObject groupFields = new BasicDBObject( "_id", "\$monitorId");
        groupFields.put("count", new BasicDBObject( "\$sum", 1));
        DBObject group = new BasicDBObject("\$group", groupFields);
        AggregationOutput output = collection.aggregate( match, group );
        def result = [:]
        Iterable<DBObject> list= output.results();
        for(DBObject dbObject:list){
            result.put(dbObject.get("_id"),dbObject.get("count"))
        }
        return result;
    }

    CopyRightFilter getCopyRightFilter(Long userId){
        def collection = mongo.getCollection("copyRightFilter")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('userId').is(userId)
        def result = collection.findOne(queryBuilder.get())
        if(result){
            CopyRightFilter resu = new CopyRightFilter(result)
            return resu;//new CopyRightFilter(result)
        }
        return null
    }

    boolean addCopyRightFilter(CopyRightFilter copyRightFilter){
        def collection = mongo.getCollection("copyRightFilter")
        collection.insert(toObj(copyRightFilter.toMap()))
        return true
    }

    boolean modifyCopyRightFilter(CopyRightFilter copyRightFilter){
        def collection = mongo.getCollection("copyRightFilter")
        collection.save(toObj(copyRightFilter.toMap()))
        return true
    }

    boolean isMonitoringInWhiteList(Long userId,String websiteDomain){
        def collection = mongo.getCollection("copyrightMonitor")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('whiteList').in([websiteDomain])
        def result = collection.findOne(queryBuilder.get())
        if(result){
            return true
        }
        return false
    }

    boolean isMonitoringInBlackList(Long userId,String websiteDomain){
        def collection = mongo.getCollection("copyrightMonitor")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('blackList').in([websiteDomain])
        def result = collection.findOne(queryBuilder.get())
        if(result){
            return true
        }
        return false
    }
}
