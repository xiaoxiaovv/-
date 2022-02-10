package com.istar.mediabroken.repo

import com.istar.mediabroken.api3rd.YqmsSession
import com.istar.mediabroken.entity.AbstractDownload
import com.istar.mediabroken.entity.AbstractSetting
import com.istar.mediabroken.entity.AbstractShare
import com.istar.mediabroken.entity.ICompileSummary
import com.istar.mediabroken.entity.News
import com.istar.mediabroken.entity.NewsAbstract
import com.istar.mediabroken.entity.capture.NewsOperation
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.utils.DateUitl
import com.istar.mediabroken.utils.MongoHolder
import com.istar.mediabroken.utils.Paging
import com.mongodb.QueryBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import java.text.SimpleDateFormat

import static com.istar.mediabroken.utils.MongoHelper.toObj
import static com.istar.mediabroken.utils.VarUtil.getValue

@Repository
class ICompileRepo {
    @Autowired
    MongoHolder mongo

    @Autowired
    AccountRepo accountRepo

    ICompileSummary getSummary(long userId, String summaryId) {
        def collection = mongo.getCollection("iCompileSummary")
        def summary = collection.findOne(toObj([_id: summaryId, userId: userId]))

        return summary ? new ICompileSummary(
                summaryId: summaryId,
                userId: userId,
                title: summary.title,
                yqmsTopicId: summary.yqmsTopicId,
                startTime: summary.startTime,
                endTime: summary.endTime,
                createTime: summary.createTime,
                session: new YqmsSession(userId: summary.yqmsUserId)
        ) : null

    }

    ICompileSummary getSummary(long userId) {
        def collection = mongo.getCollection("iCompileSummary")
        def summary = collection.findOne(toObj([userId: userId]))

        return summary ? new ICompileSummary(
                userId: userId,
                summaryId: summary._id,
                title: summary.title,
                time: summary.time,
                place: summary.place,
                person: summary.person,
                event: summary.event,
                ambiguous: summary.ambiguous,
                yqmsTopicId: summary.yqmsTopicId,
                yqmsUserId: summary.yqmsUserId,
                startTime: summary.startTime,
                endTime: summary.endTime,
                createTime: summary.createTime,
                session: new YqmsSession(userId: summary.yqmsUserId)
        ) : null

    }
    /**
     * 获取用户综述列表
     * @param userId
     * @return
     */
    List<ICompileSummary> getSummariesByUserId(long userId) {
        def collection = mongo.getCollection("iCompileSummary")
        def query = [userId: userId]
        def cursor = collection.find(toObj(query))
        def summaries = []
        while (cursor.hasNext()) {
            def summary = cursor.next()
            summaries << new ICompileSummary(
                    userId: userId,
                    summaryId: summary._id,
                    title: summary.title,
                    place: summary.place,
                    person: summary.person,
                    event: summary.event,
                    ambiguous: summary.ambiguous,
                    yqmsTopicId: summary.yqmsTopicId,
                    yqmsUserId: summary.yqmsUserId,
                    startTime: summary.startTime,
                    endTime: summary.endTime,
                    template: summary.template ? summary.template : 1,
                    createTime: summary.createTime,
                    session: new YqmsSession(userId: summary.yqmsUserId))
        }
        cursor.close()
        return summaries
    }
/*获取摘要素材总数*/
    long getNewsAbstractsTotal(long userId,String queryKeyWords) {
        def collection = mongo.getCollection("newsAbstract")
        QueryBuilder queryBuilder = QueryBuilder.start()
        if (queryKeyWords) {
            QueryBuilder queryBuilder1 = QueryBuilder.start()
            queryBuilder1.put("title").regex(~/.*(?i)${queryKeyWords}.*/)
            QueryBuilder queryBuilder4 = QueryBuilder.start()
            queryBuilder4.put("userId").is(userId)
            queryBuilder.and(queryBuilder4.get(),queryBuilder1.get())
        }else {
            queryBuilder.put("userId").is(userId)
        }

        return collection.getCount(queryBuilder.get())
    }
    /*获取要闻筛选总数*/
    long getNewsOperationsTotal(long userId,String queryKeyWords,int operationSource,int timeType,String timeStart,String timeEnd) {
        def collection = mongo.getCollection("newsOperation")
        QueryBuilder queryBuilder = QueryBuilder.start()
//        timeType -1 自定义 0 全部   1 今天  7天
        if (queryKeyWords) {
            QueryBuilder queryBuilder1 = QueryBuilder.start()
            queryBuilder1.put("news.title").regex(~/.*(?i)${queryKeyWords}.*/)
            QueryBuilder queryBuilder2 = QueryBuilder.start()
            queryBuilder2.put("news.source").regex(~/.*(?i)${queryKeyWords}.*/)
            QueryBuilder queryBuilder3 = QueryBuilder.start()
            queryBuilder3.or(queryBuilder1.get(),queryBuilder2.get())
            QueryBuilder queryBuilder4 = QueryBuilder.start()
            queryBuilder4.put("userId").is(userId)
            queryBuilder.and(queryBuilder4.get(),queryBuilder3.get())
        }else {
            queryBuilder.put("userId").is(userId)
        }
        if(operationSource != 0){
            queryBuilder.put("operationType").is(operationSource as int)
        }
        Date startTime=new Date();
        Date endTime=new Date();


        if(timeType != 0 ){
            if(timeType == -1){
                SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                startTime = dfs.parse(timeStart);
                endTime = dfs.parse(timeEnd)
            }else if(timeType > 0 ){
                def dayStart = DateUitl.getDayBegin()
                startTime = DateUitl.addDay(dayStart,((timeType as int) - 1)* -1 )
            }
            queryBuilder.put("news.publishTime").greaterThanEquals(startTime)
            queryBuilder.put("news.publishTime").lessThanEquals(endTime)
        }

        return collection.getCount(queryBuilder.get())
    }

