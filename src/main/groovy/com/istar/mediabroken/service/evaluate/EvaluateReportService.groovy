package com.istar.mediabroken.service.evaluate

import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.account.Account
import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.entity.evaluate.EvaluateChannel
import com.istar.mediabroken.entity.evaluate.EvaluateReport
import com.istar.mediabroken.entity.evaluate.UserStatusEnum
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.repo.evaluate.*
import com.istar.mediabroken.utils.DateUitl
import com.istar.mediabroken.utils.StringUtils
import com.istar.mediabroken.utils.WordUtils
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import sun.misc.BASE64Decoder

/**
 * @author zxj
 * @create 2018/7/2
 */
@Service
@Slf4j
class EvaluateReportService {

    @Autowired
    EvaluateReportRepo evaluateReportRepo
    @Autowired
    EvaluateTeamRepo evaluateTeamRepo
    @Autowired
    EvaluateChannelRepo evaluateChannelRepo
    @Autowired
    AccountRepo accountRepo
    @Autowired
    EvaluateRepo evaluateRepo
    @Autowired
    EvaluateService evaluateService
    @Value('${image.upload.path}')
    String UPLOAD_PATH

    @Value('${env}')
    String env

    @Autowired
    ChannelSummaryRepo channelSummaryRepo
    @Autowired
    EvaluateChannelDetailRepo evaluateChannelDetailRepo
    @Autowired
    EvaluateContentService evaluateContentService

    def getEvaluateReportById(long userId, String reportId) {
        return evaluateReportRepo.getEvaluateReportById(userId, [3], reportId, [true])
    }

    def retryEvaluateReportById(long userId, String reportId) {
        def id = evaluateReportRepo.getEvaluateReportById(userId, [4], reportId, [true])
        if (id.status != 4 || id == null) {
            return false
        }
        def status = evaluateReportRepo.modifyEvaluateReportStatus(userId, reportId, 1)
    }

    def delEvaluateReportById(long userId, String reportId) {

        //处理失败的都删除
        EvaluateReport report = evaluateReportRepo.getEvaluateReportById(userId, [4], reportId, [false, true])
        if (report) {
            evaluateReportRepo.delEvaluateReportById(report.userId, report.id)
            def info = evaluateService.removeEvaluateInfo(reportId)
            def flag = true
            if (info == 0) {
                flag = false
            }
            return flag
        }
        //处理的或者成功的逻辑删除
        def id = evaluateReportRepo.modifyEvaluateReportByValid(userId, [2, 3], reportId, false)
        //删除去summary表的数据

        return id
    }

    List getEvaluateReportByStatusAndName(long userId, String evaluateName, int status, int pageSize, int pageNo) {
        def name = evaluateReportRepo.getEvaluateReportByStatusAndName(userId, evaluateName, status, [true], pageSize, pageNo)
        return name
    }

