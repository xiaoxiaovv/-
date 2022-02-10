package com.istar.mediabroken.api

import com.alibaba.fastjson.JSONArray
import com.istar.mediabroken.entity.AbstractSetting
import com.istar.mediabroken.entity.ICompileSummary
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.News
import com.istar.mediabroken.entity.NewsAbstract
import com.istar.mediabroken.service.CaptureService
import com.istar.mediabroken.service.ICompileService
import com.istar.mediabroken.utils.DownloadUtils
import com.istar.mediabroken.utils.ZipUitl
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import weibo4j.org.json.JSONObject

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.text.SimpleDateFormat

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR
import static org.springframework.web.bind.annotation.RequestMethod.*

@RequestMapping(value = "/api/compile")
@RestController
@Slf4j
public class ICompileApiController {

    @Autowired
    ICompileService iCompileSrv
    @Autowired
    CaptureService captureSrv

    @Value('${image.upload.path}')
    String UPLOAD_PATH
    /*
     * 查询今日的新闻推送内容,按时间倒序排列, 48小时之内的
     */
    @RequestMapping(value = "/todayNewsPush")
    public Map getNewsPushList(
            @CurrentUser LoginUser user
    ) {
        def list = []
        iCompileSrv.todayNewsPush(user.userId, user.orgId)?.each {
            list << [
                    newId   : it.newsId,
                    title   : it.title,
                    source  : it.source,
                    heat    : it.heat,
                    time    : it.createTime
            ]
        }

        return apiResult([list : list]);
    }

    /*
     * 获取最新的要闻摘要(当天的),用来判断是否已生成摘要信息
     * @return {
        "status": 200,  200-已经生成    500-未生成过
        "id": '2017-04-18'
        }
     */
    @RequestMapping(value = "/latestNewsAbstract")
    public Map getLatestNewsAbstract(
            @CurrentUser LoginUser user
    ) {
        NewsAbstract newsAbstract = iCompileSrv.getTodayNewsAbstract(user.userId)
        if(newsAbstract){
            return apiResult([id: newsAbstract.abstractId])
        }else {
            return apiResult(SC_INTERNAL_SERVER_ERROR, '还没有生成今日要闻');
        }
    }

    /*
     * 获取最新的摘要素材，查询，排序接口
     * @return {
        "status": 200,  200-已经生成    500-未生成过
        "id": '2017-04-18'
        }
     */
    @RequestMapping(value = "/newsAbstracts", method = GET)
    public Map getNewsAbstracts(
            @CurrentUser LoginUser user,
            @RequestParam("pageNo") int pageNo,
            @RequestParam("limit") int limit,
            @RequestParam(value = "queryKeyWords",required = false , defaultValue = "") String queryKeyWords,
            @RequestParam(value = "orderType",required = false, defaultValue = "1" ) Integer orderType
    ) {

        def paging = iCompileSrv.getNewsAbstracts(user.userId,pageNo,limit,queryKeyWords,orderType);
        if(paging){
            def rep = [:]
            rep.total = paging.total
            rep.totalPage = paging.totalPage
            rep.list = []
            paging.list.each {
                rep.list << [
                        abstractId  : it.abstractId,
                        title       : it.title,
                        author      : it.author,
                        picUrl      : it.picUrl,
                        content     : it.content,
                        newsDetail  : it.newsDetail,
                        orgId       : it.orgId,
                        userId      : it.userId,
                        updateTime  : it.updateTime,
                        createTime  : it.createTime
                ]
            }

            return apiResult(rep);
        }else {
            return apiResult(SC_INTERNAL_SERVER_ERROR, '还没有生成今日要闻');
        }

    }

