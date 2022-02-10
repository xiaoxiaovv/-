package com.istar.mediabroken.repo

import com.istar.mediabroken.Const
import com.istar.mediabroken.entity.*
import com.istar.mediabroken.entity.capture.NewsOperation
import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.entity.capture.SiteAutoPush
import com.istar.mediabroken.utils.EsHolder
import com.istar.mediabroken.utils.MongoHolder
import com.istar.mediabroken.utils.Paging
import com.istar.mediabroken.utils.UrlUtils
import com.mongodb.GroupCommand
import com.mongodb.QueryBuilder
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import weibo4j.org.json.JSONArray

import java.text.SimpleDateFormat
import java.util.regex.Pattern

import static com.istar.mediabroken.utils.MongoHelper.toObj
import static com.istar.mediabroken.utils.UrlUtils.stripUrl

@Repository
@Slf4j
class CaptureRepo {
    @Autowired
    MongoHolder mongo

    @Autowired
    EsHolder esHolder

    List<Site> getValidSites(long userId, Integer siteType) {
        def collection = mongo.getCollection("site")
        def query = [userId: userId, isShow: true]
        if (siteType != null && siteType != 0) {
            query.siteType = siteType
        }
        def cursor = collection.find(toObj(query)).sort(toObj([createTime: 1]))
        def sites = [];
        while (cursor.hasNext()) {
            def site = cursor.next()
            sites << new Site(
                    siteId: site._id,
                    siteName: site.siteName,
                    userId: site.userId,

                    websiteName: site.websiteName,
                    websiteDomain: site.websiteDomain,
                    channelName: site.channelName,
                    channelDomain: site.channelDomain,
                    subjectId: site.subjectId,
                    isShow: site.isShow,
                    createTime: site.createTime,
                    siteType: site.siteType
                )
        }
        cursor.close()

        return sites
    }

    List<Site> getValidSitesBySiteTypes(long userId, List siteTypes) {
        def collection = mongo.getCollection("site")
        def query = [userId: userId, isShow: true, siteType: ['$in': siteTypes]]
        log.debug('query: {}', query)
        def cursor = collection.find(toObj(query)).sort(toObj([createTime: 1]))
        log.debug('cursor: {}', cursor.size());
        def sites = [];
        while (cursor.hasNext()) {
            def site = cursor.next()
            log.debug('site: {}', site)
            sites << new Site(
                    siteId: site._id,
                    siteName: site.siteName,
                    userId: site.userId,
                    websiteName: site.websiteName,
                    websiteDomain: site.websiteDomain,
                    channelName: site.channelName,
                    channelDomain: site.channelDomain,
                    subjectId: site.subjectId,
                    isShow: site.isShow,
                    createTime: site.createTime,
                    siteType: site.siteType
            )
        }
        cursor.close()

        return sites
    }

    Map getSite(long userId, String siteId) {
        def collection = mongo.getCollection("site")
        def site = collection.findOne(toObj([_id: siteId, userId: userId]))

        return site ? [
                siteId: site._id,
                userId: site.userId,
                siteName: site.siteName,

                websiteName: site.websiteName,
                websiteDomain: site.websiteDomain,
                channelName: site.channelName,
                channelDomain: site.channelDomain,
                subjectId: site.subjectId,
                isShow: site.isShow,
                createTime: site.createTime,
                siteType: site.siteType
               ] : null
    }

    Site getSite2(long userId, String siteId) {
        def collection = mongo.getCollection("site")
        def site = collection.findOne(toObj([_id: siteId, userId: userId]))

        return site ? new Site(
                siteId: site._id,
                userId: site.userId,
                siteName: site.siteName,

                websiteName: site.websiteName,
                websiteDomain: site.websiteDomain,
                channelName: site.channelName,
                channelDomain: site.channelDomain,
                subjectId: site.subjectId,
                isShow: site.isShow,
                createTime: site.createTime,
                siteType: site.siteType
        ) : null
    }

    /**
     * 用户已经添加的用户站点
     * @param userId
     * @return
     */
    List<String> getUserWebSiteDomains(long userId,int type) {
        def collection = mongo.getCollection("site")
        def cursor = collection.find(toObj([userId:userId,siteType:type]))
        def WebSiteDomains = [];
        while (cursor.hasNext()) {
            def site = cursor.next()
            if(type == 2){
                WebSiteDomains << site.websiteName
            }else {
                WebSiteDomains << site.websiteDomain
            }
        }
        cursor.close()
        return WebSiteDomains
    }

    Map getSubjectMap(String subjectId) {
        def collection = mongo.getCollection("subjectMap")
        def subjectMap = collection.findOne(toObj([_id: subjectId]))

        return subjectMap ? [
                subjectId: subjectMap._id,
                yqmsUserId: subjectMap.yqmsUserId,
                yqmsSubjectId: subjectMap.yqmsSubjectId]
                : null
    }

    List getHotNewsTodayCache() {
        def collection = mongo.getCollection("cache")
        def obj = collection.findOne(toObj([_id: "focus_news"]))

        return obj ? obj.list as List: []
    }

    void putHotNewsTodayCache(List list) {
        def collection = mongo.getCollection("cache")
        collection.update(toObj([_id: "focus_news"]), toObj([list:list]), true, false)

    }

    SpecialFocus getSpecialFocus(long userId) {
        def collection = mongo.getCollection("specialFocus")
        def specialFocus = collection.findOne(toObj([_id: userId]))

        log.debug('{}', specialFocus)

        return specialFocus ? new SpecialFocus(
                userId: specialFocus._id,
                siteId: specialFocus.siteId,
                siteName: specialFocus.siteName,

                websiteName: specialFocus.websiteName,
                websiteDomain: specialFocus.websiteDomain,
                channelName: specialFocus.channelName,
                channelDomain: specialFocus.channelDomain,
                focusKeywords: specialFocus.focusKeywords,
                subjectId: specialFocus.subjectId,
                isShow: specialFocus.isShow,
                createTime: specialFocus.createTime,
                siteType: specialFocus.siteType,

                yqmsSubjectId: specialFocus.yqmsSubjectId,
                yqmsSubjectName: specialFocus.yqmsSubjectName,
                yqmsUserId: specialFocus.yqmsUserId
        ) : null
    }

    void modifySpecialfocus(SpecialFocus specialFocus) {
        def collection = mongo.getCollection("specialFocus")
        collection.update(
                toObj([
                        _id   : specialFocus.userId]),
                toObj(['$set': [
                        siteName     : specialFocus.siteName,
                        siteId: specialFocus.siteId,

                        websiteName  : specialFocus.websiteName,
                        websiteDomain: specialFocus.websiteDomain,
                        channelName  : specialFocus.channelName,
                        channelDomain: specialFocus.channelDomain,
                        focusKeywords: specialFocus.focusKeywords,
                        subjectId    : specialFocus.subjectId,
                        isShow       : specialFocus.isShow,
                        siteType     : specialFocus.siteType,
                        yqmsSubjectId: specialFocus.yqmsSubjectId,
                        yqmsSubjectName: specialFocus.yqmsSubjectName,
                        yqmsUserId: specialFocus.yqmsUserId
                ]]), true, false)
    }

