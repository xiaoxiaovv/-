package com.istar.mediabroken.api

import com.istar.mediabroken.service.CaptureService
import com.istar.mediabroken.utils.Paging
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

import java.text.SimpleDateFormat

import static com.istar.mediabroken.api.ApiResult.apiResult
import static com.istar.mediabroken.utils.DateUitl.addDay
import static com.istar.mediabroken.utils.DateUitl.getDayBegin
import static org.springframework.web.bind.annotation.RequestMethod.GET

/**
 * 传播分析
 */
@Controller
@Slf4j
public class SpreadApiController {
    @Autowired
    CaptureService captureSrv

    /**
     *
     * 传播概览
     *
     * 请求:
     * sid: "会话id"
     * newsId: "新闻ID"
     * days: int    // 天数  30, 60, 90
     *
     * 返回:
     * <pre>
     * {
     *    status: int           // 状态,200为成功
     *    reprintedMediaCount: int,                // 转载媒体数
     *    reprintedCount: int,                   // 转载总数
     *    collectedCountBySearchEngines int,     // 搜索引擎收录
     *    publishedCountByKeyChannel int,        // 重点频道刊载数
     *    forwardedCountByWeibo int,             // 微博转发数
     *    readingCountFromWeMedia int               // 自媒体阅读数
     *    commentsCount,                         // 评论总数量
     *    likesCount,                            // 点赞数
     * }
     * </pre>
     */
	@RequestMapping(value = "/propagationSummary")
	@ResponseBody
	public Map propagationSummary(String sid, String newsId, int days) {
        def random = new Random()
        def rep = [:]
        rep.status = 200
        // todo 把average换成指数
        rep.putAll([
                reprintMediaCount: random.nextInt((500 * 1.1f) as int),
                readingMediaAverage: 500,
                reprintedCount: random.nextInt((1000 * 1.1f) as int),
                reprintedAverage: 1000,
                collectedCountBySearchEngines: random.nextInt((1000 * 1.1f) as int),
                collectedAverageBySearchEngines: 1000,
                publishedCountByKeyChannel: random.nextInt((500 * 1.1f) as int),
                publishedAverageByKeyChannel: 500,
                forwardedCountByWeibo: random.nextInt((100000 * 1.1f) as int),
                forwardedAverageByWeibo: 100000,
                readingCountFromWeMedia: random.nextInt((100000 * 1.1f) as int),
                readingAverageFromWeMedia: 100000,
                commentsCount: random.nextInt((10000 * 1.1f) as int),
                commentsAverage: 10000,
                likesCount: random.nextInt((50000 * 1.1f) as int),
                likesAverage: 50000,
        ])

		return rep;
	}

    /**
     *
     * 传播概览
     *
     * 请求:
     * sid: "会话id"
     * newsId: "新闻ID"
     * days: int    // 天数  30, 60, 90
     *
     * 返回:
     * <pre>
     * {
     *    status: int           // 状态,200为成功
     *    reprintedMediaCount: int,                // 转载媒体数
     *    reprintedCount: int,                   // 转载总数
     *    collectedCountBySearchEngines int,     // 搜索引擎收录
     *    publishedCountByKeyChannel int,        // 重点频道刊载数
     *    forwardedCountByWeibo int,             // 微博转发数
     *    readingCountFromWeMedia int               // 自媒体阅读数
     *    commentsCount,                         // 评论总数量
     *    likesCount,                            // 点赞数
     * }
     * </pre>
     */
    @RequestMapping(value = "/api/spread/propagationSummary/{newsId:.+}")
    @ResponseBody
    public Map getPropagationSummary(
            @CurrentUserId long userId,
            @PathVariable("newsId") String newsId,
            @RequestParam("days") int days) {
        def result = captureSrv.getNewsStats(newsId, days)
        def spreadSummary = result.spreadSummary
        def rep = [:]
        // todo 把average换成指数
        rep.putAll([
                reprintMediaCount: getValue(spreadSummary?.reprintMediaCount, 0),
                readingMediaAverage: 500,
                reprintedCount: getValue(spreadSummary?.reprintedCount, 0),
                reprintedAverage: 1000,
                collectedCountBySearchEngines: 0,
                collectedAverageBySearchEngines: 1000,
                publishedCountByKeyChannel: 0,
                publishedAverageByKeyChannel: 500,
                forwardedCountByWeibo: getValue(spreadSummary?.forwardedAverageByWeibo, 0),
                forwardedAverageByWeibo: 100000,
                readingCountFromWeMedia: 0,
                readingAverageFromWeMedia: 100000,
                commentsCount: 0,
                commentsAverage: 10000,
                likesCount: 0,
                likesAverage: 50000,
        ])

        return apiResult(rep)
    }

