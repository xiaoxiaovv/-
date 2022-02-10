package com.istar.mediabroken.service.capture

import com.istar.mediabroken.entity.capture.InstantNewsMarked
import com.istar.mediabroken.entity.capture.InstantSite
import com.istar.mediabroken.repo.capture.InstantSiteRepo
import com.istar.mediabroken.repo.capture.NewsRepo
import com.istar.mediabroken.repo.capture.QuerySessionRepo
import com.istar.mediabroken.repo.capture.SiteRepo
import com.istar.mediabroken.service.rubbish.RubbishNewsService
import com.istar.mediabroken.utils.*
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * Author: zc
 * Time: 2018/5/9
 */
@Service
@Slf4j
class InstantNewsService {
    @Autowired
    NewsRepo newsRepo
    @Autowired
    SiteRepo siteRepo
    @Autowired
    InstantSiteRepo instantSiteRepo
    @Autowired
    QuerySessionRepo querySessionRepo
    @Autowired
    RubbishNewsService rubbishNewsService

    long getInstantNewsCount(long userId, Date startTime, Date endTime) {
        List<InstantSite> instantSites = new ArrayList<InstantSite>()
        instantSites = getUserInstantSites(userId)
        if (instantSites.size() == 0){
            return 0L
        }
        def result = instantSiteRepo.getInstantNewsCount(instantSites, startTime, endTime)
        return result
    }

    List<InstantSite> getUserInstantSites(long userId) {
        List<InstantSite> instantSites = new ArrayList<InstantSite>()
        instantSites = instantSiteRepo.getUserInstantSites(userId)
        if (instantSites.size() == 0){
            instantSites = instantSiteRepo.getUserInstantSites(0L)
        }
        return instantSites
    }

    Map getInstantNews(long userId, Date startTime, Date endTime, int pageSize, String queryId, String queryName) {
        def newsList = []
        List<InstantSite> instantSites = new ArrayList<InstantSite>()
        instantSites = getUserInstantSites(userId)
        if (instantSites.size() == 0){
            return [list: newsList, queryId: queryId]
        }
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
            List currentNewsList = instantSiteRepo.getInstantNewsFromEs(instantSites, startTime, endTime, queryName, pageSize, offset)
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

        newsList.each {
            it.captureTime = DateUitl.convertEsDate(it.captureTime).getTime()
            it.isMarked = instantSiteRepo.isNewsMarked(userId, it.id)
        }
        return [list: newsList, queryId: queryId]
    }