    int getSiteCount(long userId) {
        def collection = mongo.getCollection("site")
        return collection.getCount(toObj([userId: userId]))
    }

    Paging<Site> getSites(long userId, Paging<Site> paging,String queryKeyWords,int orderType) {
        def collection = mongo.getCollection("site")
        QueryBuilder queryBuilder = QueryBuilder.start()
        if (queryKeyWords) {
            QueryBuilder queryBuilder1 = QueryBuilder.start()
            queryBuilder1.put("siteName").regex(~/.*(?i)${queryKeyWords}.*/)
            QueryBuilder queryBuilder2 = QueryBuilder.start()
            queryBuilder2.put("websiteName").regex(~/.*(?i)${queryKeyWords}.*/)
            QueryBuilder queryBuilder3 = QueryBuilder.start()
            queryBuilder3.or(queryBuilder1.get(),queryBuilder2.get())
            QueryBuilder queryBuilder4 = QueryBuilder.start()
            queryBuilder4.put("userId").is(userId)
            queryBuilder.and(queryBuilder4.get(),queryBuilder3.get())
        }else {
            queryBuilder.put("userId").is(userId)
        }
        def sortQuery=null
        switch (orderType){
            case 1:
                sortQuery=[createTime:-1]
                break;
            case 2:
                sortQuery=[siteName:1]
                break;
            case 3:
                sortQuery=[siteName:-1]
                break;
            case 4:
                sortQuery=[websiteName:1]
                break;
            case 5:
                sortQuery=[websiteName:-1]
                break;
            case 6:
                sortQuery=[siteType:1]
                break;
            case 7:
                sortQuery=[siteType:-1]
                break;
            default:
                sortQuery=[createTime:-1]
                break;
        }
        def cursor = collection.find(queryBuilder.get()).sort(toObj(sortQuery)).skip(paging.offset).limit(paging.limit)

        while (cursor.hasNext()) {
            def site = cursor.next()
            paging.list << new Site(
                    siteId: site._id,
                    siteName: site.siteName,
                    websiteName: site.websiteName,
                    websiteDomain: site.websiteDomain,
                    channelName: site.channelName,
                    channelDomain: site.channelDomain,
                    isShow: site.isShow,
                    siteType: site.siteType,
                    isAutoPush: site.isAutoPush,
                    createTime: site.createTime)
        }
        cursor.close()
        return paging
    }

    long getSiteTotal(long userId,String queryKeyWords) {
        def collection = mongo.getCollection("site")
        QueryBuilder queryBuilder = QueryBuilder.start()
        if (queryKeyWords) {
            QueryBuilder queryBuilder1 = QueryBuilder.start()
            queryBuilder1.put("siteName").regex(~/.*(?i)${queryKeyWords}.*/)
            QueryBuilder queryBuilder2 = QueryBuilder.start()
            queryBuilder2.put("websiteName").regex(~/.*(?i)${queryKeyWords}.*/)
            QueryBuilder queryBuilder3 = QueryBuilder.start()
            queryBuilder3.or(queryBuilder1.get(),queryBuilder2.get())
            QueryBuilder queryBuilder4 = QueryBuilder.start()
            queryBuilder4.put("userId").is(userId)
            queryBuilder.and(queryBuilder4.get(),queryBuilder3.get())
        }else {
            queryBuilder.put("userId").is(userId)
        }

        return collection.getCount(queryBuilder.get())
    }

    String addSite(Site site) {
        def collection = mongo.getCollection("site")
        collection.insert(toObj([
                _id: site.siteId,
                userId: site.userId,
                siteName: site.siteName,
                websiteName: site.websiteName,
                websiteDomain: site.websiteDomain,
                channelName: site.channelName,
                channelDomain: site.channelDomain,
                subjectId: site.subjectId,
                isShow: site.isShow,
                createTime: site.createTime,
                siteType: site.siteType,
                isAutoPush: site.isAutoPush,
        ]))
        return site.siteId
    }
/*查询所属类别有多少条站点数据*/
    Map getSiteTypeCounts(long userId){
        Map typeCount=[type1:0,type2:0,type3:0];
        boolean isSiteExist=true;
//        siteType 1媒体网站 2微信公众号 3专业网站
        def collection = mongo.getCollection("site")
        def query = [:]
        query.userId=userId;

        query.siteType=1;
        def obj1 = collection.find(toObj(query))
        typeCount.type1=obj1.size();

        query.siteType=2;
        def obj2 = collection.find(toObj(query))
        typeCount.type2=obj2.size();

        query.siteType=3;
        def obj3 = collection.find(toObj(query))
        typeCount.type3=obj3.size();

        return typeCount;
    }
    boolean isSiteExist(Site site,String addOrEdit){
        boolean isSiteExist=true;
//        siteType 1媒体网站 2微信公众号 3专业网站
        def collection = mongo.getCollection("site")
        def query = [:]
        if ("2".equals(String.valueOf(site.siteType)))//2微信公众号
        {
            //判断微信公众号名称
            query.userId = site.userId;
            query.websiteName=site.websiteName;
            query.siteType = site.siteType;
        }else{
            //判断url
            if (site.websiteDomain) {
                site.websiteDomain = stripUrl(site.websiteDomain)
            }
            query.userId = site.userId;
            query.websiteDomain=site.websiteDomain;
            query.siteType = site.siteType;
        }
        def sites = collection.find(toObj(query))
        if(sites.size()>1){
            isSiteExist=true
        }else if(sites.size()==1)
        {
            def obj = sites[0]
            if ("0".equals(addOrEdit)){
                //添加判断
                isSiteExist=obj ? true: false
            }else if ("1".equals(addOrEdit)){
                //编辑判断
                isSiteExist=obj ? true: false
                if ("2".equals(String.valueOf(site.siteType)) &&obj._id==site.siteId && obj.websiteName==site.websiteName)//2微信公众号
                {
                    //判断微信公众号名称
                    isSiteExist=false;
                }else{
                    //判断url
                    if (site.websiteDomain) {
                        site.websiteDomain = stripUrl(site.websiteDomain)
                    }
                    if (obj._id==site.siteId &&obj.websiteDomain==site.websiteDomain){
                        isSiteExist=false;
                    }
                }
            }
        }else {
            isSiteExist=false
        }
        return isSiteExist;
    }
    SubjectMap getSubjectMap(Site site) {
        def collection = mongo.getCollection("subjectMap")
        def query = [:]
        if (site.siteType == Const.ST_WEBCHAT_PUBLIC_ACCOUNT) {
            if (site.websiteName) query.account = site.websiteName
            query.subjectType = Const.SMT_WEBCHAT_PUBLCI_ACCOUNT
        } else {
            if (site.websiteDomain) query.websiteDomain = site.websiteDomain
            if (site.channelDomain) query.channelDomain = site.channelDomain
            // todo 对于特殊关注来说, 不应该用关键字查询
            if (site.focusKeywords) query.focusKeywords = site.focusKeywords
            query.subjectType = Const.SMT_WEB_SITE
        }
        def res = collection.findOne(toObj(query))
        return res ? new SubjectMap(
                subjectId: res._id,
                yqmsSubjectId: res.yqmsSubjectId,
                yqmsUserId: res.yqmsUserId,
                yqmsSubjectName: res.yqmsSubjectName,

                websiteDomain: res.websiteDomain,
                channelDomain: res.channelDomain,
                focusKeywords: res.focusKeywords,
                account: res.account,
                createTime: res.createTime
        ) : null
    }

