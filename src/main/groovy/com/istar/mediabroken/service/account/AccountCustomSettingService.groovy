package com.istar.mediabroken.service.account

import com.istar.mediabroken.api.DashboardEnum
import com.istar.mediabroken.entity.account.AccountCustomSetting
import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.repo.account.AccountCustomSettingRepo
import com.istar.mediabroken.repo.capture.SiteRepo
import com.istar.mediabroken.repo.capture.SubjectRepo
import com.istar.mediabroken.service.capture.SiteService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AccountCustomSettingService {

    @Autowired
    AccountCustomSettingRepo accountCustomSettingRepo

    @Autowired
    SiteRepo siteRepo

    @Autowired
    SubjectRepo subjectRepo

    @Autowired
    SiteService siteService

    def modifyAccountCustomDashboardSettingByType(long userId, String ids, String key) {
        if (ids != null) {
            //是否第一次进入
            def accountCustomSetting = getOneAccountCustomSettingByKey(userId, key)
            if (accountCustomSetting != null) {
                // 不是首次
                if (ids == "") {
                    def list = []
                    accountCustomSettingRepo.modifyAccountCustomSetting(userId, key, list)
                }
                def siteIdList = ids.split(",")
                def newList = []
                siteIdList.each { elem ->
                    if (elem) {
                        newList.add(elem)
                    }
                }
                accountCustomSettingRepo.modifyAccountCustomSetting(userId, key, newList)
            } else {
                //首次
                if (ids == "") {
                    def list = []
                    accountCustomSettingRepo.addAccountCustomSetting(userId, key, list, "")
                } else {
                    LinkedList siteIdList = ids.split(",")
                    def newList = []
                    siteIdList.each { elem ->
                        if (elem) {
                            newList.add(elem)
                        }
                    }
                    accountCustomSettingRepo.addAccountCustomSetting(userId, key, newList, "")
                }
            }
        }
    }

    def getDashboardModulesSetting(long userId) {

        def customSettingList = [:]
        //获得首页巡查的设置(头条的网站)
        def highlightNews = getOneAccountCustomSettingByKey(userId, DashboardEnum.highlightNews.key)
        customSettingList.put(DashboardEnum.highlightNews.key, getSiteHaveHeadLineSetting(userId, highlightNews))
        //热点新闻(所有网站)
        def hotNews = getOneAccountCustomSettingByKey(userId, DashboardEnum.hotNews.key)
        customSettingList.put(DashboardEnum.hotNews.key, getAccountCustomNewsSetting(userId, hotNews, Site.SITE_TYPE_WEBSITE))
        //全网监控主题新闻
        def subjectNews = getOneAccountCustomSettingByKey(userId, DashboardEnum.subjectNews.key)
        customSettingList.put(DashboardEnum.subjectNews.key, getSubjectNewsSetting(userId, subjectNews))
        //微博
        def weiboNews = getOneAccountCustomSettingByKey(userId, DashboardEnum.weiboNews.key)
        customSettingList.put(DashboardEnum.weiboNews.key, getAccountCustomNewsSetting(userId, weiboNews, Site.SITE_TYPE_WEIBO))
        //微信
        def wechatNews = getOneAccountCustomSettingByKey(userId, DashboardEnum.wechatNews.key)
        customSettingList.put(DashboardEnum.wechatNews.key, getAccountCustomNewsSetting(userId, wechatNews, Site.SITE_TYPE_WECHAT))

        //传播监控(所有的网站)
        def riseRateMonitor = getOneAccountCustomSettingByKey(userId, DashboardEnum.riseRateMonitor.key)
        customSettingList.put(DashboardEnum.riseRateMonitor.key, getAccountCustomNewsSetting(userId, riseRateMonitor, Site.SITE_TYPE_WEBSITE))
        //势能监控(所有网站)
        def reprintMediaMonitor = getOneAccountCustomSettingByKey(userId, DashboardEnum.reprintMediaMonitor.key)
        customSettingList.put(DashboardEnum.reprintMediaMonitor.key, getAccountCustomNewsSetting(userId, reprintMediaMonitor, Site.SITE_TYPE_WEBSITE))
        return customSettingList
    }

    def getSubjectNewsSetting(long userId, AccountCustomSetting subjectNews) {
        LinkedList subjectNewsSetting = []
        if (subjectNews != null) {
            def content = subjectNews.content
            if (content != null) {
                def subjects = subjectRepo.getUserSubjects(userId)
                if (subjects.size() > 0) {
                    subjects.each { elem ->
                        def map = [:]
                        map.put("subjectId", elem.subjectId)
                        map.put("subjectName", elem.subjectName)
                        map.put("active", false)
                        if (content.contains(elem.subjectId)) {
                            map.put("active", true)
                        }
                        subjectNewsSetting.add(map)
                    }
                }
            }
            return subjectNewsSetting
        } else {
            def subjects = subjectRepo.getUserSubjects(userId)
            subjects.each { elem ->
                def map = [:]
                map.put("subjectId", elem.subjectId)
                map.put("subjectName", elem.subjectName)
                map.put("active", true)
                subjectNewsSetting.add(map)
            }
            return subjectNewsSetting
        }
    }

    def getAccountCustomNewsSetting(long userId, AccountCustomSetting hotNews, int siteType) {
        LinkedList Setting = []
        if (hotNews != null) {
            def content = hotNews.content
            if (content != null) {
                def userSiteList = siteRepo.getUserSitesByType(userId, siteType)
                userSiteList.each { elem ->
                    if (elem != null) {
                        def map = [:]
                        map.put("siteId", elem.siteId)
                        map.put("siteName", elem.siteName)
                        map.put("active", false)
                        if (content.contains(elem.siteId)) {
                            map.put("active", true)
                        }
                        Setting.add(map)
                    }
                }
            }
            return Setting
        } else {
            def ids = []
            def siteList = siteRepo.getUserSitesByType(userId, siteType)
            siteList.each { elem ->
                ids.add(elem.siteId)
            }
            return getAccountCustomSettingContentList(userId, ids, Setting)
        }
    }

    def getSiteHaveHeadLineSetting(long userId, AccountCustomSetting accountCustomSetting) {
        LinkedList accountCustomSettingList = []
        if (accountCustomSetting) {
            def content = accountCustomSetting.content
            return getAccountCustomSettingStatus(userId, content, accountCustomSettingList)
        } else {
            //有首页的站点
            List<Site> sites = []
            /* List ids = []
             //第一次登录先获得用户被设置为头条的网站站点  siteType =  1
             def userSiteList = siteRepo.getUserSitesByType(userId, Site.SITE_TYPE_WEBSITE)
             userSiteList.each { elem ->
                 ids.add(elem.siteId)
             }
             def siteList = siteRepo.getUserSitesByIds(userId, ids)
             siteList.each { elem ->
                 if (elem != null) {
                     sites.add(siteRepo.getHeadLineSite(elem))
                 }
             }*/
            sites = siteService.getUserAllHeadLineSite(userId)
            sites.each { elem ->
                if (elem != null) {
                    def map = [:]
                    map.put("siteId", elem.siteId)
                    map.put("siteName", elem.siteName)
                    map.put("active", true)
                    accountCustomSettingList.add(map)
                }
            }
            return accountCustomSettingList
        }
    }

    def getAccountCustomSettingStatus(long userId, def content, def accountCustomSettingList) {
        if (content.size() >= 0) {
            //有头条的
            List<Site> sites = []
            /*List ids = []
            //第一次登录先获得用户被设置为头条的网站站点  siteType =  1
            def userSiteList = siteRepo.getUserSitesByType(userId, Site.SITE_TYPE_WEBSITE)
            userSiteList.each { elem ->
                ids.add(elem.siteId)
            }
            def siteList = siteRepo.getUserSitesByIds(userId, ids)
            siteList.each { elem ->
                if (elem != null) {
                    sites.add(siteRepo.getHeadLineSite(elem))
                }
            }*/
            sites = siteService.getUserAllHeadLineSite(userId)
            sites.each { elem ->
                if (elem != null) {
                    def map = [:]
                    map.put("siteId", elem.siteId)
                    map.put("siteName", elem.siteName)
                    map.put("active", false)
                    if (content.contains(elem.siteId)) {
                        map.put("active", true)
                    }
                    accountCustomSettingList.add(map)
                }
            }
        }
        return accountCustomSettingList
    }

    def getAccountCustomSettingContentList(long userId, def content, def list) {
        if (content.size() >= 0) {
            content.each { elem ->
                if (elem != null) {
                    def site = siteRepo.getUserSiteById(userId, elem)
                    if (site != null) {
                        def map = [:]
                        map.put("siteId", elem)
                        map.put("siteName", site.siteName)
                        map.put("active", true)
                        list.add(map)
                    }
                }
            }
        }
        return list
    }

    def modifyDashboardModules(long userId, def content) {
        def result = getOneAccountCustomSettingByKey(userId, "dashboard")
        def firstList = getOneAccountCustomSettingByKey(0L, "dashboard")
        def firstContent = firstList.content
        if (result == null) {
            //第一次操作给用户创建一个
            if (content == "") {
                def result1 = accountCustomSettingRepo.addAccountCustomSetting(userId, "dashboard", content, "")
                return result1
            }
            //过滤无用词
            def uniqueList = content.grep(firstContent)
            def result2 = accountCustomSettingRepo.addAccountCustomSetting(userId, "dashboard", uniqueList, "")
            return result2
        }
        if (result != null) {
            if (content == "") {
                def accountCustomSetting = modifyAccountCustomSetting(userId, "dashboard", content)
                return accountCustomSetting
            }
            //过滤无用词
            def uniqueList = content.grep(firstContent)
            def accountCustomSetting = modifyAccountCustomSetting(userId, "dashboard", uniqueList)
            return accountCustomSetting
        }
    }

    def getDashboardModules(long userId) {
        //判断是否第一次登录
        LinkedList list = []
        def result = getOneAccountCustomSettingByKey(userId, "dashboard")
        if (result != null) {
            def content = result.content
            //无数据
            if (content.size() == 0) {
                list = this.getUserDashboardsWithStatus(userId, false)
                return list
            }
            //有数据
            if (content.size() != 0) {
                def firstList = getOneAccountCustomSettingByKey(0L, "dashboard")
                def firstContent = firstList.content
                content.each { elem ->
                    firstContent.each { elem1 ->
                        if (elem.equals(elem1)) {
                            LinkedHashMap map = [:]
                            map.put("dashboard", elem)
                            map.put("isShow", true)
                            list.add(map)
                        }
                    }
                }
                firstContent.each { elem1 ->
                    LinkedHashMap map1 = [:]
                    map1.put("dashboard", elem1)
                    map1.put("isShow", false)
                    content.each { elem ->
                        if (elem.equals(elem1)) {
                            map1.remove("dashboard", elem)
                            map1.remove("isShow", false)
                        }
                    }
                    if (map1.size() > 0) {
                        list.add(map1)
                    }
                }
                return list
            }
        } else {
            //用户第一次进系统没修改过
            list = this.getUserDashboardsWithStatus(userId, true)
        }
        return list
    }

    def modifyAccountCustomSetting(long userId, String key, def content) {
        def accountCustomSetting = accountCustomSettingRepo.modifyAccountCustomSetting(userId, key, content)
    }

    def getUserDashboardsWithStatus(long userId, boolean isShow) {
        LinkedList list = []
        def result = getOneAccountCustomSettingByKey(0L, "dashboard")
        def linkedList = result.content
        linkedList.each { elem ->
            LinkedHashMap map = [:]
            map.put("dashboard", elem)
            map.put("isShow", isShow)
            list.add(map)
        }
        return list
    }

    def getOneAccountCustomSettingByKey(long userId, String key) {
        def result = accountCustomSettingRepo.getOneAccountCustomSettingByKey(userId, key)
        return result
    }

}
