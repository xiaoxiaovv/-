package com.istar.mediabroken.repo.capture

import com.istar.mediabroken.entity.capture.SiteAutoPush
import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.entity.capture.SiteDetail
import com.istar.mediabroken.entity.capture.SiteVsWeibo
import com.istar.mediabroken.utils.EsHolder
import com.istar.mediabroken.utils.MongoHolder
import com.istar.mediabroken.utils.StringUtils
import com.istar.mediabroken.utils.UrlUtils
import com.mongodb.AggregationOutput
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.QueryBuilder
import groovy.util.logging.Slf4j
import org.elasticsearch.action.search.SearchRequestBuilder
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.common.text.Text
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.MatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.RangeQueryBuilder
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.highlight.HighlightField
import org.elasticsearch.search.sort.SortBuilders
import org.elasticsearch.search.sort.SortOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository

import java.text.SimpleDateFormat

import static com.istar.mediabroken.utils.MongoHelper.toObj


/**
 * Author: Luda
 * Time: 2017/7/26
 */
@Repository
@Slf4j
class SiteRepo {
    @Autowired
    MongoHolder mongo
    @Autowired
    EsHolder elasticsearch
    @Value('${es.index.news}')
    String index

    List<Site> getUserSites(long userId) {
        def collection = mongo.getCollection("site")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("userId").is(userId)
        QueryBuilder sort = QueryBuilder.start()
        sort.put("siteType").is(1)
        sort.put("pinYinPrefix").is(1)
        def cursor = collection.find(queryBuilder.get()).sort(sort.get())
        def result = []
        while (cursor.hasNext()) {
            def site = cursor.next()
            result << new Site(site)
        }
        cursor.close()
        return result
    }

    List<Site> getUserSitesByTypeAndName(long userId, int siteType, String siteName) {
        def collection = mongo.getCollection("site")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("userId").is(userId)
        if (siteType != 0) {
            queryBuilder.put("siteType").is(siteType)
        }
        if (org.apache.commons.lang3.StringUtils.isNoneBlank(siteName)) {
            queryBuilder.put("siteName").regex(~/.*(?i)${siteName}.*/)
        }
        QueryBuilder sort = QueryBuilder.start()
        sort.put("pinYinPrefix").is(1)
        def cursor = collection.find(queryBuilder.get()).sort(sort.get())
        def result = []
        while (cursor.hasNext()) {
            def site = cursor.next()
            result << new Site(site)
        }
        cursor.close()
        return result
    }


    List<Site> getUserSitesByType(long userId, int siteType) {
        return getUserSitesByTypeAndName(userId, siteType, "")
    }

    List<Site> getUserSitesBySiteName(long userId, String siteName) {
        def collection = mongo.getCollection("site")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("userId").is(userId)
        if (!"".equals(siteName)) {
            queryBuilder.put("siteName").regex(~/.*(?i)${siteName}.*/)
        }

        QueryBuilder sort = QueryBuilder.start()
        sort.put("pinYinPrefix").is(1)
        def cursor = collection.find(queryBuilder.get()).sort(sort.get())
        def result = []
        while (cursor.hasNext()) {
            def site = cursor.next()
            result << new Site(site)
        }
        cursor.close()
        return result
    }


    List<Site> getUserSitesByIds(long userId, List ids) {
        def collection = mongo.getCollection("site")
        def cursor = collection.find(toObj([userId: userId, _id: [$in: ids]])).sort(toObj([pinYinPrefix: 1]))
        def result = []
        while (cursor.hasNext()) {
            def site = cursor.next()
            result << new Site(site)
        }
        cursor.close()
        return result
    }

    Site getHeadLineSite(Site site) {
        def collection = mongo.getCollection("siteDetail")
        def query = []
        if (site) {
            if (site.siteType == 1) {
                query = [siteType: "网站", siteDomain: site.websiteDomain, headLineStatus: "Y"]
            } else if (site.siteType == 2) {
                query = [siteType: "微信公众号", siteName: site.websiteName, headLineStatus: "Y"]
            } else {
                return null
            }
            def result = collection.findOne(toObj(query))
            return result ? site : null
        }
        return null
    }

    Site getUserSiteById(long userId, String siteId) {
        def collection = mongo.getCollection("site")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("userId").is(userId)
        queryBuilder.put("_id").is(siteId)
        def site = collection.findOne(queryBuilder.get())
        if (site) {
            return new Site(site)
        }
        return null
    }

    def getUserSiteIdByName(long userId, String siteName) {
        def collection = mongo.getCollection("site")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("userId").is(userId)
        queryBuilder.put("siteName").is(siteName)
        queryBuilder.put("siteType").is(3)
        def site = collection.findOne(queryBuilder.get())
        if (site) {
            return site._id
        }
        return ""
    }

    boolean getUserSiteByOperationType(long userId, String siteId, int operationType) {
        def collection = mongo.getCollection("site")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("userId").is(userId)
        queryBuilder.put("_id").is(siteId)
        queryBuilder.put("operationType").is(operationType)
        def site = collection.findOne(queryBuilder.get())
        if (site) {
            return true
        }
        return false
    }