    def addSubjectMap(SubjectMap subjectMap) {
        def collection = mongo.getCollection("subjectMap")
        def obj = [
                _id: subjectMap.subjectId,
                yqmsSubjectName: subjectMap.yqmsSubjectName,
                yqmsSubjectId: subjectMap.yqmsSubjectId,
                yqmsUserId: subjectMap.yqmsUserId,
                subjectType: subjectMap.subjectType,
                createTime: new Date(),
                updateTime: new Date()
        ]
        if (subjectMap.websiteDomain) obj.websiteDomain = subjectMap.websiteDomain
        if (subjectMap.channelDomain) obj.channelDomain = subjectMap.channelDomain
        if (subjectMap.focusKeywords) obj.focusKeywords = subjectMap.focusKeywords
        if (subjectMap.account) obj.account = subjectMap.account
        collection.insert(toObj(obj))
    }

    void modifySubjectMap(SubjectMap subjectMap) {
        def collection = mongo.getCollection("subjectMap")
        collection.update(
                toObj([
                        _id   : subjectMap.subjectId]),
                toObj(['$set': [
                        yqmsSubjectName: subjectMap.yqmsSubjectName,
                        yqmsSubjectId: subjectMap.yqmsSubjectId,
                        yqmsUserId: subjectMap.yqmsUserId,
                        subjectType: subjectMap.subjectType,
                        websiteDomain:  subjectMap.websiteDomain,
                        channelDomain: subjectMap.channelDomain,
                        focusKeywords: subjectMap.focusKeywords,
                        account: subjectMap.account,
                        updateTime: new Date()
                ]]))
    }


    String modifySite(Site site) {
        def collection = mongo.getCollection("site")
        collection.update(
                toObj([
                        userId: site.userId,
                        _id   : site.siteId]),
                toObj(['$set': [
                        siteName     : site.siteName,
                        websiteName  : site.websiteName,
                        websiteDomain: site.websiteDomain,
                        channelName  : site.channelName,
                        channelDomain: site.channelDomain,
                        subjectId    : site.subjectId,
                        isShow       : site.isShow,
                        siteType     : site.siteType,
                        isAutoPush   : site.isAutoPush,
                ]]))
        //移除CaptureStat信息
        removeCaptureStat(site.userId,site.siteId)
        return site.siteId
    }

    long getNewsPushTotal(long userId, String orgId, int status) {
        def collection = mongo.getCollection("newsOperation")
        def query = [userId: userId, orgId: orgId, operationType: 1]
        if(status > 0) query.status = status
        return collection.getCount(toObj(query))
    }

    Paging<NewsPush> getNewsPushList(long userId, String orgId, int status, Paging<NewsPush> paging) {
        def collection = mongo.getCollection("newsOperation")
        def query = [userId: userId, orgId: orgId, operationType: 1]
        if(status > 0) query.status = status
        def cursor = collection.find(toObj(query)).sort(toObj([createTime: -1])).skip(paging.offset).limit(paging.limit)
        while (cursor.hasNext()) {
            paging.list << NewsPush.toObject(cursor.next())
        }
        cursor.close()
        return paging
    }

    List<NewsPush> getNewsPushList(long userId, String orgId, Date createTime, int limit) {
        def collection = mongo.getCollection("newsOperation")
        def query = [userId: userId, orgId: orgId, operationType: 1, createTime : [ $gte : createTime]]
        def cursor = collection.find(toObj(query)).sort(toObj([publishDay: -1,reprintCount:-1])).limit(limit)
        def list = []
        while (cursor.hasNext()) {
            list << NewsPush.toObject(cursor.next())
        }
        cursor.close()
        return list
    }

    List<NewsPush> getNewsPushListById(long userId, String orgId, List<String> newsIdList) {
        def collection = mongo.getCollection("newsOperation")
        def query = [userId: userId, orgId: orgId,operationType: 1, newsId : [ $in : newsIdList ]]
        def cursor = collection.find(toObj(query))
        def list = []
        while (cursor.hasNext()) {
            list << NewsPush.toObject(cursor.next())
        }
        cursor.close()
        return list
    }

    NewsPush getNewsPush(long userId, String orgId, String newsId) {
        def collection = mongo.getCollection("newsOperation")
        def newsPush = collection.findOne(toObj([
                userId       : userId,
                orgId        : orgId,
                operationType: 1,
                newsId       : newsId,
                status       : Const.PUSH_STATUS_NOT_PUSH
        ]))
        return newsPush ? NewsPush.toObject(newsPush) : null
    }
    List<News> getNewsFromNewsOperationByIds(List<String> newsOperationIdList) {
        List<News> list = []
        def collection = mongo.getCollection("newsOperation")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("_id").in(newsOperationIdList)
        def cursor = collection.find(queryBuilder.get())
        def newsIds = []
        while (cursor.hasNext()) {
            def newsOperation = cursor.next()
            if(!(newsIds.contains(newsOperation.newsId))){
                newsIds << newsOperation.newsId
                list << new News([
                        title       : newsOperation.news.title,
                        source      : newsOperation.news.source,
                        author      : newsOperation.news.author,
                        keyword     : newsOperation.news.keyword,
                        url         : newsOperation.news.url,
                        newsId      : newsOperation.newsId,
                        content     : newsOperation.news.content,
                        contentAbstract : newsOperation.news.abstract,
                        createTime  : newsOperation.createTime
                ])
            }
        }
        cursor.close()
        return list
    }

    def addNewsListPush(long userId, String orgId, List newsDetailList) {
        def collection = mongo.getCollection("newsPush")
        def newsList = []
        def mongoNewsId = []
        //更新mongo id
        newsDetailList.each {
            String newsId = it.newsId as String
            it._id = new NewsPush(userId, orgId, newsId).createId()
            mongoNewsId.add(it._id)
        }
        //获取已经推送的news
        def pushedNewsId = []
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('_id').in(mongoNewsId)
        def pushedNewsList = collection.find(queryBuilder.get())
        pushedNewsList.each {
            pushedNewsId.add(it._id)
        };
        pushedNewsList.close()
        //将未推送的news插入mongo
        int listLength = newsDetailList.size()
        for (int i = 0; i < listLength ; i++) {
            def news = newsDetailList.get(i)
            if(!pushedNewsId.contains(news._id)){
                newsList.add(toObj(news as Map<String, Object>))
            }
        }
        if(newsList.size() > 0){
            collection.insert(newsList);
        }
    }
    /**
     * 添加newspush的列表信息
     * @param userId
     * @param orgId
     * @param newsDetailList
     * @param newsIds
     * @return
     */
    int addNewsPushList(long userId, String orgId, List newsDetailList,List newsIds) {
        def count = 0;
        Date now = new Date()
        def collection = mongo.getCollection("newsOperation")
        def newsList = []
        //获取已经推送的news
        def pushedNewsId = []
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('operationType').is(1)
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('orgId').is(orgId)
        queryBuilder.put('newsId').in(newsIds)
        def pushedNewsList = collection.find(queryBuilder.get())
        pushedNewsList.each {
            pushedNewsId.add(it.newsId)
        };
        pushedNewsList.close()
        //将未推送的news插入mongo
        int listLength = newsDetailList.size()
        for (int i = 0; i < listLength ; i++) {
            def news = newsDetailList.get(i)
            if(!pushedNewsId.contains(news.newsId)){
                news._id = UUID.randomUUID().toString()
                news.operationType = 1
                newsList.add(toObj(news as Map<String, Object>))
                count++
            }
        }
        if(newsList.size() > 0){
            collection.insert(newsList);
        }
        return count
    }

