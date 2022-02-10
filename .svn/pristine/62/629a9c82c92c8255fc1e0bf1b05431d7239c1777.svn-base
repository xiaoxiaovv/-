package com.istar.mediabroken.service.subscription

import com.istar.mediabroken.utils.SubscriptionApiUtils
import com.istar.mediabroken.utils.UrlUtils
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import static com.istar.mediabroken.api.ApiResult.apiResult
import static com.istar.mediabroken.utils.Md5Util.md5

/**
 * Created by zxj on   2018/2/9
 */
@Service
class SubscriptionService {

    @Value('${subscription.bjjapi.domainUrl}')
    String domainUrl

    Map getSubjectsList(String appId, String secret) {
        def map = ["": ""]
        def result = SubscriptionApiUtils.get(domainUrl + "/openapi/subject/subjectsList", appId, secret, map)
        return result
    }

    Map getCurrentNewsCount(String appId, String secret, String subjectId, String startTime, String endTime) {
        def map = [subjectId: subjectId, startTime: startTime, endTime: endTime]
        def result = SubscriptionApiUtils.get(domainUrl + "/openapi/subject/news/count", appId, secret, map)
        return result
    }

    Map getSubjectNewsList(String appId, String secret, String subjectId, int pageNo, int pageSize) {
        def map = [subjectId: subjectId, pageNo: pageNo, pageSize: pageSize]
        Map result = SubscriptionApiUtils.get(domainUrl + "/openapi/subject/newsList", appId, secret, map)
        return result
    }

    Map getNewsDetail(String appId, String secret, String subjectId, String newsId) {
        def map = [subjectId: subjectId, newsId: newsId]
        Map result = SubscriptionApiUtils.get(domainUrl + "/openapi/news/newsDetail", appId, secret, map)
        return result
    }

    Map getSubject(String appId, String secret, String subjectId) {
        def map = [subjectId: subjectId]
        Map result = SubscriptionApiUtils.get(domainUrl + "/openapi/subject", appId, secret, map)
        return result
    }

    Map addSubject(String appId, String secret, String subjectName, String description, String expression,
                   String excludeWords, String siteIds, Long startTime, Long endTime) {
        def map = [subjectName : subjectName, description: description, expression: expression,
                   excludeWords: excludeWords, siteIds: siteIds, startTime: startTime, endTime: endTime
        ]
        Map result = SubscriptionApiUtils.post(domainUrl + "/openapi/subject", appId, secret, map)
        return result
    }

    Map modifySubject(String appId, String secret, String subjectId, String subjectName, String description,
                      String expression, String excludeWords, String siteIds, Long startTime, Long endTime) {
        def map = [subjectId : subjectId, subjectName: subjectName, description: description,
                   expression: expression, excludeWords: excludeWords, siteIds: siteIds,
                   startTime : startTime, endTime: endTime
        ]
        Map result = SubscriptionApiUtils.put(domainUrl + "/openapi/subject", appId, secret, map)
        return result
    }

    Map deleteSubject(String appId, String secret, String subjectId) {
        def map = [subjectId: subjectId]
        Map result = SubscriptionApiUtils.delete(domainUrl + "/openapi/subject", appId, secret, map)
        return result
    }

    Map getExcludeWords(String appId, String secret) {
        def map = ["": ""]
        Map result = SubscriptionApiUtils.get(domainUrl + "/openapi/excludeWords", appId, secret, map)
        return result
    }

    Map modifyExcludeWords(String appId, String secret, String excludeWords) {
        def map = [excludeWords: excludeWords]
        Map result = SubscriptionApiUtils.put(domainUrl + "/openapi/excludeWords", appId, secret, map)
        return result
    }

    Map deleteExcludeWords(String appId, String secret) {
        def map = ["": ""]
        Map result = SubscriptionApiUtils.delete(domainUrl + "/openapi/excludeWords", appId, secret, map)
        return result
    }

    Map getUserSites(String appId, String secret) {
        def map = ["": ""]
        Map result = SubscriptionApiUtils.get(domainUrl + "/openapi/site/allSites", appId, secret, map)
        return result
    }

    Map addUserSite(String appId, String secret, String siteName, Integer siteType, String siteDomain, String accountId,
                    Integer urlType, Integer matchType) {
        //查询在systemSite表中是否已经配置了此站点，如果配置了此站点允许添加，否则不允许添加站点
        String systemSiteId = ""
        def map
        if (siteType == 1) {
            systemSiteId = md5(siteType + UrlUtils.getTopDomain(siteDomain))
            map = [siteName: siteName, siteType: siteType, siteDomain: siteDomain, urlType: urlType, matchType: matchType]
        } else if (siteType == 2) {
            systemSiteId = md5(siteType + siteName + accountId)
            map = [siteName: siteName, siteType: siteType, siteDomain: siteDomain, accountId: accountId]
        } else if (siteType == 3) {
            systemSiteId = md5(siteType + siteName)
            map = [siteName: siteName, siteType: siteType, siteDomain: siteDomain]
        }
        def dataMap = [systemSiteId: systemSiteId]
        Map systemSiteResult = SubscriptionApiUtils.get(domainUrl + "/openapi/systemSite", appId, secret, dataMap)

        if (systemSiteResult.status == HttpStatus.SC_OK && systemSiteResult.systemSite) {
            //配置了此站点允许添加
            Map result = SubscriptionApiUtils.post(domainUrl + "/openapi/site", appId, secret, map)
            return result
        } else {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '不能添加未配置站点!', "300010101", "不能添加未配置站点!")
        }
    }

    Map deleteUserSite(String appId, String secret, String siteId) {
        def dataMap = [siteId: siteId]
        Map result = SubscriptionApiUtils.delete(domainUrl + "/openapi/site", appId, secret, dataMap)
        return result
    }
}
