package com.istar.mediabroken.service.statistics

import com.istar.mediabroken.entity.account.Account
import com.istar.mediabroken.entity.account.Team
import com.istar.mediabroken.entity.statistics.OrgDataStatistics
import com.istar.mediabroken.entity.statistics.UserDataStatistics
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.repo.account.OrganizationRepo
import com.istar.mediabroken.repo.account.TeamRepo
import com.istar.mediabroken.repo.capture.NewsRepo
import com.istar.mediabroken.repo.statistics.DataStatisticsRepo
import com.istar.mediabroken.utils.DateUitl
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * author:hh
 * date:2018/3/15
 */
@Service
@Slf4j
class DataStatisticsService {

    @Autowired
    DataStatisticsRepo dataStatisticsRepo
    @Autowired
    AccountRepo accountRepo
    @Autowired
    OrganizationRepo organizationRepo
    @Autowired
    NewsRepo newsRepo
    @Autowired
    TeamRepo teamRepo

    void addDataStatistics() {
        addUserDataStatistics()
        addOrgDataStatistics()
    }

    void addUserDataStatistics() {
        List<Long> accountIdsList = accountRepo.getOrgAccountIdsByAgent("1")
        Map pushStatistic = newsRepo.getAcountStatisticByType(1, accountIdsList,
                DateUitl.getBeginDayOfYesterday(), DateUitl.getBeginDayOfParm())
        List<UserDataStatistics> userPushStatisticsList = getUserPushStatistics(pushStatistic, accountIdsList)
        Map syncStatistic = newsRepo.getAcountStatisticByType(5, accountIdsList,
                DateUitl.getBeginDayOfYesterday(), DateUitl.getBeginDayOfParm())
        List<UserDataStatistics> userStatisticsList = getUserSynStatistics(syncStatistic, userPushStatisticsList)
        dataStatisticsRepo.addUserDataStatistics(userStatisticsList)
    }

    void addOrgDataStatistics() {
        List<String> orgIdsList = organizationRepo.getOrgIdsByAgentId("1")
        Map pushStatistics = newsRepo.getOrgStatisticByType(1, orgIdsList,
                DateUitl.getBeginDayOfYesterday(),DateUitl.getBeginDayOfParm())
        List<OrgDataStatistics> orgPushStatisticsList = getOrgPushStatistics(pushStatistics, orgIdsList)
        Map syncStatistic = newsRepo.getOrgStatisticByType(5, orgIdsList,
                DateUitl.getBeginDayOfYesterday(),DateUitl.getBeginDayOfParm())
        List<OrgDataStatistics> orgStatisticsList = getOrgSynStatistics(syncStatistic, orgPushStatisticsList)
        dataStatisticsRepo.addOrgDataStatistics(orgStatisticsList)
    }

    List<UserDataStatistics> getUserPushStatistics(Map pushStatistic, List<Long> userIds) {
        def pushStatistics = []
        def accountMap = accountRepo.getAccountMapByAgent("1")
        userIds.each { userId ->
            List accountStatisticList = pushStatistic.get(userId)
            if (accountStatisticList) {
                def accountTeamIdMap = accountStatisticList.groupBy { it._id.teamId }
                accountTeamIdMap.each { accountTeamId ->
                    List accountTeamIdList = accountTeamId.value
                    def accountTeamNameMap = accountTeamIdList.groupBy { it._id.teamName }
                    accountTeamNameMap.each { accountTeamName ->
                        UserDataStatistics userStatistics = addUserPushStatistics(accountTeamName, userId)
                        pushStatistics.add(userStatistics)
                    }
                }
            } else {//如果没有推送统计信息 则统计结果为0
                UserDataStatistics userStatistics = addNoPushStatistics(userId,accountMap)
                pushStatistics.add(userStatistics)
            }
        }
        return pushStatistics
    }

    UserDataStatistics addUserPushStatistics(Map.Entry<Object, Object> accountTeamName, userId) {
        List userData = accountTeamName.value
        def userStatistics = new UserDataStatistics()
        userData.each { it ->
            userStatistics.setUserId(userId)
            userStatistics.setTeamId(it.getAt("_id").getAt("teamId") ?: "0")
            userStatistics.setTeamName(it.getAt("_id").getAt("teamName") ?: "")
            userStatistics.setOrgId(it.getAt("orgId") ?: "0")
            userStatistics.setPublisher(it.getAt("publisher"))
            def isAutoPush = it.getAt("_id").getAt("isAutoPush")
            def newsCount = it.getAt("count")
            if (isAutoPush == true) {
                userStatistics.setAutoPushCount(newsCount)
            } else {
                userStatistics.setManualPushCount(newsCount)
            }
        }
        userStatistics.getManualPushCount()?:userStatistics.setManualPushCount(0)
        userStatistics.getAutoPushCount()?:userStatistics.setAutoPushCount(0)
        userStatistics.setPublishTime(DateUitl.getBeginDayOfYesterdayAddOne())
        userStatistics.setWeiboCount(0)
        userStatistics.setWechatCount(0)
        userStatistics.setToutiaoCount(0)
        userStatistics.setQqomCount(0)
        userStatistics.setCreateTime(new Date())
        return userStatistics
    }

