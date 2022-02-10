package com.istar.mediabroken.api.capture

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.api.*
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.SystemSetting
import com.istar.mediabroken.entity.account.Account
import com.istar.mediabroken.entity.account.Privilege
import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.entity.capture.SiteDetail
import com.istar.mediabroken.repo.admin.SettingRepo
import com.istar.mediabroken.repo.capture.SiteRepo
import com.istar.mediabroken.service.CaptureService
import com.istar.mediabroken.service.account.AccountCustomSettingService
import com.istar.mediabroken.service.account.AccountService
import com.istar.mediabroken.service.app.AgentService
import com.istar.mediabroken.service.capture.NewsService
import com.istar.mediabroken.service.capture.SiteService
import com.istar.mediabroken.service.capture.WeiboInfoService
import com.istar.mediabroken.service.system.OperationLogService
import com.istar.mediabroken.service.system.SystemSettingService
import com.istar.mediabroken.utils.DownloadUtils
import com.istar.mediabroken.utils.StringUtils
import com.istar.mediabroken.utils.UploadUtil
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.text.SimpleDateFormat

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR
import static org.springframework.web.bind.annotation.RequestMethod.*

/**
 * Author: Luda
 * Time: 2017/7/26
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/capture/")
class SiteApiController {

    @Autowired
    SiteService siteService

    @Autowired
    CaptureService captureSrv

    @Autowired
    AccountService accountSrv

    @Autowired
    SettingRepo settingRepo

    @Autowired
    NewsService newsService

    @Autowired
    WeiboInfoService weiboInfoService

    @Autowired
    AccountCustomSettingService accountCustomSettingService

    @Autowired
    OperationLogService operationLogService

    @Autowired
    AccountService accountService
    @Autowired
    SiteRepo siteRepo

    @Value('${image.upload.path}')
    public String uploadPath

    @Autowired
    AgentService agentService

    @Autowired
    SystemSettingService systemSettingService
    /**
     * 站点列表
     * @param userId
     * @return
     */
    @RequestMapping(value = "sites", method = GET)
    public Map getSites(
            @CurrentUserId Long userId,
            @RequestParam(value = "siteType", required = false, defaultValue = "0") int siteType
    ) {
        def result = siteService.getUserSites(userId, siteType);
        return result;
    }

    @RequestMapping(value = "site/list", method = GET)
    public Map getSitesByType(
            @CurrentUserId Long userId,
            @RequestParam(value = "siteType", required = false, defaultValue = "0") int siteType,
            @RequestParam(value = "siteName", required = false, defaultValue = "") String siteName
    ) {
        def result = siteService.getUserSitesBySiteTypeAndName(userId, siteType, siteName);
        return apiResult([status: HttpStatus.SC_OK, list: result])
    }


    /**
     * 站点详情
     * @param userId
     * @return
     */
    @RequestMapping(value = "site/{siteId}", method = GET)
    public Map getSiteById(
            @CurrentUserId Long userId,
            @PathVariable("siteId") String siteId
    ) {
        def result = siteService.getUserSiteById(userId, siteId);
        return result;
    }

    /**
     * 新建站点
     * @param userId
     * @param siteType
     * @param siteName
     * @param websiteName
     * @param websiteDomain
     * @return
     */
    @RequestMapping(value = "site", method = POST)
    public Map addSite(
            @CurrentUserId Long userId,
            @RequestParam("siteName") String siteName,
            @RequestParam("websiteName") String websiteName,
            @RequestParam(value = "websiteDomain", required = false, defaultValue = "") String websiteDomain,
            @RequestParam("siteType") int siteType
    ) {
        if (siteType == Site.SITE_TYPE_WEBSITE) {
            if (websiteDomain.equals("")) {
                return apiResult(SC_INTERNAL_SERVER_ERROR, '请填写域名');
            }
        } else {
            websiteDomain = ""
        }
        def result = siteService.addUserSite(userId, siteName.trim(), websiteName.trim(), websiteDomain.trim(), siteType)
        return result
    }

    /**
     * 删除站点
     * @param userId
     * @param siteId
     * @return
     */
    @RequestMapping(value = "site/{siteId}", method = DELETE)
    public Map removeSite(
            @CurrentUserId Long userId,
            @PathVariable("siteId") String siteId
    ) {
        siteService.removeSite(userId, siteId)
        return apiResult();
    }

    /**
     * 批量删除站点
     * @param userId
     * @param siteId
     * @return
     */
    @RequestMapping(value = "site/remove", method = POST)
    public Map removeSiteList(
            @CurrentUserId Long userId,
            @RequestBody String data
    ) {
        JSONObject dataJson = JSONObject.parseObject(data);
        String siteIds = dataJson.get("siteIds").toString();
        List split = siteIds.split(",")
        siteService.removeSiteList(userId, split)
        return apiResult();
    }

    /**
     * 编辑站点
     * @param userId
     * @param siteId
     * @param siteName
     * @param websiteName
     * @param websiteDomain
     * @param siteType
     * @return
     */
    @RequestMapping(value = "site/{siteId}", method = PUT)
    public Map modifySite(
            @CurrentUserId Long userId,
            @PathVariable("siteId") String siteId,
            @RequestParam("siteName") String siteName,
            @RequestParam("websiteName") String websiteName,
            @RequestParam(value = "websiteDomain", required = false, defaultValue = "") String websiteDomain,
            @RequestParam("siteType") int siteType
    ) {
        if (siteType == Site.SITE_TYPE_WEBSITE) {
            if (websiteDomain.equals("")) {
                return apiResult(SC_INTERNAL_SERVER_ERROR, '请填写域名');
            }
        } else {
            websiteDomain = ""
        }
        def result = siteService.modifyUserSite(userId, siteId, siteName.trim(), websiteName.trim(), websiteDomain.trim(), siteType)
        return result;
    }
    /**
     * 设为自动推送
     * @param userId
     * @param siteId
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.PUSHNEWS_MANAGE])
    @RequestMapping(value = "site/{siteId}/isAutoPush", method = PUT)
    public Map modifySiteAutoPush(
            @CurrentUser LoginUser user,
            @PathVariable("siteId") String siteId,
            @RequestParam(value = "isAutoPush", required = true) boolean isAutoPush
    ) {
        if (((!user.orgId) || "0".equals(user.orgId)) && (isAutoPush)) {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "推送失败，您的账号未开通平台对接功能")
        }
        def result = siteService.modifySiteAutoPush(user.userId, siteId, isAutoPush)
        return result;
    }

    /**
     * 用户站点的新闻
     * @param userId
     * @return
     */
    @RequestMapping(value = "site/{siteId}/news", method = GET)
    public Map getSiteNews(
            @CurrentUserId Long userId,
            @PathVariable("siteId") String siteId,
            @RequestParam(value = "hot", required = false, defaultValue = "0") int hot,//0全部 1 低 2 中 3 高
            @RequestParam(value = "startTime", required = false, defaultValue = "") String startTime,
            @RequestParam(value = "endTime", required = false, defaultValue = "") String endTime,
            @RequestParam(value = "orientation", required = false, defaultValue = "0") int orientation, //0：全部 1：正向 2：负向 3：中性 4：有争议
            @RequestParam(value = "hasPic", required = false, defaultValue = "false") boolean hasPic,
            @RequestParam(value = "order", required = false, defaultValue = "1") int order, //1 时间降序 2 热度将续
            @RequestParam(value = "queryScope", required = false, defaultValue = "1") int queryScope, //搜索范围 1 正文 2标题
            @RequestParam(value = "keyWords", required = false, defaultValue = "") String keyWords, //1 搜索关键词
            @RequestParam(value = "pageSize", required = false, defaultValue = "50") int pageSize,
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
        if (pageNo == 1) {
            Account userInfo = accountService.getUserInfoById(userId)
            operationLogService.addOperationLog(userInfo, "site", "", hot, startDate, endDate, orientation, hasPic, order, keyWords, pageSize)
        }
        def result = siteService.getUserSiteNews(userId, siteId, hot, startDate, endDate, orientation, hasPic, order, queryScope, keyWords, pageSize, queryId)
        return result
    }

    /**
     * 用户微博站点的新闻
     * @param userId
     * @return
     */
    @RequestMapping(value = "site/{siteId}/weiboNews", method = GET)
    public Map getWeiboSiteNews(
            @CurrentUserId Long userId,
            @PathVariable("siteId") String siteId,
            @RequestParam(value = "hot", required = false, defaultValue = "0") int hot,//0全部 1 低 2 中 3 高
            @RequestParam(value = "startTime", required = false, defaultValue = "") String startTime,
            @RequestParam(value = "endTime", required = false, defaultValue = "") String endTime,
            @RequestParam(value = "orientation", required = false, defaultValue = "0") int orientation, //0：全部 1：正向 2：负向 3：中性 4：有争议
            @RequestParam(value = "hasPic", required = false, defaultValue = "false") boolean hasPic,
            @RequestParam(value = "order", required = false, defaultValue = "1") int order, //1 时间降序 2 热度将续
            @RequestParam(value = "keyWords", required = false, defaultValue = "") String keyWords, //1 搜索关键词
            @RequestParam(value = "pageSize", required = false, defaultValue = "50") int pageSize,
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
        if (pageNo == 1) {
            Account userInfo = accountService.getUserInfoById(userId)
            operationLogService.addOperationLog(userInfo, "site", "", hot, startDate, endDate, orientation, hasPic, order, keyWords, pageSize)
        }
        def result = siteService.getUserSiteNews(userId, siteId, hot, startDate, endDate, orientation, hasPic, order, 1, keyWords, pageSize, queryId)
        if (result.status == 200) {
            def list = []

            def newsIds = []
            result.newsList.each {
                newsIds.add(it.id)
            }
            List col = newsService.getExistNewsOperation(userId, newsIds, 3)
            List push = newsService.getExistNewsOperation(userId, newsIds, 1)

            result.newsList ? result.newsList.each {
                def res = [
                        id          : it.id,
                        title       : it.title,
                        content     : StringUtils.removeWeiboSuffix(it.content),
                        simhash     : it.simhash,
                        reprintCount: it.reprintCount,
                        commentCount: it.commentCount,
                        likeCount   : it.likeCount,
                        publishTime : it.publishTime,
                        siteName    : it.siteName,
                        imgUrls     : it.imgUrls,
                        posterAvatar: it.posterAvatar,
                        url         : it.url,
                        vflag       : weiboInfoService.getVerifiedById(it.url),
                        isCollection: col.contains(it.id),
                        isPush      : push.contains(it.id)
                ]
                list << res
            } : []
            result.newsList = list
        }
        return result
    }

    /**
     * 推荐分类选择查询列表（公号秘书代理查询siteDetail_ghms基础信源表）
     * @param userId
     * @param classification
     * @param area
     * @param attr
     * @param order
     * @param siteType
     * @param pageSize
     * @param pageNo
     * @return
     */
    @RequestMapping(value = "site/recommendation", method = GET)
    public Map getSiteRecommendation(
            @CurrentUser LoginUser user,
            HttpServletRequest request,
            @RequestParam(value = "classification", required = false, defaultValue = "") String classification, //行业
            @RequestParam(value = "area", required = false, defaultValue = "") String area, //地域
            @RequestParam(value = "attr", required = false, defaultValue = "") String attr, //属性
            @RequestParam(value = "order", required = false, defaultValue = "0") int order,
            @RequestParam(value = "siteType", required = false, defaultValue = "") String siteType,
            @RequestParam(value = "pageSize", required = false, defaultValue = "30") int pageSize,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "siteName", required = false, defaultValue = "") String siteName,
            @RequestParam(value = "mina", required = false, defaultValue = "false") boolean mina
    ) {
        try {
            List<SiteDetail> siteDetailList = []
            List<String> classificationList = []
            if (mina){
                def map = systemSettingService.getSystemSettingByTypeAndKey("site", "classification.query.mina")
                classificationList = map.content
                if (!"".equals(classification)){
                    if (classificationList.contains(classification)){
                        classificationList = []
                        classificationList << classification
                    }else {
                        return apiResult([status: HttpStatus.SC_OK, msg: []])
                    }
                }
            }else {
                if (!"".equals(classification)){
                    classificationList << classification
                }
            }
            siteDetailList = siteRepo.getSitesRecommendation("siteDetail", classificationList, area, attr, order, siteType, pageSize, pageNo, siteName)
            def agent = agentService.getAgent(request)
            if ("ghms".equals(agent.agentKey)) {
                if ("综合".equals(classification)) {
                    classification = ""
                }
                siteDetailList = siteRepo.getSitesRecommendation("siteDetail_ghms", classificationList, area, attr, order, siteType, pageSize, pageNo, siteName)
            }
            def result = siteService.getSitesRecommendation(user.userId, siteDetailList)
            return apiResult(result)
        } catch (Exception e) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "列表加载失败"])
        }
    }

    /**
     * 站点分类选择(公号秘书代理查询siteDetail_ghms基础信源表)
     * @param userId
     * @param siteDetailIds
     * @return
     */
    @RequestMapping(value = "site/batch", method = POST)
    public Map addSitesFormRecommend(
            @CurrentUser LoginUser user,
            HttpServletRequest request,
            @RequestParam(value = "siteDetailIds", required = false, defaultValue = "") String siteDetailIds
    ) {
        def siteDetailIdList = siteDetailIds.split(",")
        List<SiteDetail> siteDetailList = []
        siteDetailList = siteRepo.getSiteDetailByIds(siteDetailIdList as List, "siteDetail");
        def agent = agentService.getAgent(request)
        if ("ghms".equals(agent.agentKey)) {
            siteDetailList = siteRepo.getSiteDetailByIds(siteDetailIdList as List, "siteDetail_ghms");
        }
        def siteList = siteService.getSiteDetailByIds(siteDetailList, user.userId)
        def result = siteService.addSiteList(user.userId, siteList)
        return result
    }

    /**
     * 模板下载
     * @param userId
     * @return
     */
    @RequestMapping(value = "site/template", method = RequestMethod.GET)
    public Object sitesTemplate(
            HttpServletResponse response,
            @CurrentUserId long userId
    ) {
        def file = siteService.sitesTemplate();//获取下载文件的路径和名称
        try {
            DownloadUtils.download(file, response,null)
        } catch (IOException e) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '下载文件发生错误！')
        }
        return apiResult()
    }

    /**
     * 站点批量导入
     * @param userId
     * @return
     */
    @RequestMapping(value = "site/import", method = RequestMethod.POST, produces = ["text/plain;charset=UTF-8"])
    @CheckExpiry
    public String sitesImport(
            @CurrentUserId long userId,
            HttpServletRequest request
    ) {
        def res = UploadUtil.uploadExcel(request, uploadPath, '/xls')
        if (!(res.status == HttpStatus.SC_OK)) {
            return JSONObject.toJSONString(apiResult(res))
        }
        File f1 = new File(uploadPath + File.separator + res.msg)
        def result = [status: HttpStatus.SC_OK, msg: '']
        if (null == f1) {
            result.msg = "模板文件为空,请选择文件";
            result.status = HttpStatus.SC_BAD_REQUEST;
            return JSONObject.toJSONString(apiResult(result))
        } else {
            result = siteService.manySitesImport(userId, f1);
        }
        return JSONObject.toJSONString(result)
    }

    /**
     * 用户所有站点的新闻时间内按热度排序
     * @param userId
     * @return
     */
    @RequestMapping(value = "sites/hotNews/fullDay", method = GET)
    public Map getSitesHotNews(
            @CurrentUserId Long userId
    ) {
        def content = []
        def result = []
        SystemSetting setting = settingRepo.getSystemSetting("news", "hotNews")
        if (setting && setting.content) {
            content = setting.content
            result = newsService.getNewsListByIds(content as List)
        } else {
            def endDate = new Date()
            def startDate = new Date(endDate.getTime() - 24 * 3600 * 1000L)
            result = siteService.getUserSitesNewsByTime(userId, true, 3, 2, startDate, endDate)
        }
        return result
    }

    @RequestMapping(value = "sites/hotNews/eightHours", method = GET)
    public Map getSitesHotNewsEightHours(
            @CurrentUserId Long userId

    ) {
        def endDate = new Date()
        endDate = new Date(endDate.getTime() - 4 * 3600 * 1000L)
        def startDate = new Date(endDate.getTime() - 4 * 3600 * 1000L)
        def result = siteService.getUserSitesNewsByTime(userId, false, 5, 1, startDate, endDate)
        return result
    }

    @RequestMapping(value = "sites/hotNews/fourHours", method = GET)
    public Map getSitesHotNewsFourHours(
            @CurrentUserId Long userId
    ) {

        def endDate = new Date()
        def startDate = new Date(endDate.getTime() - 4 * 3600 * 1000L)
        def result = siteService.getUserSitesNewsByTime(userId, false, 5, 1, startDate, endDate)
        return result
    }

    @RequestMapping(value = "sites/hotWeChat/fullDay", method = GET)
    public Map getSitesHotWeChatFullDay(
            @CurrentUserId Long userId
    ) {
        def endDate = new Date()
        def startDate = new Date(endDate.getTime() - 24 * 3600 * 1000L)
        def result = siteService.getUserSitesNewsByTime(userId, false, 5, 2, startDate, endDate)
        return result
    }

    @RequestMapping(value = "sites/hotWeiBo/fullDay", method = GET)
    public Map getSitesHotWeiBoFullDay(
            @CurrentUserId Long userId
    ) {

        def result = siteService.getHotWeiBoNews(userId, 5)
        return result
    }

    @RequestMapping(value = "sites/wholeNet/fullDay", method = GET)
    public Map getSitesWholeNetFullDay(
            @CurrentUserId Long userId
    ) {
        def result = siteService.getSitesWholeNetFullDay(userId)
        return result
    }

    @RequestMapping(value = "sites/keyCloud", method = GET)
    public Map getSitesKeyCloud(
            @CurrentUserId Long userId
    ) {
        def result = newsService.getSitesKeyCloud(userId)
        return apiResult([keyCloud: result])
    }

    /**
     * 获取url建议信息
     * @param url
     * @param name
     * @param siteType
     * @return
     */
    @RequestMapping(value = "site/urlSuggestion", method = GET)
    @ResponseBody
    public Map getUrlSuggestion(
            @RequestParam(value = "url", required = false, defaultValue = "") String url,
            @RequestParam(value = "name", required = false, defaultValue = "") String name,
            @RequestParam(value = "siteType", required = false, defaultValue = "1") int siteType
    ) {
        if (siteType == Site.SITE_TYPE_WEBSITE) {
            name = ""
        }
        if (siteType == Site.SITE_TYPE_WECHAT) {
            url = ""
        }
        if (siteType == Site.SITE_TYPE_WEIBO) {
            url = ""
        }
        def result = siteService.getUrlSuggestion(siteType, url, name)
        return result
    }

    /**
     * 获取站点分类
     * @param request
     * @param flag 新手引导推荐站点时flag=0，最新资讯-分类选择查询行业时flag=1
     * @return
     */
    @RequestMapping(value = "site/classification", method = GET)
    @ResponseBody
    public Map getSiteClassification(
            HttpServletRequest request,
            @RequestParam(value = "flag", required = false, defaultValue = "0") int flag,
            @RequestParam(value = "mina", required = false, defaultValue = "false") boolean mina
    ) {
        def agent = agentService.getAgent(request)
        if ("ghms".equals(agent.agentKey) && flag != 0) {
            def result = siteService.getSiteClassification("site", "classification.ghms")
            return result
        }
        if (mina){
            def result = siteService.getSiteClassification("site", "classification.mina")
            return result
        }
        def result = siteService.getSiteClassification("site", "classification")
        return result
    }
    /**
     * 获取地区分类
     */
    @RequestMapping(value = "site/area", method = GET)
    @ResponseBody
    public Map getSiteArea(
            HttpServletRequest request
    ) {
        def agent = agentService.getAgent(request)
        if ("ghms".equals(agent.agentKey)) {
            def result = siteService.getSiteArea("site", "area.ghms")
            return result
        }
        def result = siteService.getSiteArea("site", "area")
        return result
    }
    /**
     * 获取属性分类
     */
    @RequestMapping(value = "site/attr", method = GET)
    @ResponseBody
    public Map getSiteAttr(
            HttpServletRequest request
    ) {
        def agent = agentService.getAgent(request)
        if ("ghms".equals(agent.agentKey)) {
            def result = siteService.getSiteAttr("site", "attr.ghms")
            return result
        }
        def result = siteService.getSiteAttr("site", "attr")
        return result
    }
    /**
     * 获取微博查询列表设置的关键词
     * @param userId
     * @return
     */
    @RequestMapping(value = "site/weiboKeyWords", method = GET)
    @ResponseBody
    Map getWeiboKeyWords(
            @CurrentUserId Long userId
    ) {
        def weiboKeyWords = accountCustomSettingService.getOneAccountCustomSettingByKey(userId, DashboardEnum.weiboKeyWords.key)
        if (weiboKeyWords) {
            def keyWords = weiboKeyWords.content
            def result = []
            keyWords.each {
                result << ["keyword": it]
            }
            return apiResult([weiboKeyWords: result])
        }
        return apiResult([weiboKeyWords: []])
    }

    /**
     * 保存微博查询列表设置的关键词
     * @param userId
     * @param weiboKeyWords
     * @return
     */
    @RequestMapping(value = "site/weiboKeyWords", method = PUT)
    @ResponseBody
    Map saveWeiboKeyWords(
            @CurrentUserId Long userId,
            @RequestParam(value = "weiboKeyWords", required = false, defaultValue = "") String weiboKeyWords
    ) {
        try {
            def result = siteService.saveWeiboKeyWords(userId, weiboKeyWords)
            return apiResult([status: HttpStatus.SC_OK, msg: '保存成功'])
        } catch (Exception e) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '保存失败'])
        }
    }

    //    @RequestMapping(value = "site/counts", method = GET) //暂不开放
    @CheckPrivilege(privileges = [Privilege.NEWS_VIEW])
    @ResponseBody
    Object getSiteNewsCounts(
            @CurrentUser LoginUser user,
            @RequestParam int type
    ) {

        def obj = siteService.getSiteNewsCountsByUser(user.userId, type)

        return apiResult([msg: obj])
    }

    @CheckExpiry
    @CheckPrivilege(privileges = [Privilege.NEWS_VIEW])
    @RequestMapping(value = "site/{siteId}/count", method = GET)
    @ResponseBody
    Object getSiteNewsCount(
            @CurrentUser LoginUser user,
            @PathVariable String siteId
    ) {
        def obj = siteService.getSiteNewsCountBySiteId(user.userId, siteId)
        return apiResult([msg: obj])
    }

    @CheckPrivilege(privileges = [Privilege.NEWS_VIEW])
    @RequestMapping(value = "site/{siteId}/count", method = DELETE)
    @ResponseBody
    Object resetSiteResetTime(
            @CurrentUser LoginUser user,
            @PathVariable String siteId
    ) {
        siteService.resetSiteCountInfo(user.userId, siteId)
        return apiResult()
    }

}
