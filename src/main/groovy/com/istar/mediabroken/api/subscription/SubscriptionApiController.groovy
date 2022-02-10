package com.istar.mediabroken.api.subscription

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.api.CheckPrivilege
import com.istar.mediabroken.api.CurrentUser
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.account.Privilege
import com.istar.mediabroken.service.subscription.SubscriptionService
import com.istar.mediabroken.utils.DateUitl
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.springframework.web.bind.annotation.RequestMethod.*

/**
 * Created by zxj on   2018/2/9
 */
@RestController
@RequestMapping(value = "/api/subscription")
class SubscriptionApiController {

    @Autowired
    SubscriptionService subscriptionService

    /**
     * 查询所有主题列表
     * @param user
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.SUBSCRIPTION_SETTING])
    @RequestMapping(value = "/subject/subjectsList", method = GET)
    public Map getSubjectsList(
            @CurrentUser LoginUser user
    ) {
        Map result = subscriptionService.getSubjectsList(user.org.appId, user.org.secret)
        return result
    }

    /**
     * 当天推送新闻数查询
     * @param user
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.SUBSCRIPTION_SETTING])
    @RequestMapping(value = "/subject/newsCount", method = GET)
    public Map getCurrentNewsCount(
            @CurrentUser LoginUser user,
            @RequestParam(value = "subjectId", required = true) String subjectId
    ) {
        String startTime = DateUitl.getDayBegin().getTime().toString()
        String endTime = new Date().getTime().toString()
        Map result = subscriptionService.getCurrentNewsCount(user.org.appId, user.org.secret, subjectId, startTime, endTime)
        return result
    }

    /**
     * 获得主题所有新闻列表
     * @param userId
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.SUBSCRIPTION_SETTING])
    @RequestMapping(value = "/subject/{subjectId}/news", method = GET)
    public Map getNewsList(
            @CurrentUser LoginUser user,
            @PathVariable("subjectId") String subjectId,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize
    ) {
        def list = subscriptionService.getSubjectNewsList(user.org.appId, user.org.secret, subjectId, pageNo, pageSize)
        return list
    }

    /**
     * 查询新闻详情
     * @param user
     * @param subjectId
     * @param newsId
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.SUBSCRIPTION_SETTING])
    @RequestMapping(value = "/news/newsDetail", method = GET)
    public Map getNewsDetail(
            @CurrentUser LoginUser user,
            @RequestParam(value = "subjectId", required = true) String subjectId,
            @RequestParam(value = "newsId", required = true) String newsId
    ) {
        def data = subscriptionService.getNewsDetail(user.org.appId, user.org.secret, subjectId, newsId)
        if (data.status != 200) {
            return data
        }
        def list = []
        def keywords = JSONObject.parse(data.msg.news.keywords)
        def iterator = keywords.iterator()
        while (iterator.hasNext()) {
            def at = iterator.next().getAt("word")
            list.add(at)
        }
        data.msg.news.putAt("keywords", list)
        return data
    }
    /**
     * 查询主题配置详情
     * @param user
     * @param subjectId
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.SUBSCRIPTION_SETTING])
    @RequestMapping(value = "/subject", method = GET)
    public Map getSubject(
            @CurrentUser LoginUser user,
            @RequestParam(value = "subjectId", required = true) String subjectId
    ) {
        def data = subscriptionService.getSubject(user.org.appId, user.org.secret, subjectId)
        if (data.status != 200) {
            return data
        }
        def subject = data.get("subject")
        def startTimes = subject.getAt("startTime")
        def endTimes = subject.getAt("endTime")
        def startTime = DateUitl.convertStrDate(DateUitl.convertEsDate(startTimes))
        def endTime = DateUitl.convertStrDate(DateUitl.convertEsDate(endTimes))
        subject.putAt("startTime", startTime)
        subject.putAt("endTime", endTime)
        data.put("subject", subject)
        return data
    }
    /**
     * 新增主题配置
     * @param user
     * @param subjectName
     * @param description
     * @param expression
     * @param excludeWords
     * @param siteIds
     * @param startTime
     * @param endTime
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.SUBSCRIPTION_SETTING])
    @RequestMapping(value = "/subject", method = POST)
    public Map addSubject(
            @CurrentUser LoginUser user,
            @RequestParam(value = "subjectName", required = true) String subjectName,
            @RequestParam(value = "description", required = false, defaultValue = "") String description,
            @RequestParam(value = "expression", required = false, defaultValue = "") String expression,
            @RequestParam(value = "excludeWords", required = false, defaultValue = "") String excludeWords,
            @RequestParam(value = "siteIds", required = false, defaultValue = "") String siteIds,
            @RequestParam(value = "startTime", required = true) String startTime,
            @RequestParam(value = "endTime", required = true) String endTime
    ) {
        if (siteIds.equals("")) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "请选择站点"])
        }
        def start
        def end
        try {
            start = DateUitl.getTimes(startTime)
            end = DateUitl.getTimes(endTime)
        } catch (Exception e) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "非法时间格式"])
        }
        def data = subscriptionService.addSubject(user.org.appId, user.org.secret, subjectName, description, expression,
                excludeWords, siteIds, start, end)
        return data
    }
    /**
     * 修改主题配置
     * @param user
     * @param subjectName
     * @param description
     * @param expression
     * @param excludeWords
     * @param siteIds
     * @param startTime
     * @param endTime
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.SUBSCRIPTION_SETTING])
    @RequestMapping(value = "/subject/{subjectId}", method = PUT)
    public Map modifySubject(
            @CurrentUser LoginUser user,
            @PathVariable("subjectId") String subjectId,
            @RequestParam(value = "subjectName", required = true) String subjectName,
            @RequestParam(value = "description", required = false, defaultValue = "") String description,
            @RequestParam(value = "expression", required = false, defaultValue = "") String expression,
            @RequestParam(value = "excludeWords", required = false, defaultValue = "") String excludeWords,
            @RequestParam(value = "siteIds", required = false, defaultValue = "") String siteIds,
            @RequestParam(value = "startTime", required = true) String startTime,
            @RequestParam(value = "endTime", required = true) String endTime
    ) {
        if (siteIds.equals("")) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "请选择站点"])
        }
        def start
        def end
        try {
            start = DateUitl.getTimes(startTime)
            end = DateUitl.getTimes(endTime)
        } catch (Exception e) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "非法时间格式"])
        }

        def data = subscriptionService.modifySubject(user.org.appId, user.org.secret, subjectId, subjectName, description,
                expression, excludeWords, siteIds, start, end)
        return data
    }
    /**
     * 删除主题配置
     * @param user
     * @param subjectId
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.SUBSCRIPTION_SETTING])
    @RequestMapping(value = "/subject", method = DELETE)
    public Map deleteSubject(
            @CurrentUser LoginUser user,
            @RequestParam(value = "subjectId", required = true) String subjectId
    ) {
        def data = subscriptionService.deleteSubject(user.org.appId, user.org.secret, subjectId)
        return data
    }
    /**
     * 获得全局否定词
     * @param userId
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.SUBSCRIPTION_EXCLUSION])
    @RequestMapping(value = "/excludeWords", method = GET)
    public Map getExcludeWords(
            @CurrentUser LoginUser user
    ) {
        def result = subscriptionService.getExcludeWords(user.org.appId, user.org.secret)
        String excludeWords = result.get("excludeWords")
        if (excludeWords) {
            excludeWords = excludeWords.replace(",", " ")
            result.put("excludeWords", excludeWords)
        }
        return result
    }

    /**
     * 添加全局否定词
     * @param userId
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.SUBSCRIPTION_EXCLUSION])
    @RequestMapping(value = "/excludeWords", method = PUT)
    public Map addExcludeWords(
            @CurrentUser LoginUser user,
            @RequestParam(value = "excludeWords", required = false, defaultValue = "") String excludeWords
    ) {
        def result = subscriptionService.modifyExcludeWords(user.org.appId, user.org.secret, excludeWords)
        return result
    }

    /**
     * 删除全局否定词
     * @param user
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.SUBSCRIPTION_EXCLUSION])
    @RequestMapping(value = "/excludeWords", method = DELETE)
    public Map deleteExcludeWords(
            @CurrentUser LoginUser user
    ) {
        def result = subscriptionService.deleteExcludeWords(user.org.appId, user.org.secret)
        return result
    }

    /**
     * 站点查询
     * @param userId
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.SUBSCRIPTION_SETTING])
    @RequestMapping(value = "/allSites", method = GET)
    public Map getUserSites(
            @CurrentUser LoginUser user
    ) {
        def result = subscriptionService.getUserSites(user.org.appId, user.org.secret)
        return result
    }

    /**
     * 站点新增
     * @param user
     * @param siteName
     * @param siteType
     * @param siteDomain
     * @param accountId
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.SUBSCRIPTION_SETTING])
    @RequestMapping(value = "/site", method = POST)
    public Map addUserSite(
            @CurrentUser LoginUser user,
            @RequestParam(value = "siteName", required = true) String siteName,
            @RequestParam(value = "siteType", required = true) Integer siteType,
            @RequestParam(value = "siteDomain", required = true) String siteDomain,
            @RequestParam(value = "accountId", required = true) String accountId,
            @RequestParam(value = "urlType", required = false, defaultValue = "1") Integer urlType,
            @RequestParam(value = "matchType", required = false, defaultValue = "2") Integer matchType
    ) {
        def result = subscriptionService.addUserSite(user.org.appId, user.org.secret, siteName, siteType, siteDomain, accountId,
                urlType, matchType)
        return result
    }

    /**
     * 站点删除
     * @param user
     * @param siteId
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.SUBSCRIPTION_SETTING])
    @RequestMapping(value = "/site", method = DELETE)
    public Map deleteUserSite(
            @CurrentUser LoginUser user,
            @RequestParam(value = "siteId", required = true) String siteId
    ) {
        def result = subscriptionService.deleteUserSite(user.org.appId, user.org.secret, siteId)
        return result
    }

}
