package com.istar.mediabroken.repo.capture

import com.istar.mediabroken.entity.capture.InstantNewsMarked
import com.istar.mediabroken.entity.capture.InstantSite
import com.istar.mediabroken.entity.capture.NewsUrlTypeEnum
import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.utils.EsHolder
import com.istar.mediabroken.utils.MongoHolder
import com.istar.mediabroken.utils.UrlUtils
import com.mongodb.QueryBuilder
import groovy.util.logging.Slf4j
import org.elasticsearch.action.search.SearchRequestBuilder
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.RangeQueryBuilder
import org.elasticsearch.search.sort.SortOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.*


/**
 * Author: zc
 * Time: 2018/5/9
 */
@Repository
@Slf4j
class InstantSiteRepo {
    @Autowired
    MongoHolder mongo
    @Autowired
    EsHolder elasticsearch
    @Value('${es.index.news}')
    String index

    void addInstantSites(long userId, List<Map> instantSites) {
        def collection = mongo.getCollection("instantNewsSite")
        collection.remove(toObj([userId: userId]))
        try {
            collection.insert(toList(instantSites))
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    void addInstantSite(InstantSite instantSite) {
        def collection = mongo.getCollection("instantNewsSite")
        try {
            collection.insert(toObj(instantSite.toMap()))
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    List<InstantSite> getUserInstantSites(long userId) {
        def collection = mongo.getCollection("instantNewsSite")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("userId").is(userId)
        QueryBuilder sort = QueryBuilder.start()
        sort.put("siteType").is(1)
        sort.put("pinYinPrefix").is(1)
        def cursor = collection.find(queryBuilder.get()).sort(sort.get())
        def result = []
        while (cursor.hasNext()) {
            def instantSite = cursor.next()
            result << new InstantSite(instantSite)
        }
        cursor.close()
        return result
    }

    InstantSite getUserInstantSite(long userId, int siteType, String siteName, String websiteDomain) {
        def collection = mongo.getCollection("instantNewsSite")
        def instantSite = collection.findOne(toObj([userId: userId, siteType: siteType, siteName: siteName, websiteDomain: websiteDomain]))
        return instantSite ? new InstantSite(instantSite.toMap()) : null
    }

    String addInstantNewsMarked(InstantNewsMarked instantNewsMarked) {
        def collection = mongo.getCollection("instantNewsMarked")
        collection.insert(toObj(instantNewsMarked.toMap()))
        return instantNewsMarked.id;
    }

    void removeInstantNewsMarked(long userId, String newsId) {
        def collection = mongo.getCollection("instantNewsMarked")
        collection.remove(toObj([userId: userId, newsId: newsId]))
    }

    long removeMarkedNews(Date overDate) {
        def collection = mongo.getCollection("instantNewsMarked")
        long count = collection.count(toObj([createTime: [$lt: overDate]]))
        collection.remove(toObj([createTime: [$lt: overDate]]))
        return count
    }

    boolean isNewsMarked(long userId, String newsId) {
        def collection = mongo.getCollection("instantNewsMarked")
        def result = collection.findOne(toObj([userId: userId, newsId: newsId]))
        return result ? true : false
    }

    List<InstantNewsMarked> getInstantMarkedNews(long userId, String queryName, int pageSize, int pageNo) {
        def collection = mongo.getCollection("instantNewsMarked")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("userId").is(userId)
        if (queryName) {
            QueryBuilder titleQuery = QueryBuilder.start()
            titleQuery.put("title").regex(~/.*(?i)${queryName}.*/)
            queryBuilder.or(titleQuery.get())
        }

        def cursor = collection.find(toObj(queryBuilder.get())).sort(toObj([captureTime: -1])).skip(pageSize * (pageNo - 1)).limit(pageSize)
        def result = []
        while (cursor.hasNext()) {
            def markedNews = cursor.next()
            result << new InstantNewsMarked(markedNews)
        }
        cursor.close()
        return result
    }

    BoolQueryBuilder queryCondition(List<InstantSite> instantSites, Date startTime, Date endTime) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder()

        BoolQueryBuilder siteQueryBuilder = new BoolQueryBuilder()
        BoolQueryBuilder weiboNewsTypeQueryBuilder = new BoolQueryBuilder()
        weiboNewsTypeQueryBuilder.should(new BoolQueryBuilder().must(QueryBuilders.termQuery("newsType", 401)))
                .should(new BoolQueryBuilder().must(QueryBuilders.termQuery("newsType", 402)))
        instantSites.each { instantSite ->
            switch (instantSite.siteType) {
                case Site.SITE_TYPE_WEBSITE:
                    //详情url 完全匹配
                    if (NewsUrlTypeEnum.detailsUrl.getKey() == instantSite.urlType) {
                        siteQueryBuilder.should(QueryBuilders.boolQuery()
                                .must(QueryBuilders.termQuery("newsType", 1))
                                .must(QueryBuilders.termQuery("siteTopDomain", UrlUtils.getTopDomain(instantSite.websiteDomain)))
                                .must(QueryBuilders.prefixQuery("domainReverseUrl", (null == instantSite.domainReverse || instantSite.domainReverse == "") ? UrlUtils.getReverseDomainFromUrl(instantSite.websiteDomain) : instantSite.domainReverse))
                        )
                    }
                    //频道url 完全匹配
                    if (NewsUrlTypeEnum.channelUrl.getKey() == instantSite.urlType) {
                        siteQueryBuilder.should(QueryBuilders.boolQuery()
                                .must(QueryBuilders.termQuery("newsType", 1))
                                .must(QueryBuilders.termQuery("siteTopDomain", UrlUtils.getTopDomain(instantSite.websiteDomain)))
                                .must(QueryBuilders.prefixQuery("channelUrl", instantSite.websiteDomain))
                        )
                    }
                    break
                case Site.SITE_TYPE_WECHAT:
                    siteQueryBuilder.should(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("siteName", instantSite.websiteName))
                            .must(QueryBuilders.termQuery("newsType", 6)))
                    break
                case Site.SITE_TYPE_WEIBO:
                    siteQueryBuilder.should(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("siteName", instantSite.websiteName))
                            .must(weiboNewsTypeQueryBuilder))
                    break
            }
        }
        if (instantSites.size() > 0) {
            boolQueryBuilder.must(siteQueryBuilder)
        }

        if (null != startTime || null != endTime) {
            RangeQueryBuilder timeRange
            if (null != startTime && null == endTime) {
                timeRange = QueryBuilders
                        .rangeQuery("captureTime")
                        .gt(startTime);
            }
            if (null == startTime && null != endTime) {
                timeRange = QueryBuilders
                        .rangeQuery("captureTime")
                        .lte(endTime);
            }
            if (null != startTime && null != endTime) {
                timeRange = QueryBuilders
                        .rangeQuery("captureTime")
                        .gt(startTime).lte(endTime);
            }
            boolQueryBuilder.must(timeRange)
        }
        return boolQueryBuilder
    }

    List getInstantNewsFromEs(List<InstantSite> instantSites, Date startTime, Date endTime, String queryName, int pageSize, int offset) {
        String[] includeList = ["id", "siteName", "contentAbstract", "title", "imgUrls", "hasImg", "captureTime", "url", "simhash"]
        String[] excludeList = []
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder()
        boolQueryBuilder = queryCondition(instantSites, startTime, endTime)

        if (!(null == queryName || queryName.equals(""))) {
//            BoolQueryBuilder titleSubBoolQueryBuilder = new BoolQueryBuilder()
//            BoolQueryBuilder contentSubBoolQueryBuilder = new BoolQueryBuilder()
//            titleSubBoolQueryBuilder.must(QueryBuilders.matchPhraseQuery("siteName", queryName))
//            contentSubBoolQueryBuilder.must(QueryBuilders.matchPhraseQuery("title", queryName))
//            boolQueryBuilder.must(new BoolQueryBuilder().should(titleSubBoolQueryBuilder).should(contentSubBoolQueryBuilder))
//            QueryBuilders.wildcardQuery("title", "?"+queryName+"?")
            boolQueryBuilder.must(new BoolQueryBuilder().should(QueryBuilders.multiMatchQuery(queryName, "title")))
        }

        SearchRequestBuilder prepareSearch = elasticsearch.client.prepareSearch(index)
                .setTypes("news")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFetchSource(includeList, excludeList)
                .setQuery(boolQueryBuilder)
                .setFrom(offset)
                .setSize(pageSize)
                .addSort("captureTime", SortOrder.DESC)
        SearchResponse response = prepareSearch.execute().actionGet()
        def result = []
        response?.getHits()?.getHits()?.each { hit ->
            def searchResult = hit.source
            result << searchResult
        }
        return result
    }

    long getInstantNewsCount(List<InstantSite> instantSites, Date startTime, Date endTime) {
        String[] includeList = ["id"]
        String[] excludeList = []
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder()
        boolQueryBuilder = queryCondition(instantSites, startTime, endTime)
        SearchResponse response = elasticsearch.client.prepareSearch(index)
                .setTypes("news")
                .setSearchType(SearchType.COUNT)
                .setQuery(boolQueryBuilder)
                .execute()
                .actionGet()
        return response?.getHits()?.totalHits
    }
}