    Map getSiteSummaryCountByType(long userId) {
        def collection = mongo.getCollection("site")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('userId').is(userId)
        DBObject match = new BasicDBObject("\$match", queryBuilder.get());
        BasicDBObject projection = new BasicDBObject("\$project", new BasicDBObject("_id", 1).append("siteType", 1))
        DBObject groupFields = new BasicDBObject("_id", "\$siteType");
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

    Long getUserSiteCountByType(long userId, int siteType) {
        def collection = mongo.getCollection("site")
        QueryBuilder key = QueryBuilder.start()
        key.put('userId').is(userId)
        key.put('siteType').is(siteType)
        def result = collection.count(key.get())
        return result
    }

    String addSite(Site site) {
        def collection = mongo.getCollection("site")
        collection.insert(toObj(site.toMap()))
        return site.siteId
    }

    void removeSite(long userId, String siteId) {
        def collection = mongo.getCollection("site")
        collection.remove(toObj([userId: userId, _id: siteId]))
    }

    void removeSiteList(long userId, List siteIds) {
        def collection = mongo.getCollection("site")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('_id').in(siteIds)
        collection.remove(queryBuilder.get())
    }
    boolean modifySite(Site site) {
        def collection = mongo.getCollection("site")
        collection.update(
                toObj([
                        userId: site.userId,
                        _id   : site.siteId]),
                toObj(['$set': [
                        siteName     : site.siteName,
                        websiteName  : site.websiteName,
                        websiteDomain: site.websiteDomain,
                        siteType     : site.siteType,
                        domainReverse: site.domainReverse,
                        pinYinPrefix : site.pinYinPrefix,
                        updateTime   : site.updateTime,
                        message      : site.message,
                        approved     : site.approved,
                        dataStatus   : site.dataStatus
                ]]))
        return true
    }

    void modifySiteAutoPush(long userId, String id, boolean isAutoPush) {
        def collection = mongo.getCollection("site")
        collection.update(toObj([_id: id, userId: userId]), toObj(['$set': [isAutoPush: isAutoPush, updateTime: new Date()]]))
        return
    }

    void resetSiteCountInfo(long userId, String id) {
        def collection = mongo.getCollection("site")
        Date now = new Date()
        collection.update(toObj([_id: id, userId: userId]), toObj(['$set': [resetTime: now, count: 0]]))
    }

    void modifySiteCountInfo(long userId, String id, long count) {
        def collection = mongo.getCollection("site")
        Date now = new Date()
        collection.update(toObj([_id: id, userId: userId]), toObj(['$set': [countTime: now, count: count]]))
    }

    String modifySiteAutoPush(SiteAutoPush siteAutoPush) {
        def collection = mongo.getCollection("siteAutoPush")
        collection.update(
                toObj([_id: siteAutoPush.siteId]),
                toObj(['$set': [
                        prevStartTime : siteAutoPush.prevStartTime,
                        prevEndTime   : siteAutoPush.prevEndTime,
                        prevNewsCount : siteAutoPush.prevNewsCount,
                        prevPushCount : siteAutoPush.prevPushCount,
                        totalNewsCount: siteAutoPush.totalNewsCount,
                        totalPushCount: siteAutoPush.totalPushCount,
                        updateTime    : siteAutoPush.updateTime,
                        createTime    : siteAutoPush.createTime,
                ]]))
        return siteAutoPush.siteId
    }

    def getSite(long userId, int siteType, List siteDomains) {
        def collection = mongo.getCollection("site")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('siteType').is(siteType)
        if (siteType == Site.SITE_TYPE_WECHAT) {
            queryBuilder.put("websiteName").in(siteDomains)
        } else if (siteType == Site.SITE_TYPE_WEBSITE) {
            queryBuilder.put("websiteDomain").in(siteDomains)
        } else if (siteType == Site.SITE_TYPE_WEIBO) {
            queryBuilder.put("websiteName").in(siteDomains)
        }
        def count = collection.findOne(queryBuilder.get())
        return count
    }

    boolean isSiteExist(Site site) {
        def collection = mongo.getCollection("site")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("userId").is(site.userId)
        queryBuilder.put("siteType").is(site.siteType)
        if (site.siteType == Site.SITE_TYPE_WEBSITE) {
            queryBuilder.put("websiteDomain").is(UrlUtils.webSiteDomainUrl(site.websiteDomain))
        } else if (site.siteType == Site.SITE_TYPE_WECHAT) {
            queryBuilder.put("websiteName").is(site.websiteName)
        } else if (site.siteType == Site.SITE_TYPE_WEIBO) {
            queryBuilder.put("websiteName").is(site.websiteName)
        }
        if (null != site.siteId && (!site.siteId.equals(""))) {
            queryBuilder.put("_id").notEquals(site.siteId)
        }
        def existSite = collection.findOne(queryBuilder.get())
        if (existSite) {
            return true
        } else {
            return false
        }
    }

    List getSitesNewsFromEs(List<Site> sites, int hot, Date startTime, Date endTime,
                            int orientation, boolean hasPic, int order, int queryScope, String keyWords, int pageSize, int offset) {
        String[] includeList = ["id", "author", "poster", "posterAvatar", "siteName", "newsType", "contentAbstract", "content", "title", "cover", "imgUrls", "reprintCount", "commentCount", "likeCount", "captureTime", "url", "firstPublishTime", "firstPublishSiteName", "simhash", "publishTime"]
        String[] excludeList = []

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder()

        BoolQueryBuilder siteQueryBuilder = new BoolQueryBuilder()
        BoolQueryBuilder weiboNewsTypeQueryBuilder = new BoolQueryBuilder()
        weiboNewsTypeQueryBuilder.should(new BoolQueryBuilder().must(QueryBuilders.termQuery("newsType", 401)))
                .should(new BoolQueryBuilder().must(QueryBuilders.termQuery("newsType", 402)))
        /*.should(new BoolQueryBuilder().must(QueryBuilders.termQuery("newsType", 8)))*/
        sites.each { site ->
            switch (site.siteType) {
                case Site.SITE_TYPE_WEBSITE:
                    siteQueryBuilder.should(QueryBuilders.boolQuery()
                            .must(QueryBuilders.prefixQuery("domainReverseUrl", (null == site.domainReverse || site.domainReverse == "") ? UrlUtils.getReverseDomainFromUrl(site.websiteDomain) : site.domainReverse))
                            .must(QueryBuilders.termQuery("newsType", 1))
                            .must(QueryBuilders.termQuery("siteTopDomain", UrlUtils.getTopDomain(site.websiteDomain)))
                    )
                    break
                case Site.SITE_TYPE_WECHAT:
                    siteQueryBuilder.should(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("siteName", site.websiteName))
                            .must(QueryBuilders.termQuery("newsType", 6)))
                    break
                case Site.SITE_TYPE_WEIBO:
                    siteQueryBuilder.should(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("siteName", site.websiteName))
                            .must(weiboNewsTypeQueryBuilder))
                    break
            }
        }
        if (sites.size() > 0) {
            boolQueryBuilder.must(siteQueryBuilder)
        }