    def static getValue(Object value, def defaultValue) {
        return value != null ? value : defaultValue
    }
/**
     *
     * 热点趋势
     *
     * 请求:
     * sid: "会话id"
     * newsId: "新闻ID"
     *
     *  返回:
     * <pre>
     * {
     *    status: int           // 状态,200为成功
     *    list: [{
     *      reprintedCountByMedia int,    // 媒体转载数
     *      forwardedCountByWeibo int,    // 微博转发数
     *      time long                // 时间
     *    }, ...]
     * }
     * </pre>
     */
    @RequestMapping(value = "/heatTrend")
    @ResponseBody
    public Map heatTrend(String sid, String newsId, int days) {
        def random = new Random()
        def ONE_DAY = 24 * 60 * 60 * 1000
        def firstTime = System.currentTimeMillis() - ONE_DAY * days
        def rep = [:]

        rep.status = 200
        rep.list = []
        for (int i = 0; i < days; i++) {
            rep.list << [
                    reprintedCountByMedia: random.nextInt(100),
                    forwardedCountByWeibo: random.nextInt(100),
                    time: firstTime + ONE_DAY * i,
            ]
        }

        return rep;
    }

    /**
     *
     * 热点趋势
     *
     * 请求:
     * sid: "会话id"
     * newsId: "新闻ID"
     *
     *  返回:
     * <pre>
     * {
     *    status: int           // 状态,200为成功
     *    list: [{
     *      reprintedCountByMedia int,    // 媒体转载数
     *      forwardedCountByWeibo int,    // 微博转发数
     *      time long                // 时间
     *    }, ...]
     * }
     * </pre>
     */
    @RequestMapping(value = "/api/spread/heatTrend/{newsId:.+}")
    @ResponseBody
    public Map heatTrend(
            @CurrentUserId long userId,
            @PathVariable("newsId") String newsId,
            @RequestParam("days") int days) {
        def result = captureSrv.getNewsStats(newsId, days)
        def sdf = new SimpleDateFormat('yyyy-MM-dd')
        def ONE_DAY = 24 * 60 * 60 * 1000
        def startTime = addDay(getDayBegin(), - days)//获取days获取新闻数组的开始时间
        def rep = [:]

        rep.list = []
        def group = result.heatTrend
        for (int i = 0; i < days+1; i++) {
            def idate = addDay(startTime, i)
            def time = idate.getTime()
            def date = sdf.format(new Date(time))

            def row = group.get(date)
            if (row) {
                rep.list << [
                        reprintedCountByMedia: row.reprintedCountByMedia,
                        forwardedCountByWeibo: row.forwardedCountByWeibo,
                        time: time
                ]

            } else {
                rep.list << [
                        reprintedCountByMedia: 0,
                        forwardedCountByWeibo: 0,
                        time: time
                ]
            }

        }

        return apiResult(rep);
    }