    def getOpenNewsPushList(String orgId){
        def collection = mongo.getCollection("newsOperation")
        def cursor = collection.find(toObj([
                orgId       : orgId,
                operationType: 1,
                status      : Const.PUSH_STATUS_NOT_PUSH/*,
                pushType    : PushTypeEnum.NEWS_PUSH.index*/
        ])).sort(toObj([createTime: 1])).limit(10)
        def list = []
        while (cursor.hasNext()) {
            list << NewsOperation.toObject(cursor.next())
        }
        cursor.close()
        return list
    }

    def modifyNewsPushStatus(String orgId, def newsIdList, int status){
        def collection = mongo.getCollection("newsOperation")
        collection.update(toObj([
                orgId   : orgId,
                /*operationType: 1,*/
                status  : status == Const.PUSH_STATUS_PUSHED ? Const.PUSH_STATUS_NOT_PUSH : Const.PUSH_STATUS_PUSHED,
                newsId  : [ $in : newsIdList ]
        ]),toObj([
                '$set': [
                        status      : status,
                        updateTime  : new Date()
                ]
        ]), false, true)
    }

    long getNewsPushedCount(long userId, String siteId, Date start, Date end) {
        def collection = mongo.getCollection("newsOperation")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('operationType').is(1)
        queryBuilder.put('siteId').is(siteId)
        queryBuilder.put('status').is(2)
        queryBuilder.put('createTime').greaterThanEquals(start)
        queryBuilder.put('createTime').lessThanEquals(end)
        long count = collection.count(queryBuilder.get())
        return count;
    }
    long getUserNewsPushedCountByDate(long userId, Date start, Date end) {
        def collection = mongo.getCollection("newsOperation")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('operationType').is(1)
        queryBuilder.put('status').is(2)
        queryBuilder.put('createTime').greaterThanEquals(start)
        queryBuilder.put('createTime').lessThanEquals(end)
        long count = collection.count(queryBuilder.get())
        return count;
    }
    SummaryPush getSummaryPush(long userId, String orgId, String summaryId) {
        def collection = mongo.getCollection("summaryPush")
        def summaryPush = collection.findOne(toObj([
                summaryId: summaryId,
                userId  : userId,
                orgId   : orgId,
                status  : Const.PUSH_STATUS_NOT_PUSH
        ]))
        return summaryPush ? SummaryPush.toObject(summaryPush) : null
    }

    void addSummaryPush(def summaryPushMap) {
        long userId = summaryPushMap.userId as long
        String orgId = summaryPushMap.orgId as String
        String summaryId = summaryPushMap.summaryId as String

        def collection = mongo.getCollection("summaryPush")
        summaryPushMap._id = new SummaryPush().createId()
        collection.insert(toObj(summaryPushMap as Map<String, Object>))
    }

    long getSummaryPushTotal(long userId, String orgId, int status) {
        def collection = mongo.getCollection("summaryPush")
        def query = [userId: userId, orgId: orgId]
        if(status > 0) query.status = status
        return collection.getCount(toObj(query))
    }

    List<SummaryPush> getSummaryPushList(long userId, String orgId) {
        def collection = mongo.getCollection("summaryPush")
        def query = [userId: userId, orgId: orgId]
        def cursor = collection.find(toObj(query)).sort(toObj([createTime: -1]))
        def list = []
        while (cursor.hasNext()) {
            list << SummaryPush.toObject(cursor.next())
        }
        cursor.close()
        return list
    }

    Paging<SummaryPush> getSummaryPushList(long userId, String orgId, int status, Paging<SummaryPush> paging) {
        def collection = mongo.getCollection("summaryPush")
        def query = [userId: userId, orgId: orgId]
        if(status > 0) query.status = status
        def cursor = collection.find(toObj(query)).sort(toObj([createTime: -1])).skip(paging.offset).limit(paging.limit)
        while (cursor.hasNext()) {
            paging.list << SummaryPush.toObject(cursor.next())
        }
        cursor.close()
        return paging
    }

    AbstractPush getAbstractPush(String id) {
        def collection = mongo.getCollection("abstractPush")
        def abstractPush = collection.findOne(toObj([
                _id  : id
        ]))
        return abstractPush ? AbstractPush.toObject(abstractPush) : null
    }

    long getAbstractPushTotal(long userId, String orgId, int status) {
        def collection = mongo.getCollection("abstractPush")
        def query = [userId: userId, orgId: orgId]
        if(status > 0) query.status = status
        return collection.getCount(toObj(query))
    }

    void addAbstractPush(def abstractPushMap) {
        def collection = mongo.getCollection("abstractPush")
        abstractPushMap._id = new AbstractPush().createId()
        collection.insert(toObj(abstractPushMap as Map<String, Object>))
    }

    Paging<AbstractPush> getAbstractPushList(long userId, String orgId, int status, Paging<AbstractPush> paging) {
        def collection = mongo.getCollection("abstractPush")
        def query = [userId: userId, orgId: orgId]
        if(status > 0) query.status = status
        def cursor = collection.find(toObj(query)).sort(toObj([createTime: -1])).skip(paging.offset).limit(paging.limit)
        while (cursor.hasNext()) {
            paging.list << AbstractPush.toObject(cursor.next())
        }
        cursor.close()
        return paging
    }

    List<SummaryPush> getOpenSummaryPushList(String orgId){
        def collection = mongo.getCollection("summaryPush")
        def cursor = collection.find(toObj([
                orgId       : orgId,
                status      : Const.PUSH_STATUS_NOT_PUSH,
                pushType    : PushTypeEnum.SUMMARY_PUSH.index
        ])).sort(toObj([createTime: 1])).limit(10)
        def list = []
        while (cursor.hasNext()) {
            list << SummaryPush.toObject(cursor.next())
        }
        cursor.close()
        return list
    }

    List<AbstractPush> getOpenAbstractPushList(String orgId){
        def collection = mongo.getCollection("abstractPush")
        def cursor = collection.find(toObj([
                orgId       : orgId,
                status      : Const.PUSH_STATUS_NOT_PUSH,
                pushType    : PushTypeEnum.ABSTRACT_PUSH.index
        ])).sort(toObj([createTime: 1])).limit(10)
        def list = []
        while (cursor.hasNext()) {
            list << AbstractPush.toObject(cursor.next())
        }
        cursor.close()
        return list
    }

    void modifySummaryPushStatus(String orgId, def summaryIdList, int status){
        def collection = mongo.getCollection("summaryPush")
        collection.update(toObj([
                orgId   : orgId,
                status  : status == Const.PUSH_STATUS_PUSHED ? Const.PUSH_STATUS_NOT_PUSH : Const.PUSH_STATUS_PUSHED,
                summaryId   : [ $in : summaryIdList ]
        ]),toObj([
                '$set': [
                        status      : status,
                        updateTime  : new Date()
                ]
        ]), false, true)
    }