        if (null != startTime || null != endTime) {
            RangeQueryBuilder timeRange
            if (null != startTime && null == endTime) {
                timeRange = QueryBuilders
                        .rangeQuery("captureTime")
                        .gte(startTime);
            }
            if (null == startTime && null != endTime) {
                timeRange = QueryBuilders
                        .rangeQuery("captureTime")
                        .lte(endTime);
            }
            if (null != startTime && null != endTime) {
                timeRange = QueryBuilders
                        .rangeQuery("captureTime")
                        .gte(startTime).lte(endTime);
            }
            boolQueryBuilder.must(timeRange)
        }

        if (!(null == keyWords || keyWords.equals(""))) {
            List wordList = keyWords.split(" ")
            BoolQueryBuilder titleSubBoolQueryBuilder = new BoolQueryBuilder()
            BoolQueryBuilder contentSubBoolQueryBuilder = new BoolQueryBuilder()
            wordList.each {
                if (it) {
                    titleSubBoolQueryBuilder.must(QueryBuilders.matchPhraseQuery("title", it))
                    contentSubBoolQueryBuilder.must(QueryBuilders.matchPhraseQuery("content", it))
                }
            }
            if (queryScope == 1){
                boolQueryBuilder.must(new BoolQueryBuilder().should(contentSubBoolQueryBuilder))
            }else if (queryScope == 2){
                boolQueryBuilder.must(new BoolQueryBuilder().should(titleSubBoolQueryBuilder))
            }
        }

        //0全部 1 低 2 中 3 高
        if (hot != 0) {
            switch (hot) {
                case 1:
                    boolQueryBuilder.must(QueryBuilders
                            .rangeQuery("reprintCount")
                            .lt(10))
                    break
                case 2:
                    boolQueryBuilder.must(QueryBuilders
                            .rangeQuery("reprintCount")
                            .from(10).to(100))
                    break
                case 3:
                    boolQueryBuilder.must(QueryBuilders
                            .rangeQuery("reprintCount")
                            .gt(100))
                    break
            }
        }