    /**
     * 获取摘要素材列表
     * @param userId
     * @return
     */
    Paging<NewsAbstract> getNewsAbstracts(long userId, Paging<NewsAbstract> paging, String queryKeyWords, int orderType) {
        def collection = mongo.getCollection("newsAbstract")
        QueryBuilder queryBuilder = QueryBuilder.start()
        if (queryKeyWords) {
            QueryBuilder queryBuilder1 = QueryBuilder.start()
            queryBuilder1.put("title").regex(~/.*(?i)${queryKeyWords}.*/)
            QueryBuilder queryBuilder2 = QueryBuilder.start()
            queryBuilder2.put("userId").is(userId)
            queryBuilder.and(queryBuilder2.get(),queryBuilder1.get())
        }else {
            queryBuilder.put("userId").is(userId)
        }
        def sortQuery=null
        switch (orderType){
            case 1:
                sortQuery=[createTime:-1]
                break;
            case 2:
                sortQuery=[title:1]
                break;
            case 3:
                sortQuery=[title: -1]
                break;
            default:
                sortQuery=[createTime:-1]
                break;
        }
        def cursor = collection.find(queryBuilder.get()).sort(toObj(sortQuery)).skip(paging.offset).limit(paging.limit)
        while (cursor.hasNext()) {
            def newsAbstract = cursor.next()
            paging.list << new NewsAbstract(
                    abstractId  : newsAbstract._id,
                    title       : newsAbstract.title,
                    author      : newsAbstract.author,
                    picUrl      : newsAbstract.picUrl,
                    content     : newsAbstract.content,
                    newsDetail  : newsAbstract.newsDetail,
                    orgId       : newsAbstract.orgId,
                    userId      : newsAbstract.userId,
                    updateTime  : newsAbstract.updateTime,
                    createTime  : newsAbstract.createTime
            )
        }
        cursor.close()
        return paging;
    }
    /**
     * 获取要闻筛选列表
     * @param userId
     * @return
     */
    Paging<NewsOperation> getNewsOperations(long userId, Paging<NewsOperation> paging, String queryKeyWords, int orderType,int operationSource,int timeType,String timeStart,String timeEnd) {
        def collection = mongo.getCollection("newsOperation")
        QueryBuilder queryBuilder = QueryBuilder.start()
//        timeType -1 自定义 0 全部   1 今天  7天
        if (queryKeyWords) {
            QueryBuilder queryBuilder1 = QueryBuilder.start()
            queryBuilder1.put("news.title").regex(~/.*(?i)${queryKeyWords}.*/)
            QueryBuilder queryBuilder2 = QueryBuilder.start()
            queryBuilder2.put("news.source").regex(~/.*(?i)${queryKeyWords}.*/)
            QueryBuilder queryBuilder3 = QueryBuilder.start()
            queryBuilder3.or(queryBuilder1.get(),queryBuilder2.get())
            QueryBuilder queryBuilder4 = QueryBuilder.start()
            queryBuilder4.put("userId").is(userId)
            queryBuilder.and(queryBuilder4.get(),queryBuilder3.get())
        }else {
            queryBuilder.put("userId").is(userId)
        }
        if(operationSource != 0){
            queryBuilder.put("operationType").is(operationSource as int)
        }
        Date startTime=new Date();
        Date endTime=new Date();


        if(timeType != 0 ){
            if(timeType == -1){
                SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                startTime = dfs.parse(timeStart);
                endTime = dfs.parse(timeEnd)
            }else if(timeType > 0 ){
                def dayStart = DateUitl.getDayBegin()
                startTime = DateUitl.addDay(dayStart,((timeType as int) - 1)* -1 )
            }
            queryBuilder.put("createTime").greaterThanEquals(startTime)
            queryBuilder.put("createTime").lessThanEquals(endTime)
        }

        def sortQuery=null
        switch (orderType){
            case 1:
                sortQuery=["createTime":-1]
                break;
            case 2:
                sortQuery=["news.reprintCount":-1]
                break;
            default:
                sortQuery=["createTime":-1]
                break;
        }
        def cursor = collection.find(queryBuilder.get()).sort(toObj(sortQuery)).skip(paging.offset).limit(paging.limit)
        while (cursor.hasNext()) {
            def newsOperation = cursor.next()
            def news = newsOperation.news;
            news.reprintCount=getHeat(news.reprintCount);
            paging.list << new NewsOperation(
                    _id :           newsOperation._id,
                    newsId :        newsOperation.newsId,
                    news :          news,
                    pushType :      newsOperation.pushType,
                    status :        newsOperation.status,
                    siteId :        newsOperation.siteId,
                    orgId :         newsOperation.orgId,
                    userId :        newsOperation.userId,
                    updateTime :    newsOperation.updateTime,
                    createTime :    newsOperation.createTime,
                    operationType : newsOperation.operationType,
                    shareChannel   : newsOperation.shareChannel,
                    shareContent   :newsOperation.shareContent
            )
        }
        cursor.close()
        return paging;
    }

