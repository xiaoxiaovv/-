package com.istar.mediabroken.repo.analysis

import com.istar.mediabroken.entity.analysis.TopNews
import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.entity.capture.SiteDetail
import com.istar.mediabroken.utils.AnalysisUtils
import com.istar.mediabroken.utils.EsHolder
import com.istar.mediabroken.utils.MongoHolder
import com.istar.mediabroken.utils.UrlUtils
import com.mongodb.AggregationOutput
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.QueryBuilder
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.MatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.RangeQueryBuilder
import org.elasticsearch.search.sort.SortOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.api.ApiResult.apiResult
import static com.istar.mediabroken.utils.MongoHelper.toObj


/**
 * Author: zc
 * Time: 2017/8/3
 */
@Repository
@Slf4j
class ChannelRepo {
    @Autowired
    MongoHolder mongo
    @Autowired
    EsHolder elasticsearch
    @Value('${es.index.news}')
    String index

    boolean modifyAnalysisSites(Map analysisSites) {
        def collection = mongo.getCollection("analysisSites")
        def userId = analysisSites.userId
        analysisSites.remove('userId')
        collection.update(
                toObj([_id: userId]),
                toObj(['$set': analysisSites]),
                true,
                false)
        return true
    }

    List getWeeklyNews(List sites) {
        def result = []
        def collection = mongo.getCollection("topnews")
        QueryBuilder queryBuilder = QueryBuilder.start()
        def query = new ArrayList<DBObject>();
        sites.each { site ->
            QueryBuilder qb = QueryBuilder.start()
            if (site.siteType == 1) {
                qb.put("newsType").is(1)
                qb.put("siteDomain").is(site.siteDomain)
            }
            if (site.siteType == 2) {
                qb.put("newsType").is(4)
                qb.put("siteName").is(site.siteName)
            }
            if (site.siteType == 3) {
                qb.put("newsType").is(6)
                qb.put("siteName").is(site.siteName)
            }
            query.add(qb.get())
        }
        queryBuilder.or(query.toArray(new DBObject[sites.size()]))

        def cursor = collection.find(queryBuilder.get()).sort(toObj(["mii": -1])).limit(5)
        while (cursor.hasNext()) {
            def topNews = cursor.next()
            sites.each {site->
                if (site.siteType == 1 && site.siteDomain.equals(topNews.siteDomain)) {
                    topNews.siteName = site.siteName
                }
            }
            result << new TopNews(topNews)
        }
        return result
    }

    List getWeeklyNewsSummary(List sites) {
        def collection = mongo.getCollection("siteInfo")
        QueryBuilder queryBuilder = QueryBuilder.start()
        def query = new ArrayList<DBObject>()
        def keyvalue = [:]
        sites.each { site ->
            QueryBuilder qb = QueryBuilder.start()
            if (site.siteType == 1) {
                qb.put("newsType").is(1)
                qb.put("siteDomain").is(site.siteDomain)
                keyvalue.put(site.siteDomain + site.siteType, site.siteName)
            }
            if (site.siteType == 2) {
                qb.put("newsType").is(4)
                qb.put("siteDomain").is(site.siteName)
            }
            if (site.siteType == 3) {
                qb.put("newsType").is(6)
                qb.put("siteDomain").is(site.siteName)
            }
            query.add(qb.get())
        }
        queryBuilder.or(query.toArray(new DBObject[sites.size()]))
        def project = toObj(["newsType": 1, "siteDomain": 1, "siteName": 1, "publishCount": 1, "time": 1, "_id": 0])
        def cursor = collection.find(queryBuilder.get(), project)
/*.sort(toObj(["newsType":1,"siteDomain":1,"siteName":1,]))*/


        def result = []
        while (cursor.hasNext()) {
            def siteInfo = cursor.next()
            result << siteInfo
        }

        cursor.close()
        return result
    }

    Map getAnalysisSites(long userId) {
        def collection = mongo.getCollection("analysisSites")
        def res = collection.findOne(toObj([_id: userId]))
        return apiResult([status: HttpStatus.SC_OK, msg: res])
    }

