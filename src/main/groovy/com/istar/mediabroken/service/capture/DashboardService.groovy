package com.istar.mediabroken.service.capture

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.entity.capture.Subject
import com.istar.mediabroken.repo.ReprintMediaMonitorRepo
import com.istar.mediabroken.repo.RiseNewsRepo
import com.istar.mediabroken.repo.capture.*
import com.istar.mediabroken.service.rubbish.RubbishNewsService
import com.istar.mediabroken.utils.DateUitl
import com.istar.mediabroken.utils.RedisClientUtil
import com.istar.mediabroken.utils.StringUtils
import com.istar.mediabroken.utils.UrlUtils
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.elasticsearch.action.ActionRequestValidationException
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.search.SearchHit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.text.SimpleDateFormat

import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * Created by zc on 2017/12/8.
 */
@Service
@Slf4j
class DashboardService {
    @Autowired
    QuerySessionRepo querySessionRepo
    @Autowired
    RubbishNewsService rubbishNewsService
    @Autowired
    SiteRepo siteRepo
    @Autowired
    RiseNewsRepo riseNewsRepo
    @Autowired
    ReprintMediaMonitorRepo reprintMediaMonitorRepo
    @Autowired
    SubjectRepo subjectRepo
    @Autowired
    WeiboInfoService weiboInfoService
    @Autowired
    DashboardRepo dashboardRepo
    @Autowired
    NewsRepo newsRepo

    List<Site> getDefaultHighlightSites() {
        List<Site> siteList = []

        siteList << new Site([
                "userId"       : 0,
                "siteName"     : "人民网",
                "websiteName"  : "人民网",
                "websiteDomain": ".people.com.cn",
                "siteType"     : 1,
                "domainReverse": "cn.com.people.",
        ])
        siteList << new Site([
                "userId"       : 0,
                "siteName"     : "新华网",
                "websiteName"  : "新华网",
                "websiteDomain": ".xinhuanet.com",
                "siteType"     : 1,
                "domainReverse": "com.xinhuanet.",
        ])
        siteList << new Site([
                "userId"       : 0,
                "siteName"     : "凤凰网",
                "websiteName"  : "凤凰网",
                "websiteDomain": ".ifeng.com",
                "siteType"     : 1,
                "domainReverse": "com.ifeng.",
        ])
        siteList << new Site([
                "userId"       : 0,
                "siteName"     : "新浪网",
                "websiteName"  : "新浪网",
                "websiteDomain": ".sina.com.cn",
                "siteType"     : 1,
                "domainReverse": "cn.com.sina.",
        ])
        siteList << new Site([
                "userId"       : 0,
                "siteName"     : "腾讯网",
                "websiteName"  : "腾讯网",
                "websiteDomain": ".qq.com",
                "siteType"     : 1,
                "domainReverse": "com.qq.",
        ])
        siteList << new Site([
                "userId"       : 0,
                "siteName"     : "网易",
                "websiteName"  : "网易",
                "websiteDomain": ".163.com",
                "siteType"     : 1,
                "domainReverse": "com.163.",
        ])
        siteList << new Site([
                "userId"       : 0,
                "siteName"     : "搜狐",
                "websiteName"  : "搜狐",
                "websiteDomain": ".sohu.com",
                "siteType"     : 1,
                "domainReverse": "com.sohu.",
        ])
    }