    Map getValidNews(List newsList, int maxCount, String id, List newsTitleList, long userId) {
        Map result = ["newsList": [], "index": newsList.size()]
        def resultList = []
        for (int i = 0; i < newsList.size(); i++) {
            result.index = i + 1
            def news = newsList.get(i)
            //2 查看Simhash是否存在在已经返回的list中
            def isExist = querySessionRepo.isNewsQuerySessionExist(id, news.id)
            if (isExist) {
                continue
            }
            if (newsTitleList.contains(news.title)) {
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
            querySessionRepo.addNewsQuerySession(id, news.id)
            if (resultList.size() >= maxCount) {
                break
            }
        }
        result.newsList = resultList
        return result
    }

    def getInstantMarkedNews(long userId, String queryName, int pageSize, int pageNo) {
        def list = []
        List<InstantNewsMarked> instantNewsMarkList = new ArrayList<InstantNewsMarked>();
        instantNewsMarkList = instantSiteRepo.getInstantMarkedNews(userId, queryName, pageSize, pageNo);
        for (int i = 0; i < instantNewsMarkList.size(); i++) {
            InstantNewsMarked instantNewsMarked = instantNewsMarkList.get(i)
            Map map = instantNewsMarked.toMap()
            map.id = map.newsId
            map.isMarked = true
            list << map
        }
        return list
    }

    String addInstantNewsMarked(long userId, String newsId) {
        Map map = newsRepo.getNewsById(newsId)
        boolean isNewsMarked = instantSiteRepo.isNewsMarked(userId, map.id)
        if (!isNewsMarked) {
            InstantNewsMarked instantNewsMarked = new InstantNewsMarked()
            instantNewsMarked.id = UUID.randomUUID().toString()
            instantNewsMarked.userId = userId
            instantNewsMarked.newsId = map.id
            instantNewsMarked.url = map.url
            instantNewsMarked.newsType = map.newsType
            instantNewsMarked.siteName = map.siteName
            instantNewsMarked.title = map.title
            instantNewsMarked.contentAbstract = map.contentAbstract
            instantNewsMarked.content = map.content
            instantNewsMarked.imgUrls = map.imgUrls
            instantNewsMarked.hasImg = map.hasImg
            instantNewsMarked.captureTime = DateUitl.convertEsDate(map.captureTime)
            instantNewsMarked.updateTime = new Date()
            instantNewsMarked.createTime = new Date()
            String instantNewsMarkId = ""
            try {
                instantNewsMarkId = instantSiteRepo.addInstantNewsMarked(instantNewsMarked)
            } catch (Exception e) {
                instantNewsMarkId = ""
            }
            return instantNewsMarkId
        } else {
            return ""
        }

    }

    void removeInstantNewsMarked(long userId, String newsId) {
        instantSiteRepo.removeInstantNewsMarked(userId, newsId)
    }

    /**
     * 删除标记新闻任务
     * 每天零点  删除二十四小时之前的标记的新闻
     */
    void deleteInstantNewsOver24Hours() {
        Date overDate = DateUitl.addHour(new Date(), -24)
        def flag = true
        while (flag) {
            long deleteCount = instantSiteRepo.removeMarkedNews(overDate)
            if (deleteCount == 0) {
                flag = false
            } else {
                log.info(new Date().toString() + "删除instantNewsMark表中数据" + deleteCount + "条!")
            }
        }
    }

    void addInstantSites() {
        long userId = 1496822110851L
        List instantSites = [
                [
                        "_id"          : UUID.randomUUID().toString(),
                        "userId"       : userId,
                        "siteName"     : "腾讯财经滚动新闻",
                        "websiteName"  : "腾讯财经滚动新闻",
                        "websiteDomain": "http://finance.qq.com/articleList/rolls/",
                        "urlType"      : 2,
                        "siteType"     : 1,
                        "isAutoPush"   : false,
                        "domainReverse": "com.qq.finance",
                        "pinYinPrefix" : "TENG1",
                        "updateTime"   : new Date(),
                        "createTime"   : new Date()
                ],
                [
                        "_id"          : UUID.randomUUID().toString(),
                        "userId"       : userId,
                        "siteName"     : "腾讯科技-滚动要闻",
                        "websiteName"  : "腾讯科技-滚动要闻",
                        "websiteDomain": "http://tech.qq.com/articleList/rolls/",
                        "urlType"      : 2,
                        "siteType"     : 1,
                        "isAutoPush"   : false,
                        "domainReverse": "com.qq.tech",
                        "pinYinPrefix" : "TENG1",
                        "updateTime"   : new Date(),
                        "createTime"   : new Date()
                ],
                [
                        "_id"          : UUID.randomUUID().toString(),
                        "userId"       : userId,
                        "siteName"     : "上证快讯-频道",
                        "websiteName"  : "上证快讯-频道",
                        "websiteDomain": "http://news.cnstock.com/bwsd",
                        "urlType"      : 2,
                        "siteType"     : 1,
                        "isAutoPush"   : false,
                        "domainReverse": "com.cnstock.news",
                        "pinYinPrefix" : "TENG1",
                        "updateTime"   : new Date(),
                        "createTime"   : new Date()
                ]
        ]
        instantSiteRepo.addInstantSites(userId, instantSites)
    }

    void addInstantSite() {
        long userId = 1496217216341L
        InstantSite instantSite = [
                "siteId"       : UUID.randomUUID().toString(),
                "userId"       : userId,
                "siteName"     : "腾讯财经滚动新闻",
                "websiteName"  : "腾讯财经滚动新闻",
                "websiteDomain": "http://finance.qq.com/articleList/rolls/",
                "urlType"      : 2,
                "siteType"     : 1,
                "isAutoPush"   : false,
                "domainReverse": "com.qq.finance",
                "pinYinPrefix" : "TENG1",
                "updateTime"   : new Date(),
                "createTime"   : new Date()
        ]
        def site = instantSiteRepo.getUserInstantSite(userId, instantSite.siteType, instantSite.siteName, instantSite.websiteDomain)
        if (!site) {
            instantSiteRepo.addInstantSite(instantSite)
        }
    }
}