    void modifyAbstractPushStatus(String orgId, def abstractIdList, int status){
        def collection = mongo.getCollection("abstractPush")
        collection.update(toObj([
                orgId   : orgId,
                status  : status == Const.PUSH_STATUS_PUSHED ? Const.PUSH_STATUS_NOT_PUSH : Const.PUSH_STATUS_PUSHED,
                abstractId  : [ $in : abstractIdList ]
        ]),toObj([
                '$set': [
                        status      : status,
                        updateTime  : new Date()
                ]
        ]), false, true)
    }

    void addNewsShare(def newsShareMap) {
        def collection = mongo.getCollection("newsOperation")
        newsShareMap._id = UUID.randomUUID().toString()
        newsShareMap.operationType = 2
        collection.insert(toObj(newsShareMap as Map<String, Object>))
    }

    long getNewsShareTotal(long userId, String orgId) {
        def collection = mongo.getCollection("newsOperation")
        def query = [userId: userId, orgId: orgId, operationType : 2]
        return collection.getCount(toObj(query))
    }

    Paging<NewsShare> getNewsShareList(long userId, String orgId, Paging<NewsShare> paging) {
        def collection = mongo.getCollection("newsOperation")
        def query = [userId: userId, orgId: orgId ,operationType : 2]
        def cursor = collection.find(toObj(query)).sort(toObj([createTime: -1])).skip(paging.offset).limit(paging.limit)
        while (cursor.hasNext()) {
            paging.list << NewsShare.toObject(cursor.next())
        }
        cursor.close()
        return paging
    }

    long getNewsShareCount(long userId, String siteId, Date start, Date end) {
        def collection = mongo.getCollection("newsOperation")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('operationType').is(2)
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('siteId').is(siteId)
        queryBuilder.put('createTime').greaterThanEquals(start)
        queryBuilder.put('createTime').lessThanEquals(end)
        long count = collection.count(queryBuilder.get())
        return count;
    }

    long getUserNewsShareCountByDate(long userId, Date start, Date end) {
        def collection = mongo.getCollection("newsOperation")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('operationType').is(2)
        queryBuilder.put('createTime').greaterThanEquals(start)
        queryBuilder.put('createTime').lessThanEquals(end)
        long count = collection.count(queryBuilder.get())
        return count;
    }

    List<Site> getRecommandWebSites(String[] contentTypes, String[] mediaTypes, int limit) {
        def collection = mongo.getCollection("recommandSite")
        def query =
                ['$and': [
                    ['$or':
                             [[contentType: ['$in': contentTypes]], [mediaType: ['$in': mediaTypes]]]
                    ],
                    [siteType: ['$in': ['中央新闻网站', '部委网站', '网络', '网站']]],
                    [siteDomain: Pattern.compile(/^http:/)]]]
        println toObj(query)
        def cursor = collection.find(toObj(query)).sort(toObj([ranking: 1])).limit(limit)
        def list = []
        while (cursor.hasNext()) {
            def site = cursor.next()
            list << new Site(
                    siteName: site.siteName,
                    websiteName: site.siteName,
                    websiteDomain: UrlUtils.stripUrl(site.siteDomain),
                    siteType: Const.SMT_WEB_SITE)
        }
        cursor.close()

        return list
    }

    List<Site> getRecommandWechatAccounts(String[] contentTypes, String[] mediaTypes, int limit) {
        def collection = mongo.getCollection("recommandSite")
        def query =
                ['$and': [
                        ['$or':
                                 [[contentType: ['$in': contentTypes]], [mediaType: ['$in': mediaTypes]]]
                        ],
                        [siteType: ['$in': ['微信公众号']]]]]
        println toObj(query)
        def cursor = collection.find(toObj(query)).sort(toObj([ranking: 1])).limit(limit)
        def list = []
        while (cursor.hasNext()) {
            def site = cursor.next()
            list << new Site(
                    siteName: site.siteName,
                    websiteName: site.siteName,
                    websiteDomain: UrlUtils.stripUrl(site.siteDomain),
                    siteType: Const.SMT_WEBCHAT_PUBLCI_ACCOUNT)
        }
        cursor.close()

        return list
    }

    List<Site> getAutoPushSites(Integer pageNo,Integer pageSize){
        def siteCollection = mongo.getCollection("site")
        def currDate = new Date();
        def date  = new Date(currDate.getTime() - 60 * 5000)
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMddHHmmss");
        String currTime = simpleDateFormat.format(date);
        def siteList = []
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('isAutoPush').is(true)
        def sites = siteCollection.find(queryBuilder.get()).limit(pageSize).skip((pageNo - 1) * pageSize).sort(toObj([_id: 1]))
        sites.each {
            Map siteMap = new HashMap<String,Object>();
            SiteAutoPush autoPush = getSiteAutoPushById(it._id)
            if(autoPush == null || autoPush.siteId == null){
                if(autoPush == null){
                    autoPush = new SiteAutoPush()
                }
                autoPush.siteId = it._id
                autoPush.prevStartTime = currTime
                autoPush.prevEndTime = currTime
                autoPush.prevNewsCount = 0L
                autoPush.prevPushCount = 0L
                autoPush.totalNewsCount = 0L
                autoPush.totalPushCount = 0L
                autoPush.updateTime = currDate
                autoPush.createTime = currDate
                addSiteAutoPush(autoPush)
            }
            siteMap.put("site",it)
            siteMap.put("siteAutoPush",autoPush)
            siteList.add(siteMap)
        }
        sites.close()
        return siteList
    }

    List getSitesByPage(Integer pageNo,Integer pageSize){
        def siteCollection = mongo.getCollection("site")
        def siteList = []
        QueryBuilder queryBuilder = QueryBuilder.start()
        def sites = siteCollection.find(queryBuilder.get()).limit(pageSize).skip((pageNo - 1) * pageSize).sort(toObj([userId: -1]))
        sites.each {
            siteList.add(it)
        }
        sites.close()
        return siteList
    }
    SiteAutoPush getSiteAutoPushById(String id) {
        SiteAutoPush siteAutoPush = new SiteAutoPush();
        def collection = mongo.getCollection("siteAutoPush")
        QueryBuilder autoPushQuery = QueryBuilder.start()
        autoPushQuery.put('_id').is(id)
        def autoPush = collection.findOne(autoPushQuery.get())
        if(autoPush){
            siteAutoPush.siteId = autoPush._id
            siteAutoPush.prevStartTime = autoPush.prevStartTime
            siteAutoPush.prevEndTime = autoPush.prevEndTime
            siteAutoPush.prevNewsCount = autoPush.prevNewsCount
            siteAutoPush.prevPushCount = autoPush.prevPushCount
            siteAutoPush.totalNewsCount = autoPush.totalNewsCount
            siteAutoPush.totalPushCount = autoPush.totalPushCount
            siteAutoPush.updateTime = autoPush.updateTime
            siteAutoPush.createTime = autoPush.createTime
        }
        return siteAutoPush
    }

