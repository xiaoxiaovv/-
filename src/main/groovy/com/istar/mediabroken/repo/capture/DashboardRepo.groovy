package com.istar.mediabroken.repo.capture

import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.utils.EsHolder
import com.istar.mediabroken.utils.MongoHolder
import com.istar.mediabroken.utils.UrlUtils
import groovy.util.logging.Slf4j
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.get.MultiGetItemResponse
import org.elasticsearch.action.get.MultiGetResponse
import org.elasticsearch.action.search.SearchRequestBuilder
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.sort.SortOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository

/**
 * Created by Zc on 2017/12/20.
 */
@Repository
@Slf4j
class DashboardRepo {
    @Autowired
    MongoHolder mongo
    @Autowired
    EsHolder elasticsearch
    @Value('${es.index.homePage}')
    String index
    @Value('${es.index.homeRiseRate}')
    String riseRateIndex
    @Value('${es.index.news}')
    String newsIndex


    def getHighlightNewsIds(List<Site> sites, int pageSize, int offset) {
        String[] includeList = ["id","publishTime"]
        String[] excludeList = []
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder()
        BoolQueryBuilder siteQueryBuilder = new BoolQueryBuilder()
        sites.each { site ->
            switch (site.siteType) {
                case Site.SITE_TYPE_WEBSITE:
                    siteQueryBuilder.should(QueryBuilders.boolQuery()
                            .must(QueryBuilders.prefixQuery("domainReverseUrl", (null == site.domainReverse || site.domainReverse == "") ? UrlUtils.getReverseDomainFromUrl(site.websiteDomain) : site.domainReverse)))
                    break
            }
        }
        if (sites.size() > 0) {
            boolQueryBuilder.must(siteQueryBuilder)
        }
        boolQueryBuilder.mustNot(QueryBuilders.termQuery("status", 2))//代表是不是头条标志 1 3 在 2不在
        String sortOrder = "publishTime";//排序
        SearchRequestBuilder prepareSearch = elasticsearch.client.prepareSearch(index)
                .setTypes("homePageNews")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFetchSource(includeList, excludeList)
                .setQuery(boolQueryBuilder)
                .setFrom(offset)
                .setSize(pageSize)
                .addSort(sortOrder, SortOrder.DESC)
        SearchResponse response = prepareSearch.execute().actionGet()
        def result = []
        response?.getHits()?.getHits()?.each { hit ->
            def searchResult = hit.getSource()
            result << searchResult
        }
        return result
    }

    def getNewsListByIds(List newsIds) {
        def result = [:]
        def existNewsIds = []
        def noExistNewsIds = []
        def newsList = []
        try {
            MultiGetResponse multiGetItemResponses = elasticsearch.client.prepareMultiGet()
                    .add(newsIndex, "news", newsIds)
                    .get();
            for (MultiGetItemResponse itemResponse : multiGetItemResponses) {
                GetResponse response = itemResponse.getResponse();
                if (response.isExists()) {
                    existNewsIds << response.getId()
                    Map currNewsMap = response.getSourceAsMap()
                    newsList << currNewsMap
                } else {
                    noExistNewsIds << response.getId()
                }
            }
        } catch (Exception e) {
            log.error("获取新闻信息异常：{}", e.getMessage())
            return null
        }
        return newsList
    }

    //传播监控
    def getRiseNewsList(List<Site> sites, int pageSize, int offset) {
        String[] includeList = ["id", "riseRate", "createTime"]
        String[] excludeList = []
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder()
        BoolQueryBuilder siteQueryBuilder = new BoolQueryBuilder()
        sites.each { site ->
            switch (site.siteType) {
                case Site.SITE_TYPE_WEBSITE:
                    siteQueryBuilder.should(QueryBuilders.boolQuery()
                            .must(QueryBuilders.prefixQuery("domainReverseUrl", (null == site.domainReverse || site.domainReverse == "") ? UrlUtils.getReverseDomainFromUrl(site.websiteDomain) : site.domainReverse)))
                    break
            }
        }
        if (sites.size() > 0) {
            boolQueryBuilder.must(siteQueryBuilder)
        }
        String sortOrder = "riseRate";//排序
        SearchRequestBuilder prepareSearch = elasticsearch.client.prepareSearch(riseRateIndex)
                .setTypes("news")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFetchSource(includeList, excludeList)
                .setQuery(boolQueryBuilder)
                .setFrom(offset)
                .setSize(pageSize)
                .addSort(sortOrder, SortOrder.DESC)
        SearchResponse response = prepareSearch.execute().actionGet()
        def result = []
        response?.getHits()?.getHits()?.each { hit ->
            def searchResult = hit.getSource()
            result << searchResult
        }
        return result
    }


}
