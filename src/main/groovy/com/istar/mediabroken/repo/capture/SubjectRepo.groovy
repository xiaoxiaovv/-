package com.istar.mediabroken.repo.capture

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.entity.capture.KeywordsScopeEnum
import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.entity.capture.Subject
import com.istar.mediabroken.utils.EsHolder
import com.istar.mediabroken.utils.MongoHolder
import com.istar.mediabroken.utils.StringUtils
import com.mongodb.QueryBuilder
import groovy.util.logging.Slf4j
import org.elasticsearch.action.search.SearchRequestBuilder
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.common.text.Text
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.RangeQueryBuilder
import org.elasticsearch.search.highlight.HighlightField
import org.elasticsearch.search.sort.SortOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Author: zhaochen
 * Time: 2017/8/1
 */
@Repository
@Slf4j
class SubjectRepo {
    @Autowired
    MongoHolder mongo
    @Autowired
    EsHolder elasticsearch
    @Value('${es.index.news}')
    String index

    private String collectionName = "subject"

    String addSubject(Subject subject) {
        def collection = mongo.getCollection("subject")
        def map = subject.toMap()
        collection.insert(toObj(map))
        return map._id
    }

    int getSubjectCount(long userId) {
        int count = 0
        def collection = mongo.getCollection("subject")
        count = collection.find(toObj([userId: userId])).count()
        return count
    }

    boolean isSubjectExist(long userId, String subjectName) {
        def collection = mongo.getCollection("subject")
        def subject = collection.findOne(toObj([userId: userId, subjectName: subjectName]))
        if (subject) {
            return true
        } else {
            return false
        }
    }

    List<Subject> getUserSubjects(long userId) {
        def collection = mongo.getCollection("subject")
        QueryBuilder sort = QueryBuilder.start()
        sort.put("pinYinPrefix").is(1)
        def cursor = collection.find(toObj([userId: userId])).sort(sort.get())
        def result = []
        while (cursor.hasNext()) {
            def subject = cursor.next()
            result << new Subject(subject)
        }
        cursor.close()
        return result
    }

    Subject getUserSubjectById(long userId, String subjectId) {
        def collection = mongo.getCollection("subject")
        def subject = collection.findOne(toObj([userId: userId, _id: subjectId]))
        while (subject) {
            return new Subject(subject)
        }
        return null
    }

    void removeSubject(long userId, String subjectId) {
        def collection = mongo.getCollection("subject")
        collection.remove(toObj([userId: userId, _id: subjectId]))
    }

    boolean modifySubject(Subject subject) {
        def collection = mongo.getCollection("subject")
        collection.update(
                toObj([
                        userId: subject.userId,
                        _id   : subject.subjectId]),
                toObj(['$set': [
                        subjectName : subject.subjectName,
                        keywordsScope: subject.keywordsScope,
                        titleKeywords: subject.titleKeywords,
                        keyWords    : subject.keyWords,
                        areaWords   : subject.areaWords,
                        excludeWords: subject.excludeWords,
                        pinYinPrefix: subject.pinYinPrefix,
                        updateTime  : subject.updateTime
                ]]))
        return true
    }

    void resetSubjectCountInfo(long userId, String subjectId) {
        def collection = mongo.getCollection(collectionName)
        Date now = new Date()
        collection.update(toObj([_id: subjectId, userId: userId]), toObj(['$set': [resetTime: now, count: 0]]))
    }
    void modifySubjectCountInfo(long userId, String subjectId, long count) {
        def collection = mongo.getCollection(collectionName)
        Date now = new Date()
        collection.update(toObj([_id: subjectId, userId: userId]), toObj(['$set': [countTime: now, count: count]]))
    }