    String addSiteAutoPush(SiteAutoPush siteAutoPush) {
        def collection = mongo.getCollection("siteAutoPush")
        collection.insert(toObj([
                _id: siteAutoPush.siteId,
                prevStartTime: siteAutoPush.prevStartTime,
                prevEndTime  : siteAutoPush.prevEndTime,
                prevNewsCount: siteAutoPush.prevNewsCount,
                prevPushCount: siteAutoPush.prevPushCount,
                totalNewsCount: siteAutoPush.totalNewsCount,
                totalPushCount: siteAutoPush.totalPushCount,
                updateTime    : siteAutoPush.updateTime,
                createTime    : siteAutoPush.createTime,
        ]))
        return siteAutoPush.siteId
    }
    String modifySiteAutoPush(SiteAutoPush siteAutoPush) {
        def collection = mongo.getCollection("siteAutoPush")
        collection.update(
                toObj([ _id   : siteAutoPush.siteId]),
                toObj(['$set': [
                        prevStartTime: siteAutoPush.prevStartTime,
                        prevEndTime: siteAutoPush.prevEndTime,
                        prevNewsCount: siteAutoPush.prevNewsCount,
                        prevPushCount: siteAutoPush.prevPushCount,
                        totalNewsCount: siteAutoPush.totalNewsCount,
                        totalPushCount: siteAutoPush.totalPushCount,
                        updateTime: siteAutoPush.updateTime,
                        createTime: siteAutoPush.createTime,
                ]]))
        return siteAutoPush.siteId
    }

    NewsDownload getNewsDownload(long userId, String orgId, String newsId) {
        def collection = mongo.getCollection("newsDownload")
        def newsDownload = collection.findOne(toObj([
                _id     : (new NewsDownload(userId, orgId, newsId)).createId()
        ]))
        return newsDownload ? NewsDownload.toObject(newsDownload) : null
    }

    void addNewsDownload(LoginUser user,String newsId,def news) {

        NewsDownload newsDownload = new NewsDownload();
        newsDownload.userId = user.userId
        newsDownload.orgId = user.orgId
        newsDownload.newsId = newsId
        newsDownload.news = news
        newsDownload.updateTime = new Date();
        newsDownload.createTime = new Date();

        def collection = mongo.getCollection("newsDownload")
        NewsDownload newsDownload1 = getNewsDownload(user.userId, user.orgId, newsId)
        if(!newsDownload1) {
            def downloadMap = newsDownload.toMap();
            downloadMap._id = newsDownload.createId()
            collection.insert(toObj(downloadMap))
        }
    }

    long getNewsDownloadTotal(long userId, String orgId) {
        def collection = mongo.getCollection("newsDownload")
        def query = [userId: userId, orgId: orgId]
        return collection.getCount(toObj(query))
    }

    Paging<NewsDownload> getNewsDownloadList(long userId, String orgId, Paging<NewsDownload> paging) {
        def collection = mongo.getCollection("newsDownload")
        def query = [userId: userId, orgId: orgId]
        def cursor = collection.find(toObj(query)).sort(toObj([createTime: -1])).skip(paging.offset).limit(paging.limit)
        while (cursor.hasNext()) {
            paging.list << NewsDownload.toObject(cursor.next())
        }
        cursor.close()
        return paging
    }

    String addFocus(Focus focus) {
        def collection = mongo.getCollection("focus")
        collection.insert(toObj([
                _id: focus.focusId,
                userId: focus.userId,
                focusName: focus.focusName,
                mustKeywords: focus.mustKeywords,
                shouldKeywords: focus.shouldKeywords,
                eventKeywords: focus.eventKeywords,
                notKeywords: focus.notKeywords,
                expression: focus.expression,
                createTime: focus.createTime,
                yqmsSubjectId: focus.yqmsSubjectId,
                yqmsSubjectName: focus.yqmsSubjectName,
                yqmsUserId: focus.yqmsUserId,
                captureCount: focus.captureCount ? focus.captureCount : 0
        ]))
        return focus.focusId
    }

    int getFocusCount(long userId) {
        def collection = mongo.getCollection("focus")
        return collection.getCount(toObj([userId: userId]))
    }

    Focus getFocus(Focus focus2) {
        def collection = mongo.getCollection("focus")
        def focus = collection.findOne(toObj([_id: focus2.focusId, userId: focus2.userId]))

        return focus ? new Focus(
                focusId: focus._id,
                focusName: focus.focusName,
                userId: focus.userId,
                mustKeywords: focus.mustKeywords,
                shouldKeywords: focus.shouldKeywords,
                eventKeywords: focus.eventKeywords,
                notKeywords: focus.notKeywords,
                expression: focus.expression,
                createTime: focus.createTime,
                yqmsSubjectId: focus.yqmsSubjectId,
                yqmsUserId: focus.yqmsUserId,
                yqmsSubjectName: focus.yqmsSubjectName,
                captureCount: focus.captureCount ? focus.captureCount : 0
        ) : null
    }

    void modifyFocus(Focus focus) {
        def collection = mongo.getCollection("focus")
        collection.update(
                toObj([
                        _id   : focus.focusId]),
                toObj(['$set': [
                        focusName: focus.focusName,
                        userId: focus.userId,
                        mustKeywords: focus.mustKeywords,
                        shouldKeywords: focus.shouldKeywords,
                        eventKeywords: focus.eventKeywords,
                        notKeywords: focus.notKeywords,
                        expression: focus.expression,
                        createTime: focus.createTime,
                        yqmsSubjectId: focus.yqmsSubjectId,
                        yqmsUserId: focus.yqmsUserId,
                        captureCount: focus.captureCount ? focus.captureCount : 0
                ]]), true, false)
    }

    List getFocusList(long userId) {
        def collection = mongo.getCollection("focus")
        def query = [userId: userId]
        def cursor = collection.find(toObj(query))
        def list = []
        while (cursor.hasNext()) {
            def focus = cursor.next()
            list << new Focus(
                    focusId: focus._id,
                    focusName: focus.focusName,
                    userId: focus.userId,
                    mustKeywords: focus.mustKeywords,
                    shouldKeywords: focus.shouldKeywords,
                    eventKeywords: focus.eventKeywords,
                    notKeywords: focus.notKeywords,
                    expression: focus.expression,
                    createTime: focus.createTime,
                    yqmsSubjectId: focus.yqmsSubjectId,
                    yqmsSubjectName: focus.yqmsSubjectName,
                    yqmsUserId: focus.yqmsUserId,
                    captureCount: focus.captureCount ? focus.captureCount : 0)
        }
        cursor.close()

        return list
    }

    List getFocusListByPage(Integer pageSize,Integer pageNo ) {
        def collection = mongo.getCollection("focus")
        def query = [:]
        def cursor = collection.find(toObj(query)).limit(pageSize).skip((pageNo - 1) * pageSize)
        def list = []
        while (cursor.hasNext()) {
            def focus = cursor.next()
            list << new Focus(
                    focusId: focus._id,
                    focusName: focus.focusName,
                    userId: focus.userId,
                    mustKeywords: focus.mustKeywords,
                    shouldKeywords: focus.shouldKeywords,
                    eventKeywords: focus.eventKeywords,
                    notKeywords: focus.notKeywords,
                    expression: focus.expression,
                    createTime: focus.createTime,
                    yqmsSubjectId: focus.yqmsSubjectId,
                    yqmsSubjectName: focus.yqmsSubjectName,
                    yqmsUserId: focus.yqmsUserId,
                    captureCount: focus.captureCount ? focus.captureCount : 0)
        }
        cursor.close()

        return list
    }