    def addEvaluateReport(long userId, String evaluateName, String channelsName, boolean isAuto, Date startTime, Date endTime) {
        List<EvaluateChannel> channelList = evaluateChannelRepo.getUserAllChannel(userId)
        def account = accountRepo.getUserById(userId)
        int statFlag = 2
        if (account) {
            if (account.userType.equals(Account.USERTYPE_TRIAL)) {
                statFlag = 2
            }
            if (account.userType.equals(Account.USERTYPE_OFFICIAL)) {
                statFlag = 1
            }
        }

        def channels = []
        channelList.each { elem ->
            def map = [:]
            map.put("evaluateChannelId", elem.id)
            map.put("siteName", elem.siteName)
            map.put("siteDomain", elem.siteDomain)
            map.put("siteType", elem.siteType)
            map.put("evaluateTeamId", elem.evaluateTeamId)
            if (elem.evaluateTeamId.equals("0")) {
                map.put("evaluateTeamName", "未分组")
            } else {
                def team = evaluateTeamRepo.getEvaluateTeamById(elem.userId, elem.evaluateTeamId)
                String teamName = "未分组"
                if (team) {
                    teamName = team.teamName
                }
                map.put("evaluateTeamName", teamName)
            }
            channels.add(map)
            //同时处理数据存到channelForStat 给大数据做统计
            def channelStat = evaluateChannelRepo.getChannelForStat(elem.siteName, elem.siteDomain, elem.siteType, statFlag)
            if (!channelStat) {
                evaluateChannelRepo.addChannelForStat(elem.siteName, elem.siteDomain, elem.siteType, statFlag)
            }

        }
        def report = new EvaluateReport()
        report.setId(UUID.randomUUID().toString())
        report.setUserId(userId)
        report.setEvaluateName(evaluateName)
        report.setChannelsName(channelsName)
        report.setChannels(channels)
        report.setIsAuto(isAuto)
        report.setStatus(1)
        report.setStartTime(startTime)
        report.setEndTime(endTime)
        report.setCreateTime(new Date())
        report.setUpdateTime(new Date())
        report.setValid(true)
        evaluateReportRepo.addEvaluate(report)
        evaluateService.addEvaluateInfo(report)

    }

    def getEvaluateByTime(long userId, boolean isAuto, Date createStr, Date createEnd) {
        def time = evaluateReportRepo.getEvaluateByTime(userId, isAuto, createStr, createEnd)
        return time
    }

    def getEvaluateByName(long userId, String evaluateName) {
        def time = evaluateReportRepo.getEvaluateByName(userId, true, evaluateName)
        return time
    }

    //定时任务调的(每周的)
    def autoAddEvaluateReport() {
        def report = evaluateReportRepo.getUserEvaluateReport()
        String name = "自动测评"
        def date = new Date()
        def format = date.format("yyyyMMdd")
        def evaluateName = format + name
        report.each { elem ->
            def rep = evaluateReportRepo.getUserLastEvaluateReportById(elem._id)
            this.addEvaluateReport(rep.userId, evaluateName, rep.channelsName, true, DateUitl.getLastWeekMondayBegin(new Date()), DateUitl.addSeconds(DateUitl.getThisWeekMondayBegin(new Date()), -1))
        }
    }
    //修改status的状态
    def checkEvaluateReportStatus() {
        Date nowTime = new Date()
        List<EvaluateReport> reports = evaluateReportRepo.getEvaluateReportByStatus(1)
        def daily = []
        def weekly = []
        def channelSize = 0
        reports.each { elem ->
            List channelList = elem.channels
            channelSize = channelList.size()
            channelList.each { channel ->
                def siteName = channel.siteName
                def siteDomain = channel.siteDomain
                def siteType = channel.siteType
                def che = null
                def week = null
                if (siteType == Site.SITE_TYPE_WEBSITE) {
                    che = evaluateRepo.getDailyEvaluateChannelByTime(siteType, siteDomain, elem.startTime, elem.endTime)
                    week = evaluateRepo.getWeeklyEvaluateChannelByTime(siteType, siteDomain, elem.startTime, elem.endTime)
                }
                if (siteType == Site.SITE_TYPE_WECHAT) {
                    che = evaluateRepo.getDailyEvaluateChannelByTime(siteType, siteName, elem.startTime, elem.endTime)
                    week = evaluateRepo.getWeeklyEvaluateChannelByTime(siteType, siteName, elem.startTime, elem.endTime)

                }
                if (siteType == Site.SITE_TYPE_WEIBO) {
                    che = evaluateRepo.getDailyEvaluateChannelByTime(siteType, siteName, elem.startTime, elem.endTime)
                    week = evaluateRepo.getWeeklyEvaluateChannelByTime(siteType, siteName, elem.startTime, elem.endTime)
                }
                if (che != null && week != null) {
                    daily.add(che)
                    weekly.add(week)
                }

            }

            if (daily != null && weekly != null && weekly.size() == channelSize) {
                //有数据了 处理周四力和每天四力
                try {
                    evaluateService.dailyEvaluateChannelFourPower(elem.userId)
                    evaluateService.weeklyEvaluateChannelFourPower(elem.userId)

                } catch (e) {
                    print(e)
                    evaluateReportRepo.modifyEvaluateReportStatus(elem.userId, elem.id, 4)
                    return
                }
                //待生成（有数据）
                evaluateReportRepo.modifyEvaluateReportStatus(elem.userId, elem.id, 2)
                //更新测评概览信息(因为根据status 为2计算的)
                try {
                    evaluateService.updateEvaluateInfo(elem.id)
                    evaluateService.addIndexTop(elem.id)

                } catch (e1) {
                    print(e1)
                    evaluateReportRepo.modifyEvaluateReportStatus(elem.userId, elem.id, 4)
                    return
                }

            }
            //130分后
            if (DateUitl.addMins(elem.createTime, 150).time < nowTime.time) {
                //有数据了 处理周四力和每天四力
                try {
                    evaluateService.dailyEvaluateChannelFourPower(elem.userId)
                    evaluateService.weeklyEvaluateChannelFourPower(elem.userId)

                } catch (e) {
                    print(e)
                    evaluateReportRepo.modifyEvaluateReportStatus(elem.userId, elem.id, 4)
                    return
                }
                //待生成（有数据）
                evaluateReportRepo.modifyEvaluateReportStatus(elem.userId, elem.id, 2)
                //更新测评概览信息(因为根据status 为2计算的)
                try {
                    log.info("报告状态超时，调用接口" + new Date())
                    evaluateService.updateEvaluateInfo(elem.id)
                    evaluateService.addIndexTop(elem.id)

                } catch (e1) {
                    print(e1)
                    evaluateReportRepo.modifyEvaluateReportStatus(elem.userId, elem.id, 4)
                    return
                }
            }
            daily.clear()
            weekly.clear()
        }

    }