    /*
     * 获取最新的摘要素材，查询，排序接口
     * @return {
        "status": 200,  200-已经生成    500-未生成过
        "id": '2017-04-18'
        }
     */
    @RequestMapping(value = "/newsOperations", method = GET)
    public Map getNewsOperations(
            @CurrentUser LoginUser user,
            @RequestParam("pageNo") int pageNo,
            @RequestParam("limit") int limit,
            @RequestParam(value = "queryKeyWords",required = false , defaultValue = "") String queryKeyWords,
            @RequestParam(value = "operationSource",required = false , defaultValue = "1") Integer operationSource,
            @RequestParam(value = "timeType",required = false , defaultValue = "0") Integer timeType,
            @RequestParam(value = "timeStart",required = false , defaultValue = "") String timeStart,
            @RequestParam(value = "timeEnd",required = false , defaultValue = "") String timeEnd,
            @RequestParam(value = "orderType",required = false, defaultValue = "1" ) Integer orderType
    ) {
        /*queryKeyWords 标题/来源
        * operationSource 要闻来源
        * timeType 时间 createTime
        * orderType ：createTime/news.reprintCount 排序
        * */
        def paging = iCompileSrv.getNewsOperations(user.userId,pageNo,limit,queryKeyWords,orderType,operationSource,timeType,timeStart,timeEnd);
        if(paging){
            def rep = [:]
            rep.total = paging.total
            rep.totalPage = paging.totalPage
            rep.list = []
            paging.list.each {
                rep.list << [
                        _id :           it._id,
                        newsId :        it.newsId,
                        news :          it.news,
                        /*                        pushType :      it.pushType,
                                              status :        it.status,
                                               siteId :        it.siteId,
                                               orgId :         it.orgId,*/
                        userId :        it.userId,
                        updateTime :    it.updateTime,
                        createTime :    it.createTime,
                        operationType : it.operationType

                ]
            }

            return apiResult(rep);
        }else {
            return apiResult(SC_INTERNAL_SERVER_ERROR, 'error');
        }
    }
    /*
     * 获取要闻摘要
     * //
     */
    @RequestMapping(value = "/newsAbstract/{abstractId:.*}")
    public Map getNewsAbstract(
            @CurrentUser LoginUser user,
            @PathVariable String abstractId
    ) {
        NewsAbstract newsAbstract = iCompileSrv.getNewsAbstract(user.userId, abstractId)
        if(newsAbstract){
            return apiResult([newsAbstract: newsAbstract])
        }else {
            return apiResult(SC_INTERNAL_SERVER_ERROR, '没有找到相关要闻信息')
        }
    }
    /*
     * 下载要闻摘要
     * //
     */
    @Deprecated
    @RequestMapping(value = "/newsAbstract/downLoad/{abstractId:.*}")
    public Map downLoadNewsAbstract(
            @CurrentUser LoginUser user,
            @PathVariable String abstractId,
            HttpServletResponse response
    ) {

        def wordRes = iCompileSrv.getNewsAbstractWord(user.userId, abstractId)
        if(wordRes.status != HttpStatus.SC_OK) {
            return apiResult(wordRes)
        }
        // download
        try {
            DownloadUtils.download(wordRes.msg, response,null)
        } catch (IOException e) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '新闻下载发生错误！')
        }
        return apiResult()
    }
    /*
     * 本地上传图片 上传图片
     */
    @RequestMapping(value = "/newsAbstract/imgFromPc", method = POST)
    public Map imgFromPc(
            @CurrentUser LoginUser user,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "type") String type
    ) {
        //type可以指定存储的路径，如果不指定默认/img
        return iCompileSrv.uploadImgFromPc(user.userId,request, UPLOAD_PATH, type ?: '/img')

    }
    /*
     * 从原文上传图片 展示原文中的图片 返回string的List
     */
    @RequestMapping(value = "/newsAbstract/imgs", method = GET)
    public Map imgs(
            @CurrentUser LoginUser user,
            @RequestParam("abstract_id") String abstract_id
    ) {
        def imgUrls= iCompileSrv.getAbstractImgsById(user.userId, abstract_id);
        return apiResult([imgUrls:imgUrls]);
    }
    /*
 * 从原文上传图片 确定上传 返回上传图片的地址
 */
    @RequestMapping(value = "/newsAbstract/imgFromNews", method = POST)
    public Map imgFromNews(
            @CurrentUser LoginUser user,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "type") String type
    ) {
        iCompileSrv.uploadImgFromPc(user.userId,request, UPLOAD_PATH, type)
    }

    /*
     * 要闻摘要创建
     *
     */
    @RequestMapping(value = "/newsAbstract/create", method = PUT)
    public Map newsAbstractCreate(
            @CurrentUser LoginUser user,
            @RequestParam("newsOperationIds") String newsOperationIds
    ) {
        return iCompileSrv.createNewsAbstract(user.userId, user.orgId, newsOperationIds)
    }

    /**
     * 保存摘要信息
     * @param user
     * @param abstractId
     * @return
     */
    @RequestMapping(value = "/newsAbstract/modify", method = POST)
    public Map newsAbstractTempSave(
            @CurrentUser LoginUser user,
            @RequestBody String data
    ) {
        JSONObject newsAbstractJson = new JSONObject(data)
        def abstractMap = [:]
        newsAbstractJson.keys().each {
            abstractMap.put(it,newsAbstractJson.get(it))
        }
        return iCompileSrv.modifyNewsAbstract(user, abstractMap)
    }

    @RequestMapping(value = "/newsAbstract/{abstractId}" , method = DELETE)
    public Map removeNewsAbstract(
            @CurrentUser LoginUser user,
            @PathVariable String abstractId
    ) {
        return iCompileSrv.removeNewsAbstract(user.userId, abstractId)
    }

    /*
     * 获取要闻摘要设置
     */
    @RequestMapping(value = "/abstractSetting", method = GET)
    public Map getAbstractSetting(
            @CurrentUser LoginUser user
    ) {
        AbstractSetting setting = iCompileSrv.getAbstractSetting(user.userId)
        if(setting){
            return apiResult([setting: setting])
        }else {
            return apiResult(SC_INTERNAL_SERVER_ERROR, '没有找到对应的配置信息');
        }
    }

    /*
     * 更新要闻摘要设置
     * thumbnailStrategy  1. 最热, 2. 最新, 待讨论实现策略
     * showThumbnail    0- false  1- true
     */
    @RequestMapping(value = "/abstractSetting", method = PUT)
    public Map modifyAbstractSetting(
            @CurrentUser LoginUser user,
            @RequestParam int newsCount,
            @RequestParam int abstractLength,
            @RequestParam int showThumbnail,
            @RequestParam int thumbnailStrategy,    // 默认缩略图选择策略
            @RequestParam int thumbnailWidth,       // 默认缩略图宽度
            @RequestParam int thumbnailHeight,      // 默认缩略图高度
            @RequestParam int titleLength,
            @RequestParam String author
    ) {
        AbstractSetting setting = new AbstractSetting([
                newsCount           : newsCount,
                abstractLength      : abstractLength,
                showThumbnail       : showThumbnail == 1,
                thumbnailStrategy   : thumbnailStrategy,
                thumbnailWidth      : thumbnailWidth,
                thumbnailHeight     : thumbnailHeight,
                titleLength         : titleLength,
                author              : author,
                userId              : user.userId
        ])
        iCompileSrv.setAbstractSetting(setting)
        return apiResult()
    }

    /*
     * 查询综述相关的文章
     */
    // todo 话题详情接口用这个FrontTopicAllInfo!detailInfo.do,但是没有相关数据
    @RequestMapping(value = "/summary/{summaryId:.*}/relatedNews")
    public Map getRelatedNewsList(
            @CurrentUserId long userId,
            @PathVariable String summaryId) {
        def summary = iCompileSrv.getSummary(userId, summaryId)
        def news = iCompileSrv.getRelatedNews(summary, 5)
        def rep = [:]
        rep.list = []
        news.each {
            rep.list << [
                    newsId: it.newsId,
                    title: it.title,
                    source: it.source,
                    site: it.site,
                    time: it.createTime,
                    contentAbstract: it.contentAbstract
            ]
        }
        def top2NewsTracks = []
        int i = 0
        news.each {
            if ( i < 2 ){
                top2NewsTracks << it.title
            }
            i++
        }
        rep.top2NewsTracks = top2NewsTracks
        return apiResult(rep);
    }

    /*
     * 查询综述事件的事件传播趋势
     */
    @RequestMapping(value = "/summary/{summaryId:.*}/spreadTrend")
    public Map getSprendTrend(
            @CurrentUserId long userId,
            @PathVariable String summaryId) {
        def timeSdf = new SimpleDateFormat('yyyyMMdd')
        def summary = iCompileSrv.getSummary(userId, summaryId)
        def news = iCompileSrv.getSprendTrend(summary)

        def rep = [list:[]]
        def topNews = 0
        def topTime = new Date()
        news.list.each {
            if (it.news > topNews) {
                topNews = it.news   // 报道高峰新闻数
                topTime = it.time  // 报道高峰日期
            }
            rep.list << [
                    negativeNews: it.negativeNews,  // 负面消息数
                    positiveNews: it.positiveNews,  // 正面消息数
                    neutralNews: it.neutralNews, // 中性消息数
                    news: it.news,              // 全部消息数
                    time: timeSdf.format(it.time)
            ]
        }

        rep.topNews = topNews
        rep.topTime = topTime
        return apiResult(rep);
    }

    /*
     * 查询用户当前保存的综述id和查询标题
     */
    @RequestMapping(value = "/summary")
    public Map getSummary(
            @CurrentUserId long userId) {

        def summary = iCompileSrv.getSummary(userId)
        if (summary) {
            return apiResult([
                    summaryId: summary.summaryId,
                    title: summary.title,
                    time: summary.time,
                    place: summary.place,
                    person: summary.person,
                    event: summary.event,
                    ambiguous: summary.ambiguous,
                    startTime: summary.startTime,
                    endTime: summary.endTime,
            ]);
        } else {
            return apiResult([:]);
        }
    }
    /**
     * 查询传播综述列表
     * @param userId
     * @return
     */
    @RequestMapping(value = "/summaries" , method = GET)
    public Map getSummaries(
            @CurrentUserId long userId) {
        def summaries = iCompileSrv.getSummariesByUserId(userId)
        return apiResult([summaries : summaries]);
    }
    /**
     * 新加要闻综述
     * @param userId
     * @param title
     * @param place
     * @param body
     * @param event
     * @param ambiguous
     * @param startTime
     * @param endTime
     * @return
     */
    @RequestMapping(value = "/summary" , method = POST)
    public Map addSummary(
            @CurrentUserId long userId,
            @RequestParam("title") String title,
            @RequestParam(value = "place", required = false,  defaultValue = '') String place,  // 地点
            @RequestParam(value = "person", required = false,  defaultValue = '') String person, // 人物
            @RequestParam(value = "event", required = false,  defaultValue = '') String event, // 事件
            @RequestParam(value = "ambiguous", required = false,  defaultValue = '') String ambiguous, // 歧义词
            @RequestParam(value = "startTime") String startTime,
            @RequestParam(value = "endTime") String endTime
            ) {
        if (!title.trim()) {
            return apiResult(SC_BAD_REQUEST, "名称不能为空")
        }

        def sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
        def startDate = new Date()
        def endDate = new Date()
        try{
            startDate = sdf.parse(startTime)
            endDate   = sdf.parse(endTime)
        }catch(Exception e){
            return apiResult(SC_INTERNAL_SERVER_ERROR, "时间格式不正确")
        }

        if(startDate.getTime() >= endDate.getTime()){
            return apiResult(SC_INTERNAL_SERVER_ERROR, "开始时间不能大于结束时间")
        }

        def result = iCompileSrv.addSummary(
                new ICompileSummary(
                        userId   : userId,
                        title    : title.trim(),
                        place    : place.trim(),
                        person   : person.trim(),
                        event    : event.trim(),
                        ambiguous: ambiguous.trim(),
                        startTime: startDate,
                        endTime  : endDate
                )
        )
        return result;
    }

    /**
     * 删除要闻综述
     * @param userId
     * @param summaryId
     * @return
     */
    @RequestMapping(value = "/summary/{summaryId}" , method = DELETE)
    public Map removeSummary(
            @CurrentUserId long userId,
            @PathVariable String summaryId
    ) {
        return iCompileSrv.removeSummaryById(userId,summaryId);
    }

    /**
     * 获取要闻综述
     * @param userId
     * @param summaryId
     * @return
     */
    @RequestMapping(value = "/summary/{summaryId}" , method = GET)
    public Map getSummary(
            @CurrentUserId long userId,
            @PathVariable String summaryId
    ) {
        def summary = iCompileSrv.getSummaryById(summaryId)
        if(userId != summary.userId){
            return apiResult(SC_BAD_REQUEST, "没有找到用户的要闻综述")
        }
        return apiResult([summary : summary])
    }

    /**
     * 更新要闻综述
     * @param userId
     * @param summaryId
     * @return
     */
    @RequestMapping(value = "/summary/{summaryId}" , method = PUT)
    public Map modifySummary(
            @CurrentUserId long userId,
            @PathVariable String summaryId,
            @RequestParam("title") String title,
            @RequestParam(value = "place", required = false,  defaultValue = '') String place,  // 地点
            @RequestParam(value = "person", required = false,  defaultValue = '') String person, // 人物
            @RequestParam(value = "event", required = false,  defaultValue = '') String event, // 事件
            @RequestParam(value = "ambiguous", required = false,  defaultValue = '') String ambiguous, // 歧义词
            @RequestParam(value = "startTime") String startTime,
            @RequestParam(value = "endTime") String endTime
    ) {
        if (!title.trim()) {
            return apiResult(SC_BAD_REQUEST, "名称不能为空")
        }

        def sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
        def startDate = new Date()
        def endDate = new Date()
        try{
            startDate = sdf.parse(startTime)
            endDate   = sdf.parse(endTime)
        }catch(Exception e){
            return apiResult(SC_INTERNAL_SERVER_ERROR, "时间格式不正确")
        }

        if(startDate.getTime() >= endDate.getTime()){
            return apiResult(SC_INTERNAL_SERVER_ERROR, "开始时间不能大于结束时间")
        }
        return iCompileSrv.modifySummary(new ICompileSummary(
                summaryId: summaryId,
                userId   : userId,
                title    : title.trim(),
                place    : place.trim(),
                person   : person.trim(),
                event    : event.trim(),
                ambiguous: ambiguous.trim(),
                startTime: startDate,
                endTime  : endDate
        ));
    }

    /**
     * 更新要闻综述
     * @param userId
     * @param summaryId
     * @return
     */
    @RequestMapping(value = "/summary/{summaryId}/distribution" , method = GET)
    public Map getSummaryDistribution(
            @CurrentUserId long userId,
            @PathVariable String summaryId
    ) {
        def summary = iCompileSrv.getSummary(userId, summaryId)
        if(!summary){
            return apiResult(SC_BAD_REQUEST, "没有找到用户的要闻综述")
        }
        def distribution = iCompileSrv.getSummaryDistribution(summary)
        distribution.summaryId = summaryId
        return apiResult([distribution:distribution])
    }
    /**
     * 摘要模板更新
     * @param userId
     * @param summaryId
     * @param template
     * @return
     */
    @RequestMapping(value = "/summary/{summaryId}/template" , method = PUT)
    public Map modifySummaryTemplate(
            @CurrentUserId long userId,
            @PathVariable String summaryId,
            @RequestParam("template") int template
    ) {
        def result = iCompileSrv.modifySummaryTemplate(userId, summaryId, template)
        return result
    }
    /**
     * 获取用户默认的模板配置
     * @param userId
     * @return
     */
    @RequestMapping(value = "/summary/defaultTemplate" , method = GET)
    public Map getSummaryDefaultTemplate(
            @CurrentUserId long userId
    ) {
        def defaultTemplate = iCompileSrv.getSummaryDefaultTemplate(userId)
        return apiResult([defaultTemplate : defaultTemplate])
    }
    /**
     * 新加，编辑默认的模板
     * @param userId
     * @return
     */
    @RequestMapping(value = "/summary/defaultTemplate" , method = PUT)
    public Map modifySummaryDefaultTemplate(
            @CurrentUserId long userId,
            @RequestParam("defaultTemplate") int defaultTemplate

    ) {
        def result = iCompileSrv.modifySummaryDefaultTemplate(userId, defaultTemplate)
        return result
    }
    /*
     * 根据title查询综述信息, 如果标题没有变,综述信息没有过期, 综述设置就不会变
     */
    @RequestMapping(value = "/summary/search")
    public Map querySummary(
            @CurrentUserId long userId,
            @RequestParam("title") String title,
            @RequestParam(value = "time", required = false,  defaultValue = '') String time,  // 时间
            @RequestParam(value = "place", required = false,  defaultValue = '') String place,  // 地点
            @RequestParam(value = "person", required = false,  defaultValue = '') String person, // 人物
            @RequestParam(value = "event", required = false,  defaultValue = '') String event, // 事件
            @RequestParam(value = "ambiguous", required = false,  defaultValue = '') String ambiguous, // 歧义词
            @RequestParam(value = "startTime") String startTime,
            @RequestParam(value = "endTime") String endTime
            ) {
        if (!title.trim()) {
            return apiResult(SC_BAD_REQUEST, "查询不能为空")
        }
        def sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm')
        // todo vvv high 检查输入的关键字是否够3个,不够返回错误
        def summary = new ICompileSummary(
                userId: userId,
                title: title,
                time: time,
                place: place,
                person: person,
                event: event,
                ambiguous: ambiguous,
                startTime: sdf.parse(startTime),
                endTime: sdf.parse(endTime)
        )
        summary = iCompileSrv.querySummary(summary)
        return apiResult([summaryId: summary.summaryId, title: summary.title]);
    }

    /*
     * 查询综述事件的最新微博信息
     */
    @RequestMapping(value = "/summary/{summaryId:.*}/latestWeibo")
    public Map getLatestWeibo(
            @CurrentUserId long userId,
            @PathVariable String summaryId) {

        def summary = iCompileSrv.getSummary(userId, summaryId)
        def news = iCompileSrv.getLatestWei(summary, 10)
        def rep = [:]
        rep.list = []
        news.each {
            rep.list << [
                    newsId: it.newsId,
                    title: it.title,
                    site: it.site,
                    time: it.createTime
            ]
        }
        def top2NewsViews = []
        int i = 0
        news.each {
            if ( i < 2 ){
                top2NewsViews << it.author
            }
            i++
        }
        rep.top2NewsViews = top2NewsViews
        return apiResult(rep);
    }

    /*
     * 查询综述事件的活跃网友和媒体报道排名
     */
    @RequestMapping(value = "/summary/{summaryId:.*}/topN")
    public Map getTopN(
            @CurrentUserId long userId,
            @PathVariable String summaryId) {

        def summary = iCompileSrv.getSummary(userId, summaryId)
        def news = iCompileSrv.getTopN(summary)
        def rep = [:]
        rep.topAuthors = []
        int limit = 5
        int i = 0
        news.topAuthors.each {
            if (i == limit ) {
                return
            } else {
                i++
            }
            rep.topAuthors << [
                    author: it.author,                  // 作者
                    publishCount: it.publishCount,      // 发贴数
            ]
        }

        rep.topSites = []
        i = 0
        news.topSites.each {
            if (i == 10 ) {
                return
            } else {
                i++
            }
            rep.topSites << [
                    site: it.site,                  // 网站
                    publishCount: it.publishCount,      // 发贴数
            ]
        }
        return apiResult(rep);
    }

    /*
     * 查询综述事件的事件演化, 包括词云(hotKeywords)
     */
    // todo 是否要把路径修改成hotKeywords
    @RequestMapping(value = "/summary/{summaryId:.*}/eventEvolution")
    public Map getTopAuthors(
            @CurrentUserId long userId,
            @PathVariable String summaryId) {

        def summary = iCompileSrv.getSummary(userId, summaryId)
        def result = iCompileSrv.getEventEvolution(summary)
        def rep = [:]

        // 热词
        rep.hotKeywords = []
        result.hotKeywords.each {
            rep.hotKeywords << [
                    keyword: it.keyword,  // 关键词
                    count: it.count,      // 数目
            ]
        }

        // 新闻脉落
        def newsEvolution = [:]
        def sdf = new SimpleDateFormat("yyyy-MM-dd")
        def j = 1
        def k = 0
        result.news.each {News news ->
            if (k > 30 ) return
            k++
            def simhash = news.simhash
            if (!simhash) simhash = 'sh: ' + UUID.randomUUID().toString()
            def evolution = newsEvolution.get(simhash)
            if (evolution) {
                def time = sdf.format(news.createTime)
                int i = 0
                for (; i < evolution.evolution.size(); i++) {
                    if (evolution.evolution[i].time == time) {
                        break
                    }
                }
                if (i == evolution.evolution.size()) {
                    evolution.evolution << [time: sdf.format(news.createTime), value: 1]
                }

            } else {
                evolution = [name: news.title, weight: j++, evolution: [[time: sdf.format(news.createTime), value: 1]]]
                newsEvolution.put(simhash, evolution)
            }

        }
        rep.newsEvolution = []
        newsEvolution.each {
            rep.newsEvolution << it.value
        }

        // 主体脉落
        def siteEvolution = [:]
        j = 1
        result.news.each {News news ->
            def site = news.site
            if (!site) site = 'sh: ' + UUID.randomUUID().toString()
            def evolution = siteEvolution.get(site)
            if (evolution) {
                def time = sdf.format(news.createTime)
                int i = 0
                for (; i < evolution.evolution.size(); i++) {
                    if (evolution.evolution[i].time == time) {
                        break
                    }
                }
                if (i == evolution.evolution.size()) {
                    evolution.evolution << [time: sdf.format(news.createTime), value: 1, detail: [text: news.title]]
                }

            } else {
                evolution = [name: news.site, weight: j++, evolution: [[time: sdf.format(news.createTime), value: 1, detail: [text: news.title]]]]
                siteEvolution.put(site, evolution)
            }

        }
        rep.siteEvolution = []
        siteEvolution.each {
            rep.siteEvolution << it.value
        }


        return apiResult(rep);
    }


    /*
     * 查询综述事件的专题简介
     */
    // todo 是否要把路径修改成hotKeywords
    @RequestMapping(value = "/summary/{summaryId:.*}/intro")
    public Map getIntro(
            @CurrentUserId long userId,
            @PathVariable String summaryId) {

        def summary = iCompileSrv.getSummary(userId, summaryId)
        def result = iCompileSrv.getIntro(summary)

        def sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm")
        def rep = [:]
        def sites = []
        result.topSites.each {
            return sites << '【' + it + '】'
        }
        rep.intro = String.valueOf("新闻【${result.title}】；首发网站【${result.firstPublishSite}】；首发时间【${sdf.format(result.firstPublishTime)}】；" +
                "对【${sdf.format(result.startTime)}~${sdf.format(new Date())}】期间，互联网上采集到的【${result.totalNews}】条信息进行深入分析。全网信息量最高峰出现在" +
                "【${sdf.format(result.maxDate)}】，当天共产生【${result.maxCount}】篇相关讯息；后续报道主要来源于${sites.join(",")}等几大站点。")
//                "总体来说，整个事件的发展趋势较为【平稳】，网络转载新闻以【正面】为主，网友评论以【正面】为主")
        return apiResult(rep)

    }

