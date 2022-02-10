package com.istar.mediabroken.api.capture

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.api.CheckPrivilege
import com.istar.mediabroken.api.CurrentUser
import com.istar.mediabroken.api.DashboardEnum
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.account.Privilege
import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.service.account.AccountCustomSettingService
import com.istar.mediabroken.service.capture.DashboardService
import com.istar.mediabroken.service.capture.NewsService
import com.istar.mediabroken.service.capture.SiteService
import com.istar.mediabroken.utils.DateUitl
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

import java.text.SimpleDateFormat

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR

/**
 * Author : YCSnail
 * Date   : 2017-12-07
 * Email  : liyancai1986@163.com
 */
@RestController
@Slf4j
@RequestMapping(value = '/api/capture/dashboard')
class DashboardApiController {

    @Autowired
    DashboardService dashboardService
    @Autowired
    SiteService siteService
    @Autowired
    AccountCustomSettingService accountCustomSettingService
    @Autowired
    NewsService newsService

    /**
     * 头条区新闻 首页巡查
     * @param user
     * @param siteIds
     * @param pageSize
     * @param pageNo
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.DASHBOARD_VIEW])
    @RequestMapping(value = '/highlight/news', method = RequestMethod.GET)
    Object getHighlightNews(
            @CurrentUser LoginUser user,
            @RequestParam(value = "siteIds", required = false, defaultValue = "") String siteIds,
            @RequestParam(value = "pageSize", required = false, defaultValue = "12") int pageSize,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
    ) {
        if ((pageSize * pageNo) > 50000) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "请求的数据太多，失败")
        }
        //首页巡查模块暂取7大站点数据，先将用户设置站点替换掉
//        List sites = siteService.getUserSelectedHeadLineSite(user.userId, siteIds)
//        if (!sites) {
//            return apiResult([status: HttpStatus.SC_BAD_REQUEST, msg: '没有配置对应的站点'])
//        }
        List sites = []
        def result = dashboardService.getUserHighlightNews(user.userId, sites, pageSize, pageNo, null)
        return result
    }

    /**
     * 头条区新闻 首页巡查 更多新闻
     * @param user
     * @param pageSize
     * @param pageNo
     * @return
     */
    @RequestMapping(value = '/highlight/moreNews', method = RequestMethod.GET)
    Object getHighlightMoreNews(
            @CurrentUser LoginUser user,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "queryId", required = false, defaultValue = "") String queryId
    ) {
        if ((pageSize * pageNo) > 50000) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "请求的数据太多，失败")
        }
        //首页巡查模块暂取7大站点数据，先将用户设置站点替换掉
