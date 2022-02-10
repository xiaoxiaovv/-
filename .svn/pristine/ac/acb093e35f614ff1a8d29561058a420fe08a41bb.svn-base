package com.istar.mediabroken.api

import com.alibaba.fastjson.JSONArray
import com.istar.mediabroken.Const
import com.istar.mediabroken.entity.CopyrightMonitor
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.service.CompetitionAnalysService
import com.istar.mediabroken.utils.DownloadUtils
import groovy.util.logging.Slf4j
import org.apache.commons.httpclient.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse
import java.text.SimpleDateFormat

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR
import static org.springframework.web.bind.annotation.RequestMethod.*


// 竞争分析
@RequestMapping(value = "/api/competitionAnalys")
@RestController
@Slf4j
public class CompetitionAnalysApiController {

    @Autowired
    CompetitionAnalysService competitionAnalysSrv

    /*
     * 添加版权监控设置
     */
    @RequestMapping(value = "/copyrightMonitor", method = POST)
    public Map addCopyrightMonitor(
            @CurrentUser LoginUser user,
            @RequestParam("title") String title,      // 标题
            @RequestParam("url") String url,
            @RequestParam(value = "author", required = false, defaultValue = '') String author,    // 作者
            @RequestParam(value = "date", required = false, defaultValue = '') String date,        // 发布日期
            @RequestParam(value = "media", required = false, defaultValue = '') String media,      // 媒体
            @RequestParam(value = "whiteList", required = false, defaultValue = '') String whiteList,      // 白名单,用逗号分隔
            @RequestParam(value = "blackList", required = false, defaultValue = '') String blackList,      // 黑白单,用逗号分隔
            @RequestParam(value = "contentAbstract", required = false, defaultValue = "") String contentAbstract
    ) {

        def count = competitionAnalysSrv.getCopyrightMonitorCount(user.userId)

        if (count > 5) {
            return apiResult(HttpStatus.SC_BAD_REQUEST, '只能创建5个版权监控项目');
        }

        def sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        if (!date) {
            date = sdf.format(new Date())
        }
        def currDate = null;
        try{
            currDate = sdf.parse(date)
        }catch(Exception e){
            return apiResult(SC_INTERNAL_SERVER_ERROR, "时间格式不正确")
        }
        List<String> wList= new ArrayList<String>();
        JSONArray whiteLists = JSONArray.parseArray(whiteList);
        whiteLists.each {
            wList.add(it)
        }
        List<String> bList= new ArrayList<String>();
        JSONArray blackLists = JSONArray.parseArray(blackList);
        blackLists.each {
            bList.add(it)
        }
        def monitorId = competitionAnalysSrv.addCopyrightMonitor(new CopyrightMonitor(
                userId: user.userId,
                title: title,
                url: url,
                author: author,
                date: currDate,
                media: media,
                contentAbstract: contentAbstract,
                whiteList: wList,
                blackList: bList
        ))

        return apiResult([monitorId: monitorId]);
    }

    /*
     * 修改版权监控设置
     */
    @RequestMapping(value = "/copyrightMonitor/{monitorId:.*}", method = PUT)
    public Map modifyCopyrightMonitor(
            @CurrentUser LoginUser user,
            @PathVariable("monitorId") String monitorId,
            @RequestParam("title") String title,
            @RequestParam("url") String url,
            @RequestParam(value = "author", required = false, defaultValue = '') String author,    // 作者
            @RequestParam(value = "date", required = false, defaultValue = '') String date,        // 发布日期
            @RequestParam(value = "media", required = false, defaultValue = '') String media,      // 媒体
            @RequestParam(value = "whiteList", required = false, defaultValue = '') String whiteList,      // 白名单,用逗号分隔
            @RequestParam(value = "blackList", required = false, defaultValue = '') String blackList,      // 黑白单,用逗号分隔
            @RequestParam(value = "contentAbstract", required = false, defaultValue = "") String contentAbstract
    ) {
        def sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        if (!date) {
            date = sdf.format(new Date())
        }
        def currDate = null;
        try{
            currDate = sdf.parse(date)
        }catch(Exception e){
            return apiResult(SC_INTERNAL_SERVER_ERROR, "时间格式不正确")
        }
        List<String> wList= new ArrayList<String>();
        JSONArray whiteLists = JSONArray.parseArray(whiteList);
        whiteLists.each {
            wList.add(it)
        }
        List<String> bList= new ArrayList<String>();
        JSONArray blackLists = JSONArray.parseArray(blackList);
        blackLists.each {
            bList.add(it)
        }
        competitionAnalysSrv.modifyCopyrightMonitor(new CopyrightMonitor(
                monitorId: monitorId,
                userId: user.userId,
                title: title,
                url: url,
                author: author,
                date: currDate,
                media: media,
                contentAbstract: contentAbstract,
                whiteList: wList,
                blackList: bList
        ))

        return apiResult();
    }