    void removeFocus(Focus focus) {
        def collection = mongo.getCollection("focus")
        collection.remove(toObj([_id: focus.focusId, userId: focus.userId]))
    }

    PersonalAllSite getPersonlAllSiteSubject(long userId) {
        def collection = mongo.getCollection("personalAllSite")
        def result = collection.findOne(toObj([_id: userId]))

        return result ? new PersonalAllSite(
                userId: result._id,
                yqmsUserId: result.yqmsUserId,
                yqmsSubjectId: result.yqmsSubjectId,
                yqmsSubjectName: result.yqmsSubjectName
        ) : null
    }

    void addPersionAllSiteSubject(PersonalAllSite subject) {
        def collection = mongo.getCollection("personalAllSite")
        collection.insert(toObj([
                _id: subject.userId,
                yqmsUserId: subject.yqmsUserId,
                yqmsSubjectId: subject.yqmsSubjectId,
                yqmsSubjectName: subject.yqmsSubjectName
        ]))
    }

    List getYqmsUserIdsForPersonalAllSiteSubject() {
        def collection = mongo.getCollection("personalAllSite")
        def cmd = new GroupCommand(
                collection,
                toObj([yqmsUserId: 1]),
                toObj([:]),
                toObj([count:0]),
"""
function( curr, result ) {
    result.count++;
}
""",
                '')
        // todo vvv higth 完善,使用新的group方法
        def array = collection.group(cmd) as JSONArray
        def yqmsUserIds = []
        for (int i = 0; i < array.length(); i++) {
            yqmsUserIds << (array.get(i).yqmsUserId as long)
        }

        yqmsUserIds.sort { a, b ->
            return a < b
        }

        return yqmsUserIds
    }

    List getSiteTopN(long userId,Date today,int siteType) {
        def collection = mongo.getCollection("captureStat")
        def cursor = collection.find(toObj([userId: userId, siteType: siteType, date: today])).sort(toObj([captureCount: -1])).limit(3)
        def list = []
        while (cursor.hasNext()) {
            def it = cursor.next()
            list << [
                    name:  it.siteName,
                    captureCount:  it.captureCount,
                    pushCount:  it.pushCount
            ]
        }
        cursor.close()
        return list
    }

    def getSumaryToday(long userId, Date today) {
        def collection = mongo.getCollection("captureStat")
        def cursor = collection.find(toObj([userId: userId, date: today]))

        def list = []
        while (cursor.hasNext()) {
            def it = cursor.next()
            list << [
                    captureCount:  it.captureCount,
                    pushCount:  it.pushCount,
                    shareCount: it.shareCount
            ]
        }
        def map = mapValueSum(list)
        def resultList = new HashMap<String, Integer>()
        for(String key:map.keySet()){
            def value = map.get(key)
            int sum = 0
            for(int values:value){
                sum += values
            }
            resultList.put(key, sum)
        }
        cursor.close()
        return resultList

    }

    Map mapValueSum(List<Map> list){
        def map = new HashMap<>()
        for (Map m : list) {
            def it = m.keySet().iterator()
            while (it.hasNext()) {
                def key = it.next()
                if (!map.containsKey(key)) {
                    def newList = []
                    newList.add(m.get(key))
                    map.put(key,newList)
                } else {
                    map.get(key).add(m.get(key))
                }
            }
        }
        return map
    }

    List getTrendWeekly(long userId, Date start, Date today) {
        def collection = mongo.getCollection("captureStat")
        def cmd = new GroupCommand(
                collection,
                toObj([date: 1]),
                toObj([userId: userId, date:[$gte: start, $lt: today]]),
                toObj([captureCount: 0, pushCount: 0, shareCount: 0]),
                """
                    function(curr,result){
                        result.captureCount += curr.captureCount;
                        result.pushCount += curr.pushCount;
                        result.shareCount += curr.shareCount;
                    }
                """,
                ''
        )
        def list = collection.group(cmd)
        return list
    }

    String saveCaptureStat(CaptureStat captureStat) {
        def collection = mongo.getCollection("captureStat")
        def insertResult = collection.save(toObj(captureStat.toMap()))
        return insertResult
    }
    Map getCaptureStatById(String id) {
        def collection = mongo.getCollection("captureStat")
        def captureStat = collection.findOne(toObj([_id: id]))

        return captureStat?[
                _id              : captureStat._id,
                userId           : captureStat.userId,
                siteId           : captureStat.siteId,
                siteName         : captureStat.siteName,
                siteType         : captureStat.siteType,
                captureCount    : captureStat.captureCount,
                pushCount        : captureStat.pushCount,
                shareCount       : captureStat.shareCount,
                date              : captureStat.date,
                updateTime       : captureStat.updateTime,
        ]:null
    }
    long getCaptureCountByDate(long userId, Date today) {
        def collection = mongo.getCollection("captureStat")
        QueryBuilder key = QueryBuilder.start()
        key.put('userId').is('userId')
        key.put('date').is('today')

        QueryBuilder cond = QueryBuilder.start()
        cond.put('userId').is(userId)
        cond.put('date').is(today)

        QueryBuilder init = QueryBuilder.start()
        init.put('sum').is(0)

        String reduce = """
                    function(curr,result){
                        result.sum += curr.captureCount;
                    }
                """
        def result = collection.group(key.get(),cond.get(),init.get(),reduce)
        return result.size() > 0 ? result[0].sum : 0
    }
    long getPushCountByDate(long userId, Date today) {
        def collection = mongo.getCollection("captureStat")
        QueryBuilder key = QueryBuilder.start()
        key.put('userId').is('userId')
        key.put('date').is('today')

        QueryBuilder cond = QueryBuilder.start()
        cond.put('userId').is(userId)
        cond.put('date').is(today)

        QueryBuilder init = QueryBuilder.start()
        init.put('sum').is(0)

        String reduce = """
                    function(curr,result){
                        result.sum += curr.pushCount;
                    }
                """
        def result = collection.group(key.get(),cond.get(),init.get(),reduce)
        return result.size() > 0 ? result[0].sum : 0
    }

    void removeCaptureStat(long userId,String siteId ) {
        def collection = mongo.getCollection("captureStat")
        collection.remove(toObj([userId: userId, siteId: siteId]))
        return
    }

    long getRootSiteClassificationCount() {
        def collection = mongo.getCollection("siteClassification")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("parentId").is("")
        def result = collection.count(queryBuilder.get())
        return (result as long)
    }

    Paging<SiteClassification> getRootSiteClassifications(Paging<SiteClassification> paging) {
        def collection = mongo.getCollection("siteClassification")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("parentId").is("")
        def cursor = collection.find(queryBuilder.get()).skip(paging.offset).limit(paging.limit)
        while (cursor.hasNext()) {
            def site = cursor.next()
            paging.list << new SiteClassification(site)
        }
        cursor.close()
        return paging
    }

    long getSubSiteClassificationCount(String parentId) {
        def collection = mongo.getCollection("siteClassification")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("parentId").is(parentId)
        def result = collection.count(queryBuilder.get())
        return result
    }