    def createReport(LoginUser user, String reportId, List imgList) {
        EvaluateReport report = evaluateReportRepo.getEvaluateReportById(user.userId, [2], reportId, [true])
        if (!report) {
            return "报告已生成！"
        }
        def channelSummary = channelSummaryRepo.getEvaluateInfoByEvaId(report.id)
        Map<String, Object> data = new HashMap<String, Object>()
        data.put("fileName", report.id)
        data.put("fileType", "evaluate")
        data.put("channelName", report.channelsName)
        data.put("startTime", DateUitl.convertStrDate(report.startTime))
        data.put("endTime", DateUitl.convertStrDate(report.endTime))
        data.put("channelCount", channelSummary.channelCount)
        data.put("articleCount", channelSummary.articleCount)
        data.put("multiple", channelSummary.multiple)

        def multipleRate = ""
        if (channelSummary.multipleRate && channelSummary.multipleRate.contains("-")) {
            multipleRate = "下降" + channelSummary.multipleRate.replaceAll("-", "")
        } else if (channelSummary.multipleRate && !channelSummary.multipleRate.contains("-")) {
            multipleRate = "上升" + channelSummary.multipleRate
        }
        if (!channelSummary.multipleRate) {
            multipleRate = "变化率为 0.00%"
        }
        data.put("multipleRate", multipleRate)
        data.put("psi", channelSummary.psi ?: 0)

        def psiRate = ""
        if (channelSummary.psiRate && channelSummary.psiRate.contains("-")) {
            psiRate = "下降" + channelSummary.psiRate.replaceAll("-", "")
        } else if (channelSummary.psiRate && !channelSummary.psiRate.contains("-")) {
            psiRate = "上升" + channelSummary.psiRate
        }
        if (!channelSummary.psiRate) {
            psiRate = "变化率为 0.00%"
        }

        data.put("psiRate", psiRate)
        data.put("mii", channelSummary.mii ?: 0)


        def miiRate = ""
        if (channelSummary.miiRate && channelSummary.miiRate.contains("-")) {
            miiRate = "下降" + channelSummary.miiRate.replaceAll("-", "")
        } else if (channelSummary.miiRate && !channelSummary.miiRate.contains("-")) {
            miiRate = "上升" + channelSummary.miiRate
        }
        if (!channelSummary.miiRate) {
            miiRate = "变化率为 0.00%"
        }

        data.put("miiRate", miiRate)
        data.put("bsi", channelSummary.bsi ?: 0)

        def bsiRate = ""
        if (channelSummary.bsiRate && channelSummary.bsiRate.contains("-")) {
            bsiRate = "下降" + channelSummary.bsiRate.replaceAll("-", "")
        } else if (channelSummary.bsiRate && !channelSummary.bsiRate.contains("-")) {
            bsiRate = "上升" + channelSummary.bsiRate
        }
        if (!channelSummary.bsiRate) {
            bsiRate = "变化率为 0.00%"
        }
        data.put("bsiRate", bsiRate)
        data.put("tsi", channelSummary.tsi ?: 0)

        def tsiRate = ""
        if (channelSummary.tsiRate && channelSummary.tsiRate.contains("-")) {
            tsiRate = "下降" + channelSummary.tsiRate.replaceAll("-", "")
        } else if (channelSummary.tsiRate && !channelSummary.tsiRate.contains("-")) {
            tsiRate = "上升" + channelSummary.tsiRate
        }
        if (!channelSummary.tsiRate) {
            tsiRate = "变化率为 0.00%"
        }

        String outfilePath = "/${UPLOAD_PATH}/download/${data.fileType as String}/${data.fileName as String}";
        File outPath = new File(outfilePath)
        if (!outPath.exists()) {
            FileUtils.forceMkdir(outPath)
        } else {
            FileUtils.forceDelete(outPath)
            FileUtils.forceMkdir(outPath)
        }

        data.put("tsiRate", tsiRate)

        //图片处理
        //综合
        this.ImgStr(outfilePath, this.getImgSplit(imgList[0].toString()), "multipleImg")
        data.put("multipleImg", this.getImgSplit(imgList[0].toString()))
        //发文量
        this.ImgStr(outfilePath, this.getImgSplit(imgList[1].toString()), "publishImg")
        data.put("publishImg", this.getImgSplit(imgList[1].toString()))
        //传播力
        this.ImgStr(outfilePath, this.getImgSplit(imgList[2].toString()), "psiImg")
        data.put("psiImg", this.getImgSplit(imgList[2].toString()))
        //影响力
        this.ImgStr(outfilePath, this.getImgSplit(imgList[3].toString()), "miiImg")
        data.put("miiImg", this.getImgSplit(imgList[3].toString()))
        //引导力
        this.ImgStr(outfilePath, this.getImgSplit(imgList[4].toString()), "bsiImg")
        data.put("bsiImg", this.getImgSplit(imgList[4].toString()))
        //公信力
        this.ImgStr(outfilePath, this.getImgSplit(imgList[5].toString()), "tsiImg")
        data.put("tsiImg", this.getImgSplit(imgList[5].toString()))
        //平均阅读点赞
        this.ImgStr(outfilePath, this.getImgSplit(imgList[6].toString()), "avgReadAndLikeImg")
        data.put("avgReadAndLikeImg", this.getImgSplit(imgList[6].toString()))
        //词云
        this.ImgStr(outfilePath, this.getImgSplit(imgList[7].toString()), "wordCloudImg")
        data.put("wordCloudImg", this.getImgSplit(imgList[7].toString()))

        //网站综合
        def webMultipleList = evaluateService.getWebsiteRank("multiple", report, 1, 10).list
        def webSiteMultiple = []
        webMultipleList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    multiple       : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            webSiteMultiple.add(map)
        }
        data.put("webSiteMultiple", webSiteMultiple)