    /*
     * 删除版权监控设置
     */
    @RequestMapping(value = "/copyrightMonitor/{monitorId:.*}", method = DELETE)
    public Map removeCopyrightMonitor(
            @CurrentUser LoginUser user,
            @PathVariable("monitorId") String monitorId
    ) {
        competitionAnalysSrv.removeCopyrightMonitor(user.userId, monitorId)

        return apiResult();
    }

    /*
    * 查询版权监控详情
    */
    @RequestMapping(value = "/copyrightMonitor/{monitorId:.*}")
    public Map getCopyrightMonitor(
            @CurrentUser LoginUser user,
            @PathVariable("monitorId") String monitorId
    ) {
        def it = competitionAnalysSrv.getCopyrightMonitor(user.userId, monitorId)
        def tortCountMap = competitionAnalysSrv.getNewsTortCounts(user.userId, [monitorId])
        def rep =  [
                monitorId: it.monitorId,
                userId: it.userId,
                title: it.title,
                url: it.url,
                author: it.author,
                date: it.date,
                media: it.media,
                contentAbstract: it.contentAbstract,
                reprintCount: competitionAnalysSrv.getReprintCount(it),
                tortCount: tortCountMap.get(it.monitorId) ?: 0,
                blackList: it.blackList,
                whiteList: it.whiteList,
        ]
        return apiResult(rep);
    }

    /*
    * 查询版权监控详情新闻列表
    */
    @RequestMapping(value = "/copyrightMonitor/{monitorId:.*}/news")
    public Map getCopyrightMonitorNews(
            @CurrentUserId long userId,
            @PathVariable("monitorId") String monitorId,
            @RequestParam("pageNo") int pageNo,
            @RequestParam("limit") int limit
    ) {
        def paging = competitionAnalysSrv.getPagingCopyrightMonitorNews(userId, monitorId, pageNo, limit)

        def rep = [:]
        rep.total = paging.total
        rep.totalPage = paging.totalPage
        rep.list = []
        paging.list.each {
            rep.list << [
                    title       : it.title,
                    source      : it.source,
                    author      : it.author,
                    keyword     : it.keyword,
                    url         : it.url,
                    newsId      : it.newsId,
                    content     : it.content,
                    contentAbstract : it.contentAbstract,
                    site        : it.site,
                    createTime  : it.createTime.getTime(),
                    isTort      : it.isTort,       // 为true, 表示疑似侵权
                    isBlack     :it.isBlack,
                    isWhite     :it.isWhite
            ]
        }

        return apiResult(rep);
    }

    /*
    * 查询版权监控详情新闻列表-疑似侵权列表
    */
    @RequestMapping(value = "/copyrightMonitorSuspected/{monitorId:.*}/news", method = GET)
    public Map getCopyrightMonitorNewsSuspected(
            @CurrentUserId long userId,
            @PathVariable("monitorId") String monitorId,
            @RequestParam("pageNo") int pageNo,
            @RequestParam("limit") int limit
    ) {
        def paging = competitionAnalysSrv.getPagingCopyrightMonitorNews(userId, monitorId, pageNo, limit)

        def rep = [:]
        rep.total = paging.total
        rep.totalPage = paging.totalPage
        rep.list = []
        paging.list.each {
            if (it.isTort&&!it.isWhite){
                rep.list << [
                        title       : it.title,
                        source      : it.source,
                        author      : it.author,
                        keyword     : it.keyword,
                        url         : it.url,
                        newsId      : it.newsId,
                        content     : it.content,
                        contentAbstract : it.contentAbstract,
                        site        : it.site,
                        createTime  : it.createTime.getTime(),
                        isTort: true,       // 为true, 表示疑似侵权
                        isWhite: it.isWhite,
                        isBlack: it.isBlack
                ]
            }
        }

        return apiResult(rep);
    }