    Paging<SiteClassification> getSubSiteClassifications(String parentId,Paging<SiteClassification> paging) {
        def collection = mongo.getCollection("siteClassification")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("parentId").is(parentId)
        def cursor = collection.find(queryBuilder.get()).skip(paging.offset).limit(paging.limit)
        while (cursor.hasNext()) {
            def site = cursor.next()
            paging.list << new SiteClassification(site)
        }
        cursor.close()
        return paging
    }

    long getSitesByClassificationCount(List parentIds, List subIds,int siteType,List<String> existsWebSiteDomains) {
        def collection = mongo.getCollection("siteDetail")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("siteType")is(siteType)
        queryBuilder.put("parentClassificationIdGroup").all(parentIds)
        queryBuilder.put("classificationId").in(subIds)
        if(siteType == 2){
            queryBuilder.put("siteName").notIn(existsWebSiteDomains)
        }else {
            queryBuilder.put("siteDomain").notIn(existsWebSiteDomains)
        }
        def result = collection.count(queryBuilder.get())
        return result
    }

    Paging<SiteDetail> getSitesByClassification(List parentIds, List subIds, Paging<SiteClassification> paging,int siteType,List<String> existsWebSiteDomains) {
        def collection = mongo.getCollection("siteDetail")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("siteType")is(siteType)
        queryBuilder.put("parentClassificationIdGroup").all(parentIds)
        queryBuilder.put("classificationId").in(subIds)
        if(siteType == 2){
            queryBuilder.put("siteName").notIn(existsWebSiteDomains)
        }else {
            queryBuilder.put("siteDomain").notIn(existsWebSiteDomains)
        }
        def cursor = collection.find(queryBuilder.get()).sort(toObj([rank:-1])).skip(paging.offset).limit(paging.limit)
        while (cursor.hasNext()) {
            def site = cursor.next()
            paging.list << new SiteDetail(site)
        }
        cursor.close()
        return paging
    }

    List getSiteDetailByIds(List siteDetailIdList){
        def collection = mongo.getCollection("siteDetail")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("_id").in(siteDetailIdList)
        def cursor = collection.find(queryBuilder.get())
        def result = []
        while (cursor.hasNext()) {
            def site = cursor.next()
            result << new SiteDetail(site)
        }
        cursor.close()
        return result
    }

    List getSiteByIds(List siteDetailIdList){
        def collection = mongo.getCollection("siteDetail")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("_id").in(siteDetailIdList)
        def cursor = collection.find(queryBuilder.get())
        def result = []
        while (cursor.hasNext()) {
            def site = cursor.next()
            result << new SiteDetail(site)
        }
        cursor.close()
        return result
    }
    Map getSiteDetailCountByType(List siteDetailIdList){

        def collection = mongo.getCollection("siteDetail")
        QueryBuilder key = QueryBuilder.start()
        key.put('siteType').is(true)

        QueryBuilder cond = QueryBuilder.start()
        cond.put("_id").in(siteDetailIdList)

        QueryBuilder init = QueryBuilder.start()
        init.put('count').is(0)

        String reduce = """
                function(curr,result){
                    result.count++;
                }
            """
        def result = collection.group(key.get(),cond.get(),init.get(),reduce)
        def resultMap = [:]
        result.each {
            resultMap.put((it.siteType as int) as String,it.count as int)
        }
        return resultMap
    }
    /**
     * 站点的区域和类型是否匹配
     * @param site
     * @param classification
     * @param area
     * @return
     */
    boolean isSiteDetailMatched(Site site,Integer classification,Integer area){
        if(classification == 0 && area == 0){
            return true
        }
        def collection = mongo.getCollection("siteDetail")
        QueryBuilder key = QueryBuilder.start()
        key.put('siteType').is(site.siteType)
        if(site.siteType == Const.SMT_WEBCHAT_PUBLCI_ACCOUNT){
            key.put('siteName').is(site.siteName)
        }else {
            key.put('siteDomain').is(site.websiteDomain)
        }
        if(classification != 0){
            key.put('filterClassificationId').is(classification)
        }
        if(area != 0){
            key.put('filterAreaId').is(area)
        }

        def count = collection.count(key.get())

        if(count > 0){
            return true
        }else {
            return false
        }
    }

    Map getSiteSummaryCountByType(long userId){

        def collection = mongo.getCollection("site")
        QueryBuilder key = QueryBuilder.start()
        key.put('siteType').is(true)

        QueryBuilder cond = QueryBuilder.start()
        cond.put('userId').is(userId)

        QueryBuilder init = QueryBuilder.start()
        init.put('count').is(0)

        String reduce = """
                function(curr,result){
                    result.count++;
                }
            """
        def result = collection.group(key.get(),cond.get(),init.get(),reduce)
        def resultMap = [:]
        result.each {
         resultMap.put((it.siteType as int) as String,it.count as int)
        }
        return resultMap
    }

    long getSiteCount(long userId,int siteType, List siteDomains){
        def collection = mongo.getCollection("site")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('siteType').is(siteType)
        if(siteType == 2){
            queryBuilder.put("websiteName").in(siteDomains)
        }else {
            queryBuilder.put("websiteDomain").in(siteDomains)
        }
        def count = collection.count(queryBuilder.get())
        return count
    }

    List<SubjectMap> getMockSubjects() {
        def collection = mongo.getCollection("subjectMap")
        def cursor = collection.find(toObj([yqmsSubjectId: Const.MOCK_SUBJECT_ID]))
        def list = []
        while (cursor.hasNext()) {
            def res = cursor.next()
            list << new SubjectMap(
                    subjectId: res._id,
                    subjectType: res.subjectType,
                    yqmsSubjectId: res.yqmsSubjectId,
                    yqmsUserId: res.yqmsUserId,
                    yqmsSubjectName: res.yqmsSubjectName,

                    websiteDomain: res.websiteDomain,
                    channelDomain: res.channelDomain,
                    focusKeywords: res.focusKeywords,
                    account: res.account,
                    createTime: res.createTime
            )
        }
        cursor.close()
        return list
    }

/**
 * 用户所有的筛选偏好
 * @param userId
 * @return
 */
    List<CaptureFilter> getCaptureFilters(Long userId) {
        def collection = mongo.getCollection("captureFilter")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("userId").is(userId)
        def cursor = collection.find(queryBuilder.get())
        def list = []
        while (cursor.hasNext()) {
            def res = cursor.next()
            list << new CaptureFilter(res)
        }
        cursor.close()
        return list
    }
    /**
     * 获取筛选偏好
     * @param userId
     * @param id
     * @return
     */
    CaptureFilter getCaptureFilter(Long userId, String id) {
        def collection = mongo.getCollection("captureFilter")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("_id").is(id)
        queryBuilder.put("userId").is(userId)
        def result = collection.findOne(queryBuilder.get())
        if(result){
            return new CaptureFilter(result)
        }else {
            return null
        }

    }
    /**
     * 保存当前的筛选偏好
     * @param captureFilter
     */
    void saveCaptureFilter(CaptureFilter captureFilter){
        def collection = mongo.getCollection("captureFilter")
        collection.save(toObj(captureFilter.toMap()))
    }
}

