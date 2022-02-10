package com.istar.mediabroken.api

import com.istar.mediabroken.Const
import com.istar.mediabroken.api3rd.CaptureApi3rd
import com.istar.mediabroken.entity.Focus
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.News
import com.istar.mediabroken.service.CaptureService
import com.istar.mediabroken.service.account.AccountService
import com.istar.mediabroken.utils.DownloadUtils
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.text.SimpleDateFormat

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR
import static org.springframework.web.bind.annotation.RequestMethod.*

@Controller
@Slf4j
@RestController
public class CaptureApiController {

    @Autowired
    CaptureService captureSrv

    @Autowired
    AccountService accountSrv

    @Autowired
    CaptureApi3rd captureApi

    @Value('${max.site.count}')
    int MAX_SITE_COUNT
    @Value('${image.upload.path}')
    String UPLOAD_PATH

    @Value('${env}')
    String env

    /**
     *
     * 添加站点设置
     *
     * 请求:
     * sid: "会话id"
     *      siteName: "站点名称"
     *      siteDomain: "站点域名",
     *      channelName: "频道名称",
     *      channelDomain: "频道域名",
     *      navName: "导航名称",
     *      isShow: boolean, //导航显示
     *      categoryId: "" // 所属类别名称
     *
     * 返回:
     * {
     *    status: int           // 状态,200为成功
     *    navId: int            // 导航ID
     * }
     */
    @RequestMapping(value = "/api/capture/settings", method = POST)
    @ResponseBody
    public Map addSettings(String sid) {
        def rep = [:]
        rep.status = 200
        rep.navId = System.currentTimeMillis()

        return rep;
    }

    /**
     *
     * 修改站点设置
     *
     * 请求:
     * sid: "会话id"
     *      siteName: "站点名称"
     *      siteDomain: "站点域名",
     *      channelName: "频道名称",
     *      channelDomain: "频道域名",
     *      navName: "导航名称",
     *      isShow: boolean, //导航显示
     *      categoryId: "" // 所属类别名称
     *
     * 返回:
     * {
     *    status: int           // 状态,200为成功
     * }
     */
    @RequestMapping(value = "/api/capture/settings/{navId:.+}", method = PUT)
	@ResponseBody
	public Map modifySettings(String sid, @PathVariable("navId") String navId) {
        def rep = [:]
        rep.status = 200

		return rep;
	}

    /**
     *
     * 删除站点设置
     *
     * 请求:
     * sid: "会话id"
     *
     * 返回:
     * {
     *    status: int           // 状态,200为成功
     * }
     */
    @RequestMapping(value = "/api/capture/settings/{navId:.+}", method = RequestMethod.DELETE)
    @ResponseBody
    public Map deleteSettings(String sid, @PathVariable("navId") String navId) {
        def rep = [:]
        rep.status = 200

        return rep;
    }


    /*
    * 获取有效的站点
    *
    *
    * 请求:
    * sid: "会话id"
    * siteType: int,        // 站点类型, 1. 媒体网站 2. 微信列表 3. 专业网站, 不传则为全部网站
    *
    * 返回:
    * {
    *    status: int           // 状态,200为成功
    *    list: [{
    *      siteId: "站点ID"
    *      siteName: "站点名称"
    *    }, ...]
    * }
    */
    @RequestMapping(value = "/api/capture/validSites")
    @ResponseBody
    public Map getValidSites(@CurrentUserId Long userId, Integer siteType) {
        // todo 解决全部新闻的问题
        def sites = captureSrv.getValidSites(userId, siteType)
        def rep = []
        sites.each {
            rep << [siteName: it.siteName, siteId: it.siteId]
        }
        return apiResult([list: rep ])
    }

    /**
     * 请求:
     * sid: "会话id"
     * siteId: "站点ID"
     * pageNo: int   // 页号
     * limit: int
     *
     * 返回:
     * <pre>
     * {
     *    status: int           // 状态,200为成功
     *    total: int,           // 新闻总数,第一页为1
     *    totalPage: int,       // 总页数
     *    list: [{
     *      newsId: "新闻Id"
     *      title: "标题",
     *      source: "新浪",      // 来源
     *      time: long,
     *      heat: int
     *    }, ...],
     *    updateTime: long    // 最后更新时间, 只有是第一页的时候才有
     * }
     * </pre>
     */
    @RequestMapping(value = "/api/capture/news")
    @ResponseBody
    public Map getNewsList(
            @CurrentUser LoginUser user,
            @RequestParam(value = "siteId") String siteId,
            @RequestParam(value = "query", required = false) String query,   // 空格分隔的字符串,至少要
            @RequestParam("pageNo") int pageNo,
            @RequestParam("limit") int limit) {
        if (query) {
            return getNewsListByQuery(user, siteId, query, pageNo, limit)
        } else {
            return getNewsListBySiteId(user, siteId, pageNo, limit)
        }

    }