    List getAllAnalysisSites() {
        def collection = mongo.getCollection("analysisSites")
        def cursor = collection.find()
        def sites = []
        while (cursor.hasNext()) {
            def analysisSite = cursor.next()
            if (analysisSite.sites) {
                analysisSite.sites.each {
                    sites << it
                }
            }
        }
        return sites
    }

    List getKeywordsWholeNet(String startDay, String endDay) {
        def collection = mongo.getCollection("keywords")
        QueryBuilder key = QueryBuilder.start()
        key.put("siteDomain").is("total")
        key.put("time").greaterThanEquals(startDay).lessThan(endDay)
        def cursor = collection.find(key.get())
        def keywordList = []
        while (cursor.hasNext()) {
            def keywords = cursor.next()
            keywordList << keywords
        }
        cursor.close()
        return keywordList
    }

    List getKeywordsByChannel(Map channel, String startDay, String endDay) {
        def keywordList = []
        def sites = channel?.sites
        if (!sites) {
            return null
        }
        def collection = mongo.getCollection("keywords")
        QueryBuilder queryBuilder = QueryBuilder.start()
        def querys = new ArrayList<DBObject>()
        sites.each { site ->
            QueryBuilder qb = QueryBuilder.start()
            if (site.siteType == 1) {
                qb.put("newsType").is(1)
                qb.put("siteDomain").is(site.siteDomain)
            }
            if (site.siteType == 2) {
                qb.put("newsType").is(4)
                qb.put("siteDomain").is(site.siteName)
            }
            if (site.siteType == 3) {
                qb.put("newsType").is(6)
                qb.put("siteDomain").is(site.siteName)
            }
            querys.add(qb.get())
        }
        queryBuilder.or(querys.toArray(new DBObject[sites.size()]))
        BasicDBObject match = new BasicDBObject("\$match", queryBuilder.get())
        QueryBuilder timeFilter = QueryBuilder.start()
        timeFilter.put("time").greaterThanEquals(startDay).lessThan(endDay)
        BasicDBObject filter = new BasicDBObject("\$match", timeFilter.get())
        def project = new BasicDBObject("\$project", new BasicDBObject("wordCloud", 1).append("time", 1).append("_id", 0))
        def unwind = new BasicDBObject("\$unwind", "\$wordCloud")
        BasicDBObject group = new BasicDBObject("\$group", new BasicDBObject("_id", "\$wordCloud.word").
                append("count", new BasicDBObject("\$sum", "\$wordCloud.count")))
        def sort = new BasicDBObject("\$sort", new BasicDBObject("count", -1))
        def limit = new BasicDBObject("\$limit", 30)
        def siteWord = collection.aggregate(Arrays.asList(match, project, unwind, group, sort, limit))
        for (BasicDBObject dbObject : siteWord.results()) {
            keywordList << ["word": dbObject._id, "count": dbObject.count]
        }
        return keywordList
    }

    List getLatestNewsByChannel(Map channel) {
        String[] includeList = ["siteName", "title", "publishTime", "simhash"]
        String[] excludeList = []
        def sites = channel?.sites
        if (!sites) {
            return null
        }
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder()
        sites.each { site ->
            BoolQueryBuilder subBoolQueryBuilder = new BoolQueryBuilder()
            switch (site.siteType) {
                case 1:
                    subBoolQueryBuilder.must(QueryBuilders.prefixQuery("domainReverseUrl", UrlUtils.getReverseDomainFromUrl(site.siteDomain)))
                    subBoolQueryBuilder.must(QueryBuilders.termQuery("newsType", 1))
                    break
                case 2:
                    subBoolQueryBuilder.must(QueryBuilders.termQuery("siteName", site.siteName))
                    subBoolQueryBuilder.must(QueryBuilders.termQuery("newsType", 401))
                    break
                case 3:
                    subBoolQueryBuilder.must(QueryBuilders.termQuery("siteName", site.siteName))
                    subBoolQueryBuilder.must(QueryBuilders.termQuery("newsType", 6))
                    break
            }
            boolQueryBuilder.should(subBoolQueryBuilder)
        }

        SearchResponse response = elasticsearch.client.prepareSearch(index)
                .setTypes("news")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFetchSource(includeList, excludeList)
                .setQuery(boolQueryBuilder)
                .setSize(50)
                .addSort("publishTime", SortOrder.DESC)
                .execute()
                .actionGet()
        def result = []
        response?.getHits()?.getHits()?.each {
            result << it.source
        }
        return result
    }