        //orientation
        //0：全部 1：正向 2：负向 3：中性 4：有争议
        if (orientation != 0) {
            switch (orientation) {
                case 1:
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("orientation").from(1).to(8))
                    break
                case 2:
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("orientation").lt(0))
                    break
                case 3:
                    boolQueryBuilder.must(QueryBuilders.termQuery("orientation", 0))
                    break
            }
        }

        //hasPic
        if (hasPic) {
            boolQueryBuilder.must(QueryBuilders.termQuery("hasImg", true))
        }
        String sortOrder = "reprintCount";//排序
        if (order == 1) {
            sortOrder = "captureTime"
        } else if (order == 2) {
            sortOrder = "reprintCount"
        } else if (order == 3) {
            sortOrder = "publishTime"
        }
        SearchRequestBuilder prepareSearch = elasticsearch.client.prepareSearch(index)
                .setTypes("news")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFetchSource(includeList, excludeList)
                .setQuery(boolQueryBuilder)
                .setFrom(offset)
                .setSize(pageSize)
                .addSort(sortOrder, SortOrder.DESC)
        if (keyWords) {
            prepareSearch.addHighlightedField("content")
                    .addHighlightedField("title")
                    .setHighlighterPreTags("<b class='high-light'>")
                    .setHighlighterPostTags("</b>")
        }
        SearchResponse response = prepareSearch.execute().actionGet()
        def result = []
        response?.getHits()?.getHits()?.each { hit ->
            def searchResult = hit.source
            if (keyWords) {
                // 获取对应的高亮域
                Map<String, HighlightField> highlightFields = hit.highlightFields();
                // 从设定的高亮域中取得指定域
                HighlightField titleField = highlightFields.get("title");
                if (titleField != null) {
                    // 取得定义的高亮标签
                    Text[] titleTexts = titleField.fragments();
                    // 为title串值增加自定义的高亮标签
                    String title = "";
                    for (Text text : titleTexts) {
                        title += text;
                    }
                    searchResult.title = title
                }
                // 从设定的高亮域中取得指定域
                HighlightField contentField = highlightFields.get("content");
                if (contentField != null) {
                    // 取得定义的高亮标签
                    Text[] contentTexts = contentField.fragments();
                    // 为title串值增加自定义的高亮标签
                    String content = "";
                    for (Text text : contentTexts) {
                        content += text;
                    }
                    Map highlightMap = StringUtils.extractHighlight(content)
                    highlightMap.each { key, value ->
                        if (keyWords.indexOf(value) == -1) {
                            content = content.replace(key as CharSequence, value as CharSequence)
                        }
                    }
                    // 将追加了高亮标签的串值重新填充到对应的对象
                    searchResult.contentAbstract = content
                }
            }
            result << searchResult
        }
        return result
    }

    //表名只能传siteDetail或者 siteDetail_ghms
    List getSitesRecommendation(String collectionName, List<String> classifications, String area, String attr, int order, String siteType, int pageSize, int pageNo, String siteName) {
        def collection = mongo.getCollection(collectionName)
        QueryBuilder queryBuilder = QueryBuilder.start()
        if (classifications) {
            queryBuilder.put("classification").in(classifications)
        }
        if (!"".equals(area)) {
            queryBuilder.put("area").is(area)
        }
        if (!"".equals(siteType)) {
            queryBuilder.put("siteType").is(siteType)
        }
        if (!"".equals(attr)) {
            queryBuilder.put("attr").is(attr)
        }
        if (!"".equals(siteName)) {
            queryBuilder.put("siteName").regex(~/.*(?i)${siteName}.*/)
        }

        def sortQuery = null
        switch (order) {
            case 1:
                sortQuery = [siteName: -1]
                break;
            case 2:
                sortQuery = [siteName: 1]
                break;
            case 3:
                sortQuery = [area: -1]
                break;
            case 4:
                sortQuery = [area: 1]
                break;
            case 5:
                sortQuery = [attr: -1]
                break;
            case 6:
                sortQuery = [attr: 1]
                break;
            case 7:
                sortQuery = [siteType: -1]
                break;
            case 8:
                sortQuery = [siteType: 1]
                break;
            default:
                sortQuery = [level: 1]
                break;
        }

        def cursor = collection.find(queryBuilder.get()).sort(toObj(sortQuery)).skip(pageSize * (pageNo - 1)).limit(pageSize)
        def result = []
        while (cursor.hasNext()) {
            def siteDetail = cursor.next()
            result << new SiteDetail(siteDetail)
        }
        cursor.close()
        return result
    }

    //表名只能传siteDetail或者 siteDetail_ghms
    List getSiteDetailByIds(List siteDetailIdList, String collectionName) {
        def collection = mongo.getCollection(collectionName)
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

    List<Map<String, Object>> getSitesLastNewsFromEs(int siteType, boolean hasPic, int pageSize, int pageNo) {
        def boolQueryBuilder = new BoolQueryBuilder()
        if (hasPic) {
            boolQueryBuilder.must(QueryBuilders.termQuery("hasImg", hasPic))
            boolQueryBuilder.must(QueryBuilders.prefixQuery("cover", "https://yqms3.zhxgimg.com/download/img/"))
//            boolQueryBuilder.mustNot(QueryBuilders.termQuery("cover", ""))
        }
        boolQueryBuilder.mustNot(QueryBuilders.termQuery("simhash", ""))
        String[] includeList = ["id", "author", "poster", "siteName", "contentAbstract", "title", "cover", "imgUrls", "captureTime", "simhash"]
        String[] excludeList = []

        switch (siteType) {
            case 1:
                boolQueryBuilder.must(QueryBuilders.termQuery("newsType", 1))
                break
            case 2:
                boolQueryBuilder.must(QueryBuilders.termQuery("newsType", 6))
                break
        }

        SearchResponse response = elasticsearch.client.prepareSearch(index)
                .setTypes("news")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFetchSource(includeList, excludeList)
                .setQuery(boolQueryBuilder)
                .setFrom(pageSize * (pageNo - 1))
                .setSize(pageSize)
                .addSort("captureTime", SortOrder.DESC)
                .execute()
                .actionGet()
        def hits = response.hits
        def result = []
        hits.each { hit ->
            result << hit.getSource()
        }
        return result
    }

    List<Map<String, Object>> getSitesHotNewsFromEs(List<Site> sites, boolean hasPic, int hours, int pageSize, int pageNo) {
        def boolQueryBuilder = new BoolQueryBuilder()
        if (hasPic) {
            boolQueryBuilder.must(QueryBuilders.termQuery("hasImg", hasPic))
            boolQueryBuilder.must(QueryBuilders.prefixQuery("cover", "https://yqms3.zhxgimg.com/download/img/"))
//            boolQueryBuilder.mustNot(QueryBuilders.termQuery("cover", ""))
        }
        boolQueryBuilder.mustNot(QueryBuilders.termQuery("simhash", ""))
        def subBoolQuery = QueryBuilders.boolQuery()
        String[] includeList = ["id", "author", "poster", "siteName", "contentAbstract", "title", "cover", "imgUrls", "captureTime", "simhash"]
        String[] excludeList = []
        def end = System.currentTimeMillis()
        def start = end - 3600 * 1000 * hours
        boolQueryBuilder.must(QueryBuilders.rangeQuery("captureTime").from(start).to(end))
        sites.each { site ->
            switch (site.siteType) {
                case 1:
                    subBoolQuery.should(QueryBuilders.boolQuery()
                            .must(QueryBuilders.prefixQuery("domainReverseUrl", (null == site.domainReverse || site.domainReverse == "") ? UrlUtils.getReverseDomainFromUrl(site.websiteDomain) : site.domainReverse))
                            .must(QueryBuilders.termQuery("newsType", 1)))
                    break

                case 2:
                    subBoolQuery.should(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("siteName", site.websiteName))
                            .must(QueryBuilders.termQuery("newsType", 6)))
                    break
            }
        }
        boolQueryBuilder.must(subBoolQuery)
        SearchResponse response = elasticsearch.client.prepareSearch(index)
                .setTypes("news")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFetchSource(includeList, excludeList)
                .setQuery(boolQueryBuilder)
                .setFrom(pageSize * (pageNo - 1))
                .setSize(pageSize)
                .addSort("reprintCount", SortOrder.DESC)
                .execute()
                .actionGet()
        def hits = response.hits
        def result = []

        hits.each { hit ->
            result << hit.getSource()
        }

        return result
    }

    List getSitesWholeNetHotNewsFromEs(boolean hasPic, int hours, int pageSize, int pageNo) {
        def boolQueryBuilder = new BoolQueryBuilder()
        if (hasPic) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasImg", hasPic))
        }
        boolQueryBuilder.mustNot(QueryBuilders.termQuery("simhash", ""))
        String[] includeList = ["id", "author", "poster", "siteName", "contentAbstract", "title", "cover",
                                "imgUrls", "captureTime", "simhash"]
        String[] excludeList = []

        def end = System.currentTimeMillis()
        def start = end - 3600 * 1000 * hours
        boolQueryBuilder.must(QueryBuilders.rangeQuery("captureTime").from(start).to(end))

        SearchResponse response = elasticsearch.client.prepareSearch(index)
                .setTypes("news")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFetchSource(includeList, excludeList)
                .setQuery(boolQueryBuilder)
                .setFrom(pageSize * (pageNo - 1))
                .setSize(pageSize)
                .addSort("captureTime", SortOrder.DESC)
                .execute()
                .actionGet()

        def hits = response.hits
        def result = []
        hits.each { hit ->
            result << hit.getSource()
        }

        return result
    }


    void addSiteDetailList(List siteDetail) {
        def collection = mongo.getCollection("siteDetail")
        def siteDetailList = []
        siteDetail.each {
            siteDetailList << toObj(it)
        }
        collection.insert(siteDetailList)
        return
    }

    void addGhmsSiteDetailList(List siteDetail) {
        def collection = mongo.getCollection("siteDetail_ghms")
        def siteDetailList = []
        siteDetail.each {
            siteDetailList << toObj(it)
        }
        collection.insert(siteDetailList)
        return
    }

    void addNewsMonitorSite(List siteDetail) {
        def collection = mongo.getCollection("newsMonitorSite")
        def siteDetailList = []
        siteDetail.each {
            siteDetailList << toObj(it)
        }
        collection.insert(siteDetailList)
        return
    }

    List<Site> getSiteListBySiteType(List paramList1, List paramList2) {
        def result = []
        def siteIds = []
        def collection = mongo.getCollection("site")
        for (int i = 0; i < paramList1.size(); i++) {
            Map map = paramList1.get(i)
            String websiteDomain = map.get("websiteDomain")
            int siteType = map.get("siteType")
            def cursor1 = collection.find(toObj([websiteDomain: websiteDomain, siteType: siteType]))
            while (cursor1.hasNext()) {
                def site = cursor1.next()
                if (siteIds.contains(site._id)) {
                    continue;
                } else {
                    siteIds << site._id
                    result << new Site(site)
                }
            }
            cursor1.close()
        }
        for (int i = 0; i < paramList2.size(); i++) {
            Map map = paramList2.get(i)
            String siteName = map.get("siteName")
            int siteType = map.get("siteType")
            def cursor2 = collection.find(toObj([siteName: siteName, siteType: siteType]))
            while (cursor2.hasNext()) {
                def site = cursor2.next()
                if (siteIds.contains(site._id)) {
                    continue;
                } else {
                    siteIds << site._id
                    result << new Site(site)
                }
            }
            cursor2.close()
        }
        return result
    }

    void addSiteVsWeiboList(List siteVsWeiboList) {
        def collection = mongo.getCollection("siteVsWeibo")
        def weiboList = []
        siteVsWeiboList.each {
            weiboList << toObj(it.toMap())
        }
        collection.insert(weiboList)
        return
    }

    void removeSiteVsWeibo() {
        def collection = mongo.getCollection("siteVsWeibo")
        collection.dropIndexes()
        collection.drop()
        return
    }

    boolean modifySiteMessageBySiteType(List paramList1, List paramList2) {
        def collection = mongo.getCollection("site")
        for (int i = 0; i < paramList1.size(); i++) {
            Map map = paramList1.get(i)
            String websiteDomain = map.get("websiteDomain")
            int siteType = map.get("siteType")
            String message = map.get("message")
            collection.update(toObj([websiteDomain: websiteDomain, siteType: siteType]), toObj(['$set': [message: message]]), false, true)
        }
        for (int i = 0; i < paramList2.size(); i++) {
            Map map = paramList2.get(i)
            String siteName = map.get("siteName")
            int siteType = map.get("siteType")
            String message = map.get("message")
            collection.update(toObj([siteName: siteName, siteType: siteType]), toObj(['$set': [message: message]]), false, true)
        }
        return true
    }

    List searchSite(int siteType, String url, String name) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder()
        boolQueryBuilder.must(QueryBuilders.termsQuery("siteType", siteType))
        if (url) {
            boolQueryBuilder.must(new MatchQueryBuilder("siteDomain", url).analyzer("charSplit").type(MatchQueryBuilder.Type.PHRASE_PREFIX).maxExpansions(1).slop(0))
        } else {
            boolQueryBuilder.must(new MatchQueryBuilder("siteName", name).analyzer("charSplit").type(MatchQueryBuilder.Type.PHRASE_PREFIX).maxExpansions(1).slop(0))
        }

        def searchRequestBuilder = elasticsearch.getClient()
                .prepareSearch("bjjassist")
                .setTypes("property")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(boolQueryBuilder)
                .setFrom(0)
                .setSize(10)

        if (url) {
            searchRequestBuilder.addSort(SortBuilders.fieldSort("domainLen")
                    .unmappedType("integer").order(SortOrder.ASC))
        } else {
            searchRequestBuilder.addSort(SortBuilders.fieldSort("siteLen")
                    .unmappedType("integer").order(SortOrder.ASC))
        }
        def response = searchRequestBuilder.execute().actionGet()
        def res = response.getHits()
        def list = []
        for (SearchHit hit : res) {
            list << [url : hit.getSource().get('siteDomain').toString(),
                     name: hit.getSource().get('siteName').toString().trim()]
        }
        return list
    }

    List searchSiteTest(String url) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder()
        boolQueryBuilder.must(QueryBuilders.termsQuery("siteType", 1))
        if (url) {
            boolQueryBuilder.must(new MatchQueryBuilder("siteDomain", url).analyzer("charSplit").slop(0))
        }

        def searchRequestBuilder = elasticsearch.getClient()
                .prepareSearch("bjjassist")
                .setTypes("property")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(boolQueryBuilder)
                .setFrom(0)
                .setSize(10)

        def response = searchRequestBuilder.execute().actionGet()
        println(response)
        def res = response.getHits()
        def list = []
        for (SearchHit hit : res) {
            list << [url : hit.getSource().get('siteDomain').toString(),
                     name: hit.getSource().get('siteName').toString().trim()]
        }
        return list
    }

    List<Site> getAllSites() {
        def collection = mongo.getCollection("site")
        def cursor = collection.find()
        def result = []
        while (cursor.hasNext()) {
            def site = cursor.next()
            result << new Site(site)
        }
        cursor.close()
        return result
    }

    List<Site> getSitesByDate(Date timeStart, Date timeEnd) {
        def collection = mongo.getCollection("site")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("updateTime").greaterThanEquals(timeStart)
        queryBuilder.put("updateTime").lessThanEquals(timeEnd)
        def cursor = collection.find(queryBuilder.get())
        def result = []

        while (cursor.hasNext()) {
            def site = cursor.next()
            result << new Site(site)
        }
        cursor.close()
        return result
    }

    SearchResponse getSitesHotNewsByTime(List<Site> sites, boolean hasPic, int size, String sortOrder, Date startDate, Date endDate, Date firstPublishStartDate, Date firstPublishEndDate) {
        def boolQueryBuilder = new BoolQueryBuilder()
        if (hasPic) {
            boolQueryBuilder.must(QueryBuilders.termQuery("hasImg", hasPic))
//            boolQueryBuilder.must(QueryBuilders.prefixQuery("cover", "https://yqms3.zhxgimg.com/download/img/"))//大图
        }
        if (null != startDate && null != endDate) {
            RangeQueryBuilder timeRange = QueryBuilders
                    .rangeQuery("publishTime")
                    .from(startDate).to(endDate);
            boolQueryBuilder.must(timeRange)
        } else {
            if (null != endDate) {
                RangeQueryBuilder timeRange = QueryBuilders
                        .rangeQuery("publishTime").lte(endDate)
                boolQueryBuilder.must(timeRange)
            }
            if (null != startDate) {
                RangeQueryBuilder timeRange = QueryBuilders
                        .rangeQuery("publishTime").gte(startDate)
                boolQueryBuilder.must(timeRange)
            }
        }
        if (null != firstPublishStartDate && null != firstPublishEndDate) {
            RangeQueryBuilder timeRange = QueryBuilders
                    .rangeQuery("firstPublishTime")
                    .from(firstPublishStartDate).to(firstPublishEndDate);
            boolQueryBuilder.must(timeRange)
        } else {
            if (null != firstPublishEndDate) {
                RangeQueryBuilder timeRange = QueryBuilders
                        .rangeQuery("firstPublishTime").lte(firstPublishEndDate)
                boolQueryBuilder.must(timeRange)
            }
            if (null != firstPublishStartDate) {
                RangeQueryBuilder timeRange = QueryBuilders
                        .rangeQuery("firstPublishTime").gte(firstPublishStartDate)
                boolQueryBuilder.must(timeRange)
            }
        }

        boolQueryBuilder.mustNot(QueryBuilders.termQuery("simhash", ""))

        def subBoolQuery = new BoolQueryBuilder()
        BoolQueryBuilder weiboNewsTypeQueryBuilder = new BoolQueryBuilder()
        weiboNewsTypeQueryBuilder.should(new BoolQueryBuilder().must(QueryBuilders.termQuery("newsType", 401)))
                .should(new BoolQueryBuilder().must(QueryBuilders.termQuery("newsType", 402)))
        /*.should(new BoolQueryBuilder().must(QueryBuilders.termQuery("newsType", 8)))*/
        sites.each { site ->
            switch (site.siteType) {
                case Site.SITE_TYPE_WEBSITE:
                    subBoolQuery.should(QueryBuilders.boolQuery()
                            .must(QueryBuilders.termQuery("siteTopDomain", UrlUtils.getTopDomain(site.websiteDomain)))
                            .must(QueryBuilders.prefixQuery("domainReverseUrl", (null == site.domainReverse || site.domainReverse == "") ? UrlUtils.getReverseDomainFromUrl(site.websiteDomain) : site.domainReverse))
                            .must(QueryBuilders.termQuery("newsType", 1)))
                    break

                case Site.SITE_TYPE_WECHAT:
                    if (hasPic) {
                        subBoolQuery.should(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("siteName", site.websiteName))
                                .must(QueryBuilders.termQuery("newsType", 6))
                                .mustNot(QueryBuilders.termQuery("cover", ""))
//                                .must(QueryBuilders.prefixQuery("cover", "https://yqms3.zhxgimg.com/download/img/"))
                        )
//大图
                    } else {
                        subBoolQuery.should(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("siteName", site.websiteName))
                                .must(QueryBuilders.termQuery("newsType", 6)))
                    }
                    break
                case Site.SITE_TYPE_WEIBO:
                    subBoolQuery.should(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("siteName", site.websiteName))
                            .must(weiboNewsTypeQueryBuilder))
                    break
            }
        }
        boolQueryBuilder.must(subBoolQuery)

        String[] includeList = ["id", "author", "poster", "posterAvatar", "siteName", "newsType", "contentAbstract", "title", "cover", "imgUrls", "url", "captureTime", "simhash", "reprintCount", "publishTime", "firstPublishTime", "firstPublishSiteName"]
        String[] excludeList = []

        SearchResponse scrollResp = elasticsearch.client.prepareSearch(index)
                .setTypes("news")
                .setQuery(boolQueryBuilder)
                .setFetchSource(includeList, excludeList)
                .addSort(sortOrder, SortOrder.DESC)
                .setScroll(new TimeValue(10000))
                .setSize(size).execute().actionGet()
        return scrollResp
    }

    SearchResponse getScrollData(SearchResponse scrollResp, long millis) {
        return elasticsearch.client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(millis)).execute().actionGet();
    }

    SearchResponse getSiteNewsByTime(Site site, int size, Date startDate, Date endDate) {
        def boolQueryBuilder = new BoolQueryBuilder()
        if (null != startDate && null != endDate) {
            RangeQueryBuilder timeRange = QueryBuilders
                    .rangeQuery("captureTime")
                    .from(startDate).to(endDate);
            boolQueryBuilder.must(timeRange)
        } else {
            if (null != endDate) {
                RangeQueryBuilder timeRange = QueryBuilders
                        .rangeQuery("captureTime").lte(endDate)
                boolQueryBuilder.must(timeRange)
            } else {
                RangeQueryBuilder timeRange = QueryBuilders
                        .rangeQuery("captureTime").gte(startDate)
                boolQueryBuilder.must(timeRange)
            }
        }

        boolQueryBuilder.mustNot(QueryBuilders.termQuery("simhash", ""))

        def subBoolQuery = new BoolQueryBuilder()
        switch (site.siteType) {
            case 1:
                subBoolQuery.should(QueryBuilders.boolQuery()
                        .must(QueryBuilders.prefixQuery("domainReverseUrl", (null == site.domainReverse || site.domainReverse == "") ? UrlUtils.getReverseDomainFromUrl(site.websiteDomain) : site.domainReverse))
                        .must(QueryBuilders.termQuery("newsType", 1)))
                break

            case 2:
                subBoolQuery.should(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("siteName", site.websiteName))
                        .must(QueryBuilders.termQuery("newsType", 6)))
                break
        }
        boolQueryBuilder.must(subBoolQuery)