    Map getUserHighlightNews(Long userId, List sites, int pageSize, int pageNo, String queryId) {

        //首页巡查模块暂取7大站点数据
        sites = getDefaultHighlightSites()
        def newsList = []
        //解析数据
        String id = ""
        Date time = new Date()
        int offset = 0
        if (queryId) {
            try {
                List queryKeys = queryId.split(",")
                id = queryKeys.get(0)
                time = new Date(queryKeys.get(1) as long)
                offset = queryKeys.get(2) as int
                querySessionRepo.removeQuerySessionByTime(id, time)
            } catch (Exception e) {
                log.error("queryId格式不合法,解析失败", e)
                return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: 'queryId格式不合法', queryId: queryId])
            }
        } else {
            id = UUID.randomUUID().toString()
        }
        def newsTitleList = []

        def idMap = [:]
        while (true) {
            def idandPublishTime = dashboardRepo.getHighlightNewsIds(sites, pageSize, offset)//查询头条新闻
            def map = [:]
            idandPublishTime.each {
                if (!idMap.containsKey(it.id)) {
                    idMap.put(it.id, it.publishTime)
                }
                map.put(it.id, it.publishTime)
            }
            List newsIdList = map.keySet() as List//头条新闻的id
            List newsDetailList = dashboardRepo.getNewsListByIds(newsIdList)
            def validResult = getValidHighlightNews(newsIdList, newsDetailList, pageSize - newsList.size())
            offset += validResult.index
            newsList += validResult.newsList
            if (newsList.size() >= pageSize) {
                break;
            }
            if (newsIdList.size() < pageSize) {
                break;
            }
        }

        queryId = id + "," + new Date().getTime().toString() + "," + offset

        def newsIds = []
        newsList.each {
            newsIds.add(it.id)
        }
        List col = newsRepo.getExistNewsOperation(userId, newsIds, 3)
        List push = newsRepo.getExistNewsOperation(userId, newsIds, 1)

        if (newsList && newsList.size() > 0) {
            def list = []
            newsList ? newsList.each {
                def res = [
                        id             : it.id,
                        title          : it.title,
                        contentAbstract: it.contentAbstract,
                        reprintCount   : it.reprintCount,
//                        publishTime    : DateUitl.convertFormatDate(DateUitl.convertEsDate(String.valueOf(idMap.get(it.id))), "MM-dd HH:mm"),
                        publishTime    : DateUitl.convertFormatDate(DateUitl.convertEsDate(it.publishTime), "MM-dd HH:mm"),
                        siteName       : it.siteName,
                        isCollection   : col.contains(it.id),
                        isPush         : push.contains(it.id)
                ]
                list << res
            } : []
            return apiResult([status: HttpStatus.SC_OK, newsList: list, msg: '', queryId: queryId])
        }
    }

    Map getNewsAndImgNews(long userId, String siteIds, int siteType) {
        def imageNewsList = getHotNews(userId, siteIds, true, 3, siteType)
        def newsList = getHotNews(userId, siteIds, false, 8, siteType)
        newsList = newsList - imageNewsList
        def list = []
        def listPic = []
        //加收藏 推送标识
        def imageNewsIds = []
        def newsIds = []
        newsList.each {
            newsIds.add(it.id)
        }
        imageNewsList.each {
            imageNewsIds.add(it.id)
        }
        List col = newsRepo.getExistNewsOperation(userId, newsIds, 3)
        List push = newsRepo.getExistNewsOperation(userId, newsIds, 1)

        List colImg = newsRepo.getExistNewsOperation(userId, imageNewsIds, 3)
        List pushImg = newsRepo.getExistNewsOperation(userId, imageNewsIds, 1)

        newsList ? newsList.each {

            def res = [
                    id                  : it.id,
                    title               : it.title,
                    contentAbstract     : it.contentAbstract,
                    simhash             : it.simhash,
                    reprintCount        : it.reprintCount,
                    firstPublishTime    : siteType == 1 ? it.firstPublishTime : it.publishTime,//按发布时间显示
                    firstPublishSiteName: siteType == 1 ? it.firstPublishSiteName : it.siteName,
                    publishTime         : it.publishTime,
                    siteName            : it.siteName,
                    isCollection        : col.contains(it.id),
                    isPush              : push.contains(it.id)

            ]
            if (list.size() < 5) {
                list << res
            }
        } : []
        imageNewsList ? imageNewsList.each {

            def res = [
                    id                  : it.id,
                    title               : it.title,
                    contentAbstract     : it.contentAbstract,
                    simhash             : it.simhash,
                    reprintCount        : it.reprintCount,
                    firstPublishTime    : siteType == 1 ? it.firstPublishTime : it.publishTime,
                    firstPublishSiteName: siteType == 1 ? it.firstPublishSiteName : it.siteName,
                    imgUrls             : it.imgUrls,
                    cover               : it.cover,
                    publishTime         : it.publishTime,
                    siteName            : it.siteName,
                    isCollection        : colImg.contains(it.id),
                    isPush              : pushImg.contains(it.id)


            ]
            listPic << res
        } : []
        return [newsList: list, imageNewsList: listPic]
    }


    Map getWeiboNews(long userId, String siteIds, int siteType) {
        def list = []
        def weiboNewsList = getHotNews(userId, siteIds, true, 6, siteType)
        list = getWeiboNews(userId, weiboNewsList)
        return [newsList: list]
    }

    List getWeiboNews(long userId, List list) {
        def weiboNewsList = []
        Map urlMap = [:]
        Map siteNameMap = [:]
        list ? list.each {
            urlMap.(it.url) = ""
            siteNameMap.(it.siteName) = ""
        } : []
        for (int i = 0; i < urlMap.size(); i++) {
            urlMap.keySet()
        }
        List urlList = urlMap.keySet() as List
        for (int i = 0; i < urlList.size(); i++) {
            def url = urlList.get(i)
            urlMap.put(url, weiboInfoService.getVerifiedById(url))
        }
        List siteNameList = siteNameMap.keySet() as List
        for (int i = 0; i < siteNameList.size(); i++) {
            def siteName = siteNameList.get(i)
            siteNameMap.put(siteName, siteRepo.getUserSiteIdByName(userId, siteName))
        }
        //加收藏 推送标识
        def newsIds = []
        list.each {
            newsIds.add(it.id)
        }
        List col = newsRepo.getExistNewsOperation(userId, newsIds, 3)
        List push = newsRepo.getExistNewsOperation(userId, newsIds, 1)

        list ? list.each {
            def res = [
                    id             : it.id,
                    title          : it.title,
                    contentAbstract: it.contentAbstract,
                    simhash        : it.simhash,
                    reprintCount   : it.reprintCount,
                    publishTime    : it.publishTime,
                    siteName       : it.siteName,
                    imgUrls        : it.imgUrls,
                    posterAvatar   : it.posterAvatar,
                    url            : it.url,
                    vflag          : urlMap.get(it.url),
                    siteId         : siteNameMap.get(it.siteName),
                    isCollection   : col.contains(it.id),
                    isPush         : push.contains(it.id)
            ]
            weiboNewsList << res
        } : []
        return weiboNewsList
    }

    List getHotNews(long userId, String siteIds, boolean hasPic, int maxCount, int siteType) {
        List result = []
        List<Site> sites = []
        //拿到用户站点信息
        if (siteIds) {
            List siteIdList = siteIds.split(",")
            sites = siteRepo.getUserSitesByIds(userId, siteIdList)
            if (sites.size() <= 0 || sites.isEmpty()) {
                return result
            }
        }
        def endDate = new Date()
        def startDate = null
        def firstPublishStartDate = null
        if (siteType == Site.SITE_TYPE_WEBSITE) {//查询热点新闻时，首发时间在24小时内，发布时间在4小时内,若数据不够8条，则显示发布时间在24小时内最热新闻。
            startDate = new Date(endDate.getTime() - 4 * 3600 * 1000L)//发布时间
            firstPublishStartDate = new Date(endDate.getTime() - 24 * 3600 * 1000L)//首发时间
        }
        String sortOrder = "publishTime" //排序字段
        if (siteType == Site.SITE_TYPE_WEBSITE) {//热点新闻以热度降序排列
            sortOrder = "reprintCount"
        } else if (siteType == Site.SITE_TYPE_WECHAT) {//热门微信以发布时间降序排列
            sortOrder = "publishTime"
        } else if (siteType == Site.SITE_TYPE_WEIBO) {
            sortOrder = "publishTime"
        }
        result = getSitesHotNews(sites, hasPic, maxCount, sortOrder, startDate, endDate, firstPublishStartDate, endDate)
        if (siteType == Site.SITE_TYPE_WEBSITE) {
            if (result.size() < maxCount) {
                startDate = new Date(endDate.getTime() - 24 * 3600 * 1000L)
                result = getSitesHotNews(sites, hasPic, maxCount, sortOrder, startDate, endDate, firstPublishStartDate, endDate)
            }
            if (result.size() < maxCount) {
                result = getSitesHotNews(sites, hasPic, maxCount, sortOrder, null, endDate, null, endDate)
            }
        }
        return result
    }

    List getSitesHotNews(List<Site> sites, boolean hasPic, int maxCount, String sortOrder, Date startDate, Date endDate, Date firstPublishStartDate, Date firstPublishEndDate) {
        if (sites.isEmpty()) {
            return null
        }
        SearchResponse scrollResp = null
        def resultMap = [:]
        def simhashMap = [:]
        def result = []
        while (true) {
            if (!scrollResp) {
                scrollResp = siteRepo.getSitesHotNewsByTime(sites, hasPic, 50, sortOrder, startDate, endDate, firstPublishStartDate, firstPublishEndDate)
            } else {
                scrollResp = siteRepo.getScrollData(scrollResp, 10000)
            }
            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                def news = hit.getSource()
                def imgUrls = news.imgUrls
                if (resultMap.containsKey(StringUtils.removeSpecialCode(news.title)) || simhashMap.containsKey(news.simhash)) {
                    continue
                }
                if (hasPic && imgUrls.size() == 0) {
                    continue
                }
                if (news.newsType == 1 || news.newsType == 6) {
                    if ((!news.cover) && imgUrls) {//如果cover没有值，过滤掉imgUrls里不符合条件的图片，把符合条件的第一张图片给cover封面
                        for (int i = 0; i < imgUrls.size(); i++) {
                            if (UrlUtils.imgFilter(imgUrls[i])) {
                                news.cover = imgUrls[i]
                                break
                            }
                        }
                        if (!news.cover) {
                            continue
                        }
                    }
                }
                resultMap.put(StringUtils.removeSpecialCode(news.title), news)
                simhashMap.put(news.simhash, news)
                if (resultMap.size() >= maxCount) {
                    break
                }
            }
            if (resultMap.size() >= maxCount) {
                break
            }
        }
        resultMap.each { key, value ->
//            value.put("captureTime", DateUitl.convertEsDate(value.captureTime as String).getTime())
            result << value
        }
        return result
    }

    Map getValidNews(List newsList, int maxCount, String id, List newsTitleList, long userId) {
        Map result = ["newsList": [], "index": newsList.size()]
        def resultList = []
        for (int i = 0; i < newsList.size(); i++) {
            result.index = i + 1
            def news = newsList.get(i)
            //2 查看Simhash是否存在在已经返回的list中
            def isExist = querySessionRepo.isQuerySessionRecordExist(id, news.simhash)
            if (isExist) {
                continue
            }
            if (newsTitleList.contains(StringUtils.removeSpecialCode(news.title)) || isExist) {
                continue
            }
            //查看新闻是不是被用户标记过为垃圾数据
            if (rubbishNewsService.isRubbishNews(userId, news.id as String, news.simhash as String)) {
                continue
            }
            //3 如果没有存在，则添加到返回的newslist
            resultList << news
            newsTitleList << news.title
            //4 插入到querysession
            querySessionRepo.addQuerySession(id, news.simhash)
            if (resultList.size() >= maxCount) {
                break
            }
        }
        result.newsList = resultList
        return result
    }

    Map getValidHighlightNews(List ids, List newsDetailList, int maxCount) {
        def validResult = [:]
        if (newsDetailList.size() <= maxCount) {
            validResult.index = ids.size()
            validResult.newsList = newsDetailList
            return validResult
        }
        List newsList = []
        int index = 0
        Map newsDetailMap = [:]
        newsDetailList.each {
            newsDetailMap.put(it.id, it)
        }
        for (int i = 0; i < ids.size(); i++) {
            if (newsDetailMap.get(ids.get(i))) {
                newsList << newsDetailMap.get(ids.get(i))
            }
            index++
            if (newsList.size() >= maxCount) {
                break
            }
        }
        validResult.index = index
        validResult.newsList = newsList
        return validResult
    }

    def getRiseNews(Long userId,
                    def headLineSiteList, int hot, Date startTime, Date endTime, int orientation, boolean hasPic, int order, String keyWords, int pageSize, String queryId) {
        def newsList = []
        //解析数据
        String id = ""
        Date time = new Date()
        int offset = 0
        if (queryId) {
            try {
                List queryKeys = queryId.split(",")
                id = queryKeys.get(0)
                time = new Date(queryKeys.get(1) as long)
                offset = queryKeys.get(2) as int
                querySessionRepo.removeQuerySessionByTime(id, time)
            } catch (Exception e) {
                log.error("queryId格式不合法,解析失败", e)
                return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: 'queryId格式不合法', queryId: queryId])
            }
        } else {
            id = UUID.randomUUID().toString()
        }
        def newsTitleList = []
        while (true) {

            def currentNewsList = riseNewsRepo.getRiseNewsFromEs(headLineSiteList, hot, startTime, endTime, orientation, hasPic, order, keyWords, pageSize, offset)
            def validResult = getValidNews(currentNewsList, pageSize - newsList.size(), id, newsTitleList, userId)
            offset += validResult.index
            newsList += validResult.newsList
            if (newsList.size() >= pageSize) {
                break;
            }
            if (currentNewsList.size() < pageSize) {
                break;
            }
        }

        queryId = id + "," + new Date().getTime().toString() + "," + offset

        if (newsList && newsList.size() > 0) {
            newsList.each {
                it.firstPublishSiteName = it.firstPublishSiteName ?: it.siteName
            }
            return apiResult([status: HttpStatus.SC_OK, newsList: newsList, msg: '', queryId: queryId])
        } else {
            if ("".equals(keyWords)) {
                def sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
                String start = "1970-01-01 00:00:00"
                Date startDate = sdf.parse(start)
                newsList = riseNewsRepo.getRiseNewsFromEs(headLineSiteList, 0, startDate, new Date(), 0, false, 1, "", 1, 1)
                if (newsList && newsList.size() > 0) {
                    newsList.each {
                        it.firstPublishSiteName = it.firstPublishSiteName ?: it.siteName
                    }
                    return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: '未检测到热点新闻或所选站点新闻热度上升过慢，建议添加更多站点', queryId: queryId])
                } else {
                    String msg = headLineSiteList[0] ? (headLineSiteList[0].message ?: "未检测到热点新闻或所选站点新闻热度上升过慢，建议添加更多站点") : "未检测到热点新闻或所选站点新闻热度上升过慢，建议添加更多站点"
                    return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: msg, queryId: queryId])
                }
            } else {
                return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: '未检测到热点新闻或所选站点新闻热度上升过慢，建议添加更多站点', queryId: queryId])
            }
        }
    }

    def getReprintNews(Long userId, List<Site> sitesList, int hot, Date startTime, Date endTime, int orientation, boolean hasPic, int order, String keyWords, int pageSize, String queryId) {
        def newsList = []
        //解析数据
        String id = ""
        Date time = new Date()
        int offset = 0
        if (queryId) {
            try {
                List queryKeys = queryId.split(",")
                id = queryKeys.get(0)
                time = new Date(queryKeys.get(1) as long)
                offset = queryKeys.get(2) as int
                querySessionRepo.removeQuerySessionByTime(id, time)
            } catch (Exception e) {
                log.error("queryId格式不合法,解析失败", e)
                return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: 'queryId格式不合法', queryId: queryId])
            }
        } else {
            id = UUID.randomUUID().toString()
        }
        def newsTitleList = []
        while (true) {

            def currentNewsList = reprintMediaMonitorRepo.getReprintMediaMonitorFromEs(sitesList, hot, startTime, endTime, orientation, hasPic, order, keyWords, pageSize, offset)
            def validResult = getValidNews(currentNewsList, pageSize - newsList.size(), id, newsTitleList, userId)
            offset += validResult.index
            newsList += validResult.newsList
            if (newsList.size() >= pageSize) {
                break;
            }
            if (currentNewsList.size() < pageSize) {
                break;
            }
        }

        queryId = id + "," + new Date().getTime().toString() + "," + offset

        if (newsList && newsList.size() > 0) {
            newsList.each {
                it.firstPublishSiteName = it.firstPublishSiteName ?: it.siteName
            }
            return apiResult([status: HttpStatus.SC_OK, newsList: newsList, msg: '', queryId: queryId])
        } else {
            if ("".equals(keyWords)) {
                def sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
                String start = "1970-01-01 00:00:00"
                Date startDate = sdf.parse(start)
                newsList = reprintMediaMonitorRepo.getReprintMediaMonitorFromEs(sitesList, 0, startDate, new Date(), 0, false, 1, "", 1, 1)
                if (newsList && newsList.size() > 0) {
                    newsList.each {
                        it.firstPublishSiteName = it.firstPublishSiteName ?: it.siteName
                    }
                    return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: '未检测到热点新闻或所选站点新闻热度上升过慢，建议添加更多站点', queryId: queryId])
                } else {
                    String msg = sitesList[0] ? (sitesList[0].message ?: "未检测到热点新闻或所选站点新闻热度上升过慢，建议添加更多站点") : "未检测到热点新闻或所选站点新闻热度上升过慢，建议添加更多站点"
                    return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: msg, queryId: queryId])
                }
            } else {
                return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: '未检测到热点新闻或所选站点新闻热度上升过慢，建议添加更多站点', queryId: queryId])
            }
        }

    }

    BoolQueryBuilder getSubjectConditions(long userId, String subjectIds) {
        def subjectIdList = subjectIds.split(",")
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder()
        for (int i = 0; i < subjectIdList.size(); i++) {
            String subjectId = subjectIdList[i]
            Subject subject = subjectRepo.getUserSubjectById(userId, subjectId)
            if (subject) {
                boolQueryBuilder.should(subjectRepo.getSubjectQuery(subject))
            } else {
                continue
            }
        }
        return boolQueryBuilder
    }

    Map getSubjectNewsAndImgNews(long userId, String subjectIds) {
        BoolQueryBuilder boolQueryBuilder = getSubjectConditions(userId, subjectIds)
        def imageNewsList = getSubjectsNews(boolQueryBuilder, true, 3)
        def newsList = getSubjectsNews(boolQueryBuilder, false, 8)
        newsList = newsList - imageNewsList
        def list = []
        def listPic = []

        //加收藏 推送标识
        def imageNewsIds = []
        def newsIds = []
        newsList.each {
            newsIds.add(it.id)
        }
        imageNewsList.each {
            imageNewsIds.add(it.id)
        }
        List col = newsRepo.getExistNewsOperation(userId, newsIds, 3)
        List push = newsRepo.getExistNewsOperation(userId, newsIds, 1)

        List colImg = newsRepo.getExistNewsOperation(userId, imageNewsIds, 3)
        List pushImg = newsRepo.getExistNewsOperation(userId, imageNewsIds, 1)

        newsList ? newsList.each {
            def res = [
                    id             : it.id,
                    title          : it.title,
                    contentAbstract: it.contentAbstract,
                    simhash        : it.simhash,
                    reprintCount   : it.reprintCount,
                    publishTime    : DateUitl.convertEsDate(it.publishTime),
                    siteName       : it.siteName,
                    isCollection   : col.contains(it.id),
                    isPush         : push.contains(it.id)
            ]
            if (list.size() < 5) {
                list << res
            }
        } : []
        imageNewsList ? imageNewsList.each {
            def res = [
                    id             : it.id,
                    title          : it.title,
                    contentAbstract: it.contentAbstract,
                    simhash        : it.simhash,
                    reprintCount   : it.reprintCount,
                    publishTime    : DateUitl.convertEsDate(it.publishTime),
                    siteName       : it.siteName,
                    imgUrls        : it.imgUrls,
                    cover          : it.cover,
                    isCollection   : colImg.contains(it.id),
                    isPush         : pushImg.contains(it.id)
            ]
            listPic << res
        } : []
        return [newsList: list, imageNewsList: listPic]
    }

    Map getSubjectNewsAndImgNewsOneByOne(long userId, String subjectIds) {
        def subjectIdList = subjectIds.split(",")
        def list = []
        def listPic = []
        def imageNewsListAll = []
        def newsListAll = []
        for (int i = 0; i < subjectIdList.size(); i++) {
            def subjectId = subjectIdList[i]
            BoolQueryBuilder boolQueryBuilder = getSubjectConditions(userId, subjectId)
            def imageNewsList = getSubjectsNews(boolQueryBuilder, true, 3)
            def newsList = getSubjectsNews(boolQueryBuilder, false, 8)
            newsList = newsList - imageNewsList
            imageNewsListAll += imageNewsList
            newsListAll += newsList
        }
        imageNewsListAll = imageNewsListAll.unique()
        newsListAll = newsListAll.unique()
        imageNewsListAll = imageNewsListAll.sort { a, b ->
            (Long) b.sortTime - (Long) a.sortTime
        }
        newsListAll = newsListAll.sort { a, b ->
            (Long) b.sortTime - (Long) a.sortTime
        }

        //加收藏 推送标识
        def imageNewsIds = []
        def newsIds = []
        newsList.each {
            newsIds.add(it.id)
        }
        imageNewsList.each {
            imageNewsIds.add(it.id)
        }
        List col = newsRepo.getExistNewsOperation(userId, newsIds, 3)
        List push = newsRepo.getExistNewsOperation(userId, newsIds, 1)

        List colImg = newsRepo.getExistNewsOperation(userId, imageNewsIds, 3)
        List pushImg = newsRepo.getExistNewsOperation(userId, imageNewsIds, 1)
        newsListAll ? newsListAll.each {
            def res = [
                    id             : it.id,
                    title          : it.title,
                    contentAbstract: it.contentAbstract,
                    simhash        : it.simhash,
                    reprintCount   : it.reprintCount,
                    publishTime    : it.publishTime,
                    siteName       : it.siteName,

                    isCollection   : col.contains(it.id),
                    isPush         : push.contains(it.id)
            ]
            if (list.size() < 5) {
                list << res
            }
        } : []
        imageNewsListAll ? imageNewsListAll.each {
            def res = [
                    id             : it.id,
                    title          : it.title,
                    contentAbstract: it.contentAbstract,
                    simhash        : it.simhash,
                    reprintCount   : it.reprintCount,
                    publishTime    : it.publishTime,
                    siteName       : it.siteName,
                    imgUrls        : it.imgUrls,
                    cover          : it.cover,
                    isCollection   : colImg.contains(it.id),
                    isPush         : pushImg.contains(it.id)
            ]
            if (listPic.size() < 3) {
                listPic << res
            }
        } : []

        return [newsList: list, imageNewsList: listPic]
    }

    List getSubjectsNews(BoolQueryBuilder boolQueryBuilder, boolean hasPic, int maxCount) {
        SearchResponse scrollResp = null
        def resultMap = [:]
        def simhashMap = [:]
        def result = []
        while (true) {
            if (!scrollResp) {
                scrollResp = subjectRepo.getLatestSubjectsNews(boolQueryBuilder, hasPic, 50)
            } else {
                try {
                    scrollResp = subjectRepo.getScrollData(scrollResp, 10000)
                }catch (ActionRequestValidationException exception){
                    exception.printStackTrace();
                    break;
                }
            }
            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                def news = hit.getSource()
                def imgUrls = news.imgUrls
                if (resultMap.containsKey(StringUtils.removeSpecialCode(news.title)) || simhashMap.containsKey(news.simhash)) {
                    continue
                }
                if (hasPic && imgUrls.size() == 0) {
                    continue
                }
                if ((!news.cover) && imgUrls) {//如果cover没有值，过滤掉imgUrls里不符合条件的图片，把符合条件的第一张图片给cover封面
                    for (int i = 0; i < imgUrls.size(); i++) {
                        if (UrlUtils.imgFilter(imgUrls[i])) {
                            news.cover = imgUrls[i]
                            break
                        }
                    }
                    if (!news.cover) {
                        continue
                    }
                }
                news.imgUrls = []
                resultMap.put(StringUtils.removeSpecialCode(news.title), news)
                simhashMap.put(news.simhash, news)
                if (resultMap.size() >= maxCount) {
                    break
                }
            }
            if (resultMap.size() >= maxCount) {
                break
            }
            if (scrollResp.getHits().hits.size() < 50) {
                break
            }
        }
        resultMap.each { key, value ->
            value.put("sortTime", DateUitl.convertEsDate(value.publishTime as String).getTime())
            result << value
        }
        return result
    }

    //传播监控新接口（使用）
    Map getUserRiseNews(Long userId, List sites, int pageSize, int pageNo, String queryId) {
        def newsList = []
        //解析数据
        String id = ""
        Date time = new Date()
        int offset = 0
        if (queryId) {
            try {
                List queryKeys = queryId.split(",")
                id = queryKeys.get(0)
                time = new Date(queryKeys.get(1) as long)
                offset = queryKeys.get(2) as int
                querySessionRepo.removeQuerySessionByTime(id, time)
            } catch (Exception e) {
                log.error("queryId格式不合法,解析失败", e)
                return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: 'queryId格式不合法', queryId: queryId])
            }
        } else {
            id = UUID.randomUUID().toString()
        }
        def newsTitleList = []
        def newsIdList = []
        def newsDetailList = []
        while (true) {
            def ids = []
            newsIdList = dashboardRepo.getRiseNewsList(sites, pageSize, offset)//查询头条新闻的id
            for (int i = 0; i < newsIdList.size(); i++) {
                Object o = newsIdList.get(i);
                ids.add(o.id)
            }
            if (!ids) {
                return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: '暂无数据', queryId: queryId])
            }
            newsDetailList = dashboardRepo.getNewsListByIds(ids)
            def validResult = getValidHighlightNews(newsIdList, newsDetailList, pageSize - newsList.size())
            offset += validResult.index
            newsList += validResult.newsList
            if (newsList.size() >= pageSize) {
                break;
            }
            if (newsIdList.size() < pageSize) {
                break;
            }
        }
        queryId = id + "," + new Date().getTime().toString() + "," + offset
        if (newsList && newsList.size() > 0) {
            def list = []
            newsIdList.each { elem ->
                newsDetailList ? newsDetailList.each { it ->
                    if (elem.id == it.id) {
                        def res = [
                                id                  : elem.id,
                                title               : it.title,
                                contentAbstract     : it.contentAbstract,
                                simhash             : it.simhash,
                                reprintCount        : it.reprintCount,
                                firstPublishTime    : DateUitl.convertFormatDate(DateUitl.convertEsDate(it.firstPublishTime), "MM-dd HH:mm"),
                                firstPublishSiteName: it.firstPublishSiteName,
                                siteName            : it.siteName,
                                publishTime         : DateUitl.convertEsDate(it.publishTime),
                                riseRate            : Math.round(elem.riseRate * 100) + "%"
                        ]
                        list << res
                    }
                } : []
            }
            return apiResult([status: HttpStatus.SC_OK, newsList: list, msg: '', queryId: queryId])
        }
    }

    public Map getPpcNews() {
        String ppcNewsStr = RedisClientUtil.get("luda_test:bjzx")
        if (!ppcNewsStr) {
            return null
        }
        JSONArray ppcNewsList = JSON.parseArray(RedisClientUtil.get("luda_test:bjzx"));
        def picNews = []
        def titleNews = []
        for (int i = 0; i < ppcNewsList.size(); i++) {
            JSONObject curNews = ppcNewsList.get(i)
            if ("big_pic".equals(curNews.get("type"))) {
                picNews.add([title : curNews.get("title"),
                             url   : curNews.get("url"),
                             picUrl: curNews.get("pic_url"),
                             order : curNews.get("num"),
                ])
            }
            if ("list".equals(curNews.get("type"))) {
                titleNews.add([title      : curNews.get("title"),
                               url        : curNews.get("url"),
                               picUrl     : curNews.get("pic_url"),
                               order      : curNews.get("num"),
                               publishTime: curNews.get("ctime"),
                ])
            }
        }
        return [picNews: picNews, titleNews: titleNews]

    }
}