    private Map<String, Integer> getNewsListBySiteId(LoginUser user, String siteId, int pageNo, int limit) {
        def rep = captureSrv.getNewsList(user.userId, siteId, pageNo, limit)

        //根据推送记录，来包装新闻列表在本系统内的推送状态
        List<String> idList = []
        rep.list?.each {
            idList << it.newsId
        }
        def newsPushMap = captureSrv.getNewsPushList(user.userId, user.orgId, idList)?.groupBy { it.newsId }
        rep.list?.collect {
            it.pushStatus = newsPushMap.get(it.newsId) != null
        }

        // todo total和totalPage两个参数都通过其他接口获取
        rep.total = 3001
        rep.totalPage = Math.round(rep.total / limit)

        if (pageNo == 1) {
            if (rep.list) {
                rep.updateTime = rep.list[0].time
            } else {
                rep.updateTime = System.currentTimeMillis()
            }
        }


        return apiResult(rep)
    }

    private Map<String, Integer> getNewsListByQuery(LoginUser user, String siteId, String query, int pageNo, int limit) {
        def paging = captureSrv.queryNewsList(user.userId, siteId, query, pageNo, limit)

        def rep = [list: []]
        paging.list.each {
            rep.list << [
                    newsId: it.newsId,
                    title: it.title,
                    source: it.source,
                    time: it.createTime,
                    reprintCount: it.reprintCount,
                    heat: it.heat
            ]
        }

        //根据推送记录，来包装新闻列表在本系统内的推送状态
        List<String> idList = []
        rep.list?.each {
            idList << it.newsId
        }
        def newsPushMap = captureSrv.getNewsPushList(user.userId, user.orgId, idList)?.groupBy { it.newsId }
        rep.list?.collect {
            it.pushStatus = newsPushMap.get(it.newsId) != null
        }

        // todo total和totalPage两个参数都通过其他接口获取
        rep.total = paging.total
        rep.totalPage = paging.totalPage

        if (pageNo == 1) {
            if (rep.list) {
                rep.updateTime = rep.list[0].time
            } else {
                rep.updateTime = System.currentTimeMillis()
            }
        }

        return apiResult(rep)
    }

    /**
     *
     * 获取站点更新新闻的数目
     *
     * 请求:
     * sid: "会话id"
     *
     * 返回:
     * <pre>
     * {
     *    status: int           // 状态,200为成功
     *    total: int,           // 新闻总数,第一页为1
     * }
     * </pre>
     */
    @RequestMapping(value = "/api/capture/newNewsCount", method = GET)
    @ResponseBody
    public Map getNewsList(
            @CurrentUserId Long userId,
            @RequestParam("siteId") String siteId,
            @RequestParam("updateTime") long updateTime) {
        def total = captureSrv.getNewNewsCount(userId, siteId, new Date(updateTime))
        return apiResult([total: total])
    }

    /*
    * 请求:
    * sid: "会话id"
    * newsId: "新闻ID"
    * limit: int
    *
    * 返回:
    * {
    *    status: int           // 状态,200为成功
    *    list: [{
    *      newsId: "新闻Id"
    *      title: "标题",
    *      source: "新浪",      // 来源
    *      time: long,
    *      heat: int
    *    }, ...]
    * }
    */
    @RequestMapping(value = "/api/capture/relatedNews/{newsId:.+}")
    @ResponseBody
    public Map getRelatedNews(
            @CurrentUserId Long userId,
            @PathVariable("newsId") String newsId,
            @RequestParam("limit") int limit) {
        def newsList = captureSrv.getRelatedNews(newsId)

        limit = limit > newsList.size() ? newsList.size() : limit
        return apiResult([list: newsList.subList(0, limit)]);
    }