    int getHeat(int num) {
        return News.caculateHeat(num)
    }
    /**
     * 拿到当前用户总的
     * @param userId
     * @return
     */
    int getUserSummaryCount(long userId) {
        def collection = mongo.getCollection("iCompileSummary")
        def count = collection.count(toObj([userId: userId]))
        return count
    }
    /**
     * 获取summaryByid
     * @param id
     * @return
     */
    ICompileSummary getSummaryById(String id) {
        def collection = mongo.getCollection("iCompileSummary")
        def summary = collection.findOne(toObj([_id: id]))

        return summary ? new ICompileSummary(
                userId: summary.userId,
                summaryId: summary._id,
                title: summary.title,
                time: summary.time,
                place: summary.place,
                person: summary.person,
                event: summary.event,
                ambiguous: summary.ambiguous,
                yqmsTopicId: summary.yqmsTopicId,
                yqmsUserId: summary.yqmsUserId,
                startTime: summary.startTime,
                endTime: summary.endTime,
                template: summary.template? summary.template : 1,
                createTime: summary.createTime,
                session: new YqmsSession(userId: summary.yqmsUserId)
        ) : null

    }
    /*根据id获取对应的imgs*/
    def getAbstractImgsById(long userId,String abstract_id){
//        List<String> list = new ArrayList<String>()
        def list=[];
        def collection = mongo.getCollection("newsAbstract")
        def query = [userId: userId, _id: abstract_id]
        def cursor = collection.findOne(toObj(query))
        if(cursor){
            return cursor.imgs
        }
        return list
    }
    AbstractSetting getAbstractSetting(long userId) {
        def collection = mongo.getCollection("abstractSetting")
        def setting = collection.findOne(toObj([userId: userId]))

        return setting ? AbstractSetting.toObject(setting) : null
    }