    List<Subject> getSubjects() {
        def collection = mongo.getCollection("subject")
        def cursor = collection.find()
        def result = []
        while (cursor.hasNext()) {
            def subject = cursor.next()
            result << new Subject(subject)
        }
        cursor.close()
        return result
    }
    @Deprecated
    List getSubjectNewsFromEs(Subject subject, int siteType, int hot, Date startTime, Date endTime, String classification,
                              int orientation, int hasPic, int order, String keyWords, int pageSize, int pageNo) {
        String[] includeList = ["id", "author", "poster", "siteName", "contentAbstract", "title", "cover", "imgUrls", "reprintCount", "captureTime", "url", "firstPublishTime", "firstPublishSiteName"]
        String[] excludeList = []

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder()

        //处理话题 关键词 地域词 排除词
        if (subject.keyWords && !(subject.keyWords.equals(""))) {
            def orKeywords = subject.keyWords.split("\\|")//每一组or条件
            def keywordsBoolQueryBuilder = new BoolQueryBuilder()
            for (int i = 0; i < orKeywords.size(); i++) {
                String strWithSpace = orKeywords[i]//每一组and条件
                def andKeywords = strWithSpace.split(" ")//每一项and[]
                def andBoolQueryBuilder = new BoolQueryBuilder()
                for (int j = 0; j < andKeywords.size(); j++) {
                    andBoolQueryBuilder.must(QueryBuilders.matchPhraseQuery("content", andKeywords[j]))
                }
                keywordsBoolQueryBuilder.should(andBoolQueryBuilder)
            }
            boolQueryBuilder.must(keywordsBoolQueryBuilder)
        }

        if (subject.areaWords && !(subject.areaWords.equals(""))) {
            def subjectAreas = subject.areaWords.split(" ")
            def areaBoolQueryBuilder = new BoolQueryBuilder()
            subjectAreas.each {
                areaBoolQueryBuilder.should(QueryBuilders.matchPhraseQuery("content", it))
            }
            boolQueryBuilder.must(areaBoolQueryBuilder)
        }

        if (subject.excludeWords && !(subject.excludeWords.equals(""))) {
            def subjectExclude = subject.excludeWords.split(" ")
            def excludeBoolQueryBuilder = new BoolQueryBuilder()
            subjectExclude.each {
                excludeBoolQueryBuilder.should(QueryBuilders.matchPhraseQuery("content", it))
            }
            boolQueryBuilder.mustNot(excludeBoolQueryBuilder)
        }

        switch (siteType) {
            case 1:
                boolQueryBuilder.must(QueryBuilders.termQuery("newsType", 1))
                break
            case 2:
                boolQueryBuilder.must(QueryBuilders.termQuery("newsType", 6))
                break
        }

        if (null != startTime && null != endTime) {
            RangeQueryBuilder timeRange = QueryBuilders
                    .rangeQuery("captureTime")
                    .from(startTime).to(endTime);
            boolQueryBuilder.must(timeRange)
        }

        if (!(null == keyWords || keyWords.equals(""))) {
//            MatchQueryBuilder matchTitle = new MatchQueryBuilder("title", keyWords).operator(MatchQueryBuilder.Operator.AND).analyzer("index_ansj")
//            MatchQueryBuilder matchContent = new MatchQueryBuilder("content", keyWords).operator(MatchQueryBuilder.Operator.AND).analyzer("index_ansj")
            boolQueryBuilder.must(QueryBuilders.boolQuery().should(QueryBuilders.matchPhraseQuery("title", keyWords)).should(QueryBuilders.matchPhraseQuery("content", keyWords)))
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
        if (hasPic > 0) {
            if (hasPic == 1) {
                boolQueryBuilder.must(QueryBuilders.termQuery("hasImg", true))
            } else {
                boolQueryBuilder.must(QueryBuilders.termQuery("hasImg", false))
            }
        }

        if (classification && (!classification.equals("全部"))) {
            boolQueryBuilder.must(QueryBuilders.termQuery("siteClassification", classification))
        }
        SearchRequestBuilder prepareSearch =
                elasticsearch.client.prepareSearch(index)
                        .setTypes("news")
                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                        .setFetchSource(includeList, excludeList)
                        .setQuery(boolQueryBuilder)
                        .setFrom(pageSize * (pageNo - 1))
                        .setSize(pageSize)
                        .addSort((order == 1 ? "captureTime" : "reprintCount"), SortOrder.DESC)

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
                    // 将追加了高亮标签的串值重新填充到对应的对象
                    searchResult.contentAbstract = content
                }
            }
            result << searchResult
        }
        return result
    }

    BoolQueryBuilder getSubjectQuery(Subject subject) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder()

        if (subject.keywordsScope == KeywordsScopeEnum.contentScope.key){
            if (!subject.keyWords) {
                return boolQueryBuilder
            }
            JSONArray jsonArray = JSONArray.parse(subject.keyWords)
            def list = []
            boolean needKeywords = true
            boolean needExpress = false
            def prevSubList = []
            jsonArray.each {
                if (it.containsKey("operator")) {
                    if (!needExpress) {
                        log.error("表达式错误，缺少表达式")
                        return
                    }
                    if (it.operator.equals("or") || it.operator.equals("and")) {
                        needExpress = false
                        needKeywords = true
                    } else {
                        log.error("表达式错误，缺少表达式")
                        return
                    }
                    if (it.operator.equals("or")) {
                        list.add(prevSubList.clone())
                        prevSubList.clear()
                    }
                    return
                }
                if (!needKeywords) {
                    log.error("表达式错误，缺少关键词")
                    return
                }
                needExpress = true
                needKeywords = false
                prevSubList.add(it)
            }
            if (prevSubList.size() > 0) {
                list.add(prevSubList)
            }
            list.each { elements ->
                BoolQueryBuilder subBoolQueryBuilder = new BoolQueryBuilder()
                elements.each { element ->
                    //解析最小单元
                    subBoolQueryBuilder.must(getQuery(element))
                }
                boolQueryBuilder.should(subBoolQueryBuilder)
            }
        }else if (subject.keywordsScope == KeywordsScopeEnum.titleScope.key){
            //标题关键词查询
            if (!subject.titleKeywords) {
                return boolQueryBuilder
            }
            if (subject.titleKeywords && !("".equals(subject.titleKeywords))) {
                def titleKeywords = subject.titleKeywords.split(" ")
                titleKeywords.each {
                    boolQueryBuilder.must(QueryBuilders.matchPhraseQuery("title", it))
                }
            }
        }
        BoolQueryBuilder rootQueryBuilder = new BoolQueryBuilder()
        rootQueryBuilder.must(boolQueryBuilder)
        if (subject.excludeWords && !(subject.excludeWords.equals(""))) {
            def subjectExclude = subject.excludeWords.split(" ")
            subjectExclude.each {
                rootQueryBuilder.mustNot(QueryBuilders.matchPhraseQuery("content", it))
            }
        }
        return rootQueryBuilder
    }

    private BoolQueryBuilder getQuery(JSONObject element) {
        boolean andVsOr = element.get("subOperator").equals("and")
        List<String> keys = element.get("keywords")
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder()
        keys.each {
            if (andVsOr) {
                boolQueryBuilder.must(QueryBuilders.matchPhraseQuery("content", it))
            } else {
                boolQueryBuilder.should(QueryBuilders.matchPhraseQuery("content", it))
            }
        }
        return boolQueryBuilder
    }

    private String getKeywordsString(Subject subject, String keyWords) {
        Set keywordsList = []
        JSONArray jsonArray = JSONArray.parse(subject.keyWords)
        jsonArray.each {
            if (it.containsKey("keywords")) {
                keywordsList += it.keywords
            }
        }
        return keywordsList.join(" ") + " " + keyWords
    }
    Long getSubjectNewsCountFromEsByExpressionAndDate(Subject subject,Date startTime, Date endTime){
        BoolQueryBuilder boolQueryBuilder = getSubjectQuery(subject)
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
        SearchResponse response = elasticsearch.client.prepareSearch(index)
                .setTypes("news")
                .setSearchType(SearchType.COUNT)
                .setQuery(boolQueryBuilder)
                .execute()
                .actionGet()
        return response?.getHits()?.totalHits
    }

    List getSubjectNewsFromEsByExpression(Subject subject, int siteType, int hot, Date startTime, Date endTime, String classification,
                                          int orientation, int hasPic, int order, int queryScope, String queryString, int pageSize, int offset, boolean withHighlight) {
        String[] includeList = ["id", "author", "poster", "posterAvatar", "siteName", "newsType", "contentAbstract", "content","title", "cover", "imgUrls", "reprintCount", "captureTime", "publishTime", "url", "firstPublishTime", "firstPublishSiteName", "simhash"]
        String[] excludeList = []

        BoolQueryBuilder boolQueryBuilder = getSubjectQuery(subject)

        BoolQueryBuilder weiboNewsTypeQueryBuilder = new BoolQueryBuilder()
        weiboNewsTypeQueryBuilder.should(new BoolQueryBuilder().must(QueryBuilders.termQuery("newsType", 401)))
                .should(new BoolQueryBuilder().must(QueryBuilders.termQuery("newsType", 402)))
                .should(new BoolQueryBuilder().must(QueryBuilders.termQuery("newsType", 8)))
        switch (siteType) {
            case Site.SITE_TYPE_WEBSITE:
                boolQueryBuilder.must(QueryBuilders.termQuery("newsType", 1))
                break
            case Site.SITE_TYPE_WECHAT:
                boolQueryBuilder.must(QueryBuilders.termQuery("newsType", 6))
                break
            case Site.SITE_TYPE_WEIBO:
                boolQueryBuilder.should(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("siteName", siteType))
                        .must(weiboNewsTypeQueryBuilder))
                break
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

        if (!(null == queryString || queryString.equals(""))) {
            List wordList = queryString.split(" ")
            BoolQueryBuilder titleSubBoolQueryBuilder = new BoolQueryBuilder()
            BoolQueryBuilder contentSubBoolQueryBuilder = new BoolQueryBuilder()
            wordList.each {
                if(it){
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
        if (hasPic > 0) {
            if (hasPic == 1) {
                boolQueryBuilder.must(QueryBuilders.termQuery("hasImg", true))
            } else {
                boolQueryBuilder.must(QueryBuilders.termQuery("hasImg", false))
            }
        }

        if (classification && (!classification.equals("全部"))) {
            boolQueryBuilder.must(QueryBuilders.termQuery("siteClassification", classification))
        }
        SearchRequestBuilder prepareSearch =
                elasticsearch.client.prepareSearch(index)
                        .setTypes("news")
                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                        .setFetchSource(includeList, excludeList)
                        .setQuery(boolQueryBuilder)
                        .setFrom(offset)
                        .setSize(pageSize)
                        .addSort((order == 1 ? "captureTime" : "reprintCount"), SortOrder.DESC)
                        .addHighlightedField("content")
                        .addHighlightedField("title")
                        .setHighlighterPreTags("<b class='high-light'>")
                        .setHighlighterPostTags("</b>")

        SearchResponse response = prepareSearch.execute().actionGet()
        def result = []
        response?.getHits()?.getHits()?.each { hit ->
            def searchResult = hit.source
            if (withHighlight){
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
                    // 将追加了高亮标签的串值重新填充到对应的对象
                    Map highlightMap = StringUtils.extractHighlight(content)
                    String keywordsStr = getKeywordsString(subject, queryString)
                    highlightMap.each { key, value ->
                        if (keywordsStr.indexOf(value) == -1) {
                            content = content.replace(key as CharSequence, value as CharSequence)
                        }
                    }
                    searchResult.contentAbstract = content
                }
            }
            result << searchResult
        }
        return result
    }


    List getSubjectsLatestNewsFromEs(List<Subject> subjects, int pageSize, int pageNo) {
        String[] includeList = ["id", "author", "poster", "siteName", "content", "contentAbstract", "title", "cover", "imgUrls", "reprintCount", "captureTime", "url", "simhash", "siteClassification", "classification", "keywords"]
        String[] excludeList = []

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder()
        BoolQueryBuilder subjectBoolQueryBuilder = new BoolQueryBuilder()
        subjects.each { subject ->
            subjectBoolQueryBuilder.should(getSubjectQuery(subject))
        }

        boolQueryBuilder.must(subjectBoolQueryBuilder)

        SearchResponse response = elasticsearch.client.prepareSearch(index)
                .setTypes("news")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFetchSource(includeList, excludeList)
                .setQuery(boolQueryBuilder)
                .setFrom(pageSize * (pageNo - 1))
                .setSize(pageSize)
                .addSort("captureTime", SortOrder.DESC)
                .addSort("simhash", SortOrder.DESC)
                .execute()
                .actionGet()
        def result = []
        response?.getHits()?.getHits()?.each {
            result << it.source
        }
        return result
    }

    SearchResponse getLatestSubjectsNews(BoolQueryBuilder queryCondition, boolean hasPic, int size) {
        String[] includeList = ["id", "author", "poster", "siteName", "contentAbstract", "title", "cover", "imgUrls", "captureTime", "simhash", "reprintCount", "publishTime"]
        String[] excludeList = []
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder()
        boolQueryBuilder.must(queryCondition)
        if (hasPic) {
            boolQueryBuilder.must(QueryBuilders.termQuery("hasImg", true))
        }
        SearchRequestBuilder prepareSearch =
                elasticsearch.client.prepareSearch(index)
                        .setTypes("news")
                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                        .setFetchSource(includeList, excludeList)
                        .setQuery(boolQueryBuilder)
                        .setFrom(0)
                        .setSize(size)
                        .addSort("publishTime", SortOrder.DESC)
        SearchResponse response = prepareSearch.execute().actionGet()
        return response
    }

    SearchResponse getScrollData(SearchResponse scrollResp, long millis) {
        return elasticsearch.client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(millis)).execute().actionGet();
    }
}