    /*
    * 请求:
    * sid: "会话id"
    * limit: int
    *
    * 返回:
    * {
    *    status: int           // 状态,200为成功
    *    list: [{
    *      newsId: "新闻Id"
    *      title: "标题",
    *      source: "新浪",      // 来源
    *      time: long,
    *      heat: int
    *    }, ...]
    * }
    */
    @RequestMapping(value = "/api/capture/focusNews", method = GET)
    @ResponseBody
    public Map focusNews(
            @CurrentUserId Long userId,
            @RequestParam("siteId") String siteId,
            @RequestParam("limit") int limit) {
        def newsList = captureSrv.getFocusNews(userId, siteId)
        limit = limit > newsList.size() ? newsList.size() : limit
        return apiResult([list: newsList.subList(0, limit)]);
    }

    /*
    * 获取特别关注设置
    *
    * 返回:
    * {
    *    status: int           // 状态,200为成功
    *    siteId: "站点ID",
    *    keywords: "关键字,用空格隔开"
    * }
    */
    @RequestMapping(value = "/api/capture/specialFocus", method = GET)
    @ResponseBody
    public Map getSpecialFocus(
            @CurrentUserId Long userId) {
        def site = captureSrv.getSpecialFocus(userId)

        return apiResult([siteId: site ? site.siteId : "", keywords: site ? site.focusKeywords : ""]);
    }

    /*
    *
    * 请求
    *
    * 修改特别关注设置
    *
    * 返回:
    * {
    *    status: int           // 状态,200为成功
    *    siteId: "站点ID",
    *    keywords: "关键字,用空格隔开"
    * }
    */
    @RequestMapping(value = "/api/capture/specialFocus", method = PUT)
    @ResponseBody
    public Map modifySpecialFocus(
            @CurrentUserId Long userId,
            @RequestParam("siteId") String siteId,
            @RequestParam("keywords") String keywords) {
        captureSrv.modifySpecialFocus(userId, siteId, keywords)
        return apiResult();
    }

    /*
    *
    * 请求
    *
    * skipInit: 为true表未跳过
    *
    * 用户欢迎页初始化站点
    *
    * 返回:
    * {
    *    status: int           // 状态,200为成功
    * }
    */
    @RequestMapping(value = "/api/capture/initSites", method = POST)
    @ResponseBody
    public Map modifySpecialFocus(
            @CurrentUserId Long userId,
            @RequestParam("skipInit") boolean skipInit,
            @RequestParam(value="contentType", required=false) String[] contentTypes,
            @RequestParam(value="mediaType", required=false) String[] mediaTypes,
            HttpServletRequest request) {
        request.getParameterMap().each {
            log.debug("key: {}, value: {}", it.key, it.value)
        }
        if (!skipInit) {
            captureSrv.recommandSites(userId, contentTypes, mediaTypes)
        } else {
            captureSrv.recommandDefaultSites(userId)
//            captureSrv.recommandSites(userId, ['财经', '社会'] as String[], ['国家新闻网站', '地方新闻网站'] as String[])
        }

        return apiResult();
    }

    /*
    *
    * 请求
    *
    * 获取特别关注信息
    *
    * 返回:
    * {
    *    status: int           // 状态,200为成功
    * }
    */
    // todo veryveryhigh 这个方法得重写
    @RequestMapping(value = "/api/capture/specialFocusInfo", method = GET)
    @ResponseBody
    public Map modifySpecialFocusInfo(
            @CurrentUserId Long userId) {
        def limit = 5
        def newsList = captureSrv.getSpecialFocusInfo(userId, limit)
        limit = limit > newsList.size() ? newsList.size() : limit
        return apiResult([list: newsList.subList(0, limit)]);
    }


    /*
    *
    * 查询新闻推送数据, 按创建时间倒序排序
    *
    * status: 推送状态, 0, 全部, 1, 未推送  2, 已推送
    *
    *
    * 返回:
    *  新闻列表
    String title
    String publishTime
    String source
    String author
    String keyword
    String url
    String newsId
    String content
    String contentAbstract
    */
    @RequestMapping(value = "/api/capture/newsPush", method = GET)
    @ResponseBody
    public Map getNewsPushList(
            @CurrentUser LoginUser user,
            @RequestParam(value = "status", required = false, defaultValue = "0") int status,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit
    ) {
        def paging = captureSrv.getPagingNewPushList(user.userId, user.orgId, status, pageNo, limit)
        def rep = [:]
        rep.total       = paging.total
        rep.totalPage   = paging.totalPage
        rep.list        = []
        paging.list.each {
            rep.list << [
                    newsId      : it.newsId,
                    title       : it.title,
                    source      : it.source,
                    author      : it.author,
                    keyword     : it.keyword,
                    url         : it.url,
                    status      : it.status,
                    publishTime : it.publishTime,
                    createTime  : it.createTime?.time
            ]
        }

        return apiResult(rep)
    }