    def addAbstractSetting(AbstractSetting setting){
        def collection = mongo.getCollection("abstractSetting")
        def map = setting.toMap()
        map._id = UUID.randomUUID().toString()
        collection.insert(toObj(map))
    }

    def modifyAbstractSettting(AbstractSetting setting){
        def collection = mongo.getCollection("abstractSetting")
        collection.update(
                toObj([userId: setting.userId]),
                toObj(['$set': setting.toMap()])
        )
    }

    def setAbstractSetting(AbstractSetting setting){
        if(!setting) return
        AbstractSetting abstractSetting = this.getAbstractSetting(setting.userId)
        abstractSetting ? this.modifyAbstractSettting(setting) : this.addAbstractSetting(setting)
    }

    NewsAbstract getNewsAbstract(long userId, String abstractId){
        def collection = mongo.getCollection("newsAbstract")
        def newsAbstract = collection.findOne(toObj([userId: userId, _id : abstractId]))

        return newsAbstract ? NewsAbstract.toObject(newsAbstract) : null
    }

    NewsAbstract getNewsAbstractById( String abstractId){
        def collection = mongo.getCollection("newsAbstract")
        def newsAbstract = collection.findOne(toObj([ _id : abstractId]))
        return newsAbstract ? NewsAbstract.toObject(newsAbstract) : null
    }
    NewsAbstract getTodayNewsAbstract(long userId){
        def collection = mongo.getCollection("newsAbstract")
        def newsAbstract = null
        def cursor = collection.find(toObj([userId: userId])).sort(toObj([createTime: -1])).limit(1)
        cursor.each {
            newsAbstract = NewsAbstract.toObject(it)
        }
        return newsAbstract
    }

    void delNewsAbstract(long userId, String abstractId) {
        def collection = mongo.getCollection("newsAbstract")
        collection.remove(toObj([userId: userId, _id : abstractId]))
    }

    void addNewsAbstract(NewsAbstract newsAbstract) {

        def collection = mongo.getCollection("newsAbstract")
        def map = newsAbstract.toMap()
        map._id = newsAbstract.abstractId
        collection.insert(toObj(map))
    }

    /**
     * 编辑newsAbstract
     * @param newsAbstract
     */
    void modifyNewsAbstract(NewsAbstract newsAbstract) {

        def collection = mongo.getCollection("newsAbstract")
        def map = newsAbstract.toMap()
        map._id = newsAbstract.abstractId
        collection.save(toObj(map))
    }
    /**
     * 删除用户的摘要素材信息
     * @param userId
     * @param abstractId
     */
    void removeNewsAbstract(long userId, String abstractId) {
        def collection = mongo.getCollection("newsAbstract")
        collection.remove(toObj(userId: userId,_id: abstractId))
    }

    long getLeastUsedUserIdForTopic() {
        def session = accountRepo.getYqmsSession()
        return session.yqmsUserId
    }

    void modifySummary(ICompileSummary summary) {
        def collection = mongo.getCollection("iCompileSummary")
        collection.update(toObj([userId: summary.userId, _id: summary.summaryId]),
                toObj(['$set': [title: getValue(summary.title, ''),
                                place: getValue(summary.place, ''),
                                person: getValue(summary.person, ''),
                                event: getValue(summary.event, ''),
                                ambiguous: getValue(summary.ambiguous, ''),
                                startTime: summary.startTime,
                                endTime: summary.endTime,
                                yqmsTopicId: summary.yqmsTopicId,
                                updateTime: new Date()]]))
    }

    void modifySummaryTemplate(long userId,String summaryId, int template) {
        def collection = mongo.getCollection("iCompileSummary")
        collection.update(toObj([userId: userId, _id: summaryId]),
                toObj(['$set': [template: template]]))
    }
    void addSummary(ICompileSummary summary) {
        def collection = mongo.getCollection("iCompileSummary")
        collection.insert(toObj([
                        userId: summary.userId,
                        _id: summary.summaryId,
                        title: getValue(summary.title, ''),
                        place: getValue(summary.place, ''),
                        person: getValue(summary.person, ''),
                        event: getValue(summary.event, ''),
                        ambiguous: getValue(summary.ambiguous, ''),
                        yqmsTopicId: summary.yqmsTopicId,
                        yqmsUserId: summary.yqmsUserId,
                        startTime: summary.startTime,
                        endTime: summary.endTime,
                        template: summary.template,
                        createTime: summary.createTime
        ]))

    }
    /**
     * 删除summary
     * @param summaryId
     */
    void removeSummaryById(String summaryId) {
        def collection = mongo.getCollection("iCompileSummary")
        collection.remove(toObj([
                _id: summaryId,
        ]))
    }

