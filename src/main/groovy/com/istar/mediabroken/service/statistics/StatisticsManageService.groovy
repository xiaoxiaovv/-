package com.istar.mediabroken.service.statistics

import com.istar.mediabroken.entity.AccountProfile
import com.istar.mediabroken.entity.account.Team
import com.istar.mediabroken.entity.capture.NewsOperation
import com.istar.mediabroken.entity.statistics.OrgDataStatistics
import com.istar.mediabroken.entity.statistics.UserDataStatistics
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.repo.account.TeamRepo
import com.istar.mediabroken.repo.admin.SettingRepo
import com.istar.mediabroken.repo.capture.NewsRepo
import com.istar.mediabroken.repo.statistics.StatisticsManageRepo
import com.istar.mediabroken.utils.DateUitl
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import static com.istar.mediabroken.api.ApiResult.apiResult

@Service
@Slf4j
class StatisticsManageService {
    @Autowired
    StatisticsManageRepo statisticsManageRepo
    @Autowired
    TeamRepo teamRepo
    @Autowired
    AccountRepo accountRepo

    Map getTodayPublished(String orgId) {
        def count = [publishNumber: 0, cmsCount: 0, weiboCount: 0, weixinCount: 0, toutiaoCount: 0, qqomCount: 0]
        long cmsCount = 0
        long weiboCount = 0
        long weixinCount = 0
        long toutiaoCount = 0
        long qqomCount = 0
        cmsCount = statisticsManageRepo.getTodayPublishedCms(orgId);
        List newsOperationList = statisticsManageRepo.getTodayPublishedShareChannel(orgId)
        for (int i = 0; i < newsOperationList.size(); i++) {
            NewsOperation newsOperation = newsOperationList.get(i)
            if (newsOperation.weiboChannel > 0) {
                weiboCount += newsOperation.weiboChannel
            }
            if (newsOperation.wechatChannel > 0) {
                weixinCount += newsOperation.wechatChannel
            }
            if (newsOperation.toutiaoChannel > 0) {
                toutiaoCount += newsOperation.toutiaoChannel
            }
            if (newsOperation.qqomChannel > 0) {
                qqomCount += newsOperation.qqomChannel
            }
        }
        long publishNumber = cmsCount + weiboCount + weixinCount + toutiaoCount + qqomCount
        count = [publishNumber: publishNumber, cmsCount: cmsCount, weiboCount: weiboCount, weixinCount: weixinCount, toutiaoCount: toutiaoCount, qqomCount: qqomCount]
        return apiResult(status: HttpStatus.SC_OK, count: count)
    }

    Map getYesterdayPublished(String orgId) {
        def count = [publishNumber: 0, cmsCount: 0, weiboCount: 0, weixinCount: 0, toutiaoCount: 0, qqomCount: 0]
        OrgDataStatistics orgDataStatistics = statisticsManageRepo.getYesterdayPublished(orgId)
        if (orgDataStatistics) {
            long cmsCount = orgDataStatistics.manualPushCount + orgDataStatistics.autoPushCount
            long weiboCount = orgDataStatistics.weiboCount
            long weixinCount = orgDataStatistics.wechatCount
            long toutiaoCount = orgDataStatistics.toutiaoCount
            long qqomCount = orgDataStatistics.qqomCount
            long publishNumber = cmsCount + weiboCount + weixinCount + toutiaoCount + qqomCount
            count = [publishNumber: publishNumber, cmsCount: cmsCount, weiboCount: weiboCount, weixinCount: weixinCount, toutiaoCount: toutiaoCount, qqomCount: qqomCount]
        }
        return apiResult(status: HttpStatus.SC_OK, count: count)
    }

    Map getTeamNameList(String orgId) {
        List teamNameList = statisticsManageRepo.getTeamNameList(orgId)
        def result = []
        teamNameList.each {
            if (it != null && it != "") {
                result << it
            }
        }
        return apiResult(status: HttpStatus.SC_OK, list: result)
    }

    Map getPublishTrend(String orgId, int trendType) {
        def result = []
        List trendList = statisticsManageRepo.getPublishTrend(orgId, trendType)
        def timeRange = []
        for (int i = trendType; i > 0; i--) {
            Date date = DateUitl.addDay(new Date(), -i)
            String day = DateUitl.convertFormatDate(date, "MM月dd日")
            timeRange << day
        }
        def cms = []
        def weibo = []
        def wechat = []
        def toutiao = []
        def qqom = []
        def trendMap = [:]
        trendList.each { trend ->
            long time = trend.publishTime.getTime()
            String date = DateUitl.convertFormatDate(time, "MM月dd日")
            trendMap.put(date, trend)
        }
        timeRange.each {
            def trend = trendMap.get(it)
            if (trend) {
                cms << trend.manualPushCount
                weibo << trend.weiboCount
                wechat << trend.wechatCount
                toutiao << trend.toutiaoCount
                qqom << trend.qqomCount
            } else {
                cms << 0
                weibo << 0
                wechat << 0
                toutiao << 0
                qqom << 0
            }
        }
        result = [
                "CMS": cms,
                "微博" : weibo,
                "微信" : wechat,
                "头条号": toutiao,
                "企鹅号": qqom
        ]
        return apiResult(status: HttpStatus.SC_OK, data: result, timeRange: timeRange)
    }