        def wechatMultipleList = evaluateService.getWechatRank("multiple", report, 1, 10).list
        def wechatMultiple = []
        wechatMultipleList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    multiple       : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            wechatMultiple.add(map)
        }
        data.put("wechatMultiple", wechatMultiple)

        def weboMultipleList = evaluateService.getWeiboRank("multiple", report, 1, 10).list
        def weiboMultiple = []
        weboMultipleList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    multiple       : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            weiboMultiple.add(map)
        }
        data.put("weiboMultiple", weiboMultiple)

        def webPublishList = evaluateService.getWebsiteRank("publishCount", report, 1, 10).list
        def webSitePublish = []
        webPublishList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    publishCount   : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            webSitePublish.add(map)
        }
        data.put("webSitePublish", webSitePublish)

        def chatPublishList = evaluateService.getWechatRank("publishCount", report, 1, 10).list
        def wechatPublish = []
        chatPublishList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    publishCount   : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            wechatPublish.add(map)
        }


        data.put("wechatPublish", wechatPublish)

        def weiboPublishList = evaluateService.getWeiboRank("publishCount", report, 1, 10).list
        def weiboPublish = []
        weiboPublishList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    publishCount   : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            weiboPublish.add(map)
        }
        data.put("weiboPublish", weiboPublish)

        def webPsiList = evaluateService.getWebsiteRank("psi", report, 1, 10).list
        def webSitePsi = []
        webPsiList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    psi            : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            webSitePsi.add(map)
        }
        data.put("webSitePsi", webSitePsi)

        def chatPsiList = evaluateService.getWechatRank("psi", report, 1, 10).list
        def wechatPsi = []
        chatPsiList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    psi            : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            wechatPsi.add(map)
        }
        data.put("wechatPsi", wechatPsi)

        def weiboPsiList = evaluateService.getWeiboRank("psi", report, 1, 10).list
        def weiboPsi = []
        weiboPsiList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    psi            : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            weiboPsi.add(map)
        }
        data.put("weiboPsi", weiboPsi)

        def webMiiList = evaluateService.getWebsiteRank("mii", report, 1, 10).list
        def webSuiteMii = []
        webMiiList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    mii            : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            webSuiteMii.add(map)
        }
        data.put("webSuiteMii", webSuiteMii)

        def chatMiiList = evaluateService.getWechatRank("mii", report, 1, 10).list
        def wechatMii = []
        chatMiiList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    mii            : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            wechatMii.add(map)
        }
        data.put("wechatMii", wechatMii)

        def weiboMiiList = evaluateService.getWeiboRank("mii", report, 1, 10).list
        def weiboMii = []
        weiboMiiList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    mii            : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            weiboMii.add(map)
        }
        data.put("weiboMii", weiboMii)

        def webSiteBsiList = evaluateService.getWebsiteRank("bsi", report, 1, 10).list
        def webSiteBsi = []
        webSiteBsiList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    bsi            : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            webSiteBsi.add(map)
        }
        data.put("webSiteBsi", webSiteBsi)

        def chatBsiList = evaluateService.getWechatRank("bsi", report, 1, 10).list
        def wechatBsi = []
        chatBsiList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    bsi            : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            wechatBsi.add(map)
        }
        data.put("wechatBsi", wechatBsi)

        def weiboBsiList = evaluateService.getWeiboRank("bsi", report, 1, 10).list
        def weiboBsi = []
        weiboBsiList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    bsi            : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            weiboBsi.add(map)
        }
        data.put("weiboBsi", weiboBsi)

        def webSiteTsiList = evaluateService.getWebsiteRank("tsi", report, 1, 10).list
        def webSiteTsi = []
        webSiteTsiList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    tsi            : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            webSiteTsi.add(map)
        }
        data.put("webSiteTsi", webSiteTsi)

        def chatTsiList = evaluateService.getWechatRank("tsi", report, 1, 10).list
        def wechatTsi = []
        chatTsiList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    tsi            : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            wechatTsi.add(map)
        }
        data.put("wechatTsi", wechatTsi)

        def weiboTsiList = evaluateService.getWeiboRank("tsi", report, 1, 10).list
        def weiboTsi = []
        weiboTsiList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    tsi            : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            weiboTsi.add(map)
        }
        data.put("weiboTsi", weiboTsi)

        def chatSumReadList = []
        if (!user.userType.equals(Account.USERTYPE_TRIAL)) {
            chatSumReadList = evaluateService.getWechatRank("sumRead", report, 1, 10).list
        }
        def wechatSumRead = []
        chatSumReadList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    sumRead        : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            wechatSumRead.add(map)
        }
        data.put("wechatSumRead", wechatSumRead)

        def chatSumLikeList = []
        if (!user.userType.equals(Account.USERTYPE_TRIAL)) {
            chatSumLikeList = evaluateService.getWechatRank("sumLike", report, 1, 10).list
        }
        def wechatSumLike = []
        chatSumLikeList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    sumLike        : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            wechatSumLike.add(map)
        }
        data.put("wechatSumLike", wechatSumLike)

        def chatAvgReadList = []
        if (!user.userType.equals(Account.USERTYPE_TRIAL)) {
            chatAvgReadList = evaluateService.getWechatRank("avgRead", report, 1, 10).list
        }
        def wechatAvgRead = []
        chatAvgReadList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    avgRead        : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            wechatAvgRead.add(map)
        }
        data.put("wechatAvgRead", wechatAvgRead)

        def chatAvgLikeList = []
        if (!user.userType.equals(Account.USERTYPE_TRIAL)) {
            chatAvgLikeList = evaluateService.getWechatRank("avgLike", report, 1, 10).list
        }
        def wechatAvgLike = []
        chatAvgLikeList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    avgLike        : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            wechatAvgLike.add(map)
        }
        data.put("wechatAvgLike", wechatAvgLike)

        def weiboSumLikeList = []
        if (!user.userType.equals(Account.USERTYPE_TRIAL)) {
            weiboSumLikeList = evaluateService.getWeiboRank("sumLike", report, 1, 10).list
        }
        def weiboSumLike = []
        weiboSumLikeList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    sumLike        : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            weiboSumLike.add(map)
        }
        data.put("weiboSumLike", weiboSumLike)

        def weiboSumReprintList = evaluateService.getWeiboRank("sumReprint", report, 1, 10).list
        def weiboSumReprint = []
        weiboSumReprintList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    sumReprint     : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            weiboSumReprint.add(map)
        }
        data.put("weiboSumReprint", weiboSumReprint)

        def weiboAvgLikeList = []
        if (!user.userType.equals(Account.USERTYPE_TRIAL)) {
            weiboAvgLikeList = evaluateService.getWeiboRank("avgLike", report, 1, 10).list
        }
        def weiboAvgLike = []
        weiboAvgLikeList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    avgLike        : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            weiboAvgLike.add(map)
        }
        data.put("weiboAvgLike", weiboAvgLike)

        def weiboAvgReprintList = evaluateService.getWeiboRank("avgReprint", report, 1, 10).list
        def weiboAvgReprint = []
        weiboAvgReprintList.eachWithIndex { it, i ->
            def map = [
                    index          : (i + 1),
                    channelName    : it.channelName,
                    avgReprint     : it.index,
                    team           : it.teamName,
                    status         : this.getUserStatus(it.channelStatus),
                    lastPublishTime: DateUitl.convertStrDate(it.lastPublishTime),
                    siteType       : this.getSiteType(it.siteType)
            ]
            weiboAvgReprint.add(map)
        }
        data.put("weiboAvgReprint", weiboAvgReprint)

        def contentSumRead = []
        if (!user.userType.equals(Account.USERTYPE_TRIAL)) {
            contentSumRead = evaluateContentService.getNewsRankByrankType(report, "sumRead")
        }
        def contentRead = []
        contentSumRead.eachWithIndex { it, i ->
            def map = [
                    index      : (i + 1),
                    title      : it.title,
                    readCount  : it.readCount,
                    siteType   : this.getSiteType(it.siteType),
                    channelName: it.siteName,
                    poster     : it.poster,
                    publishTime: DateUitl.convertStrDate(it.publishTime),
                    url        : StringUtils.replaceSpecialWord(it.url)
            ]
            contentRead.add(map)
        }
        data.put("contentRead", contentRead)

        def contentSumLike = []
        if (!user.userType.equals(Account.USERTYPE_TRIAL)) {
            contentSumLike = evaluateContentService.getNewsRankByrankType(report, "sumLike")
        }
        def contentLike = []
        contentSumLike.eachWithIndex { it, i ->
            def map = [
                    index      : (i + 1),
                    title      : it.title,
                    likeCount  : it.likeCount,
                    siteType   : this.getSiteType(it.siteType),
                    channelName: it.siteName,
                    poster     : it.poster,
                    publishTime: DateUitl.convertStrDate(it.publishTime),
                    url        : StringUtils.replaceSpecialWord(it.url)
            ]
            contentLike.add(map)
        }
        data.put("contentLike", contentLike)

        def contentSumReprint = []
        if (!user.userType.equals(Account.USERTYPE_TRIAL)) {
            contentSumReprint = evaluateContentService.getNewsRankByrankType(report, "sumReprint")
        }
        def contentReprint = []
        contentSumReprint.eachWithIndex { it, i ->
            def map = [
                    index       : (i + 1),
                    title       : it.title,
                    reprintCount: it.reprintCount,
                    siteType    : this.getSiteType(it.siteType),
                    channelName : it.siteName,
                    poster      : it.poster,
                    publishTime : DateUitl.convertStrDate(it.publishTime),
                    url         : StringUtils.replaceSpecialWord(it.url)
            ]
            contentReprint.add(map)
        }
        data.put("contentReprint", contentReprint)

        WordUtils.createWord(UPLOAD_PATH, data, env)
        //生成word后处理status 状态
        evaluateReportRepo.modifyEvaluateReportStatus(user.userId, reportId, 3)
        evaluateReportRepo.modifyEvaluateReportIndicator(user.userId, reportId, true)
        return '生成成功！'
    }

    def modifyEvaluateReportIndicator(long userId, String reportId, boolean indicator) {
        evaluateReportRepo.modifyEvaluateReportIndicator(userId, reportId, indicator)
    }

    def ImgStr(def filePath, String img, String name) {
        byte[] b = new BASE64Decoder().decodeBuffer(img);
        File file = new File(filePath, name + ".png");
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(b);
        if (fos != null) {
            fos.close();
        }
    }


    def getUserStatus(int status) {
        if (status == UserStatusEnum.active.key) {
            return UserStatusEnum.active.desc
        }
        if (status == UserStatusEnum.negative.key) {
            return UserStatusEnum.negative.desc
        }
        if (status == UserStatusEnum.unusual.key) {
            return UserStatusEnum.unusual.desc
        }
    }

    def getSiteType(int siteType) {
        if (siteType == Site.SITE_TYPE_WEBSITE) {
            return "网站"
        }
        if (siteType == Site.SITE_TYPE_WECHAT) {
            return "微信"
        }
        if (siteType == Site.SITE_TYPE_WEIBO) {
            return "微博"
        }
    }

    def downLoadEvaluateReport(long userId, String reportId) {
        def filePath = new File(UPLOAD_PATH, 'download').path
        return filePath.toString() + File.separator + "evaluate" + File.separator + reportId + File.separator + reportId + ".doc"
    }

    String getImgSplit(String img) {
        return img.split("data:image/png;base64,").getAt(1).toString()
    }
}