//        String[] includeList = ["id", "author", "poster", "siteName", "contentAbstract", "title", "cover", "imgUrls", "captureTime", "simhash", "reprintCount", "publishTime"]
//        String[] excludeList = []

        SearchResponse scrollResp = elasticsearch.client.prepareSearch(index)
                .setTypes("news")
//                .setFetchSource(includeList, excludeList)
                .addSort("captureTime", SortOrder.ASC)
                .setScroll(new TimeValue(10000))
                .setQuery(boolQueryBuilder)
                .setSize(size).execute().actionGet()
        return scrollResp
    }

    List<Site> getAutoPushSites(Integer pageNo, Integer pageSize) {
        def siteCollection = mongo.getCollection("site")
        def currDate = new Date();
        def date = new Date(currDate.getTime() - 60 * 5000)
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String currTime = simpleDateFormat.format(date);
        def siteList = []
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('isAutoPush').is(true)
        def sites = siteCollection.find(queryBuilder.get()).limit(pageSize).skip((pageNo - 1) * pageSize).sort(toObj([_id: 1]))
        sites.each {
            Map siteMap = new HashMap<String, Object>();
            SiteAutoPush autoPush = getSiteAutoPushById(it._id)
            if (autoPush == null || autoPush.siteId == null) {
                if (autoPush == null) {
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
            siteMap.put("site", it)
            siteMap.put("siteAutoPush", autoPush)
            siteList.add(siteMap)
        }
        sites.close()
        return siteList
    }

    String addSiteAutoPush(SiteAutoPush siteAutoPush) {
        def collection = mongo.getCollection("siteAutoPush")
        collection.insert(toObj([
                _id           : siteAutoPush.siteId,
                prevStartTime : siteAutoPush.prevStartTime,
                prevEndTime   : siteAutoPush.prevEndTime,
                prevNewsCount : siteAutoPush.prevNewsCount,
                prevPushCount : siteAutoPush.prevPushCount,
                totalNewsCount: siteAutoPush.totalNewsCount,
                totalPushCount: siteAutoPush.totalPushCount,
                updateTime    : siteAutoPush.updateTime,
                createTime    : siteAutoPush.createTime,
        ]))
        return siteAutoPush.siteId
    }

    SiteAutoPush getSiteAutoPushById(String id) {
        SiteAutoPush siteAutoPush = new SiteAutoPush();
        def collection = mongo.getCollection("siteAutoPush")
        QueryBuilder autoPushQuery = QueryBuilder.start()
        autoPushQuery.put('_id').is(id)
        def autoPush = collection.findOne(autoPushQuery.get())
        if (autoPush) {
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

    Map getSubjectMap(String subjectId) {
        def collection = mongo.getCollection("subjectMap")
        def subjectMap = collection.findOne(toObj([_id: subjectId]))

        return subjectMap ? [
                subjectId    : subjectMap._id,
                yqmsUserId   : subjectMap.yqmsUserId,
                yqmsSubjectId: subjectMap.yqmsSubjectId]
                : null
    }

    /**
     * 添加newspush的列表信息
     * @param userId
     * @param orgId
     * @param newsDetailList
     * @param newsIds
     * @return
     */
    int addNewsPushList(long userId, String orgId, List newsDetailList, List newsIds) {
        def count = 0;
        Date now = new Date()
        def collection = mongo.getCollection("newsOperation")
        def newsList = []
        //获取已经推送的news
        def pushedNewsId = []
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('orgId').is(orgId)
        queryBuilder.put('operationType').is(1)
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('newsId').in(newsIds.sort())
        def pushedNewsList = collection.find(queryBuilder.get(), toObj(["newsId": 1])).hint("orgId_1_operationType_1_userId_1_newsId_1")
        pushedNewsList.each {
            pushedNewsId.add(it.newsId)
        };
        pushedNewsList.close()
        //将未推送的news插入mongo
        int listLength = newsDetailList.size()
        for (int i = 0; i < listLength; i++) {
            def news = newsDetailList.get(i)
            if (!pushedNewsId.contains(news.newsId)) {
                news._id = UUID.randomUUID().toString()
                news.operationType = 1
                newsList.add(toObj(news as Map<String, Object>))
                count++
            }
        }
        if (newsList.size() > 0) {
            collection.insert(newsList);
        }
        return count
    }

    SiteVsWeibo getSiteWeibo(Site site) {
        def collection = mongo.getCollection("siteVsWeibo")
        def obj
        if (site && site.siteType == Site.SITE_TYPE_WEBSITE && site.websiteDomain) {
            obj = collection.findOne(toObj(["siteType": site.siteType, "siteName": site.siteName, "siteDomain": site.websiteDomain]))
        }
        if (site && site.siteType == Site.SITE_TYPE_WECHAT) {
            obj = collection.findOne(toObj(["siteType": site.siteType, "siteName": site.siteName]))
        }
        return obj ? new SiteVsWeibo(obj) : null
    }

    def getUserIsHaveSite(long userId, String siteName, String siteType) {
        def collection = mongo.getCollection("site")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("userId").is(userId)
        queryBuilder.put("siteName").is(siteName)
        queryBuilder.put("siteType").is(siteType)
        def one = collection.findOne(queryBuilder.get())
        return one
    }

    List<SiteDetail> getAllHeadLineSites() {
        def collection = mongo.getCollection("siteDetail")
        def cursor = collection.find(toObj([headLineStatus: "Y"]))
        def result = []

        while (cursor.hasNext()) {
            def siteDetail = cursor.next()
            result << new SiteDetail(siteDetail)
        }
        cursor.close()
        return result
    }

    List<SiteDetail> getAllSiteDetail() {
        def collection = mongo.getCollection("siteDetail")
        def cursor = collection.find().sort(toObj(["siteType":-1]))
        def result = []
        while (cursor.hasNext()) {
            def siteDetail = cursor.next()
            result << new SiteDetail(siteDetail)
        }
        cursor.close()
        return result
    }

    public long getNewCountByTypeAndTime(int siteType, Date startTime, Date endTime) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        if (siteType == Site.SITE_TYPE_WEBSITE) {
            boolQueryBuilder.must(QueryBuilders.termQuery("newsType", 1));
        } else if (siteType == Site.SITE_TYPE_WECHAT) {
            boolQueryBuilder.must(QueryBuilders.termQuery("newsType", 6));
        } else if (siteType == Site.SITE_TYPE_WEIBO) {
            BoolQueryBuilder should = QueryBuilders.boolQuery().should(QueryBuilders.termQuery("newsType", 401))
                    .should(QueryBuilders.termQuery("newsType", 402))
                    .should(QueryBuilders.termQuery("newsType", 8));
            boolQueryBuilder.must(should);
        }
        RangeQueryBuilder timeRange = QueryBuilders.rangeQuery("createTime").gte(startTime).lte(endTime);
        SearchResponse scrollResp = elasticsearch.client.prepareSearch(index)
                .setTypes("news")
                .setQuery(boolQueryBuilder.must(timeRange)).setSize(0).setFrom(0).execute().actionGet()
        return scrollResp.hits.totalHits()

    }

    long getSiteNewsCountByTime(Site site, Date startDate, Date endDate) {
        def boolQueryBuilder = new BoolQueryBuilder()
        if (null != startDate && null != endDate) {
            RangeQueryBuilder timeRange = QueryBuilders
                    .rangeQuery("captureTime")
                    .from(startDate).to(endDate);
            boolQueryBuilder.must(timeRange)
        } else {
            if (null != endDate) {
                RangeQueryBuilder timeRange = QueryBuilders
                        .rangeQuery("captureTime").lte(endDate)
                boolQueryBuilder.must(timeRange)
            }
            if (null != startDate) {
                RangeQueryBuilder timeRange = QueryBuilders
                        .rangeQuery("captureTime").gte(startDate)
                boolQueryBuilder.must(timeRange)
            }
        }
        switch (site.siteType) {
            case Site.SITE_TYPE_WEBSITE:
                boolQueryBuilder.must(QueryBuilders.termQuery("newsType", 1))
                boolQueryBuilder.must(QueryBuilders.termQuery("siteTopDomain", UrlUtils.getTopDomain(site.websiteDomain)))
                boolQueryBuilder.must(QueryBuilders.prefixQuery("domainReverseUrl", (null == site.domainReverse || site.domainReverse == "") ? UrlUtils.getReverseDomainFromUrl(site.websiteDomain) : site.domainReverse))
                break
            case Site.SITE_TYPE_WECHAT:
                boolQueryBuilder.must(QueryBuilders.termQuery("newsType", 6))
                boolQueryBuilder.must(QueryBuilders.termQuery("siteName", site.siteName))
                break
            case Site.SITE_TYPE_WEIBO:
                boolQueryBuilder.must(QueryBuilders.termQuery("newsType", 401))
                boolQueryBuilder.must(QueryBuilders.termQuery("siteName", site.siteName))
                break
        }
        SearchResponse response = elasticsearch.client.prepareSearch(index)
                .setTypes("news")
                .setSearchType(SearchType.COUNT)
                .setQuery(boolQueryBuilder)
                .execute()
                .actionGet()
        return response?.getHits()?.totalHits
    }

    List<Map<String, Object>> getRecentNewsBySite(Site site) {
        def boolQueryBuilder = new BoolQueryBuilder()
        switch (site.siteType) {
            case Site.SITE_TYPE_WEBSITE:
                String topDomain = UrlUtils.getTopDomain(site.websiteDomain)
                boolQueryBuilder.must(QueryBuilders.termQuery("newsType", 1))
                boolQueryBuilder.must(QueryBuilders.termQuery("siteTopDomain", topDomain))
                boolQueryBuilder.must(QueryBuilders.prefixQuery("domainReverseUrl",
                        UrlUtils.getReverseDomainFromUrl(topDomain)))
                break
            case Site.SITE_TYPE_WECHAT:
                boolQueryBuilder.must(QueryBuilders.termQuery("newsType", 6))
                boolQueryBuilder.must(QueryBuilders.termQuery("siteName", site.siteName))
                break
            case Site.SITE_TYPE_WEIBO:
                boolQueryBuilder.must(QueryBuilders.termQuery("siteName", site.siteName))
                BoolQueryBuilder should = QueryBuilders.boolQuery().should(QueryBuilders.termQuery("newsType", 401))
                        .should(QueryBuilders.termQuery("newsType", 402))
                        .should(QueryBuilders.termQuery("newsType", 8));
                boolQueryBuilder.must(should)
                break
        }
        SearchResponse response = elasticsearch.client.prepareSearch(index)
                .setTypes("news")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(boolQueryBuilder)
                .setSize(1)
                .addSort("captureTime", SortOrder.DESC)
                .execute()
                .actionGet()
        def hits = response.hits
        def result = []
        hits.each { hit ->
            result << hit.getSource()
        }
        return result
    }

}