//        List sites = siteService.getUserAllHeadLineSite(user.userId)
//        if (!sites) {
//            return apiResult([status: HttpStatus.SC_BAD_REQUEST, msg: '没有配置对应的站点'])
//        }
        List sites = []
        def result = dashboardService.getUserHighlightNews(user.userId, sites, pageSize, pageNo, queryId)
        return result
    }

    /**
     * 热点新闻
     * @param user
     * @param siteIds
     * @return
     */
    @RequestMapping(value = '/hot/news', method = RequestMethod.POST)
    Object getHotNews(
            @CurrentUser LoginUser user,
            @RequestBody String data
    ) {
        JSONObject dataJson = JSONObject.parseObject(data);
        String siteIds = dataJson.get("siteIds").toString();
        if (!siteIds) {
            return apiResult([newsList: [], imageNewsList: []])
        }
        def result = dashboardService.getNewsAndImgNews(user.userId, siteIds, 1)
        return apiResult(result)
    }

    /**
     * 全网监控
     * @param user
     * @param subjectIds
     * @return
     */
    @RequestMapping(value = '/subject/news', method = RequestMethod.GET)
    Object getSubjectNews(
            @CurrentUser LoginUser user,
            @RequestParam(value = "subjectIds", required = false, defaultValue = "") String subjectIds
    ) {
        if (!subjectIds) {
            return apiResult([newsList: [], imageNewsList: []])
        }
        def result = dashboardService.getSubjectNewsAndImgNews(user.userId, subjectIds)
        return apiResult(result)
    }

    @RequestMapping(value = '/subject/news2', method = RequestMethod.GET)
    Object getSubjectNews2(
            @CurrentUser LoginUser user,
            @RequestParam(value = "subjectIds", required = false, defaultValue = "") String subjectIds
    ) {
        if (!subjectIds) {
            return apiResult([newsList: [], imageNewsList: []])
        }
        def result = dashboardService.getSubjectNewsAndImgNewsOneByOne(user.userId, subjectIds)
        return apiResult(result)
    }

    /**
     * 最新微博
     * @return
     */
    @RequestMapping(value = '/weibo/news', method = RequestMethod.POST)
    Object getWeiboNews(
            @CurrentUser LoginUser user,
            @RequestBody String data
    ) {
        JSONObject dataJson = JSONObject.parseObject(data);
        String siteIds = dataJson.get("siteIds").toString();
        if (!siteIds) {
            return apiResult([newsList: []])
        }
        def result = dashboardService.getWeiboNews(user.userId, siteIds, Site.SITE_TYPE_WEIBO)
        return apiResult(result)
    }

    /**
     * 热门微信
     * @return
     */
    @RequestMapping(value = '/wechat/news', method = RequestMethod.POST)
    Object getWechatNews(
            @CurrentUser LoginUser user,
            @RequestBody String data
    ) {
        JSONObject dataJson = JSONObject.parseObject(data);
        String siteIds = dataJson.get("siteIds").toString();
        if (!siteIds) {
            return apiResult([newsList: [], imageNewsList: []])
        }
        def result = dashboardService.getNewsAndImgNews(user.userId, siteIds, 2)
        return apiResult(result)
    }

    /**
     * 传播趋势上升的新闻首页(使用中)
     * @return
     */
    @RequestMapping(value = '/rise/news', method = RequestMethod.POST)
    Object getRiseNews(
            @CurrentUser LoginUser user,
            @RequestBody String data
    ) {
        JSONObject dataJson = JSONObject.parseObject(data);
        String siteIds = dataJson.get("siteIds").toString();
//        String startTime = dataJson.get("startTime")
//        String endTime = dataJson.get("endTime")
        int pageSize = dataJson.get("pageSize")
        int pageNo = dataJson.get("pageNo")
        if (!siteIds) {
            return apiResult([newsList: []])
        }
        //判断时间格式是否合法
//        def sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
//        def startDate
//        def endDate
//        try {
//            if (startTime) {
//                startDate = sdf.parse(startTime)
//            }
//            if (endTime) {
//                endDate = sdf.parse(endTime)
//            }
//        } catch (Exception e) {
//            return apiResult(SC_INTERNAL_SERVER_ERROR, "时间格式不正确")
//        }


        if ((pageSize * pageNo) > 100) {
            return apiResult(list: [])
        }
        def selectedSite = siteService.getUserSelectSite(user.userId, siteIds)
        if (!selectedSite) {
            return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: '没有配置对应的站点'])
        }

        def startDate
        def endDate = new Date()
        startDate = DateUitl.addMins(endDate, -30)
        def result = dashboardService.getRiseNews(user.userId, selectedSite, 0, startDate, endDate, 0, false, 1, "", pageSize, "")
        if (result.newsList.size() <= 0){
            startDate = DateUitl.addMins(endDate, -60)
            result = dashboardService.getRiseNews(user.userId, selectedSite, 0, startDate, endDate, 0, false, 1, "", pageSize, "")
        }
        if (result.newsList.size() <= 0){
            startDate = DateUitl.addHour(endDate, -24)
            result = dashboardService.getRiseNews(user.userId, selectedSite, 0, startDate, endDate, 0, false, 1, "", pageSize, "")
        }

        if (result.status == 200) {
            def list = []

            def newsIds = []
            result.newsList.each {
                newsIds.add(it.id)
            }
            List col = newsService.getExistNewsOperation(user.userId, newsIds, 3)
            List push = newsService.getExistNewsOperation(user.userId, newsIds, 1)
            result.newsList ? result.newsList.each {
                def res = [
                        id                  : it.id,
                        title               : it.title,
                        contentAbstract     : it.contentAbstract,
                        simhash             : it.simhash,
                        reprintCount        : it.reprintCount,
                        firstPublishTime    : DateUitl.convertFormatDate(DateUitl.convertEsDate(it.firstPublishTime), "MM-dd HH:mm"),
                        firstPublishSiteName: it.firstPublishSiteName,
                        createTime          : DateUitl.convertFormatDate(DateUitl.convertEsDate(it.createTime), "MM-dd HH:mm"),//为数据更新时间
                        publishTime         : DateUitl.convertEsDate(it.publishTime),
                        siteName            : it.siteName,
                        riseRate            : Math.round(it.riseRate * 100) + "%",
                        isCollection        : col.contains(it.id),
                        isPush              : push.contains(it.id)
                ]
                list << res
            } : []
            result.newsList = list
        }
        return result
    }
    /**
     * 传播趋势上升的新闻更多(使用中)
     * @return
     */
    @RequestMapping(value = '/rise/moreNews', method = RequestMethod.GET)
    Object getMoreRiseNews(
            @CurrentUser LoginUser user,
            @RequestParam(value = "hot", required = false, defaultValue = "0") int hot,//热度 0全部 1 低 2 中 3 高
            @RequestParam(value = "orientation", required = false, defaultValue = "0") int orientation, //倾向性 0：全部 1：正向 2：负向 3：中性 4：有争议
            @RequestParam(value = "hasPic", required = false, defaultValue = "false") boolean hasPic,//类别
            @RequestParam(value = "order", required = false, defaultValue = "1") int order, //1 时间降序 2 热度降序
            @RequestParam(value = "keyWords", required = false, defaultValue = "") String keyWords, // 搜索关键词
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "queryId", required = false, defaultValue = "") String queryId

    ) {
        //判断时间格式是否合法
        def sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
        long time = 0.5 * 60 * 60 * 1000
        def startTime = new Date(new Date().getTime() - time).format('yyyy-MM-dd HH:mm:ss')
        def endTime = new Date().format('yyyy-MM-dd HH:mm:ss')
        def startDate
        def endDate
        try {
            if (startTime) {
                startDate = sdf.parse(startTime)
            }
            if (endTime) {
                endDate = sdf.parse(endTime)
            }
        } catch (Exception e) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "时间格式不正确")
        }

        if ((pageSize * pageNo) > 50000) {
            return apiResult([status: HttpStatus.SC_OK, msg: '没有更多数据'])
        }
        //用户勾选站点
        def riseRateMonitor = accountCustomSettingService.getOneAccountCustomSettingByKey(user.userId, DashboardEnum.riseRateMonitor.key)
        def setting = accountCustomSettingService.getAccountCustomNewsSetting(user.userId, riseRateMonitor, Site.SITE_TYPE_WEBSITE)
        def siteList = []
        setting.each { elem ->
            if (elem.active) {
                siteList.add(elem.siteId)
            }
        }
        String siteIds = String.join(",", siteList)
        def selectedSite = siteService.getUserSelectSite(user.userId, siteIds)
        if (!selectedSite) {
            return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: '没有配置对应的站点'])
        }
        def result = [status: HttpStatus.SC_OK, newsList: [], msg: 'queryId格式不合法', queryId: queryId]
        result = dashboardService.getRiseNews(user.userId, selectedSite, hot, startDate, endDate, orientation, hasPic, order, keyWords, pageSize, queryId)
        if (!result.newsList) {
            endTime = new Date().format('yyyy-MM-dd HH:mm:ss')
            startTime = "1970-01-01 00:00:00"//new Date(0).format('yyyy-MM-dd HH:mm:ss')
            try {
                if (startTime) {
                    startDate = sdf.parse(startTime)
                }
                if (endTime) {
                    endDate = sdf.parse(endTime)
                }
            } catch (Exception e) {
                return apiResult(SC_INTERNAL_SERVER_ERROR, "时间格式不正确")
            }
            result = dashboardService.getRiseNews(user.userId, selectedSite, hot, startDate, endDate, orientation, hasPic, order, keyWords, pageSize, queryId)
        }
        if (result.status == 200) {
            def list = []

            def newsIds = []
            result.newsList.each {
                newsIds.add(it.id)
            }
            List col = newsService.getExistNewsOperation(user.userId, newsIds, 3)
            List push = newsService.getExistNewsOperation(user.userId, newsIds, 1)
            result.newsList ? result.newsList.each {
                def res = [
                        id                  : it.id,
                        title               : it.title,
                        contentAbstract     : it.contentAbstract,
                        simhash             : it.simhash,
                        reprintCount        : it.reprintCount,
                        firstPublishTime    : DateUitl.convertFormatDate(DateUitl.convertEsDate(it.firstPublishTime), "MM-dd HH:mm"),
                        firstPublishSiteName: it.firstPublishSiteName,
                        createTime          : DateUitl.convertFormatDate(DateUitl.convertEsDate(it.createTime), "MM-dd HH:mm"),//为数据更新时间
                        siteName            : it.siteName,
                        publishTime         : it.publishTime,
                        riseRate            : Math.round(it.riseRate * 100) + "%",
                        isCollection        : col.contains(it.id),
                        isPush              : push.contains(it.id)
                ]
                list << res
            } : []
            result.newsList = list
        }
        return result
    }

    /**
     * 转载媒体趋势上升的新闻首页
     * @return
     */
    @RequestMapping(value = '/reprint/news', method = RequestMethod.POST)
    Object getReprintNews(
            @CurrentUser LoginUser user,
            @RequestBody String data
    ) {
        JSONObject dataJson = JSONObject.parseObject(data);
        String siteIds = dataJson.get("siteIds").toString();
        String startTime = dataJson.get("startTime")
        String endTime = dataJson.get("endTime")
        int pageSize = dataJson.get("pageSize")
        int pageNo = dataJson.get("pageNo")
        if (!siteIds) {
            return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: '暂无数据'])
        }
        //判断时间格式是否合法
        def sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
        def startDate
        def endDate
        try {
            if (startTime) {
                startDate = sdf.parse(startTime)
            }
            if (endTime) {
                endDate = sdf.parse(endTime)
            }
        } catch (Exception e) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "时间格式不正确")
        }

        if ((pageSize * pageNo) > 50000) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "请求的数据太多，失败")
        }

        def sitesList = siteService.getUserSelectSite(user.userId, siteIds)
        if (!sitesList) {
            return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: '没有配置对应的站点'])
        }
        def result = dashboardService.getReprintNews(user.userId, sitesList, 0, startDate, endDate, 0, false, 1, "", pageSize, "")
        if (result.status == 200) {
            def list = []

            def newsIds = []
            result.newsList.each {
                newsIds.add(it.id)
            }
            List col = newsService.getExistNewsOperation(user.userId, newsIds, 3)
            List push = newsService.getExistNewsOperation(user.userId, newsIds, 1)
            result.newsList ? result.newsList.each {
                def res = [
                        id                  : it.id,
                        title               : it.title,
                        contentAbstract     : it.contentAbstract,
                        simhash             : it.simhash,
                        reprintCount        : it.reprintCount,
                        firstPublishTime    : DateUitl.convertFormatDate(DateUitl.convertEsDate(it.firstPublishTime), "MM-dd HH:mm"),
                        firstPublishSiteName: it.firstPublishSiteName,
                        createTime          : DateUitl.convertFormatDate(DateUitl.convertEsDate(it.createTime), "MM-dd HH:mm"),//为数据更新时间
                        siteName            : it.siteName,
                        poster              : it.poster,
                        publishTime         : DateUitl.convertFormatDate(DateUitl.convertEsDate(it.publishTime), "MM-dd HH:mm"),
                        isCollection        : col.contains(it.id),
                        isPush              : push.contains(it.id)
                ]
                list << res
            } : [status: HttpStatus.SC_OK, newsList: [], msg: '未检测到新闻热点或所选站点新闻未达热点标准，建议添加更多站点']
            result.newsList = list
        }
        return result
    }
    /**
     * 转载媒体趋势上升的更多列表
     * @return
     */
    @RequestMapping(value = '/reprint/moreNews', method = RequestMethod.GET)
    Object getMoreReprintNews(
            @CurrentUser LoginUser user,
            @RequestParam(value = "hot", required = false, defaultValue = "0") int hot,//热度 0全部 1 低 2 中 3 高
            @RequestParam(value = "startTime", required = false, defaultValue = "") String startTime,
            @RequestParam(value = "endTime", required = false, defaultValue = "") String endTime,
            @RequestParam(value = "orientation", required = false, defaultValue = "0") int orientation, //倾向性 0：全部 1：正向 2：负向 3：中性 4：有争议
            @RequestParam(value = "hasPic", required = false, defaultValue = "false") boolean hasPic,//类别
            @RequestParam(value = "order", required = false, defaultValue = "1") int order, //1 时间降序 2 热度降序
            @RequestParam(value = "keyWords", required = false, defaultValue = "") String keyWords, // 搜索关键词
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "queryId", required = false, defaultValue = "") String queryId
    ) {
        //判断时间格式是否合法
        def sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
        def startDate
        def endDate
        try {
            if (startTime) {
                startDate = sdf.parse(startTime)
            }
            if (endTime) {
                endDate = sdf.parse(endTime)
            }
        } catch (Exception e) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "时间格式不正确")
        }

        if ((pageSize * pageNo) > 50000) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "请求的数据太多，失败")
        }

        //用户勾选站点
        def reprintMediaMonitor = accountCustomSettingService.getOneAccountCustomSettingByKey(user.userId, DashboardEnum.reprintMediaMonitor.key)
        def setting = accountCustomSettingService.getAccountCustomNewsSetting(user.userId, reprintMediaMonitor, Site.SITE_TYPE_WEBSITE)
        def siteList = []
        setting.each { elem ->
            if (elem.active) {
                siteList.add(elem.siteId)
            }
        }
        String siteIds = String.join(",", siteList)
        def selectedSite = siteService.getUserSelectSite(user.userId, siteIds)
        if (!selectedSite) {
            return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: '没有配置对应的站点'])
        }
        def result = dashboardService.getReprintNews(user.userId, selectedSite, hot, startDate, endDate, orientation, hasPic, order, keyWords, pageSize, queryId)
        if (result.status == 200) {
            def list = []

            def newsIds = []
            result.newsList.each {
                newsIds.add(it.id)
            }
            List col = newsService.getExistNewsOperation(user.userId, newsIds, 3)
            List push = newsService.getExistNewsOperation(user.userId, newsIds, 1)
            result.newsList ? result.newsList.each {
                def res = [
                        id                  : it.id,
                        title               : it.title,
                        contentAbstract     : it.contentAbstract,
                        simhash             : it.simhash,
                        reprintCount        : it.reprintCount,
                        firstPublishTime    : DateUitl.convertFormatDate(DateUitl.convertEsDate(it.firstPublishTime), "MM-dd HH:mm"),
                        firstPublishSiteName: it.firstPublishSiteName,
                        createTime          : DateUitl.convertFormatDate(DateUitl.convertEsDate(it.createTime), "MM-dd HH:mm"),//为数据更新时间
                        siteName            : it.siteName,
                        poster              : it.poster,
                        publishTime         : DateUitl.convertFormatDate(DateUitl.convertEsDate(it.publishTime), "MM-dd HH:mm"),
                        isCollection        : col.contains(it.id),
                        isPush              : push.contains(it.id)
                ]
                list << res
            } : [status: HttpStatus.SC_OK, newsList: [], msg: '未检测到热点新闻或所选站点新闻热度上升过慢，建议添加更多站点']
            result.newsList = list
        }
        return result
    }

    /**
     * 上升最快（旧接口）
     * @param user
     * @param siteIds
     * @param pageSize
     * @param pageNo
     * @param queryId
     * @return
     */
    @RequestMapping(value = '/rise/newsList', method = RequestMethod.GET)
    Object getRiseNewsList(
            @CurrentUser LoginUser user,
            @RequestParam(value = "siteIds", required = false, defaultValue = "") String siteIds,
            @RequestParam(value = "pageSize", required = false, defaultValue = "12") int pageSize,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "queryId", required = false, defaultValue = "") String queryId
    ) {
        if (!siteIds) {
            return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: '暂无数据'])
        }
        if ((pageSize * pageNo) > 100) {
            return apiResult([status: HttpStatus.SC_OK, msg: '没有更多数据'])
        }
        def selectedSite = siteService.getUserSelectSite(user.userId, siteIds)
        if (!selectedSite) {
            return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: '没有配置对应的站点'])
        }
        def result = dashboardService.getUserRiseNews(user.userId, selectedSite, pageSize, pageNo, queryId)
        if (result) {
            return result
        }
        return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: '暂无数据'])
    }

    @RequestMapping(value = '/rise/moreNewsList', method = RequestMethod.GET)
    Object getMoreRiseNewsList(
            @CurrentUser LoginUser user,
            @RequestParam(value = "pageSize", required = false, defaultValue = "12") int pageSize,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "queryId", required = false, defaultValue = "") String queryId
    ) {

        if ((pageSize * pageNo) > 100) {
            return apiResult([status: HttpStatus.SC_OK, msg: '没有更多数据'])
        }
        def sitesList = siteService.getUserSite(user.userId)
        if (!sitesList) {
            return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: '没有配置对应的站点'])
        }
        def result = dashboardService.getUserRiseNews(user.userId, sitesList, pageSize, pageNo, queryId)
        if (result) {
            return result
        }
        return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: '暂无数据'])
    }

    /**
     * 获取政协动态新闻
     * @param user
     * @return
     */
    @RequestMapping(value = '/ppc/news', method = RequestMethod.GET)
    Object getPpcNews(
            @CurrentUser LoginUser user
    ) {
        def news = dashboardService.getPpcNews()
        if (news) {
            return apiResult([status: HttpStatus.SC_OK, news: news, msg: ""])
        } else {
            return apiResult([status: HttpStatus.SC_OK, news: null, msg: "暂无数据"])
        }
    }
}