    /*
    * 查询版权监控详情新闻列表-EXCEL导出
    */
    @RequestMapping(value = "/copyrightMonitorExcelOut", method = POST)
    public Map getCopyrightMonitorNewsExcelOut(
            @RequestBody  String data
    ) {
        def result = competitionAnalysSrv.copyrightMonitorNewsExcelOut( data)
        if(result.status != org.apache.http.HttpStatus.SC_OK) {
            return apiResult(result)
        }
        return apiResult([token:result.outfileName])
    }
    /*
    * 查询版权监控详情新闻列表-EXCEL导出
    */
    @Deprecated
    @RequestMapping(value = "/copyrightMonitorExcelByToken/{token}", method = GET)
    public Map getCopyrightMonitorNewsExcelOutByToken(
            @PathVariable("token") String token,
            HttpServletResponse response
    ) {
        def path = competitionAnalysSrv.getCopyrightMonitorNewsExcelByToken(token);
        if (path.equals("")){
            return apiResult(org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, '没有找到相关下载文件！')
        }
        try {
            DownloadUtils.download(path, response,null)
        } catch (IOException e) {
            return apiResult(org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, '下载发生错误！')
        }
        return apiResult()
    }
    /*
    * 查询版权监控列表
    */
    @RequestMapping(value = "/copyrightMonitors")
    public Map getCopyrightMonitors(
            @CurrentUser LoginUser user) {
        def list = competitionAnalysSrv.getCopyrightMonitors(user.userId)
        def monitorIds = []
        list.each {
            monitorIds << it.monitorId
        }

        def tortCountMap = competitionAnalysSrv.getNewsTortCounts(user.userId, monitorIds)
        def rep = [:]
        rep.list = []
        println tortCountMap
        list.each {
            rep.list << [
                    monitorId: it.monitorId,
                    userId: it.userId,
                    title: it.title,
                    url: it.url,
                    author: it.author,
                    date: it.date,
                    media: it.media,
                    tortCount: tortCountMap.get(it.monitorId) ?: 0 ,
                    reprintCount: competitionAnalysSrv.getReprintCount(it),   // 转发数
                    isTort: false,       // 为true, 表示疑似侵权
                    blackList: it.blackList,
                    whiteList: it.whiteList,
            ]
        }


        return apiResult(rep);
    }

    static String getCopyrightTypeName(int type) {
        switch (type) {
            case Const.CT_ORIGINAL: return '原创'
            case Const.CT_EXCLUSIVE: return '独家'
            case Const.CT_NON_EXCLUSIVE: return '非独家'
            case Const.CT_UNAUTHORIZED: return '非授权'
            default: ''
        }
    }
/*
    * 查询url对应的新闻信息
    */
    @RequestMapping(value = "/news")
    public Map getNews(
            @CurrentUserId Long userId,
            @RequestParam("url") String url
    ) {
        def news = competitionAnalysSrv.getNewsByUrl(url)
        def rep
        if (news) {
            rep = [
                    title       : news.title,
                    source      : news.source,
                    author      : news.author,
                    keyword     : news.keyword,
                    url         : news.url,
                    newsId      : news.newsId,
                    content     : news.content,
                    contentAbstract : news.contentAbstract,
                    site        : news.site,
                    createTime  : news.createTime.getTime()
            ]
        } else {
            rep = [:]
        }
        return apiResult(rep);
    }
    /**
     * 疑似侵权的条数
     */
    @RequestMapping(value = "/copyrightMonitor/isTortNewsCount", method = GET)
    public Map isTortNewsCount(
            @CurrentUser LoginUser user,
            @RequestParam("monitorIds") String monitorIds
    ) {
        def result = competitionAnalysSrv.getNewsTortCounts(user.userId,monitorIds)
        return apiResult(result);
    }
    /*
    * 查询相似站点
    */
    @RequestMapping(value = "/similarSites")
    public Map getSimilarMedia(
            @CurrentUserId Long userId,
            @RequestParam("siteType") int siteType,
            @RequestParam("siteName") String siteName
    ) {

        def rep = [:]
        rep.list = ["媒体1", "媒体2", "媒体3", "媒体4", "媒体5", "媒体6", "媒体7", "媒体8", "媒体9", "媒体10"]
        return apiResult(rep);
    }