//    discussion

    /*
     * 查询综述相关的文章
     */
    @RequestMapping(value = "/summary/{summaryId:.*}/discussions")
    public Map getDiscussionList(
            @CurrentUserId long userId,
            @PathVariable String summaryId) {
        def summary = iCompileSrv.getSummary(userId, summaryId)
        def news = iCompileSrv.getDiscussions(summary, 5)
        def rep = [:]
        rep.list = []
        news.each {
            rep.list << [
                    newsId: it.newsId,
                    title: it.title,
                    source: it.site,
                    contentAbstract: it.contentAbstract,
                    time: it.createTime
            ]
        }
        def top2NewsDiscussionss = []
        int i = 0
        news.each {
            if ( i < 2 ){
                top2NewsDiscussionss << it.title
            }
            i++
        }
        rep.top2NewsDiscussionss = top2NewsDiscussionss
        return apiResult(rep);
    }

    /*
    * 查询综述的处理进度
    */
    @RequestMapping(value = "/summary/{summaryId:.*}/progress")
    public Map getProgress(
            @CurrentUserId long userId,
            @PathVariable String summaryId) {
        def summary = iCompileSrv.getSummary(userId, summaryId)
        def stats = iCompileSrv.getSprendTrend(summary)
        def rep = [totalNews: stats.totalNews]


        // 计算进度
        def interval = summary.endTime.getTime() - System.currentTimeMillis()
        if (interval < 0) {
            rep.progress = '100%'
            rep.restTime = 0
            rep.restTimeHuman = '0秒'
        } else {
            log.debug("{}, {}, {}, {}", summary.endTime, summary.createTime, interval, summary.endTime.getTime() - summary.createTime.getTime())
            def progress = (int)((System.currentTimeMillis() - summary.createTime.getTime()) * 100 / (summary.endTime.getTime() - summary.createTime.getTime()))
            rep.progress = progress == 100 ? '99%' : progress + '%'
            rep.restTime = interval

            // 计算时间
            def n = interval / (24 * 60 * 60 * 1000)
            if (n > 1) {
                rep.restTimeHuman = getNum(n) + '天'
            } else {
                n = interval / (60 * 60 * 1000)
                if (n > 1) {
                    rep.restTimeHuman = getNum(n) + '小时'
                } else {
                    n = interval / (60 * 1000)
                    if (n > 1) {
                        rep.restTimeHuman = getNum(n) + '分'
                    } else {
                        rep.restTimeHuman = getNum(n / 1000) + '秒'
                    }
                }

            }
        }


        return apiResult(rep);
    }

    /*
    * 查询综述的热议话题
    */
    @RequestMapping(value = "/summary/{summaryId:.*}/hotTopic")
    public Map getHotTopic(
            @CurrentUserId long userId,
            @PathVariable String summaryId) {
        def summary = iCompileSrv.getSummary(userId, summaryId)
        def news = iCompileSrv.getHotTopic(summary)

        def rep = [:]
        rep.list = []
        def top2NewsSources = []
        for (int i = 0; i < news.size() && i < 5; i++) {
            if ( i < 2 ){
                top2NewsSources << news[i].title
            }
            rep.list << [
                    title: news[i].title
            ]
        }
        rep.top2NewsSources = top2NewsSources
        return apiResult(rep);
    }

    /*
    * 查询综述的关联脉落
    */
    @RequestMapping(value = "/summary/{summaryId:.*}/weiboRelations")
    public Map getWeiboAuthorRelations(
            @CurrentUserId long userId,
            @PathVariable String summaryId) {
        def summary = iCompileSrv.getSummary(userId, summaryId)
        println summary.yqmsTopicId
        def relations = iCompileSrv.getWeiboAuthorRelations(summary)
        return apiResult(relations);
    }

    private String getNum(def n) {
        def num = String.format("%.1f", n)
        if (num.indexOf('.0')) {
            return num.substring(0, num.size() - 2)
        } else {
            return num
        }
    }

    /*
    *
    * 查询综述推送数据, 按创建时间倒序排序
    *
    * status: 推送状态, 0, 全部, 1, 未推送  2, 已推送
    *
    *
    * 返回:
    *  新闻列表
    String title
    String source
    */
    @RequestMapping(value = "/summaryPush", method = GET)
    public Map getSummaryPushList(
            @CurrentUser LoginUser user,
            @RequestParam(value = "status", required = false, defaultValue = "0") int status,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit
    ) {
        def paging = iCompileSrv.getPagingSummaryPushList(user.userId, user.orgId, status, pageNo, limit)
        def rep = [:]
        rep.total       = paging.total
        rep.totalPage   = paging.totalPage
        rep.list        = []
        paging.list.each {
            rep.list << [
                    summaryId   : it.summaryId,
                    title       : it.title,
                    source      : it.source,
                    status      : it.status as int,
                    createTime  : it.createTime?.time
            ]
        }

        return apiResult(rep)
    }

    /*
    *
    * 创建推送
    *
    * summaryId: 综述ID
    *
    *
    * 返回:
    */
    @RequestMapping(value = "/summaryPush", method = POST)
    public Map createSummaryPush(
            @CurrentUser LoginUser user,
            @RequestParam("summaryId") String summaryId
    ) {
        iCompileSrv.createSummaryPush(user.userId, user.orgId, summaryId)
        return apiResult()
    }

    /*
    *
    * 查询要闻推送数据, 按创建时间倒序排序
    *
    * status: 推送状态, 0, 全部, 1, 未推送  2, 已推送
    *
    *
    * 返回:
    *  新闻列表
    String title
    String source
    */
    @RequestMapping(value = "/abstractPush", method = GET)
    public Map getAbstractPushList(
            @CurrentUser LoginUser user,
            @RequestParam(value = "status", required = false, defaultValue = "0") int status,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit
    ) {
        def paging = iCompileSrv.getPagingAbstractPushList(user.userId, user.orgId, status, pageNo, limit)
        def rep = [:]
        rep.total       = paging.total
        rep.totalPage   = paging.totalPage
        rep.list        = []
        paging.list.each {
            rep.list << [
                    abstractId  : it.abstractId,
                    title       : it.title,
                    source      : it.source,
                    status      : it.status as int,
                    createTime  : it.createTime?.time
            ]
        }

        return apiResult(rep)
    }

    /*
    *
    * 创建推送
    *
    * abstractId: 要闻ID
    *
    *
    * 返回:
    */
    @RequestMapping(value = "/abstractPush", method = POST)
    public Map createAbstractPush(
            @CurrentUser LoginUser user,
            @RequestParam("abstractId") String abstractId
    ) {
        iCompileSrv.createAbstractPush(user.userId, user.orgId, abstractId)
        return apiResult()
    }

   @RequestMapping(value = "/downLoadTextSummary", method = POST)
    public Object downLoadTextSummary(
            HttpServletResponse response,
            @CurrentUser LoginUser user,
            @RequestParam(value = "intro" ,required = false) String intro,
            @RequestParam(value = "keyArticle" ,required = false) String keyArticle,
            @RequestParam(value = "newsRank" ,required = false) String newsRank,
            @RequestParam(value = "hotTopic" ,required = false) String hotTopic,
            @RequestParam(value = "relatedNews" ,required = false) String relatedNews,
            @RequestParam(value = "latestWeibo" ,required = false) String latestWeibo,
            @RequestParam(value = "discussions" ,required = false) String discussions,
            @RequestParam(value = "title" ,required = false) String title,
            @RequestParam(value = "relatedNewsTrack" ,required = false) String relatedNewsTrack,
            @RequestParam(value = "hotTopicIntro" ,required = false) String hotTopicIntro,
            @RequestParam(value = "latestWeiboView" ,required = false) String latestWeiboView,
            @RequestParam(value = "hotDiscussions" ,required = false) String hotDiscussions
    ) {

        def wordRes = iCompileSrv.getTextSummaryWord(intro,title,keyArticle,newsRank,hotTopic,relatedNews,latestWeibo,discussions,relatedNewsTrack,hotTopicIntro,latestWeiboView,hotDiscussions)

        if(wordRes.status != HttpStatus.SC_OK) {
            return apiResult(wordRes)
        }
        return apiResult([token:wordRes.outfileName])
    }
    @Deprecated
    @RequestMapping(value = "/downLoadTextSummaryByToken/{token}", method = GET)
    @ResponseBody
    public Object downLoadTextSummary(
            HttpServletResponse response,
            @PathVariable String token
            ){
        def path = iCompileSrv.downLoadTextSummaryPathByToken(token);
        if (path.equals("")){
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '没有找到相关下载文件！')
        }
        try {
            DownloadUtils.download(path, response,null)
        } catch (IOException e) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '新闻下载发生错误！')
        }
        return apiResult()
    }
    @Deprecated
    @RequestMapping(value = "/downLoadImageSummaryByToken/{token}", method = GET)
    @ResponseBody
    public Object downLoadImageSummary(
            HttpServletResponse response,
            @PathVariable String token
    ){
        def path = iCompileSrv.downLoadImageSummaryPathByToken(token);
        if (path.equals("")){
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '没有找到相关下载文件！')
        }
        try {
            DownloadUtils.download(path, response,null)
        } catch (IOException e) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '新闻下载发生错误！')
        }
        return apiResult()
    }


    @Deprecated
    @RequestMapping(value = "/downLoadSummary", method = POST)
    public Object imgInfo(
            HttpServletResponse response,
            @CurrentUser LoginUser user,
            @RequestParam(value = "keyWordCloudImg" ,required = false) String keyWordCloudImg,
            @RequestParam(value = "relatedNews" ,required = false) Object relatedNews
    ) {
        println("keyWordCloudImg"+ keyWordCloudImg)
        println("relatedNews"+ relatedNews)

        def wordRes = iCompileSrv.getSummaryWord(keyWordCloudImg, relatedNews)

        if(wordRes.status != HttpStatus.SC_OK) {
            return apiResult(wordRes)
        }
        try {
            DownloadUtils.download(wordRes.msg, response,null)
        } catch (IOException e) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '新闻下载发生错误！')
        }
        return apiResult()
    }
