package com.istar.mediabroken.repo.capture

import com.istar.mediabroken.entity.capture.NewsOperation
import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.utils.DataProcessMongoHolder
import com.istar.mediabroken.utils.EsHolder
import com.istar.mediabroken.utils.MongoHolder
import com.istar.mediabroken.utils.UrlUtils
import com.mongodb.BasicDBObject
import com.mongodb.QueryBuilder
import com.mongodb.WriteConcern
import com.mongodb.WriteResult
import groovy.util.logging.Slf4j
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.get.MultiGetItemResponse
import org.elasticsearch.action.get.MultiGetResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.RangeQueryBuilder
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.sort.SortOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Author: Luda
 * Time: 2017/7/28
 */
@Repository
@Slf4j
class NewsRepo {
    @Autowired
    MongoHolder mongo
    @Autowired
    EsHolder elasticsearch
    @Value('${es.index.news}')
    String index
    @Value('${es.index.detail}')
    String detailIndex
    @Autowired
    DataProcessMongoHolder dataProcessMongoHolder

    void getNewsCountByCaptureTime(Date startTime, Date endTime) {
        SearchResponse response = elasticsearch.client.prepareSearch(index)
                .setTypes("news")
                .setSearchType(SearchType.COUNT)
                .setQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("captureTime").from(startTime)
                .to(endTime)))
                .execute()
                .actionGet()
        println(response)
    }

    long getNewsTotalBySite(Site site) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (site.siteType == Site.SITE_TYPE_WEBSITE) {
            boolQueryBuilder.must(QueryBuilders.termQuery("siteTopDomain", UrlUtils.getTopDomain(site.websiteDomain)));
            boolQueryBuilder.must(QueryBuilders.termQuery("newsType", 1));
        } else if (site.siteType == Site.SITE_TYPE_WECHAT) {
            boolQueryBuilder.must(QueryBuilders.termQuery("newsType", 6));
            boolQueryBuilder.must(QueryBuilders.termQuery("siteName", site.websiteName));
        } else if (site.siteType == Site.SITE_TYPE_WEIBO) {
            boolQueryBuilder.must(QueryBuilders.termQuery("siteName", site.websiteName));
            boolQueryBuilder.must(QueryBuilders.termQuery("newsType", 401));
        }

        SearchResponse response = elasticsearch.client.prepareSearch(index).setQuery(boolQueryBuilder).execute().actionGet();
        SearchHits hits = response.getHits();
        long totalHits = hits.getTotalHits();
        return totalHits
    }

    Map getNewsById(String newsId) {
        def result = null
        try {
            GetResponse response = elasticsearch.client.prepareGet(index, "news", newsId)
                    .setOperationThreaded(false)
                    .get();
            if (response.exists) {
                result = response.getSourceAsMap()
                if ("Y".equals(result.htmlContent)) {
                    GetResponse htmlResponse = elasticsearch.client.prepareGet(detailIndex, "news", newsId)
                            .setOperationThreaded(false)
                            .get();
                    if (htmlResponse.exists) {
                        result.htmlContent = htmlResponse.getSourceAsMap().get("htmlContent")
                    } else {
                        result.htmlContent = ""
                    }
                } else {
                    result.htmlContent = ""
                }
                return result
            } else {
                return null
            }
        } catch (Exception e) {
            log.error("获取新闻信息异常：{}", e.getMessage())
            return null
        }
    }

    def getNewsHtmlContent(String newsId) {
        def htmlContent = ""
        try {
            GetResponse htmlResponse = elasticsearch.client.prepareGet(detailIndex, "news", newsId)
                    .setOperationThreaded(false)
                    .get();
            if (htmlResponse.exists) {
                htmlContent = htmlResponse.getSourceAsMap().get("htmlContent")
            } else {
                htmlContent = ""
            }
            return htmlContent
        } catch (Exception e) {
            log.error("获取新闻详细信息异常：{}", e.getMessage())
            return htmlContent
        }
    }

    Map getWeChatNewsByTitle(String siteName, String title) {
        List result = []
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        boolQueryBuilder.filter(QueryBuilders.termQuery("siteName", siteName))
                .must(QueryBuilders.termQuery("siteType", 6));
        boolQueryBuilder.must(QueryBuilders.matchPhraseQuery("title", title));

        try {
            SearchResponse response = elasticsearch.client.prepareSearch(index).setQuery(boolQueryBuilder).setFrom(0).setSize(100).execute().actionGet();
            SearchHits hits = response.getHits();
            long totalHits = hits.getTotalHits();
            System.out.println(totalHits);
            for (SearchHit hit : hits) {
                Map<String, Object> source = hit.getSource();
                Object replyCount = source.get("replyCount");
                Object reprintCount = source.get("reprintCount");
                Object commentCount = source.get("commentCount");
                Object id = source.get("id");
                Object likeCount = source.get("likeCount");
                System.out.println("id:" + id + "replyCount:" + replyCount + ";reprintCount:" + reprintCount + ";commentCount:" + commentCount + ";likeCount:" + likeCount);
                return ["replyCount": replyCount, "reprintCount": reprintCount, "commentCount": commentCount, "likeCount": likeCount]
            }
            return null
        } catch (Exception e) {
            log.error("获取新闻信息异常：{}", e.getMessage())
            return null
        }
    }

    void addNews(Map news) {
        def collection = mongo.getCollection("testNews")
        collection.save(toObj(news))
    }

    Map getNewsWithoutContentById(String newsId) {
        String[] includeList = []
        String[] excludeList = ["contentHtml", "content", "contentAbstract"]
        try {
            GetResponse response = elasticsearch.client.prepareGet(index, "news", newsId)
                    .setFetchSource(includeList, excludeList)
                    .setOperationThreaded(false)
                    .get();
            if (response.exists) {
                return response.getSourceAsMap()
            } else {
                return null
            }
        } catch (Exception e) {
            log.error("获取新闻信息异常：{}", e.getMessage())
            return null
        }
    }

    /**
     *
     * @param newsIds
     * @param flag 0不要html图文混排格式，1要html图文混排格式
     * @return
     */
    Map getNewsListByIds(List newsIds, int flag) {

        def result = [:]
        def existNewsIds = []
        def noExistNewsIds = []
        def newsList = []
        try {
            MultiGetResponse multiGetItemResponses = elasticsearch.client.prepareMultiGet()
                    .add(index, "news", newsIds)
                    .get();

            for (MultiGetItemResponse itemResponse : multiGetItemResponses) {
                GetResponse response = itemResponse.getResponse();
                if (response.isExists()) {
                    existNewsIds << response.getId()
                    Map currNewsMap = response.getSourceAsMap()
//                    if (6 == currNewsMap.get("newsType")) {
                    if (1 == flag) {
                        if (currNewsMap.get("htmlContent")) {
                            /*if ("Y".equals(currNewsMap.get("htmlContent"))) {
                                GetResponse htmlResponse = elasticsearch.client.prepareGet(detailIndex, "news", currNewsMap.id)
                                        .setOperationThreaded(false)
                                        .get();
                                if (htmlResponse.exists) {
                                    currNewsMap.content = htmlResponse.getSourceAsMap().get("htmlContent")
                                    currNewsMap.isHtmlContent = true
                                    currNewsMap.imgUrls = []
                                } else {
                                    currNewsMap.isHtmlContent = false
                                }
                            }else if ("N".equals(currNewsMap.get("htmlContent"))){
                                currNewsMap.htmlContent = ""
                                currNewsMap.isHtmlContent = false
                            }else {
                                currNewsMap.content = currNewsMap.htmlContent
                                currNewsMap.isHtmlContent = true
                                currNewsMap.imgUrls = []
                                currNewsMap.htmlContent = ""
                            }*/
                            if ("Y".equals(currNewsMap.get("htmlContent"))) {
                                GetResponse htmlResponse = elasticsearch.client.prepareGet(detailIndex, "news", currNewsMap.id)
                                        .setOperationThreaded(false)
                                        .get();
                                if (htmlResponse.exists) {
                                    currNewsMap.content = htmlResponse.getSourceAsMap().get("htmlContent")
                                    currNewsMap.isHtmlContent = true
                                    currNewsMap.imgUrls = []
                                } else {
                                    currNewsMap.isHtmlContent = false
                                }
                            } else {
                                if (!("N".equals(currNewsMap.get("htmlContent")))) {//兼容老数据，老数据中htmlContent存的是带div的新闻内容
                                    currNewsMap.content = currNewsMap.htmlContent
                                    currNewsMap.isHtmlContent = true
                                    currNewsMap.imgUrls = []
                                    currNewsMap.htmlContent = ""
                                } else {
                                    currNewsMap.htmlContent = ""
                                    currNewsMap.isHtmlContent = false
                                }
                            }
                            currNewsMap.remove("htmlContent")
                        } else {
                            currNewsMap.isHtmlContent = false
                        }
                    }
//                    }
                    newsList << currNewsMap
                } else {
                    noExistNewsIds << response.getId()
                }
            }
        } catch (Exception e) {
            log.error("获取新闻信息异常：{}", e.getMessage())
            return null
        }
        result.existNewsIds = existNewsIds
        result.noExistNewsIds = noExistNewsIds
        result.newsList = newsList
        return result
    }

    SearchResponse getHotWeiboNews(def siteNameList, def noSiteNameList, Date startDate, Date endDate) {

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder()
        if (null != startDate && null != endDate) {
            RangeQueryBuilder timeRange = QueryBuilders
                    .rangeQuery("publishTime")
                    .from(startDate).to(endDate);
            boolQueryBuilder.must(timeRange)
        }

//        BoolQueryBuilder newsTypeBoolQueryBuilder = new BoolQueryBuilder()
//        newsTypeBoolQueryBuilder.should(QueryBuilders.termQuery("newsType", 4))
//        newsTypeBoolQueryBuilder.should(QueryBuilders.termQuery("newsType", 401))
//        newsTypeBoolQueryBuilder.should(QueryBuilders.termQuery("newsType", 402))
        boolQueryBuilder.must(QueryBuilders.termQuery("newsType", 401))

        if (siteNameList) {
            def siteBoolQueryBuilder = new BoolQueryBuilder()
            siteNameList.each {
                siteBoolQueryBuilder.should(QueryBuilders.termQuery("siteName", it))
            }
            boolQueryBuilder.must(siteBoolQueryBuilder)
        }
        if (noSiteNameList) {
            def siteBoolQueryBuilder = new BoolQueryBuilder()
            noSiteNameList.each {
                siteBoolQueryBuilder.should(QueryBuilders.termQuery("siteName", it))
            }
            boolQueryBuilder.mustNot(siteBoolQueryBuilder)
        }

        String[] includeList = ["captureTime", "content", "poster", "posterAvatar", "publishTime"]
        String[] excludeList = []

        SearchResponse scrollResp = elasticsearch.client.prepareSearch(index)
                .setTypes("news")
                .setFetchSource(includeList, excludeList)
                .addSort("reprintCount", SortOrder.DESC)
                .setScroll(new TimeValue(10000))
                .setQuery(boolQueryBuilder)
                .setSize(50).execute().actionGet(); //100 hits per shard will be returned for each scroll
        return scrollResp
    }

    SearchResponse getScrollData(SearchResponse scrollResp) {
        return elasticsearch.client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(10000)).execute().actionGet();
    }

    List getSitesKeyCloud(List sites, String startDay, String endDay) {
        def keywordList = []
        def collection = mongo.getCollection("keywords")
        def cursor = null
        sites.each { site ->
            QueryBuilder key = QueryBuilder.start()
            switch (site.siteType as int) {
                case 1:
                    key.put("siteDomain").is(UrlUtils.getDomainFromUrl(site.websiteDomain))
                    key.put("newsType").is(1)
                    break
                case 2:
                    key.put("siteDomain").is(site.siteName)
                    key.put("newsType").is(6)
                    break
                case 3:
                    key.put("siteDomain").is(site.siteName)
                    key.put("newsType").is(4)
                    break
            }
            key.put("time").greaterThanEquals(startDay).lessThanEquals(endDay)
            cursor = collection.find(key.get())
            while (cursor.hasNext()) {
                def keywords = cursor.next()
                keywordList << keywords
            }
        }
        cursor.close()
        return keywordList
    }

    List getNoneFavoritedNewsIds(long userId, List newsIds) {
        List favouritedNewsIds = getExistNewsOperation(userId, newsIds, 3)
        return newsIds - favouritedNewsIds
    }

    List getExistNewsPush(long userId, List newsIds) {

        return getExistNewsOperation(userId, newsIds, 1)

    }

    List getExistNewsOperation(long userId, List newsIds, int operationType) {
        def collection = mongo.getCollection("newsOperation")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("userId").is(userId)
        queryBuilder.put("operationType").is(operationType)
        queryBuilder.put("newsId").in(newsIds)
        QueryBuilder selectField = QueryBuilder.start()
        selectField.put("newsId").is(1)
        def cursor = collection.find(queryBuilder.get(), selectField.get()).hint("userId_1_operationType_1_createTime_1")
        def result = []
        try {
            while (cursor.hasNext()) {
                def newsOperation = cursor.next()
                result << newsOperation.newsId
            }
        } catch (Exception e) {
            return result
        } finally {
            cursor.close()
        }
        return result
    }

    List getNewsPushHistory(long userId, String orgId, String teamId, long pushUserId, String pushChannel, String autoPushType, Date startDate, Date endDate, int pageNo, int pageSize) {
        def collection = mongo.getCollection("newsOperation")
        QueryBuilder queryBuilder = QueryBuilder.start()
        if ((!"0".equals(teamId)) && teamId != null) {//有组才可以查组内成员的推送
            if (pushUserId) {//选择推送人
                if (pushUserId != userId) {
                    queryBuilder.put("teamId").is(teamId)
                    queryBuilder.put("userId").is(pushUserId)
                } else if (pushUserId == userId) {//如果推送人选择自己，查询自己所有的推送记录
                    queryBuilder.put("userId").is(userId)
                }
            } else {//全部 = 本组的+本人的
                QueryBuilder queryTeam = QueryBuilder.start()
                queryTeam.put("teamId").is(teamId)
                QueryBuilder queryUser = QueryBuilder.start()
                queryUser.put("userId").is(userId)
                queryBuilder.or(queryTeam.get()).or(queryUser.get())
            }
        } else {//没有组只能查询自己的推送
            queryBuilder.put("userId").is(userId)
        }
        def newsType
        if (pushChannel) {
            if ("1".equals(pushChannel)) {
                newsType = 1
                queryBuilder.put("news.newsType").is(newsType)
            } else if ("2".equals(pushChannel)) {
                newsType = 6
                queryBuilder.put("news.newsType").is(newsType)
            } else if ("3".equals(pushChannel)) {
                newsType = [401, 402]
                queryBuilder.put("news.newsType").in(newsType)
            } else if ("4".equals(pushChannel)) {//我的文稿
                newsType = 4
                queryBuilder.put("pushType").is(newsType)
            }
        }
        if (!"".equals(autoPushType)) {
            if ("0".equals(autoPushType)) {
                queryBuilder.put("isAutoPush").is(false)
            } else if ("1".equals(autoPushType)) {
                queryBuilder.put("isAutoPush").is(true)
            }
        }
        if (startDate) {
            queryBuilder.put("createTime").greaterThanEquals(startDate)
        }
        if (endDate) {
            queryBuilder.put("createTime").lessThanEquals(endDate)
        }
        queryBuilder.put("orgId").is(orgId)
        queryBuilder.put("operationType").is(1)
        QueryBuilder selectField = QueryBuilder.start()
        selectField.put("news.title").is(1)
        selectField.put("news.contentAbstract").is(1)
        selectField.put("news.siteName").is(1)
        selectField.put("news.reprintCount").is(1)
        selectField.put("news.publishTime").is(1)
        selectField.put("news.createTime").is(1)
        selectField.put("userId").is(1)
        selectField.put("createTime").is(1)
        selectField.put("status").is(1)
        QueryBuilder sort = QueryBuilder.start()
        sort.put("createTime").is(-1)
        def cursor = collection.find(queryBuilder.get(), selectField.get()).sort(sort.get()).skip(pageSize * (pageNo - 1)).limit(pageSize)
        def result = []
        while (cursor.hasNext()) {
            def newsOperation = cursor.next()
            result << new NewsOperation(newsOperation)
        }
        cursor.close()
        return result
    }

    long getTeamNewsPushHistoryCount(long userId, String orgId, String teamId, Date startDate, Date endDate) {
        def collection = mongo.getCollection("newsOperation")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("teamId").is(teamId)
        queryBuilder.put("orgId").is(orgId)
        if (startDate) {
            queryBuilder.put("createTime").greaterThanEquals(startDate)
        }
        if (endDate) {
            queryBuilder.put("createTime").lessThanEquals(endDate)
        }
        queryBuilder.put("operationType").is(1)
        def count = collection.find(queryBuilder.get()).count()
        return count
    }

    long getUserNewsPushHistoryCount(long userId, Date startDate, Date endDate) {
        def collection = mongo.getCollection("newsOperation")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("userId").is(userId)
        if (startDate) {
            queryBuilder.put("createTime").greaterThanEquals(startDate)
        }
        if (endDate) {
            queryBuilder.put("createTime").lessThanEquals(endDate)
        }
        queryBuilder.put("operationType").is(1)
        def count = collection.find(queryBuilder.get()).count()
        return count
    }

    long getNewsPushHistoryCount(long userId, String orgId, String teamId, long pushUserId, String pushChannel, String autoPushType, Date startDate, Date endDate) {
        def collection = mongo.getCollection("newsOperation")
        QueryBuilder queryBuilder = QueryBuilder.start()
        if ((!"0".equals(teamId)) && teamId != null) {//有组才可以查组内成员的推送
            if (pushUserId) {//选择推送人
                if (pushUserId != userId) {
                    queryBuilder.put("teamId").is(teamId)
                    queryBuilder.put("userId").is(pushUserId)
                } else if (pushUserId == userId) {//如果推送人选择自己，查询自己所有的推送记录
                    queryBuilder.put("userId").is(userId)
                }
            } else {//全部 = 本组的+本人的
                QueryBuilder queryTeam = QueryBuilder.start()
                queryTeam.put("teamId").is(teamId)
                QueryBuilder queryUser = QueryBuilder.start()
                queryUser.put("userId").is(userId)
                queryBuilder.or(queryTeam.get()).or(queryUser.get())
            }
        } else {//没有组只能查询自己的推送
            queryBuilder.put("userId").is(userId)
        }
        def newsType
        if (pushChannel) {
            if ("1".equals(pushChannel)) {
                newsType = 1
                queryBuilder.put("news.newsType").is(newsType)
            } else if ("2".equals(pushChannel)) {
                newsType = 6
                queryBuilder.put("news.newsType").is(newsType)
            } else if ("3".equals(pushChannel)) {
                newsType = [401, 402]
                queryBuilder.put("news.newsType").in(newsType)
            } else if ("4".equals(pushChannel)) {//我的文稿
                newsType = 4
                queryBuilder.put("pushType").is(newsType)
            }
        }
        if (!"".equals(autoPushType)) {
            if ("0".equals(autoPushType)) {
                queryBuilder.put("isAutoPush").is(false)
            } else if ("1".equals(autoPushType)) {
                queryBuilder.put("isAutoPush").is(true)
            }
        }
        if (startDate) {
            queryBuilder.put("createTime").greaterThanEquals(startDate)
        }
        if (endDate) {
            queryBuilder.put("createTime").lessThanEquals(endDate)
        }
        queryBuilder.put("orgId").is(orgId)
        queryBuilder.put("operationType").is(1)
        def result = collection.find(queryBuilder.get()).count()
        return result
    }

    void addNewsOperationList(List<NewsOperation> newsOperationList) {
        def collection = mongo.getCollection("newsOperation")
        def newsList = []
        newsOperationList.each {
            newsList << toObj(it.toMap())
        }
        collection.insert(newsList);
        return
    }


    void findAndModifyNewsOperation(long userId, def newsOperation, String timeStamp, int channelType) {
        def collection = mongo.getCollection("newsOperation")
        def setMap = ["_id"          : newsOperation._id,
                      "newsId"       : newsOperation.newsId,
                      "news"         : newsOperation.news,
                      "pushType"     : newsOperation.pushType,
                      "status"       : newsOperation.status,
                      "siteId"       : newsOperation.siteId,
                      "agentId"      : newsOperation.agentId,
                      "orgId"        : newsOperation.orgId,
                      "userId"       : newsOperation.userId,
                      "createTime"   : newsOperation.createTime,
                      "updateTime"   : newsOperation.updateTime,
                      "operationType": newsOperation.operationType,
                      "shareChannel" : newsOperation.shareChannel,
                      "shareContent" : newsOperation.shareContent,
                      "groupId"      : newsOperation.groupId,
                      "teamId"       : newsOperation.teamId,
                      "teamName"     : newsOperation.teamName,
                      "publisher"    : newsOperation.publisher,
                      "isAutoPush"   : newsOperation.isAutoPush,
                      "shareResult"  : newsOperation.shareResult]
        if (channelType == 1) {
            collection.findAndModify(
                    toObj(["userId": userId, "timeStamp": timeStamp]),               //query
                    toObj(["timeStamp": timeStamp, "shareChannelCount": 1, weiboChannel: 1]),      //fields
                    null,
                    false,
                    toObj([$inc: ["shareChannelCount": 1, weiboChannel: 1],
                           $set: setMap
                    ]),    //update
                    false,
                    true
            )
        } else if (channelType == 2) {
            collection.findAndModify(
                    toObj(["userId": userId, "timeStamp": timeStamp]),               //query
                    toObj(["timeStamp": timeStamp, "shareChannelCount": 1, wechatChannel: 1]),      //fields
                    null,
                    false,
                    toObj([$inc: ["shareChannelCount": 1, wechatChannel: 1],
                           $set: setMap
                    ]),    //update
                    false,
                    true
            )
        } else if (channelType == 3) {
            collection.findAndModify(
                    toObj(["userId": userId, "timeStamp": timeStamp]),               //query
                    toObj(["timeStamp": timeStamp, "shareChannelCount": 1, toutiaoChannel: 1]),      //fields
                    null,
                    false,
                    toObj([$inc: ["shareChannelCount": 1, toutiaoChannel: 1],
                           $set: setMap
                    ]),    //update
                    false,
                    true
            )
        } else if (channelType == 4) {
            collection.findAndModify(
                    toObj(["userId": userId, "timeStamp": timeStamp]),               //query
                    toObj(["timeStamp": timeStamp, "shareChannelCount": 1, qqomChannel: 1]),      //fields
                    null,
                    false,
                    toObj([$inc: ["shareChannelCount": 1, qqomChannel: 1],
                           $set: setMap
                    ]),    //update
                    false,
                    true
            )
        }

    }

    List getUserNewsFavorites(Long userId, int pageSize, int pageNo) {
        return getUserNewsOperations(userId, [3, 4], pageSize, pageNo)
    }

    List getUserNewsOperations(long userId, List operationType, int pageSize, int pageNo) {
        def collection = mongo.getCollection("newsOperation")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("userId").is(userId)
        queryBuilder.put("operationType").in(operationType)
        QueryBuilder sort = QueryBuilder.start()
        sort.put("createTime").is(-1)
        QueryBuilder filed = QueryBuilder.start()
        filed.put("_id").is(1)
        filed.put("newsId").is(1)
        filed.put("news.title").is(1)
        filed.put("news.siteName").is(1)
        filed.put("news.url").is(1)
        filed.put("news.captureTime").is(1)
        filed.put("news.contentAbstract").is(1)
        filed.put("news.content").is(1)
        filed.put("news.keywords").is(1)
        filed.put("news.imgUrls").is(1)
        filed.put("news.classification").is(1)
        filed.put("operationType").is(1)
        def cursor = collection.find(queryBuilder.get(), filed.get()).sort(sort.get()).skip(pageSize * (pageNo - 1)).limit(pageSize)
        def result = []
        while (cursor.hasNext()) {
            def newsOperation = cursor.next()
            result << new NewsOperation(newsOperation)
        }
        cursor.close()
        return result
    }

    NewsOperation getUserNewsFavorite(long userId, String id) {
        return getUserNewsOperation(userId, id, 3)
    }

    NewsOperation getUserNewsOperation(long userId, String id, int operationType) {
        def collection = mongo.getCollection("newsOperation")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("userId").is(userId)
        queryBuilder.put("operationType").is(operationType)
        queryBuilder.put("_id").is(id)
        def newsOperation = collection.findOne(queryBuilder.get())
        if (newsOperation) {
            return new NewsOperation(newsOperation)
        }
        return null
    }

    NewsOperation getUserNewsOperation(long userId, String id) {
        def collection = mongo.getCollection("newsOperation")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("_id").is(id)
        def newsOperation = collection.findOne(queryBuilder.get())
        if (newsOperation) {
            return new NewsOperation(newsOperation)
        }
        return null
    }

    boolean removeUserNewsOperation(long userId, String id) {
        def collection = mongo.getCollection("newsOperation")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("_id").is(id)
        queryBuilder.put("userId").is(userId)
        def result = collection.remove(queryBuilder.get())
        return true
    }

    boolean removeUserNewsOperationList(long userId, List id) {
        def collection = mongo.getCollection("newsOperation")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("_id").in(id)
        queryBuilder.put("userId").is(userId)
        def result = collection.remove(queryBuilder.get())
        return true
    }

    long getNewsCommentCount(String suffix, String id) {
        long commentCount = 0;
        def collection = dataProcessMongoHolder.getCollection("newsCommentCount_" + suffix)
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("_id").is(id)
        def newsCommentCount = collection.findOne(queryBuilder.get())
        if (newsCommentCount) {
            commentCount = newsCommentCount.count
        }
        return commentCount
    }

    long favouriteCount(long userId) {
        long commentCount = 0;
        def collection = mongo.getCollection("newsOperation")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("userId").is(userId)
        queryBuilder.put("operationType").is(3)
        def count = collection.count(queryBuilder.get());
        return count;
    }
    //旧数据处理给旧数据添加已导入分组id
    void addGroupIdToImport(String groupId, long userId) {
        def collection = mongo.getCollection("newsOperation")
        collection.update(
                toObj([userId: userId, operationType: 4]),
                toObj(['$set': [
                        groupId: groupId,
                ]]),
                true,
                true
        );
    }
    //旧数据处理给旧数据添加已默认分组id
    void addGroupIdToNormal(String groupId, long userId) {
        def collection = mongo.getCollection("newsOperation")
        collection.update(
                toObj([userId: userId, operationType: 3]),
                toObj(['$set': [
                        groupId: groupId,
                ]]),
                true,
                true
        );
    }

    def findOperationListByType(int operationType, long userId) {
        def collection = mongo.getCollection("newsOperation")
        def list = collection.find(toObj([operationType: operationType, userId: userId]))
        def result = []
        while (list.hasNext()) {
            def newsOperation = list.next()
            result << new NewsOperation(toObj(newsOperation))
        }
        list.close()
        return result
    }

    def removeUserNewsOperationByGroupId(long userId, String groupId) {
        def col = mongo.getCollection("newsOperation")
        col.remove(toObj([userId: userId, groupId: groupId]))
    }

    def getNewsOperationListByGroupId(long userId, String groupId) {
        def collection = mongo.getCollection("newsOperation")
        def list = collection.find(toObj([userId: userId, groupId: groupId])).hint("userId_1_groupId_1_createTime_-1")
        def result = []
        while (list.hasNext()) {
            def newsOperation = list.next()
            result << new NewsOperation(toObj(newsOperation))
        }
        list.close()
        return result
    }

    def pageNewsOperationListByGroupId(Long userId, String groupId, int pageNo, int pageSize) {
        def collection = mongo.getCollection("newsOperation")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("userId").is(userId)
        queryBuilder.put("groupId").is(groupId)
        QueryBuilder sort = QueryBuilder.start()
        sort.put("createTime").is(-1)
        QueryBuilder filed = QueryBuilder.start()
        filed.put("_id").is(1)
        filed.put("newsId").is(1)
        filed.put("news.title").is(1)
        filed.put("news.siteName").is(1)
        filed.put("news.url").is(1)
        filed.put("news.captureTime").is(1)
        filed.put("news.contentAbstract").is(1)
        filed.put("news.content").is(1)
        filed.put("news.keywords").is(1)
        filed.put("news.imgUrls").is(1)
        filed.put("news.classification").is(1)
        filed.put("operationType").is(1)
        filed.put("groupId").is(1)
        def cursor = collection.find(queryBuilder.get(), filed.get()).sort(sort.get()).hint("userId_1_groupId_1_createTime_-1").skip(pageSize * (pageNo - 1)).limit(pageSize)
        def result = []
        while (cursor.hasNext()) {
            def newsOperation = cursor.next()
            result << new NewsOperation(newsOperation)
        }
        cursor.close()
        return result
    }

    def pageNewsOperationSharedList(long userId, String groupId, String orgId, String teamId, Date startDate, Date endDate, int pageNo, int pageSize) {
        def collection = mongo.getCollection("newsOperation")
        QueryBuilder queryBuilder = QueryBuilder.start()
        if (startDate) {
            queryBuilder.put("createTime").greaterThanEquals(startDate)
        }
        if (endDate) {
            queryBuilder.put("createTime").lessThanEquals(endDate)
        }
        queryBuilder.put("orgId").is(orgId)
        queryBuilder.put("teamId").is(teamId)
        queryBuilder.put("userId").notEquals(userId)
        queryBuilder.put("operationType").in([1, 5])
        queryBuilder.put("isAutoPush").notEquals(true)//手动推送
        QueryBuilder sort = QueryBuilder.start()
        sort.put("createTime").is(-1)
        def cursor = collection.find(queryBuilder.get()).sort(sort.get()).hint("orgId_1_operationType_1_userId_1_newsId_1").skip(pageSize * (pageNo - 1)).limit(pageSize)
        def result = []
        while (cursor.hasNext()) {
            def newsOperation = cursor.next()
            result << new NewsOperation(newsOperation)
        }
        cursor.close()
        return result
    }

    def modifyNewsGroupByGroupId(String groupId, String newsOperationId) {
        def collection = mongo.getCollection("newsOperation")
        collection.update(
                toObj([_id: newsOperationId]),
                toObj(['$set': [
                        groupId: groupId,
                ]]),
                false,
                false
        );
    }
    //根据条件统计用户数据
    Map getAcountStatisticByType(Integer operationType, List<Long> userIds, Date startTime, Date endTime) {
        def collection = mongo.getCollection("newsOperation")
        BasicDBObject query = new BasicDBObject("userId", new BasicDBObject("\$in", userIds))
                .append("createTime", new BasicDBObject("\$gte", startTime).append("\$lt", endTime))
                .append("operationType", operationType)
                .append("publisher", new BasicDBObject("\$ne", null))
        BasicDBObject match = new BasicDBObject("\$match", query)
        BasicDBObject groupObj = null
        if (operationType == 1) {//推送数据统计
            groupObj = new BasicDBObject("_id", new BasicDBObject("userId", "\$userId")
                    .append("isAutoPush", "\$isAutoPush")
                    .append("teamId", "\$teamId")
                    .append("teamName", "\$teamName"))
                    .append("orgId", new BasicDBObject("\$first", "\$orgId"))
                    .append("publisher", new BasicDBObject("\$first", "\$publisher"))
                    .append("count", new BasicDBObject("\$sum", 1))
        } else if (operationType == 5) {//同步数据统计
            groupObj = new BasicDBObject("_id", new BasicDBObject("userId", "\$userId"))
                    .append("weiboCount", new BasicDBObject("\$sum", "\$weiboChannel"))
                    .append("wechatCount", new BasicDBObject("\$sum", "\$wechatChannel"))
                    .append("toutiaoCount", new BasicDBObject("\$sum", "\$toutiaoChannel"))
                    .append("qqomCount", new BasicDBObject("\$sum", "\$qqomChannel"))
        }
        BasicDBObject group = new BasicDBObject("\$group", groupObj)
        def aggregate = collection.aggregate(Arrays.asList(match, group))
        def list = aggregate.results()
        Map userStatistics = list.groupBy { it._id.userId }
        return userStatistics
    }

    //根据条件统计机构数据
    Map getOrgStatisticByType(Integer operationType, List<String> orgIds, Date startTime, Date endTime) {
        def collection = mongo.getCollection("newsOperation")
        BasicDBObject query = new BasicDBObject("orgId", new BasicDBObject("\$in", orgIds))
                .append("createTime", new BasicDBObject("\$gte", startTime).append("\$lt", endTime))
                .append("operationType", operationType)
                .append("publisher", new BasicDBObject("\$ne", null))
        BasicDBObject match = new BasicDBObject("\$match", query)
        BasicDBObject groupObj = null
        if (operationType == 1) {//推送数据统计
            groupObj = new BasicDBObject("_id", new BasicDBObject("orgId", "\$orgId")
                    .append("isAutoPush", "\$isAutoPush"))
                    .append("publisher", new BasicDBObject("\$first", "\$publisher"))
                    .append("count", new BasicDBObject("\$sum", 1))
        } else if (operationType == 5) {//同步数据统计
            groupObj = new BasicDBObject("_id", new BasicDBObject("orgId", "\$orgId"))
                    .append("weiboCount", new BasicDBObject("\$sum", "\$weiboChannel"))
                    .append("wechatCount", new BasicDBObject("\$sum", "\$wechatChannel"))
                    .append("toutiaoCount", new BasicDBObject("\$sum", "\$toutiaoChannel"))
                    .append("qqomCount", new BasicDBObject("\$sum", "\$qqomChannel"))
        }
        BasicDBObject group = new BasicDBObject("\$group", groupObj)
        def aggregate = collection.aggregate(Arrays.asList(match, group))
        def list = aggregate.results()
        Map orgStatistics = list.groupBy { it._id.orgId }
        return orgStatistics
    }

    boolean removeNewsOperationByNewsId(long userId, String newsId, int operationType) {
        def collection = mongo.getCollection("newsOperation")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("newsId").is(newsId)
        queryBuilder.put("userId").is(userId)
        queryBuilder.put("operationType").is(operationType)
        WriteResult result = collection.remove(queryBuilder.get())
        return result
    }

    ArrayList getTimeOutNewsData(Date timeOutDate) {
        def collection = mongo.getCollection("newsOperation")
        def result = []
        def cursor = collection.find(toObj([createTime: [$lt: timeOutDate]])).sort(toObj([createTime: 1])).limit(50)
        while (cursor.hasNext()) {
            def newsOperation = cursor.next()
            result << new NewsOperation(newsOperation)
        }
        return result
    }

    def getOrgPushNewsList(long userId, Date timeOutDate, int pageNo, int pageSize) {
        def collection = mongo.getCollection("newsOperation")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("userId").is(userId)
        queryBuilder.put("createTime").lessThanEquals(timeOutDate)
        QueryBuilder sort = QueryBuilder.start()
        sort.put("createTime").is(1)
        //不用获取全部的字段
        QueryBuilder filed = QueryBuilder.start()
        filed.put("_id").is(1)
        filed.put("newsId").is(1)
        filed.put("userId").is(1)
        def cursor = collection.find(queryBuilder.get(), filed.get()).sort(sort.get()).hint("userId_1_operationType_1_createTime_1").skip(pageSize * (pageNo - 1)).limit(pageSize)
        def result = []
        while (cursor.hasNext()) {
            def newsOperation = cursor.next()
            result << new NewsOperation(newsOperation)
        }
        cursor.close()
        return result
    }

    void deleteTimeOutData(List<String> idsList) {
        idsList.sort()
        def collection = mongo.getCollection("newsOperation")
        collection.remove(toObj([_id: ["\$in": idsList]]), WriteConcern.ACKNOWLEDGED)
    }
}