    void addAbstractDowdload(NewsAbstract newsAbstract) {
        def collection = mongo.getCollection("abstractDownload")
        AbstractDownload abstractDownload = new AbstractDownload();

        abstractDownload.abstractId = newsAbstract.abstractId
        abstractDownload.title = newsAbstract.title
        abstractDownload.picUrl = newsAbstract.picUrl
        abstractDownload.content = newsAbstract.content
        abstractDownload.newsDetail = newsAbstract.newsDetail
        abstractDownload.orgId = newsAbstract.orgId
        abstractDownload.userId = newsAbstract.userId
        abstractDownload.updateTime = new Date();
        abstractDownload.createTime = new Date();

        def downloadMap = abstractDownload.toMap();
        downloadMap._id = abstractDownload.createId()
        collection.insert(toObj(downloadMap))
    }

    Long getAbstractTotal(long userId, String orgId) {
        def collection = mongo.getCollection("newsAbstract")
        return collection.getCount(toObj([userId: userId,orgId: orgId]))
    }

    Paging<NewsAbstract> getPagingAbstractList(long userId, String orgId, Paging<NewsAbstract> paging) {
        def collection = mongo.getCollection("newsAbstract")
        def query = [userId: userId, orgId: orgId]
        def cursor = collection.find(toObj(query)).sort(toObj([createTime: -1])).skip(paging.offset).limit(paging.limit)
        while (cursor.hasNext()) {
            paging.list << NewsAbstract.toObject(cursor.next())
        }
        cursor.close()
        return paging
    }

    Long getAbstractDownloadTotal(String userId, String orgId) {
        def collection = mongo.getCollection("abstractDownload")
        return collection.getCount(toObj([userId: userId,orgId: orgId]))
    }

    Paging<AbstractDownload> getPagingAbstractDownloadList(String userId, String orgId, Paging<AbstractDownload> paging) {
        def collection = mongo.getCollection("abstractDownload")
        def query = [userId: userId, orgId: orgId]
        def cursor = collection.find(toObj(query)).sort(toObj([createTime: -1])).skip(paging.offset).limit(paging.limit)
        while (cursor.hasNext()) {
            paging.list << AbstractDownload.toObject(cursor.next())
        }
        cursor.close()
        return paging
    }

    AbstractDownload getAbstractDownload(long userId, String id) {
        def collection = mongo.getCollection("abstractDownload")
        def abstractDowdload = collection.findOne(toObj([
                _id  : id
        ]))
        return abstractDowdload ? AbstractDownload.toObject(abstractDowdload) : null
    }

    void addAbstractShare(def abstractShareMap) {
        def collection = mongo.getCollection("abstractShare")
        abstractShareMap._id = new AbstractShare().createId()
        collection.insert(toObj(abstractShareMap as Map<String, Object>))
    }

    long getAbstractShareTotal(long userId, String orgId) {
        def collection = mongo.getCollection("abstractShare")
        def query = [userId: userId, orgId: orgId]
        return collection.getCount(toObj(query))
    }

    Paging<AbstractShare> getAbstractShareList(long userId, String orgId, Paging<AbstractShare> paging) {
        def collection = mongo.getCollection("abstractShare")
        def query = [userId: userId, orgId: orgId]
        def cursor = collection.find(toObj(query)).sort(toObj([createTime: -1])).skip(paging.offset).limit(paging.limit)
        while (cursor.hasNext()) {
            paging.list << AbstractShare.toObject(cursor.next())
        }
        cursor.close()
        return paging
    }

    AbstractShare getAbstractShare(String id) {
        def collection = mongo.getCollection("abstractShare")
        def abstractShare = collection.findOne(toObj([_id : id]))
        return abstractShare ? AbstractShare.toObject(abstractShare) : null
    }


}