    /*
    * 查询站点比较条件
    */
    @RequestMapping(value = "/siteComparisonQuery")
    public Map getSimilarMedia(
            @CurrentUserId Long userId
    ) {

        def rep = competitionAnalysSrv.getSiteComparison(userId)
        return apiResult(rep);
    }

    /*
    * 站点对比
    */
    @RequestMapping(value = "/siteComparison")
    public Map siteComparison(
            @CurrentUserId Long userId,
            @RequestParam("siteType") int mediaType,
            @RequestParam("siteNames") String[] siteNames
    ) {
        competitionAnalysSrv.saveSiteComparison(userId, mediaType, siteNames)

        def rep = [:]

        // 发布数统计
        rep.publishCount = [:]
        rep.publishCount.date = ['周一', '周二', '周三', '周四', '周五','周六','周日']
        rep.publishCount.list = []
        for (int i = 0; i < siteNames.length; i++) {
            rep.publishCount.list << randomList(7, 100, 0)
        }

        // 转载数统计
        rep.reprintCount = [:]
        rep.reprintCount.date = ['周一', '周二', '周三', '周四', '周五','周六','周日']
        rep.reprintCount.list = []
        for (int i = 0; i < siteNames.length; i++) {
            rep.reprintCount.list << randomList(7, 1000, 500)
        }

        // 阅读数统计
        rep.readingCount = [:]
        rep.readingCount.date = ['周一', '周二', '周三', '周四', '周五','周六','周日']
        rep.readingCount.list = []
        for (int i = 0; i < siteNames.length; i++) {
            rep.readingCount.list << randomList(7, 10000, 5000)
        }

        // 评论数统计
        rep.commentCount = [:]
        rep.commentCount.date = ['周一', '周二', '周三', '周四', '周五','周六','周日']
        rep.commentCount.list = []
        for (int i = 0; i < siteNames.length; i++) {
            rep.commentCount.list << randomList(7, 10000, 5000)
        }

        return apiResult(rep);
    }

    List randomList(int days, int high, int low) {
        def interval = high - low
        def list = []
        def random = new Random()
        for (int i = 0; i < days; i++) {
            list << random.nextInt(interval) + low
        }
        return list
    }
    /**
     * 黑白名单
     * @param user
     * @return
     */
    @RequestMapping(value = "/copyrightMonitor/copyRightFilter", method = GET)
    public Map getCopyRightFilter(
            @CurrentUser LoginUser user
    ) {
        def result = competitionAnalysSrv.getCopyRightFilter(user.userId)
        return apiResult([copyRightFilter:result]);
    }
    /**
     * 删除的站点是否在监控中
     * @param user
     * @param websiteDomain
     * @return
     */
    @RequestMapping(value = "/copyrightMonitor/copyRightFilter/isSiteMonitoring", method = GET)
    public Map isSiteMonitoring(
            @CurrentUser LoginUser user,
            @RequestParam("websiteDomain") String websiteDomain
    ) {
        def result = competitionAnalysSrv.isSiteMonitoring(user.userId,websiteDomain)
        return apiResult([isSiteMonitoring:result]);
    }
    /**
     * 保存copyRightFilter
     * @param user
     * @param whiteList
     * @param blackList
     * @return
     */
    @RequestMapping(value = "/copyrightMonitor/saveCopyRightFilter", method = PUT)
    public Map modifyCopyRightFilter(
            @CurrentUser LoginUser user,
            @RequestParam("whiteList") String whiteList,
            @RequestParam("blackList") String blackList
    ) {
        def result = competitionAnalysSrv.modifyCopyRightFilter(user.userId,whiteList,blackList)
        return result;
    }

}