    /*
    *
    * 创建推送
    *
    * newsId: 新闻ID
    *
    *
    * 返回:
    */
    @RequestMapping(value = "/api/capture/newsPush", method = POST)
    @ResponseBody
    public Map createNewsPush(
            @CurrentUser LoginUser user,
            @RequestParam("newsId") String newsId,
            @RequestParam(value = "siteId", required = false, defaultValue = "") String siteId
    ) {
        captureSrv.createNewsPush(user.userId, user.orgId, newsId, siteId)
        return apiResult()
    }

    @RequestMapping(value = "/api/capture/isNewsPushed", method = GET)
    @ResponseBody
    public Map isNewsPushed(
            @CurrentUser LoginUser user,
            @RequestParam("newsId") String newsId
    ) {
        def result = captureSrv.getNewsPush(user.userId, user.orgId, newsId)
        return apiResult([isNewsPushed:(result!=null && result.status==1)?true:false])
    }

    @RequestMapping(value = "/api/capture/newsShare", method = GET)
    @ResponseBody
    public Map getNewsShareList(
            @CurrentUser LoginUser user,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit
    ) {
        def paging = captureSrv.getPagingNewsShareList(user.userId, user.orgId, pageNo, limit)
        def rep = [:]
        rep.total       = paging.total
        rep.totalPage   = paging.totalPage
        rep.list        = []
        paging.list.each {
            rep.list << [
                    newsId      : it.newsId,
                    title       : it.news.title,
                    source      : it.news.source,
                    author      : it.news.author,
                    url         : it.news.url,
                    publishTime : it.news.publishTime,
                    shareChannel: it.shareChannel,
                    shareContent: it.shareContent,
                    createTime  : it.createTime?.time
            ]
        }

        return apiResult(rep)
    }

    @Deprecated
    @RequestMapping(value = "/api/capture/newsDownload/{newsId}", method = GET)
    @ResponseBody
    public Map getNewsDownloadList(
            @CurrentUser LoginUser user,
            @PathVariable String newsId,
            HttpServletResponse response
    ) {

        def wordRes = captureSrv.getNewsWord(newsId,user);
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
    @RequestMapping(value = "/api/capture/newsDownload", method = GET)
    @ResponseBody
    public Map getNewsDownloadLog(
            @CurrentUser LoginUser user,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit
    ) {
        def paging = captureSrv.getPagingNewsDownloadList(user.userId, user.orgId, pageNo, limit)
        def rep = [:]
        rep.total       = paging.total
        rep.totalPage   = paging.totalPage
        rep.list        = []
        paging.list.each {
            rep.list << [
                    newsId      : it.newsId,
                    title       : it.news.title,
                    source      : it.news.source,
                    author      : it.news.author,
                    url         : it.news.url,
                    publishTime : it.news.publishTime,
                    createTime  : it.createTime?.time
            ]
        }

        return apiResult(rep)
    }

    /*
        在测试环境中暴露出来
     */
    @RequestMapping(value = "/api/capture/solr", method = GET)
    @ResponseBody
    public Map getNewsDownloadLog(
            @RequestParam(value = "q") String q,
            @RequestParam(value = "start", required = false, defaultValue = "1") int start,
            @RequestParam(value = "rows", required = false, defaultValue = "10") int rows,
            @RequestParam(value = "fl", required = false, defaultValue = "") String fl,
            @RequestParam(value = "sort", required = false, defaultValue = "kvDkTime asc") String sort
    ) {
        if (env == Const.ENV_ONLINE) {
            return apiResult(SC_BAD_REQUEST, '不能在线上环境使用')
        }

        def rep = captureApi.queryPagingNew(q, start, rows, fl, sort)

        return apiResult(rep)
    }


    @RequestMapping(value = "/api/capture/focus", method = POST)
    @ResponseBody
    public Map addFocus(
            @CurrentUserId Long userId,
            @RequestParam(value = "focusName") String focusName,   // 主题名称
            @RequestParam(value = "mustKeywords", required = false, defaultValue = "") String mustKeywords,  // 地域词
            @RequestParam(value = "shouldKeywords", required = false, defaultValue = "") String shouldKeywords, // 主题词
            @RequestParam(value = "eventKeywords", required = false, defaultValue = "") String eventKeywords, // 事件词
            @RequestParam(value = "notKeywords", required = false, defaultValue = "") String notKeywords,  // 歧义词
            @RequestParam(value = "expression", required = false, defaultValue = "") String expression // 表达式

    ) {

        def count = captureSrv.getFocusCount(userId)
        if (count > 20) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, '最多只能创建' + 20 + '个关注');
        }