    /**
     *
     * 转发详情
     *
     * 请求:
     * sid: "会话id"
     * newsId: "新闻ID"
     * pageNo: int   // 页号
     * limit: int
     *
     * 返回:
     * <pre>
     * {
     *    status: int           // 状态,200为成功
     *    list: [{
     *      title: "文章标题"
     *      mediaName: "转载媒体"
     *      mediaType: "媒体类别",
     *      reprintTime: long  // 转载时间
     *      readingCount: int  //阅读数
     *      commentsCount: int //评论数
     *      url: "原文链接"
     *    }, ...]
     * </pre>
     */
    @RequestMapping(value = "/reprintDetails")
    @ResponseBody
    public Map reprintDetails(String sid, String newsId, int pageNo, int limit) {
        def random = new Random()
        def rep = [:]
        rep.status = 200
        rep.total = 3001
        rep.totalPage = Math.round(rep.total / limit)
        rep.list = []
        def offset = pageNo * limit
        for (int i = 0; i < limit; i++) {
            rep.list << [
                    title: "文章标题${offset + i}".toString(),
                    mediaName: "转载媒体${offset + i}".toString(),
                    mediaType: "媒体类别${offset + i}".toString(),
                    reprintTime: System.currentTimeMillis(),
                    readingCount: random.nextInt(100),
                    commentsCount: random.nextInt(100),
                    url: "http://www.baidu.com",
            ]
        }

        return rep;
    }

    /**
     *
     * 转发详情
     *
     * 请求:
     * sid: "会话id"
     * newsId: "新闻ID"
     * pageNo: int   // 页号
     * limit: int
     *
     * 返回:
     * <pre>
     * {
     *    status: int           // 状态,200为成功
     *    list: [{
     *      title: "文章标题"
     *      mediaName: "转载媒体"
     *      mediaType: "媒体类别",
     *      reprintTime: long  // 转载时间
     *      readingCount: int  //阅读数
     *      commentsCount: int //评论数
     *      url: "原文链接"
     *    }, ...]
     * </pre>
     */
    @RequestMapping(value = "/api/spread/reprintDetails/{newsId:.+}")
    @ResponseBody
    public Map getReprintDetails(
            @CurrentUserId long userId,
            @PathVariable("newsId") String newsId,
            @RequestParam("pageNo") int pageNo,
            @RequestParam("limit") int limit) {
        def newsList = captureSrv.getRelatedNews(newsId)
        def paging = new Paging(pageNo, limit, newsList.size())
        def offset = paging.offset > newsList.size() ? newsList.size() : paging.offset
        def offset2 = (paging.offset + limit) > newsList.size() ? newsList.size() : paging.offset + limit
        if (newsList) {
            paging.list = newsList.subList(offset, offset2)
        }

        def rep = [:]
        rep.total = paging.total
        rep.totalPage = paging.totalPage
        rep.list = []
        paging.list.each {
            rep.list << [
                    newsId: it.newsId,
                    title: it.title,
                    mediaName: it.source,
                    mediaType: getMediaType(it.source),
                    reprintTime: newsId.startsWith("solr:") ?  it.createTime : it.time,
                    readingCount: 0,
                    commentsCount: 0,
            ]
        }

        return apiResult(rep);
    }

    String getMediaType(String s) {
        if (s.indexOf('微博') > 0) {
            return '微博'
        } else if (s.indexOf('微信') > 0) {
            return '微信'
        } else {
            return '网站'
        }
    }
/**
     *
     * 传播路径
     *
     * 请求:
     * sid: "会话id"
     *
     * 返回:
     * <pre>
     * {
     *    status: int           // 状态,200为成功
     *    list: [{
     *      reprintedCountByMedia int,    // 媒体转载数
     *      forwardedCountByWeibo int,    // 微博转发数
     *      time long                // 时间
     *    }, ...]
     * }
     * </pre>
     */
    @RequestMapping(value = "/api/spread/spreadPath/{newsId:.+}", method = GET)
    @ResponseBody
    public Map getSpreadPath(@CurrentUserId long userId,
                             @PathVariable("newsId") String newsId) {
        def sites = captureSrv.getRelatedNews(newsId)
        def spreadPath = []
        def sitesMap = [:]
        def sdf = new SimpleDateFormat("yyyyMMddHH")
        sites.each {
            def time = sdf.parse(sdf.format(newsId.startsWith("solr:") ? it.createTime.getTime() : (new Date(it.time)).getTime()))
            def siteList = sitesMap.get(time)
            if (!siteList) {
                siteList = [time: time, sites: []]
                spreadPath << siteList
                sitesMap.put(time, siteList)
            }
            siteList.sites << [name: it.source]
        }

        return ApiResult.apiResult(list: spreadPath)
    }
}