/*智能组稿-传播综述-图表下载*/
    @RequestMapping(value = "/downLoadImageSummary", method = POST)
    public Object downLoadImageSummary(
            HttpServletResponse response,
            @CurrentUser LoginUser user,
            @RequestParam(value = "intro" ,required = false) String intro,
            @RequestParam(value = "topNews" ,required = false) String topNews,
            @RequestParam(value = "list" ,required = false) String list,
            @RequestParam(value = "newsRank" ,required = false) String newsRank,
            @RequestParam(value = "psiTrend" ,required = false) String psiTrend,
            @RequestParam(value = "bsiTrend" ,required = false) String bsiTrend,
            @RequestParam(value = "bloggerRank" ,required = false) String bloggerRank,
            @RequestParam(value = "distribution" ,required = false) String distribution,
            @RequestParam(value = "trendWeek" ,required = false) String trendWeek,
            @RequestParam(value = "firstShow" ,required = false) String firstShow,
            @RequestParam(value = "writenTop" ,required = false) String writenTop,
            @RequestParam(value = "structure" ,required = false) String structure,
            @RequestParam(value = "miiTop" ,required = false) String miiTop,
            @RequestParam(value = "powerTop" ,required = false) String powerTop,
            @RequestParam(value = "region" ,required = false) String region,
            @RequestParam(value = "opinion" ,required = false) String opinion,
            @RequestParam(value = "trendImg" ,required = false) String trendImg,
            @RequestParam(value = "mapImg" ,required = false) String mapImg,
            @RequestParam(value = "formImg" ,required = false) String formImg,
            @RequestParam(value = "title" ,required = false) String title

            ) {
        def wordRes = iCompileSrv.getImageSummaryWord(intro,title,topNews,list,newsRank,psiTrend,bsiTrend,bloggerRank,distribution,trendWeek,firstShow,writenTop,structure,miiTop,powerTop,region,opinion,trendImg,mapImg,formImg)
        if(wordRes.status != HttpStatus.SC_OK) {
            return apiResult(wordRes)
        }
        return apiResult([token:wordRes.outfileName])
    }
    /*
       模板2相关接口
     */

    /*
     * 传播范围
     */
    // todo 是否要把路径修改成hotKeywords
    @RequestMapping(value = "/summary/{summaryId:.*}/spreadRange")
    public Map getSpreadRangeIntro(
            @CurrentUserId long userId,
            @PathVariable String summaryId) {

        def summary = iCompileSrv.getSummary(userId, summaryId)
        def list = iCompileSrv.getSourceTypeDistribution(summary)

        def rep = [:]
        rep.spreadCount = []
        def total = 0
        list.each {
            rep.spreadCount << [
                    newsTypeName: it.source,
                    count: it.count,
            ]
            total += it.count
        }

        def pencents = []
        def counts = []
        list.each {
            def percent = total == 0 ?  '0' : String.format('%.1f', it.count / total * 100)
            counts << "${it.source} ${it.count}"
            pencents << "${it.source}【${percent}%】"
        }
        rep.intro = String.valueOf(
                "传播周期内【${summary.title}】共获取传播信息${total}条" +
                "（${counts.join(' / ')} ）。其中，" +
                "${pencents.join('，')}。")

        list.sort { a, b ->
            return b.count - a.count
        }

        rep.keyMedias = [list[0].source, list[1].source]
        return apiResult(rep)
    }

    /*
    *   各渠道媒体信息
    */
    @RequestMapping(value = "/summary/{summaryId:.*}/firstPublishMedias")
    public Map getFirstPublishMedias(
            @CurrentUserId long userId,
            @PathVariable String summaryId) {

        def summary = iCompileSrv.getSummary(userId, summaryId)
        def result = iCompileSrv.getFirstPublishMedias(summary)

        def rep = [list: []]
        def newsTypes = new HashSet()
        result.each {
             rep.list << toNews(it, { news ->
                def newsTypeName
                switch (news.newsType) {
                    case 1: newsTypeName = '新闻'; break;
                    case 2: newsTypeName = '论坛'; break;
                    case 3: newsTypeName = '博客'; break;
                    case 4: newsTypeName = '微博'; break;
                    case 5: newsTypeName = '平媒'; break;
                    case 7: newsTypeName = '视频'; break;
                    case 8: newsTypeName = '长微博'; break;
                    case 9: newsTypeName = 'APP'; break;
                    case 10: newsTypeName = '评论'; break;
                    case 99: newsTypeName = '其他'; break;
                    default: newsTypeName = '其他'
                }
                newsTypes << newsTypeName
                return [
                        newsTypeName: newsTypeName
                ]
            })
        }

        rep.newsTypes = newsTypes.toList()
        return apiResult(rep)
    }

    private static Map toNews(News news, def cloure = null) {
        def result = [
                newsId: news.newsId,
                title: news.title,
                contentAbstract: news.contentAbstract,
                source: news.source,
                author: news.author,
                url: news.url,
                time: news.createTime,
                newsType: news.newsType,
        ]

        if (cloure) {
            def result2 = cloure(news)
            if (result) {
                result.putAll(result2)
            }
        }

        return result
    }

    /*
    *   各渠道媒体信息
    */
    @RequestMapping(value = "/summary/{summaryId:.*}/weiboAnalysis")
    public Map getWeiboAnalysis(
            @CurrentUserId long userId,
            @PathVariable String summaryId) {

        def summary = iCompileSrv.getSummary(userId, summaryId)
        def result = iCompileSrv.getWeiboAnalysis(summary)

        def rep = [distribution: [], bloggerRank: []]


        result.distribution.each {
            rep.distribution << [
                    region: it.region,
                    persons: it.persons
            ]
        }

        result.distribution.sort { a, b ->
            return b.persons - a.persons
        }

        if (result.distribution) {
            rep.top3Locations = [result.distribution[0].region, result.distribution[1].region, result.distribution[2].region]
        } else {
            rep.top3Locations = []
        }

        def personsAffectedRamdom = randomList(result.bloggerRank.size(), 35000, 1200000)
        int i = 0
        result.bloggerRank.each {
            rep.bloggerRank << [
                    name: it.name,
                    personsAffected: personsAffectedRamdom[i++]
            ]
        }


        if (rep.bloggerRank) {
            rep.topPersonsAffected = rep.bloggerRank[0].personsAffected
            rep.topBlogger = rep.bloggerRank[0].name
        } else {
            rep.topPersonsAffected = 0
            rep.topBlogger = ''
        }
        return apiResult(rep)
    }

    /*
    *   稿件排行
    */
    @RequestMapping(value = "/summary/{summaryId:.*}/newsRank")
    public Map getNewsRank(
            @CurrentUserId long userId,
            @PathVariable String summaryId,
            @RequestParam(value = "limit", required = false, defaultValue = "5") int limit
    ) {

        def summary = iCompileSrv.getSummary(userId, summaryId)
        def result = iCompileSrv.getEventEvolution(summary)

        def rep = [list: [], topSite: '', topNews: '', reprintCountBySite: 0]


        def reprintCountBySiteRandom = randomList(limit, 100, 500)
        def reprintCountByPersionRandom = randomList(limit, 500, 1000)
        def commentCountRandom = randomList(limit, 400, 800)
        def likesCountRandom = randomList(limit, 1500, 3000)
        int i = 0
        result.news.each { News news ->

            if (i < limit) {
                rep.list << toNews(news, {
                    return [
                            reprintCountBySite: reprintCountBySiteRandom[i],
                            reprintCountByPersion: reprintCountByPersionRandom[i],
                            commentCount: commentCountRandom[i],
                            likesCount: likesCountRandom[i]
                    ]
                })
                i++
            }
        }
        rep.topSite = rep.list[0].source
        rep.topNews = rep.list[0].title
        int k = 0
        rep.list.each {
            if ( k++ < 1) {
                rep.reprintCountBySite = it.reprintCountByPersion
            }
        }

        def top3News = []
        def j = 0
        result.news.each { News news ->
            if (j < 3) {
                top3News << news.title
                j++
            }

        }
        rep.top3News = top3News

        return apiResult(rep)
    }

    static String floatRandom(float min, float max, int digit) {
        def num = Math.random() * (max - min) + min
        return String.format('%.' + digit + 'f', num)
    }

    int random(int low, int high) {
        def interval = high - low
        def random = new Random()
        return random.nextInt(interval) + low
    }

    List randomList(int size, int low, int high) {
        def randomList = []
        def interval = high - low
        def random = new Random()
        for (int i = 0; i < size; i++) {
            def randomNum = random.nextInt(interval)
            randomList << randomNum + low
        }
        randomList.sort {a , b ->
                return b - a
        }
        return randomList
    }

    List floatRandomList(int size, int low, int high, int digit) {
        def randomList = []
        for (int i = 0; i < size; i++) {
            randomList << floatRandom(low, high, digit)
        }
        randomList.sort {String a , String b ->
            return b.compareTo(a)
        }
        return randomList
    }

    /*
    *   媒体传播力排行
    */
    @RequestMapping(value = "/summary/{summaryId:.*}/psiTrend")
    public Map getPsiTrend(
            @CurrentUserId long userId,
            @PathVariable String summaryId) {

        def summary = iCompileSrv.getSummary(userId, summaryId)

        def rep = [list: [], sites: []]
        def news = iCompileSrv.getTopN(summary)

        int limit = 5
        int i = 0
        i = 0
        def psiRamdom = floatRandomList(limit, 300, 450, 2)
        int j = 0
        news.topSites.each {
            if (i == limit ) {
                return
            } else {
                i++
            }
            if ( i < 4 ){
                rep.sites.add(it.site)
            }
            rep.list << [
                    siteName: it.site, // 网站
                    psi: psiRamdom[j++],
            ]
        }

        return apiResult(rep)
    }

    /*
    *   媒体影响力排行
    */
    @RequestMapping(value = "/summary/{summaryId:.*}/bsiTrend")
    public Map getsiTrend(
            @CurrentUserId long userId,
            @PathVariable String summaryId) {

        def summary = iCompileSrv.getSummary(userId, summaryId)

        def rep = [list: [], sites: []]
        def news = iCompileSrv.getTopN(summary)


        int limit = 5
        int i = 0
        i = 0
        def psiRandom = floatRandomList(limit, 300, 500, 2)

        int j = 0
        news.topSites.each {
            if (i == limit ) {
                return
            } else {
                i++
            }
            if ( i < 4 ){
                rep.sites.add(it.site)
            }
            rep.list << [
                    siteName: it.site, // 网站
                    psi: psiRandom[j++],      // 发贴数
            ]
        }

        return apiResult(rep)
    }

    @RequestMapping(value = "/newsAbstract/getHistoryList", method = GET)
    public Map getHistoryList(
            @CurrentUser LoginUser user,
            @RequestParam(value = "type", required = false, defaultValue = "1") int type,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit
    ) {
        def paging = null;

        switch (type){
            case 1 :
                paging = iCompileSrv.getPagingAbstractList(user.userId, user.orgId, pageNo, limit)
                break
            case 2 :
                paging = iCompileSrv.getPagingAbstractPushList(user.userId, user.orgId, 1, pageNo, limit)
                break
            case 3 :
                paging = iCompileSrv.getPagingAbstractDownload(user.userId, user.orgId, pageNo, limit)
                break
            case 4 :
                paging = iCompileSrv.getPagingAbstractShare(user.userId, user.orgId, pageNo, limit)
                break
            default:
                return apiResult(SC_INTERNAL_SERVER_ERROR, '请求类型不匹配')
        }
        def rep = [:]
        rep.total       = paging.total
        rep.totalPage   = paging.totalPage
        rep.list        = []
        paging.list.each {
            def id = (type in [1]) ? it.abstractId : it.id
            def source = (type in [1, 4]  ? "" : it?.source)
            def status = (type in [1, 4]  ? "" : it?.status)
            def res = [
                    id          : id,
                    type        : type,
                    abstractId  : it.abstractId,
                    title       : it.title,
                    source      : source,
                    status      : status,
                    createTime  : it.createTime?.time
            ]
            if(type == 4) {
                res.shareChannel = it.shareChannel
            }

            rep.list << res
        }

        return apiResult(rep)
    }

    @RequestMapping(value = "/newsAbstract/getNewsDeatil/{type}/{id}", method = GET)
    public Map getNewsDeatil(
            @PathVariable int type,
            @PathVariable String id
    ) {
        def detail = null
        switch (type){
            case 1 :
                detail = iCompileSrv.getNewsAbstractById(id)
                break
            case 2 :
                detail = iCompileSrv.getAbstractPush(id)
                break
            case 3 :
                detail = iCompileSrv.getAbstractDownload(id)
                break
            case 4 :
                detail = iCompileSrv.getAbstractShare(id)
                break
            default:
                return apiResult(SC_INTERNAL_SERVER_ERROR, '请求类型不匹配')
        }
        return apiResult([detail:detail])
    }

    @RequestMapping(value = "/material/{type}/{id}", method = GET)
    public Map getMaterial(
            @PathVariable int type,
            @PathVariable String id
    ) {
        def detail = null
        switch (type){
            case 1 :
                detail = captureSrv.getNewsById4Material(id)
                break
            case 2 :
                detail = iCompileSrv.getNewsAbstractById4Material(id)
                break
            default:
                return apiResult(SC_INTERNAL_SERVER_ERROR, '请求类型不匹配')
        }
        return detail
    }
}