        if (!mustKeywords && !shouldKeywords && !eventKeywords && !notKeywords && !expression) {
            return apiResult(SC_BAD_REQUEST, '没有输入任何关键字');
        }
        if (!expression){
            def set = new HashSet()
            mustKeywords.split(/\s+/).each {
                set << it
            }
            shouldKeywords.split(/\s+/).each {
                set << it
            }
            eventKeywords  .split(/\s+/).each {
                set << it
            }
            set.remove("")
            if(set.size() < 3){
                return apiResult(SC_BAD_REQUEST, '至少输入3个关键字');
            }
        }
        def focusId = captureSrv.addFocus(new Focus(
                focusName: focusName,
                userId: userId,
                mustKeywords: mustKeywords,
                shouldKeywords: shouldKeywords,
                eventKeywords: eventKeywords,
                notKeywords: notKeywords,
                expression: expression,
        ))

        return apiResult([focusId: focusId]);
    }

    @RequestMapping(value = "/api/capture/focus/{focusId:.*}", method = PUT)
    @ResponseBody
    public Map modifyFocus(
            @CurrentUserId Long userId,
            @RequestParam(value = "focusName") String focusName,   // 主题名称
            @RequestParam(value = "mustKeywords", required = false, defaultValue = "") String mustKeywords,  // 地域词
            @RequestParam(value = "shouldKeywords", required = false, defaultValue = "") String shouldKeywords, // 主题词
            @RequestParam(value = "eventKeywords", required = false, defaultValue = "") String eventKeywords, // 事件词
            @RequestParam(value = "notKeywords", required = false, defaultValue = "") String notKeywords,  // 歧义词
            @RequestParam(value = "expression", required = false, defaultValue = "") String expression, // 表达式
            @PathVariable(value = "focusId") String focusId
    ) {
        if (!mustKeywords && !shouldKeywords && !eventKeywords && !notKeywords && !expression) {
            return apiResult(SC_BAD_REQUEST, '没有输入任何关键字');
        }
        if (!expression){
            def set = new HashSet()
            mustKeywords.split(/\s+/).each {
                set << it
            }
            shouldKeywords.split(/\s+/).each {
                set << it
            }
            eventKeywords  .split(/\s+/).each {
                set << it
            }
            set.remove("")
            if(set.size() < 3){
                return apiResult(SC_BAD_REQUEST, '至少输入3个关键字');
            }
        }
        captureSrv.modifyFocus(new Focus(
                focusId: focusId,
                focusName: focusName,
                userId: userId,
                mustKeywords: mustKeywords,
                shouldKeywords: shouldKeywords,
                eventKeywords: eventKeywords,
                notKeywords: notKeywords,
                expression: expression,
        ))

        return apiResult();
    }



    @RequestMapping(value = "/api/capture/focus/{focusId:.*}")
    @ResponseBody
    public Map getFocus(
            @CurrentUserId Long userId,
            @PathVariable(value = "focusId") String focusId
    ) {
        def focus = captureSrv.getFocus(userId, focusId)

        def rep = focus ? [
                focusId: focus.focusId,
                focusName: focus.focusName,
                mustKeywords: focus.mustKeywords,
                shouldKeywords: focus.shouldKeywords,
                eventKeywords: focus.eventKeywords,
                notKeywords: focus.notKeywords,
                expression: focus.expression,
                createTime: focus.createTime,
            ] : [:]

        return apiResult(rep);
    }

    @RequestMapping(value = "/api/capture/focus/{focusId:.*}", method = DELETE)
    @ResponseBody
    public Map removeFocus(
            @CurrentUserId Long userId,
            @PathVariable(value = "focusId") String focusId
    ) {
        captureSrv.removeFocus(userId, focusId)

        return apiResult();
    }

    @RequestMapping(value = "/api/capture/focus")
    @ResponseBody
    public Map getFocusList(
            @CurrentUserId long userId) {
        def list = captureSrv.getFocusList(userId)

        def rep = [list: []]
        list.each { focus ->
            rep.list << [
                    focusId: focus.focusId,
                    focusName: focus.focusName,
                    newsCount: 0,
                    createTime: focus.createTime,
                    newsCount : focus.captureCount,
            ]
        }

        return apiResult(rep)
    }

    @RequestMapping(value = "/api/capture/focus/{focusId:.*}/news")
    @ResponseBody
    // todo vvv hith 处理站点类型
    public Map getFocusNewsList(
            @CurrentUser LoginUser user,
            @PathVariable(value = "focusId") String focusId,
            @RequestParam(value = "keywords", required = false, defaultValue = "") String keywords,
            @RequestParam(value = "siteType", required = false, defaultValue = "0") int siteType,
            @RequestParam("pageNo") int pageNo,
            @RequestParam("limit") int limit
    ) {

        def paging = captureSrv.queryPagingFocusNewsList(user.userId, focusId, keywords, siteType, pageNo, limit)

        def rep = [list: [], total: paging.total, totalPage: paging.totalPage]
        paging.list.each {
            rep.list << toNews(it)
        }

        //根据推送记录，来包装新闻列表在本系统内的推送状态
        List<String> idList = []
        rep.list?.each {
            idList << it.newsId
        }
        def newsPushMap = captureSrv.getNewsPushList(user.userId, user.orgId, idList)?.groupBy { it.newsId }
        rep.list?.collect {
            it.pushStatus = newsPushMap.get(it.newsId) != null
        }

        if (pageNo == 1) {
            if (rep.list) {
                rep.updateTime = rep.list[0].time
            } else {
                rep.updateTime = System.currentTimeMillis()
            }
        }


        return apiResult(rep)
    }

    /*
        采集首页统计信息
     */
    @RequestMapping(value = "/api/capture/summary2")
    public Map getSummary2(
            @CurrentUserId long userId
    ) {

        def rep = [
            summaryToday: [
                captureCount: 100000,
                pushCount: 2000,
                shareCount: 1000
            ],
            trendWeekly: [[
                            captureCount: 100000,
                            pushCount: 2000,
                            shareCount: 1000,
                            date: 12324324234
                    ],[
                          captureCount: 100000,
                          pushCount: 2000,
                          shareCount: 1000,
                          date: 12324324234
                    ],

            ],
            topN: [
                professionalSite: [[
                        name: '网站1',
                        captureCount: 200,
                        pushCount: 100
                    ], [
                        name: '网站2',
                        captureCount: 200,
                        pushCount: 100
                    ], [
                        name: '网站3',
                        captureCount: 200,
                        pushCount: 100
                    ]
                ],
                normalSite: [
                    [
                            name: '网站1',
                            captureCount: 200,
                            pushCount: 100
                    ], [
                            name: '网站2',
                            captureCount: 200,
                            pushCount: 100
                    ], [
                            name: '网站3',
                            captureCount: 200,
                            pushCount: 100
                    ]

                ],
                weixinSite: [
                    [
                            name: '网站1',
                            captureCount: 200,
                            pushCount: 100
                    ], [
                            name: '网站2',
                            captureCount: 200,
                            pushCount: 100
                    ], [
                            name: '网站3',
                            captureCount: 200,
                            pushCount: 100
                    ]

                ]
            ]
        ]

        return apiResult(rep)
    }

    @RequestMapping(value = "/api/capture/summary")
    public Map getSummary(
            @CurrentUserId long userId
    ) {
        def rep =[summaryToday: [:],trendWeekly:[:], topN:[:]]

//        def trendWeekly = captureSrv.getTrendWeekly(userId)
        def trendWeekly = captureSrv.getSummaryWeeklyTemp(userId)
        rep.trendWeekly = trendWeekly
        rep.summaryToday = trendWeekly.get(6)

        //媒体网站
        def siteType = 1
        def normalSiteTopN = captureSrv.getSiteTopN(userId,siteType)
        rep.topN.normalSite = normalSiteTopN
        //微信网站
        siteType = 2
        def weixinSiteTopN = captureSrv.getSiteTopN(userId,siteType)
        rep.topN.weixinSite = weixinSiteTopN
        //专业网站
        siteType = 3
        def professionalSiteTopN = captureSrv.getSiteTopN(userId,siteType)
        rep.topN.professionalSite = professionalSiteTopN

        return apiResult(rep)
    }

    /*
        采集首页焦点新闻
     */
    @RequestMapping(value = "/api/capture/summary/focusNews")
    public Map getFocusNewsList(
            @CurrentUserId long userId,
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit
    ) {
        def newsList = captureSrv.getFocusNewsList(userId, limit)
        return apiResult([list: toNewsList(newsList)])
    }

    /*
        采集首页热门微信
     */
    @RequestMapping(value = "/api/capture/summary/hotWeixin")
    public Map getHotWeixinList(
            @CurrentUserId long userId,
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit
    ) {
        def newsList = captureSrv.getHotWeixinList(userId, limit)
        return apiResult([list: toNewsList(newsList)])
    }

    /*
        采集首页最新采集
     */
    @RequestMapping(value = "/api/capture/summary/latestNews")
    public Map getLatestNews(
            @CurrentUserId long userId,
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit
    ) {
        def newsList = captureSrv.getLatestNews(userId, limit)
        return apiResult([list: toNewsList(newsList)])
    }

    /**
     * 生成json格式的新闻列表,去除一些不应该显示的字段,如simhash等
     */
    private static List toNewsList(List<News> newsList) {
        def list = []
        newsList.each {
            list << toNews(it)
        }
        return list
    }

    private static Map toNews(News news) {
        return [
            newsId: news.newsId,
            title: news.title,
            contentAbstract: news.contentAbstract,
            source: news.source,
            author: news.author,
            url: news.url,
            time: news.createTime,
            newsType: news.newsType,
            reprintCount: news.reprintCount,
            heat: news.heat
        ]
    }


    /*
        采集筛选
     */
    @RequestMapping(value = "/api/capture/news/search")
    @ResponseBody
    // todo vvv hith 处理站点类型
    public Map queryNewsList(
            @CurrentUser LoginUser user,
            @RequestParam(value = "keyWords", required = false, defaultValue = "") String keywords,
            @RequestParam(value = "siteType", required = false, defaultValue = "") String siteType,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "limit", required = false, defaultValue = "50") int limit,
            @RequestParam(value = "heat", required = false, defaultValue = "") String heat,
            @RequestParam(value = "time", required = false, defaultValue = "") String time,
            @RequestParam(value = "startTime", required = false, defaultValue = "") String startTime,
            @RequestParam(value = "endTime", required = false, defaultValue = "") String endTime,
            @RequestParam(value = "classification", required = false, defaultValue = "") String classification,
            @RequestParam(value = "area", required = false, defaultValue = "") String area,
            @RequestParam(value = "orientation", required = false, defaultValue = "") String orientation,
            @RequestParam(value = "order", required = false, defaultValue = "") String order
    ) {
        def siteTypes = []
        siteType.split(/\s*,\s*/).each {
            if (it)
                siteTypes << Integer.valueOf(it)
        }
        def keyWords = []
        keywords.split(/\s*,\s*/).each {
            if (it)
                keyWords << (it).toString()
        }

        log.debug('siteTypes: {}', siteTypes)
        //校验参数
        if(siteTypes.size() == 0){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "请选择站点类型"])
        }
        Integer heatNum = 0
        if(!(heat.equals(""))){
            try {
                heatNum = Integer.parseInt(heat)
            }catch (Exception e){
                return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "热度格式不正确"])
            }
        }
        Integer timeNum = 0
        String startTime4Solr = "19700101000000"
        def currTime = new Date();
        SimpleDateFormat simpleDateFormat4Solr = new SimpleDateFormat("yyyyMMddHHmmss");
        String endTime4Solr = simpleDateFormat4Solr.format(currTime)
        if(!(time.equals(""))){
            try {
                timeNum = Integer.parseInt(time)
            }catch (Exception e){
                return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "时间格式不正确"])
            }
            if(timeNum == -1){
                if(startTime.equals("") && endTime.equals("")){
                    return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "请填写开始时间或结束时间"])
                }else {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if(!(startTime.equals(""))){
                        try {
                            startTime4Solr = simpleDateFormat4Solr.format(simpleDateFormat.parse(startTime))
                        }catch (Exception e){
                            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "开始时间格式不正确"])
                        }
                    }
                    if(!(endTime.equals(""))){
                        try {
                            endTime4Solr = simpleDateFormat4Solr.format(simpleDateFormat.parse(endTime))
                        }catch (Exception e){
                            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "结束时间格式不正确"])
                        }
                    }
                }
                if(startTime4Solr > endTime4Solr ){
                    return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "开始时间不能大于结束时间"])
                }
            }
            if(timeNum > 0){
                startTime4Solr = simpleDateFormat4Solr.format(new Date(currTime.getTime() - timeNum * 1000 * 60 * 60))
            }
        }
        Integer classificationNum = 0
        if(!(classification.equals(""))){
            try {
                classificationNum = Integer.parseInt(classification)
            }catch (Exception e){
                return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "分类信息格式不正确"])
            }
        }
        Integer areaNum = 0
        if(!(area.equals(""))){
            try {
                areaNum = Integer.parseInt(area)
            }catch (Exception e){
                return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "区域格式不正确"])
            }
        }
        Integer orientationNum = 0
        if(!(orientation.equals(""))){
            try {
                orientationNum = Integer.parseInt(orientation)
            }catch (Exception e){
                return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "倾向格式不正确"])
            }
        }
        Integer orderNum = 1
        if(!(order.equals(""))){
            try {
                orderNum = Integer.parseInt(order)
            }catch (Exception e){
                return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "排序格式不正确"])
            }
            if(orderNum != 2){
                orderNum = 1
            }
        }

        def paging = captureSrv.queryPagingNewsList(user.userId, keyWords, siteTypes, pageNo, limit,
                heatNum, timeNum, startTime4Solr, endTime4Solr, classificationNum,
                areaNum, orientationNum, orderNum
        )

        def rep = [total: paging.total, totalPage: paging.totalPage, list: []]
        def titles = [];
        List<String> idList = []
        paging.list.each {
            if(!(titles.contains(it.title))){
                titles<<it.title
                rep.list << toNews(it)
                idList << it.newsId
            }
        }

        // todo vv high 把这部分代码提取出来
        //根据推送记录，来包装新闻列表在本系统内的推送状态
        def newsPushMap = captureSrv.getNewsPushList(user.userId, user.orgId, idList)?.groupBy { it.newsId }
        rep.list?.collect {
            it.pushStatus = newsPushMap.get(it.newsId) != null
        }

        if (pageNo == 1) {
            if (rep.list) {
                rep.updateTime = rep.list[0].time
            } else {
                rep.updateTime = System.currentTimeMillis()
            }
        }

        return apiResult(rep)
    }

    /**
    * 用户当前的所有的筛选偏好
    * @param user
    * @return
     */
    @RequestMapping(value = "/api/capture/captureFilters",method = GET)
    @ResponseBody
    public Map getCaptureFilters(
            @CurrentUser LoginUser user
    ) {
        //拿到用户的captureFilters
        List captureFilters = captureSrv.getCaptureFiltersByUserId(user.userId)
        //如果没有拿到captureFilters，初始化it
        if(captureFilters.size() == 0){
            captureFilters = captureSrv.initUserCaptureFilters(user.userId)
        }
        //获取分类信息
        def classifications = captureSrv.getCaptureFilterClassifications()
        //获取地区信息
        def areas = captureSrv.getCaptureFilterAreas()
        def detail = [
                captureFilters  :captureFilters,
                classifications :classifications,
                areas           :areas,
        ]
        return apiResult(detail:detail)
    }

    /**
     * 保存筛选偏好设置
     * @param user
     * @return
     */
    @RequestMapping(value = "/api/capture/captureFilter",method = PUT)
    @ResponseBody
    public Map modifyCaptureFilter(
            @CurrentUser LoginUser user,
            @RequestParam(value = "id", required = true) String id,
            @RequestParam(value = "name", required = false, defaultValue = "") String name,
            @RequestParam(value = "mediaType", required = false, defaultValue = "") String mediaType,
            @RequestParam(value = "keyWords", required = false, defaultValue = "") String keyWords,
            @RequestParam(value = "heat", required = false, defaultValue = "") String heat,
            @RequestParam(value = "time", required = false, defaultValue = "") String time,
            @RequestParam(value = "startTime", required = false, defaultValue = "") String startTime,
            @RequestParam(value = "endTime", required = false, defaultValue = "") String endTime,
            @RequestParam(value = "classification", required = false, defaultValue = "") String classification,
            @RequestParam(value = "area", required = false, defaultValue = "") String area,
            @RequestParam(value = "orientation", required = false, defaultValue = "") String orientation,
            @RequestParam(value = "order", required = false, defaultValue = "") String order
    ) {
        //
        def result = captureSrv.modifyCaptureFilter(user.userId, id.trim(), name.trim(), mediaType.trim(), keyWords.trim(),heat.trim(),
                time.trim(), startTime.trim(), endTime.trim(), classification.trim(), area.trim(), orientation.trim(),
                order.trim())
        return result
    }

}