    boolean getNewsBySite(def site) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder()
        BoolQueryBuilder subBoolQueryBuilder = new BoolQueryBuilder()
        switch (site.siteType) {
            case 1:
                boolQueryBuilder.must(QueryBuilders.prefixQuery("domainReverseUrl", UrlUtils.getReverseDomainFromUrl(site.siteDomain)))
                boolQueryBuilder.must(QueryBuilders.termQuery("newsType", 1))
                break
            case 2:
                boolQueryBuilder.must(QueryBuilders.termQuery("siteName", site.siteName))
                subBoolQueryBuilder.should(QueryBuilders.termQuery("newsType", 4))
                subBoolQueryBuilder.should(QueryBuilders.termQuery("newsType", 401))
                subBoolQueryBuilder.should(QueryBuilders.termQuery("newsType", 402))
                subBoolQueryBuilder.should(QueryBuilders.termQuery("newsType", 8))
                boolQueryBuilder.must(subBoolQueryBuilder)
                break
            case 3:
                boolQueryBuilder.must(QueryBuilders.termQuery("siteName", site.siteName))
                boolQueryBuilder.must(QueryBuilders.termQuery("newsType", 6))
                break
        }

        SearchResponse response = elasticsearch.client.prepareSearch(index)
                .setTypes("news")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(boolQueryBuilder)
                .setSize(50)
                .execute()
                .actionGet()
        boolean result = false
        if (response?.getHits()?.getHits()) {
            result = true
        }
        return result
    }

    //每个渠道的发稿量
    Long getPublishCount(String siteDomain, Long newsType, String start, String end) {
        def collection = mongo.getCollection("siteInfo")
        BasicDBObject query = new BasicDBObject("siteDomain", siteDomain)
                .append("time", new BasicDBObject("\$gte", start).append("\$lt", end))
                .append("newsType", newsType)
        BasicDBObject match = new BasicDBObject("\$match", query)
        BasicDBObject projection = new BasicDBObject("\$project", new BasicDBObject("newsType", 1)
                .append("publishCount", 1).append("siteDomain", 1).append("_id", 0))
        BasicDBObject group = new BasicDBObject("\$group",
                new BasicDBObject("_id", new BasicDBObject("siteDomain", "\$siteDomain").append("newsType", "\$newsType"))
                        .append("publishSum", new BasicDBObject("\$sum", "\$publishCount")))
        def aggregate = collection.aggregate(Arrays.asList(match, projection, group))
        def list = aggregate.results()
        Long publishCount = 0L
        for (BasicDBObject dbObject : list) {
            publishCount = dbObject.getLong("publishSum")
        }
        return publishCount
    }

    //每个渠道发稿的被转载量
    Long getRepintCountByType(String domain, Long type, String start, String end, int reprintType) {
        def collection = mongo.getCollection("reprint")
        BasicDBObject query = new BasicDBObject("siteDomain", domain)
                .append("time", new BasicDBObject("\$gte", start).append("\$lt", end))
                .append("newsType", type)
        if (reprintType != Integer.MIN_VALUE) {
            query.append("reprintType", reprintType)
        }
        BasicDBObject match = new BasicDBObject("\$match", query)
        BasicDBObject projection = new BasicDBObject("\$project",
                new BasicDBObject("newsType", 1)
                        .append("reprintCount", 1)
                        .append("siteDomain", 1)
                        .append("_id", 0))
        BasicDBObject group = new BasicDBObject("\$group",
                new BasicDBObject("_id",
                        new BasicDBObject("siteDomain", "\$siteDomain")
                                .append("newsType", "\$newsType"))
                        .append("reprintSum", new BasicDBObject("\$sum", "\$reprintCount")))
        def aggregate = collection.aggregate(Arrays.asList(match, projection, group))
        def list = aggregate.results()
        Long reprintCount = 0L
        for (BasicDBObject dbObject : list) {
            reprintCount = dbObject.reprintSum as Long
        }
        return reprintCount
    }

    //转载媒体数 isImportant = Int.MAX_VALUE      重点媒体数  isImportant = 1
    Long getMediaCount(String domain, Long type, String start, String end) {
        def collection = mongo.getCollection("sitereprintInfo")
        BasicDBObject query = new BasicDBObject("siteDomain", domain)
                .append("time", new BasicDBObject("\$gte", start).append("\$lt", end))
                .append("newsType", type)

        BasicDBObject match = new BasicDBObject("\$match", query)
        BasicDBObject projection = new BasicDBObject("\$project", new BasicDBObject("newsType", 1)
                .append("mediaCount", 1)
                .append("siteDomain", 1)
                .append("_id", 0))
        BasicDBObject group = new BasicDBObject("\$group", new BasicDBObject("_id",
                new BasicDBObject("siteDomain", "\$siteDomain").append("newsType", "\$newsType")).
                append("mediaCount", new BasicDBObject("\$sum", "\$mediaCount")))
        def aggregate = collection.aggregate(Arrays.asList(match, projection, group))
        def list = aggregate.results()
        Long mediaCount = 0L
        for (BasicDBObject dbObject : list) {
            mediaCount = dbObject.mediaCount
        }
        return mediaCount
    }


    Long getImportantMediaCount(String domain, Long type, String start, String end) {
        def collection = mongo.getCollection("sitereprintInfo")
        BasicDBObject query = new BasicDBObject("siteDomain", domain)
                .append("time", new BasicDBObject("\$gte", start).append("\$lt", end))
                .append("newsType", type)

        BasicDBObject match = new BasicDBObject("\$match", query)
        BasicDBObject projection = new BasicDBObject("\$project", new BasicDBObject("newsType", 1)
                .append("mediaCount", 1)
                .append("siteDomain", 1)
                .append("_id", 0))
        BasicDBObject group = new BasicDBObject("\$group", new BasicDBObject("_id",
                new BasicDBObject("siteDomain", "\$siteDomain").append("newsType", "\$newsType")).
                append("important", new BasicDBObject("\$sum", "\$important")))
        def aggregate = collection.aggregate(Arrays.asList(match, projection, group))
        def list = aggregate.results()
        Long mediaCount = 0L
        for (BasicDBObject dbObject : list) {
            mediaCount = dbObject.important
        }
        return mediaCount
    }

    //每个站点的阅读、评论、点赞统计
    Object getBaseStatistics(String domain, Long type, String field, String start, String end, String operator) {
        def collection = mongo.getCollection("siteInfo")
        BasicDBObject query = new BasicDBObject("siteDomain", domain)
                .append("time", new BasicDBObject("\$gte", start)
                .append("\$lt", end))
                .append("newsType", type)
        BasicDBObject match = new BasicDBObject("\$match", query)
        BasicDBObject projection = new BasicDBObject("\$project", new BasicDBObject("newsType", 1)
                .append(field, 1).append("siteDomain", 1).append("_id", 0))
        BasicDBObject group = new BasicDBObject("\$group",
                new BasicDBObject("_id", new BasicDBObject("siteDomain", "\$siteDomain")
                        .append("newsType", "\$newsType"))
                        .append("result", new BasicDBObject("\$" + operator, "\$" + field)))
        def aggregate = collection.aggregate(Arrays.asList(match, projection, group))
        def list = aggregate.results()
        def res = 0.0
        for (BasicDBObject dbObject : list) {
            res = dbObject.result as double
        }
        return res
    }

    //日均阅读数
    double getPerReadByDay(String domain, Long type, String start, String end) {
        return getBaseStatistics(domain, type, "readCounts", start, end, "avg")
    }

    //单篇平均阅读
    double getPerAverageRead(String domain, Long type, String start, String end) {
        return getBaseStatistics(domain, type, "avgRead", start, end, "avg")
    }

    //阅读总数
    Long getReadSum(String domain, Long type, String start, String end) {
        return getBaseStatistics(domain, type, "readCounts", start, end, "sum")
    }

    //最大单篇阅读
    Long getPerReadMax(String domain, Long type, String start, String end) {
        return getBaseStatistics(domain, type, "maxRead", start, end, "max")
    }

    //日均评论数
    double getPerCommentByDay(String domain, Long type, String start, String end) {
        return getBaseStatistics(domain, type, "commentCounts", start, end, "avg")
    }

    //单篇平均评论
    double getPerAverageComment(String domain, Long type, String start, String end) {
        return getBaseStatistics(domain, type, "avgComment", start, end, "avg")
    }

    //评论总数
    Long getCommentSum(String domain, Long type, String start, String end) {
        return getBaseStatistics(domain, type, "commentCounts", start, end, "sum")
    }

    //最大单篇评论
    Long getPerCommentMax(String domain, Long type, String start, String end) {
        return getBaseStatistics(domain, type, "maxComment", start, end, "max")
    }

    //日均点赞数
    double getPerLikeByDay(String domain, Long type, String start, String end) {
        return getBaseStatistics(domain, type, "likeCounts", start, end, "avg")
    }

    //单篇平均点赞
    double getPerAverageLike(String domain, Long type, String start, String end) {
        return getBaseStatistics(domain, type, "avgLike", start, end, "avg")
    }

    //评论总数
    Long getLikeSum(String domain, Long type, String start, String end) {
        return getBaseStatistics(domain, type, "likeCounts", start, end, "sum")
    }

    //最大单篇评论
    Long getPerLikeMax(String domain, Long type, String start, String end) {
        return getBaseStatistics(domain, type, "maxLike", start, end, "max")
    }

    //日均正面评论数
    double getPositiveComments(String domain, Long type, String start, String end) {
        return 0.0
    }
    //日均反面评论数
    double getNegativeComments(String domain, Long type, String start, String end) {
        return 0.0
    }

    //日均关键词匹配数
    double getWordMatch(String domain, Long type, String start, String end) {
        def collection = mongo.getCollection("keywords")
        BasicDBObject query = new BasicDBObject("siteDomain", domain)
                .append("time", new BasicDBObject("\$gte", start)
                .append("\$lt", end))
                .append("newsType", type)
        BasicDBObject match = new BasicDBObject("\$match", query)
        BasicDBObject projection = new BasicDBObject("\$project",
                new BasicDBObject("newsType", 1)
                        .append("siteDomain", 1)
                        .append("WordMatch", 1)
                        .append("_id", 0))
        BasicDBObject group = new BasicDBObject("\$group",
                new BasicDBObject("_id", new BasicDBObject("siteDomain", "\$siteDomain")
                        .append("newsType", "\$newsType"))
                        .append("result", new BasicDBObject("\$avg", "\$WordMatch")))
        def aggregate = collection.aggregate(Arrays.asList(match, projection, group))
        def list = aggregate.results()
        double result = 0.0
        for (BasicDBObject dbObject : list) {
            result = dbObject.result
        }
        return result
    }

    //日均词云創新数
    double getWordInnovate(String domain, Long type, String start, String end) {
        def collection = mongo.getCollection("keywords")
        BasicDBObject query = new BasicDBObject("siteDomain", domain)
                .append("time", new BasicDBObject("\$gte", start)
                .append("\$lt", end))
                .append("newsType", type)
        BasicDBObject match = new BasicDBObject("\$match", query)
        BasicDBObject projection = new BasicDBObject("\$project",
                new BasicDBObject("newsType", 1)
                        .append("siteDomain", 1)
                        .append("wordInnovate", 1)
                        .append("_id", 0))
        BasicDBObject group = new BasicDBObject("\$group",
                new BasicDBObject("_id", new BasicDBObject("siteDomain", "\$siteDomain")
                        .append("newsType", "\$newsType"))
                        .append("result", new BasicDBObject("\$avg", "\$wordInnovate")))
        def aggregate = collection.aggregate(Arrays.asList(match, projection, group))
        def list = aggregate.results()
        double result = 0.0
        for (BasicDBObject dbObject : list) {
            result = dbObject.result
        }
        return result
    }

    //日均关键词传播数
    double getWordSpread(String domain, Long type, String start, String end) {
        def collection = mongo.getCollection("keywords")
        BasicDBObject query = new BasicDBObject("siteDomain", domain)
                .append("time", new BasicDBObject("\$gte", start)
                .append("\$lt", end))
                .append("newsType", type)
        BasicDBObject match = new BasicDBObject("\$match", query)
        BasicDBObject projection = new BasicDBObject("\$project",
                new BasicDBObject("newsType", 1)
                        .append("siteDomain", 1)
                        .append("wordSpread", 1)
                        .append("_id", 0))
        BasicDBObject group = new BasicDBObject("\$group",
                new BasicDBObject("_id", new BasicDBObject("siteDomain", "\$siteDomain")
                        .append("newsType", "\$newsType"))
                        .append("result", new BasicDBObject("\$avg", "\$wordSpread")))
        def aggregate = collection.aggregate(Arrays.asList(match, projection, group))
        def list = aggregate.results()
        double result = 0.0
        for (BasicDBObject dbObject : list) {
            result = dbObject.result
        }
        return result
    }

    //日均重大事件稿量
    double getImporantEvent(String domain, Long type, String start, String end) {
        def collection = mongo.getCollection("keywords")
        BasicDBObject query = new BasicDBObject("siteDomain", domain)
                .append("time", new BasicDBObject("\$gte", start)
                .append("\$lt", end))
                .append("newsType", type)
        BasicDBObject match = new BasicDBObject("\$match", query)
        BasicDBObject projection = new BasicDBObject("\$project",
                new BasicDBObject("newsType", 1)
                        .append("siteDomain", 1)
                        .append("importantEvents", 1)
                        .append("_id", 0))
        BasicDBObject group = new BasicDBObject("\$group",
                new BasicDBObject("_id", new BasicDBObject("siteDomain", "\$siteDomain")
                        .append("newsType", "\$newsType"))
                        .append("result", new BasicDBObject("\$avg", "\$importantEvents")))
        def aggregate = collection.aggregate(Arrays.asList(match, projection, group))
        def list = aggregate.results()
        double result = 0.0
        for (BasicDBObject dbObject : list) {
            result = dbObject.result
        }
        return result
    }

    //日均原稿量
    double getManuscriptCount(String domain, Long type, String start, String end) {
        def collection = mongo.getCollection("siteInfo")
        BasicDBObject query = new BasicDBObject("siteDomain",domain)
                .append("time",new BasicDBObject("\$gte",start)
                .append("\$lt",end))
                .append("newsType",type)
        BasicDBObject match = new BasicDBObject("\$match",query)
        BasicDBObject projection = new BasicDBObject("\$project",new BasicDBObject("newsType",1)
                .append("originalCount",1).append("siteDomain",1).append("_id",0))
        BasicDBObject group = new BasicDBObject("\$group",
                new BasicDBObject("_id",new BasicDBObject("siteDomain","\$siteDomain")
                        .append("newsType","\$newsType"))
                        .append("result",new BasicDBObject("\$avg","\$originalCount") ) )
        def aggregate = collection.aggregate(Arrays.asList( match, projection,group ) )
        def list = aggregate.results()
        def res = 0.0
        for( BasicDBObject dbObject:list ){
            res =  dbObject.result ? dbObject.result as double : 0.0  as double
        }
        return res
    }

    //日均被轉載稿量
    double getAverageReprintCount(String domain, Long type, String start, String end, Long reprintType) {
        def collection = mongo.getCollection("reprint")
        BasicDBObject query = new BasicDBObject("siteDomain", domain)
                .append("time", new BasicDBObject("\$gte", start).append("\$lt", end))
                .append("newsType", type)
        if (reprintType != Integer.MIN_VALUE) {
            query.append("reprintType", reprintType)
        }
        BasicDBObject match = new BasicDBObject("\$match", query)
        BasicDBObject projection = new BasicDBObject("\$project",
                new BasicDBObject("newsType", 1)
                        .append("reprintCount", 1)
                        .append("siteDomain", 1)
                        .append("_id", 0))
        BasicDBObject group = new BasicDBObject("\$group",
                new BasicDBObject("_id",
                        new BasicDBObject("siteDomain", "\$siteDomain")
                                .append("newsType", "\$newsType"))
                        .append("reprintSum", new BasicDBObject("\$avg", "\$reprintCount")))
        def aggregate = collection.aggregate(Arrays.asList(match, projection, group))
        def list = aggregate.results()
        double reprintSum = 0.0
        for (BasicDBObject dbObject : list) {
            reprintSum = dbObject.reprintSum
        }
        return reprintSum
    }

    //日均轉載媒體总数 日均重点媒体数
    double getAverageMediaCount(String domain, Long type, String start, String end, int isImportant) {
        def collection = mongo.getCollection("sitereprintInfo")
        BasicDBObject query = new BasicDBObject("siteDomain", domain)
                .append("time", new BasicDBObject("\$gte", start).append("\$lt", end))
                .append("newsType", type)

        if (isImportant != Integer.MAX_VALUE) {
            query.append("isImportant", isImportant)
        }
        BasicDBObject match = new BasicDBObject("\$match", query)
        BasicDBObject projection = new BasicDBObject("\$project", new BasicDBObject("newsType", 1)
                .append("mediaCount", 1)
                .append("siteDomain", 1)
                .append("_id", 0))
        BasicDBObject group = new BasicDBObject("\$group", new BasicDBObject("_id",
                new BasicDBObject("siteDomain", "\$siteDomain").append("newsType", "\$newsType")).
                append("mediaCount", new BasicDBObject("\$avg", "\$mediaCount")))
        def aggregate = collection.aggregate(Arrays.asList(match, projection, group))
        def list = aggregate.results()
        double mediaCount = 0.0
        for (BasicDBObject dbObject : list) {
            mediaCount = dbObject.mediaCount
        }
        return mediaCount
    }

    //日均被转载微信数
    double getAverageReprint(String domain, Long type, String start, String end){
        def collection = mongo.getCollection("reprint")
        BasicDBObject query = new BasicDBObject("siteDomain",domain)
                .append("time", new BasicDBObject("\$gte", start).append("\$lt", end))
                .append("newsType", 6)
        BasicDBObject match = new BasicDBObject("\$match",query)
        BasicDBObject projection = new BasicDBObject("\$project",
                new BasicDBObject("newsType",1)
                        .append("reprintCount",1)
                        .append("siteDomain",1)
                        .append("_id",0))
        BasicDBObject group = new BasicDBObject("\$group",
                new BasicDBObject("_id",
                        new BasicDBObject("siteDomain","\$siteDomain")
                                .append("newsType","\$newsType"))
                        .append("reprintSum",new BasicDBObject("\$avg","\$reprintCount") ) )
        def aggregate = collection.aggregate(Arrays.asList( match, projection,group ) )
        def list = aggregate.results()
        double reprintSum = 0.0
        for( BasicDBObject dbObject:list ){
            reprintSum =  dbObject.reprintSum
        }
        return reprintSum
    }

    //日均被转载微博数
    double getAverageWeibo(String domain, Long type, String start, String end){
        def collection = mongo.getCollection("reprint")
        BasicDBObject query = new BasicDBObject("siteDomain",domain)
                .append("time", new BasicDBObject("\$gte", start).append("\$lt", end))
                .append("newsType", 4)
        BasicDBObject match = new BasicDBObject("\$match",query)
        BasicDBObject projection = new BasicDBObject("\$project",
                new BasicDBObject("newsType",1)
                        .append("reprintCount",1)
                        .append("siteDomain",1)
                        .append("_id",0))
        BasicDBObject group = new BasicDBObject("\$group",
                new BasicDBObject("_id",
                        new BasicDBObject("siteDomain","\$siteDomain")
                                .append("newsType","\$newsType"))
                        .append("reprintSum",new BasicDBObject("\$avg","\$reprintCount") ) )
        def aggregate = collection.aggregate(Arrays.asList( match, projection,group ) )
        def list = aggregate.results()
        double reprintSum = 0.0
        for( BasicDBObject dbObject:list ){
            reprintSum =  dbObject.reprintSum
        }
        return reprintSum
    }

    def newAnalysisSites(Map newAnalysisSites){
        def collection = mongo.getCollection("analysisSites")
        def save = collection.insert(toObj([
                _id: newAnalysisSites.userId,
                orgName: newAnalysisSites.orgName,
                sites: newAnalysisSites.sites,
                updateTime: newAnalysisSites.updateTime,
        ]))
        return save
    }
}