    UserDataStatistics addNoPushStatistics(long userId,Map accountMap) {
        def userStatistics = new UserDataStatistics()
        Account user = accountMap.get(userId)
        Team team = null
        if (user.getTeamId()) {
            team = teamRepo.getTeam(user.getTeamId())
        }
        userStatistics.setUserId(userId)
        userStatistics.setOrgId(user.getOrgId())
        userStatistics.setTeamId(user.getTeamId() ?: "")
        userStatistics.setTeamName(team ? team.getTeamName() : "")
        userStatistics.setPublisher(user.getRealName() ?: user.getUserName())
        userStatistics.setPublishTime(DateUitl.getBeginDayOfYesterdayAddOne())
        userStatistics.setManualPushCount(0)
        userStatistics.setAutoPushCount(0)
        userStatistics.setWeiboCount(0)
        userStatistics.setWechatCount(0)
        userStatistics.setToutiaoCount(0)
        userStatistics.setQqomCount(0)
        userStatistics.setCreateTime(new Date())
        return userStatistics
    }

    List<UserDataStatistics> getUserSynStatistics(Map accountStatistic, List<UserDataStatistics> userPushStatisticsList) {
        def unUseDataStatistics = []
        userPushStatisticsList.each { userData ->
            def synData = accountStatistic.get(userData.userId)
            if (synData) {
                userData.setWechatCount(synData[0].getAt("wechatCount"))
                userData.setWeiboCount(synData[0].getAt("weiboCount"))
                userData.setToutiaoCount(synData[0].getAt("toutiaoCount"))
                userData.setQqomCount(synData[0].getAt("qqomCount"))
            }
            if (!(userData.getAutoPushCount() != 0 || userData.getManualPushCount() != 0 ||
                    userData.getWechatCount() != 0 || userData.getWeiboCount() != 0 || userData.getToutiaoCount() != 0
                    || userData.getQqomCount() != 0)) {//把无任何统计信息的用户在List中移除
                unUseDataStatistics.add(userData)
            }
        }
        unUseDataStatistics.each { unUseData ->
            userPushStatisticsList.remove(unUseData)
        }
        return userPushStatisticsList
    }

    List<OrgDataStatistics> getOrgPushStatistics(Map pushStatistic, List<String> orgIds) {
        def pushStatistics = []
        orgIds.each { orgId ->
            List accountStatisticList = pushStatistic.get(orgId)
            if (accountStatisticList) {
                def orgIdMap = accountStatisticList.groupBy { it._id.orgId }
                orgIdMap.each { orgStatisticsList ->
                    orgStatisticsList.each { orgStatistics ->
                        OrgDataStatistics OrgDataStatistics = addOrgPushStatistics(orgStatistics, orgId)
                        pushStatistics.add(OrgDataStatistics)
                    }
                }
            } else {
                OrgDataStatistics noPushOrgData = addNoPushStatistics(orgId)
                pushStatistics.add(noPushOrgData)
            }
        }
        return pushStatistics
    }

    OrgDataStatistics addOrgPushStatistics(Map.Entry<Object, Object> orgStatistics, orgId) {
        List orgData = orgStatistics.value
        def orgDataStatistics = new OrgDataStatistics()
        orgData.each { it ->
            orgDataStatistics.setOrgId(orgId)
            def isAutoPush = it.getAt("_id").getAt("isAutoPush")
            def newsCount = it.getAt("count")
            if (isAutoPush == true) {
                orgDataStatistics.setAutoPushCount(newsCount)
            } else {
                orgDataStatistics.setManualPushCount(newsCount)
            }
        }
        orgDataStatistics.getManualPushCount()?:orgDataStatistics.setManualPushCount(0)
        orgDataStatistics.getAutoPushCount()?:orgDataStatistics.setAutoPushCount(0)
        orgDataStatistics.setPublishTime(DateUitl.getBeginDayOfYesterdayAddOne())
        orgDataStatistics.setWeiboCount(0)
        orgDataStatistics.setWechatCount(0)
        orgDataStatistics.setToutiaoCount(0)
        orgDataStatistics.setQqomCount(0)
        orgDataStatistics.setCreateTime(new Date())
        return orgDataStatistics
    }

    OrgDataStatistics addNoPushStatistics(String orgId) {
        def orgStatistics = new OrgDataStatistics()
        orgStatistics.setOrgId(orgId)
        orgStatistics.setManualPushCount(0)
        orgStatistics.setAutoPushCount(0)
        orgStatistics.setWechatCount(0)
        orgStatistics.setWechatCount(0)
        orgStatistics.setToutiaoCount(0)
        orgStatistics.setQqomCount(0)
        orgStatistics.setPublishTime(DateUitl.getBeginDayOfYesterdayAddOne())
        orgStatistics.setCreateTime(new Date())
        return orgStatistics
    }

    List<OrgDataStatistics> getOrgSynStatistics(Map orgStatistic, List<OrgDataStatistics> orgPushStatisticsList) {
        def unUseDataStatistics = []
        orgPushStatisticsList.each { orgData ->
            def synData = orgStatistic.get(orgData.getOrgId())
            if (synData) {
                orgData.setWechatCount(synData[0].getAt("wechatCount"))
                orgData.setWeiboCount(synData[0].getAt("weiboCount"))
                orgData.setToutiaoCount(synData[0].getAt("toutiaoCount"))
                orgData.setQqomCount(synData[0].getAt("qqomCount"))
            }
            if (!(orgData.getAutoPushCount() != 0 || orgData.getManualPushCount() != 0 ||
                    orgData.getWechatCount() != 0 || orgData.getWeiboCount() != 0 || orgData.getToutiaoCount() != 0
                    || orgData.getQqomCount() != 0)) {//把无任何统计信息的用户在List中移除
                unUseDataStatistics.add(orgData)
            }
        }
        unUseDataStatistics.each { unUseData ->
            orgPushStatisticsList.remove(unUseData)
        }
        return orgPushStatisticsList
    }
}