    Map getPublishDetailInfo(String orgId, Date startDate, Date endDate, String channelType, String teamName, String publisher, int pageNo, int pageSize) {
        def list = statisticsManageRepo.getPublishDetailInfo(orgId, startDate, endDate, channelType, teamName, publisher, pageNo, pageSize)
        return apiResult(status: HttpStatus.SC_OK, list: list)
    }

    Map getPublishStatistics(String orgId, Date startDate, Date endDate, String teamName, String publisher) {
        def teamIds = []
        if (teamName){
            teamIds = statisticsManageRepo.getTeamIds(teamName, orgId)
        }
        def teamNamePublishList = statisticsManageRepo.getPublishStatistics(orgId, startDate, endDate, teamName, teamIds, publisher)
        def result = []
        def teamDataTotal = [:]
        long publisherTotalCount = 0
        long manualPushTotalCount = 0
        long autoPushTotalCount = 0
        long weiboTotalCount = 0
        long wechatTotalCount = 0
        long toutiaoTotalCount = 0
        long qqomTotalCount = 0
        def teamResult = []
        def notTeamResult = []
        teamNamePublishList.each {
            def teamData = [:]
            String teamId = it.getKey()
            def valueMap = it.getValue()[0]
            long manualPushCount = valueMap.manualPushCount
            long autoPushCount = valueMap.autoPushCount
            long weiboCount = valueMap.weiboCount
            long wechatCount = valueMap.wechatCount
            long toutiaoCount = valueMap.toutiaoCount
            long qqomCount = valueMap.qqomCount
            long publishNumber = manualPushCount + autoPushCount + weiboCount + wechatCount + toutiaoCount + qqomCount

            manualPushTotalCount += manualPushCount
            autoPushTotalCount += autoPushCount
            weiboTotalCount += weiboCount
            wechatTotalCount += wechatCount
            toutiaoTotalCount += toutiaoCount
            qqomTotalCount += qqomCount

            String accountName = ""
            if ("0".equals(teamId)) {
                accountName = "未分组"
            } else {
                Team team = teamRepo.getTeam(teamId)
                String userTeamName = statisticsManageRepo.getTeamNameFromStatistics(orgId, teamId, startDate, endDate)
                accountName = team?.teamName ?: userTeamName
            }
            teamData.accountName = accountName ?: "未分组"
            teamData.teamFlag = true
            teamData.publishNumber = publishNumber
            teamData.manualPushCount = manualPushCount
            teamData.autoPushCount = autoPushCount
            teamData.weiboCount = weiboCount
            teamData.wechatCount = wechatCount
            teamData.toutiaoCount = toutiaoCount
            teamData.qqomCount = qqomCount
            def userList = statisticsManageRepo.getUserPublishStatistics(orgId, teamId, startDate, endDate, publisher)
            def userResult = []
            userList.each {
                long userId = it.getKey()
                def userMap = it.getValue()[0]
                def userData = [:]
                def userInfo = accountRepo.getUserById(userId)
                userData.publisher = userInfo?.realName ?: userInfo.userName
                userData.publishNumber = userMap.manualPushCount + userMap.autoPushCount + userMap.weiboCount + userMap.wechatCount + userMap.toutiaoCount + userMap.qqomCount
                userData.manualPushCount = userMap.manualPushCount
                userData.autoPushCount = userMap.autoPushCount
                userData.weiboCount = userMap.weiboCount
                userData.wechatCount = userMap.wechatCount
                userData.toutiaoCount = userMap.toutiaoCount
                userData.qqomCount = userMap.qqomCount
                userData.isManager = userInfo ? userInfo.isManager : false

                userResult << userData
            }
            teamData.userList = userResult
            if ("未分组".equals(teamData.accountName = accountName)) {
                notTeamResult << teamData
            } else {
                teamResult << teamData
            }
        }

        publisherTotalCount = manualPushTotalCount + autoPushTotalCount + weiboTotalCount + wechatTotalCount + toutiaoTotalCount + qqomTotalCount
        if (publisherTotalCount > 0) {
            teamDataTotal.accountName = "合计"
            teamDataTotal.teamFlag = false
            teamDataTotal.publishNumber = publisherTotalCount
            teamDataTotal.manualPushCount = manualPushTotalCount
            teamDataTotal.autoPushCount = autoPushTotalCount
            teamDataTotal.weiboCount = weiboTotalCount
            teamDataTotal.wechatCount = wechatTotalCount
            teamDataTotal.toutiaoCount = toutiaoTotalCount
            teamDataTotal.qqomCount = qqomTotalCount

            result << teamDataTotal
        }

        result += teamResult
        result += notTeamResult
        return apiResult(status: HttpStatus.SC_OK, list: result)
    }

}
