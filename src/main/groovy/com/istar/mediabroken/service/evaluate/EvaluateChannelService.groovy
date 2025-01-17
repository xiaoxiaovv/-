package com.istar.mediabroken.service.evaluate

import com.istar.mediabroken.entity.account.Account
import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.entity.evaluate.EvaluateChannel
import com.istar.mediabroken.entity.evaluate.EvaluateReport
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.repo.admin.SettingRepo
import com.istar.mediabroken.repo.evaluate.EvaluateChannelRepo
import com.istar.mediabroken.repo.evaluate.EvaluateRepo
import com.istar.mediabroken.repo.evaluate.EvaluateReportRepo
import com.istar.mediabroken.service.account.AccountService
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * @author zxj
 * @create 2018/6/20
 */
@Service
@Slf4j
class EvaluateChannelService {

    @Autowired
    EvaluateChannelRepo evaluateChannelRepo
    @Autowired
    EvaluateTeamService evaluateTeamService
    @Autowired
    EvaluateService evaluateService
    @Autowired
    EvaluateRepo evaluateRepo
    @Autowired
    AccountRepo accountRepo
    @Autowired
    SettingRepo settingRepo
    @Autowired
    AccountService accountService
    @Autowired
    EvaluateReportRepo evaluateReportRepo

    List findAllChannel(long userId) {
        return evaluateChannelRepo.getUserAllChannel(userId)
    }
    List<EvaluateChannel> findChannelByTypeAndName(long userId, int type, String name, String teamId) {
        return evaluateChannelRepo.findChannelByTypeAndName(userId, type, name, teamId)
    }

    List getEvaluateChannelAndTeam(long userId, int type, String name) {
        def list = []
        def teamsList = evaluateTeamService.getEvaluateTeams(userId)
        if (!teamsList) {
            def channelList = this.findChannelByTypeAndName(userId, type, name, '0')
            def map = [
                    id                 : '0',
                    userId             : userId,
                    teamName           : '未分组',
                    evaluateChannelList: channelList
            ]
            list.add(map)
            return list
        }
        //有分组的
        def each = teamsList.each { team ->
            def channelList = this.findChannelByTypeAndName(userId, type, name, team.id)
            def map = [
                    id                 : team.id,
                    userId             : userId,
                    teamName           : team.teamName,
                    evaluateChannelList: channelList
            ]
            list.add(map)
        }
        //未分组
        def channelList = this.findChannelByTypeAndName(userId, type, name, '0')
        if (channelList) {
            def map = [
                    id                 : '0',
                    userId             : userId,
                    teamName           : '未分组',
                    evaluateChannelList: channelList
            ]
            list.add(map)
        }
        return list
    }

    EvaluateChannel findById(long userId, String channelId){
        return evaluateChannelRepo.findById(userId, channelId)
    }

    def setChannelMultipleWeight(long userId, String channelId, String mediaAttrName, double mediaAttrVal, int psiWeight, int miiWeight, int tsiWeight, int bsiWeight) {
        Map mediaAttr = [:]
        mediaAttr.put(mediaAttrName, mediaAttrVal)

        Map parameters = ["传播力": psiWeight,
                          "影响力": miiWeight,
                          "引导力": bsiWeight,
                          "公信力": tsiWeight]
        Map multipleWeight = [mediaAttr: mediaAttr, parameters: parameters]
        def weight = evaluateChannelRepo.setChannelMultipleWeight(userId, channelId, multipleWeight)
        if (weight) {
            //更新表
            this.modifyEvaluateChannelDetailAndDaily(userId, channelId)
        }
        return weight
    }

    def setChannelFourPowerWeight(long userId, String channelId, String fourPower, String classificationName, double classificationVal, int standardKey) {
        def res = true
        try {
            Map classification = [:]
            classification.put(classificationName, classificationVal)
            Map map = [classification: classification, standard: standardKey]
            //传播力
            if ("psi".equals(fourPower)) {
                evaluateChannelRepo.setChannelPsiWeight(userId, channelId, map)
            }
            //影响力
            if ("mii".equals(fourPower)) {
                evaluateChannelRepo.setChannelMiiWeight(userId, channelId, map)
            }
            //公信力
            if ("tsi".equals(fourPower)) {
                evaluateChannelRepo.setChannelTsiWeight(userId, channelId, map)
            }
            //引导力
            if ("bsi".equals(fourPower)) {
                evaluateChannelRepo.setChannelBsiWeight(userId, channelId, map)
            }
            this.modifyEvaluateChannelDetailAndDaily(userId, channelId)
        } catch (e) {
            res = false
        }
        return res
    }

    def intiChannelWeight(long userId, String channelId, String fourPower) {
        def res = true
        try {
            Map map = [:]
            //综合
            if ("multiple".equals(fourPower)) {
                evaluateChannelRepo.setChannelMultipleWeight(userId, channelId, map)
            }
            //传播力
            if ("psi".equals(fourPower)) {
                evaluateChannelRepo.setChannelPsiWeight(userId, channelId, map)
            }
            //影响力
            if ("mii".equals(fourPower)) {
                evaluateChannelRepo.setChannelMiiWeight(userId, channelId, map)
            }
            //公信力
            if ("tsi".equals(fourPower)) {
                evaluateChannelRepo.setChannelTsiWeight(userId, channelId, map)
            }
            //引导力
            if ("bsi".equals(fourPower)) {
                evaluateChannelRepo.setChannelBsiWeight(userId, channelId, map)
            }
            this.modifyEvaluateChannelDetailAndDaily(userId, channelId)
        } catch (e) {
            res = false
        }
        return res
    }

    //用户修改权重配置时候修改的表
    def modifyEvaluateChannelDetailAndDaily(long userId, String channelId) {
        //获取报个表
        def evaluateReport = evaluateService.getEvaluateReportByUserId(userId)
        //获取渠道
        def channel = this.findById(userId, channelId)
        //计算周四力和综合指数
        Map power = evaluateService.getWeeklyFourPower(channelId, userId, channel.siteType,channel.siteName, channel.siteDomain, evaluateReport.startTime, evaluateReport.endTime)
        //计算上升率
        Map rate = evaluateService.getFourPowerRate(userId, evaluateReport.id, channel.id, evaluateReport.startTime, evaluateReport.endTime)
        def detail = evaluateChannelRepo.getEvaluateChannelDetail(userId, evaluateReport.id, channel.id, evaluateReport.startTime, evaluateReport.endTime)
        //修改渠道四力表EvaluateChannelDetail
        evaluateChannelRepo.updateEvaluateChannelDetail(detail.id, userId,
                power.psi, power.mii, power.tsi, power.bsi, power.multiple,
                rate.psiRate ?: 0.00, rate.miiRate ?: 0.00, rate.tsiRate ?: 0.00, rate.bsiRate ?: 0.00, rate.multipleRate ?: 0.00)
        //查询出dailyFourPower表四力计算加权四力
        List channelList = evaluateService.getDailyEvaluateChannel(channel.siteType,channel.siteName, channel.siteDomain, evaluateReport.startTime, evaluateReport.endTime)
        //修改EvaluateChannelDaily
        channelList.each { elem ->
            //计算每天的四力和综合指数
            Map map = evaluateService.getChannelFourPower(channelId, userId, elem)
            //获取转载阅读数
            def siteInfo = evaluateService.getSiteInfoDailyEvaluateChannel(channel.siteType,channel.siteName, channel.siteDomain, elem.time, elem.time)
            def daily = evaluateChannelRepo.getEvaluateChannelDetailList(userId, evaluateReport.id, channelId, elem.time, elem.time).get(0)
            if (siteInfo){
                evaluateChannelRepo.updateEvaluateChannelDaily(daily.id, userId,
                        map.psi ?: 0.00, map.mii ?: 0.00, map.tsi ?: 0.00, map.bsi ?: 0.00, map.multiple ?: 0.00,
                        siteInfo.publishCount ?: 0l, siteInfo.sumReprint ?: 0l, siteInfo.avgReprint ?: 0.00, siteInfo.sumRead ?: 0l, siteInfo.avgRead ?: 0.00, siteInfo.sumLike ?: 0l, siteInfo.avgLike ?: 0.00
                )
            }

            if (!siteInfo){
                evaluateChannelRepo.updateEvaluateChannelDaily(daily.id, userId,
                        map.psi ?: 0.00, map.mii ?: 0.00, map.tsi ?: 0.00, map.bsi ?: 0.00, map.multiple ?: 0.00,
                        0l,0l, 0.00, 0l, 0.00, 0l, 0.00
                )
            }
        }
        //更新测评概览信息
        evaluateService.updateEvaluateInfo(evaluateReport.id)
        evaluateService.updateIndexTop(evaluateReport.id)
    }


    def addEvaluateChannel(long userId, int siteType, String siteName, String siteDomain, String evaluateTeamId) {
        //检查数量上限
        def profile = accountService.getEvaluateChannelAccountProfile(userId)
        def meidaCount = profile.maxMediaSiteCount
        def wechatCount = profile.maxWechatSiteCount
        def weiboCount = profile.maxWeiboSiteCount
        def size = evaluateChannelRepo.findByUserId(userId, siteType).size()
        if (siteType == Site.SITE_TYPE_WEBSITE) {
            def var = meidaCount - size-1
            if (var < 0) {
                return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "网站数超出限制，已添加" + size + "个网站，还可以添加" + 0 + "个网站！")
            }
            EvaluateChannel evaluateChannel = new EvaluateChannel(UUID.randomUUID().toString(), userId, siteName, siteDomain, siteType, evaluateTeamId, new Date(), new Date())
            def channel = evaluateChannelRepo.addEvaluateChannel(evaluateChannel)
            return apiResult([status: HttpStatus.SC_OK, channelId: channel, msg: "已添加" + (size + 1) + "个网站，还可以添加" + var + "个网站！"])
        }
        if (siteType == Site.SITE_TYPE_WECHAT) {
            def var = wechatCount - size-1
            if (var < 0) {
                return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "公众号数超出限制，已添加" + size + "个公众号，还可以添加" + 0 + "个公众号！")
            }
            EvaluateChannel evaluateChannel = new EvaluateChannel(UUID.randomUUID().toString(), userId, siteName, siteDomain, siteType, evaluateTeamId, new Date(), new Date())
            def channel = evaluateChannelRepo.addEvaluateChannel(evaluateChannel)
            return apiResult([status: HttpStatus.SC_OK, channelId: channel, msg: "已添加" + (size + 1) + "个公众号，还可以添加" + var + "个公众号！"])
        }

        if (siteType == Site.SITE_TYPE_WEIBO) {
            def var = weiboCount - size-1
            if (var < 0) {
                return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "微博数超出限制，已添加" + size + "个微博，还可以添加" + 0 + "个微博！")
            }
            EvaluateChannel evaluateChannel = new EvaluateChannel(UUID.randomUUID().toString(), userId, siteName, siteDomain, siteType, evaluateTeamId, new Date(), new Date())
            def channel = evaluateChannelRepo.addEvaluateChannel(evaluateChannel)
            return apiResult([status: HttpStatus.SC_OK, channelId: channel, msg: "已添加" + (size + 1) + "个微博，还可以添加" + var + "个微博！"])
        }

        return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "请选择正确的渠道类型！")
    }

    def getEvaluateChannelByUserId(long userId, int siteType, String siteName, String siteDomain) {
        def channel = evaluateChannelRepo.findChannel(userId, siteType, siteName, siteDomain, "")
    }

    def getEvaluateChannelByUserIdAndTeam(long userId, int siteType, String siteName, String siteDomain, String teamId) {
        def channel = evaluateChannelRepo.findChannel(userId, siteType, siteName, siteDomain, teamId)
    }

    //生成测评时，把本人渠道下的统计状态改为需要统计
    def modifyStatFlag(long userId, def channelIds) {
        evaluateChannelRepo.modifyStatFlag(userId, channelIds)
    }

    def checkChannel(long userId, String channelId, String teamId) {
        def flag = false
        //获取用户所有有效测评
        try {
            EvaluateChannel id = evaluateChannelRepo.findById(userId, channelId)
            List<EvaluateReport> report = evaluateReportRepo.getEvaluateReportByUser(userId, [1, 2, 3])
            if (channelId) {
                report.each { elem ->
                    elem.channels.each { el ->
                        if (id.siteType == el.siteType && id.siteName.equals(el.siteName) && id.siteDomain.equals(el.siteDomain)) {
                            1 / 0
                        }
                    }
                }
            }

            if (teamId) {
                List<EvaluateChannel> teams = evaluateChannelRepo.findByTeamId(userId, teamId)
                report.each { elem ->
                    elem.channels.each { el ->
                        teams.each { team ->
                            if (team.siteType == el.siteType && team.siteName.equals(el.siteName) && team.siteDomain.equals(el.siteDomain)) {
                                1 / 0
                            }
                        }
                    }
                }
            }


        } catch (e) {
            flag = true
        }
        return flag
    }

    List getChannelList(int siteType, String siteName, String siteDomain) {
        return evaluateChannelRepo.getChannelListByTypeAndName(siteType, siteName, siteDomain)
    }

    def delChannelAndChannelStat(long userId, String channelId) {
        //查询用户的渠道
        def id = this.findById(userId, channelId)

        evaluateChannelRepo.delChannelById(userId, channelId)

        //删除渠道后查询evaluateChannel表
        def list = this.getChannelList(id.siteType, id.siteName, id.siteDomain)
        if (list == null || list.size() <= 0) {
            //evaluateChannel 0条，所以删除evaluateChannel 数据后在把channelForStat 数据删除
            evaluateChannelRepo.delChannelForStatById(id.siteType, id.siteName, id.siteDomain, [1, 2])
        } else {
            //判断evaluateChannel其他用户的数据在 channelForStat数据状态
            int official = 0
            int trial = 0
            list.each { channel ->
                Account account = accountRepo.getUserById(channel.userId)
                //试用
                if (account.userType.equals(Account.USERTYPE_TRIAL)) {
                    trial++
                }
                if (account.userType.equals(Account.USERTYPE_OFFICIAL)) {
                    official++
                }
            }

            if (trial <= 0) {
                //删除channelForStat数据状态 为2的
                evaluateChannelRepo.delChannelForStatById(id.siteType, id.siteName, id.siteDomain, [2])

            }
            if (official <= 0) {
                //删除channelForStat数据状态 为1的
                evaluateChannelRepo.delChannelForStatById(id.siteType, id.siteName, id.siteDomain, [1])
            }
        }

    }


    def modifyEvaluateChannel(long userId, String channelId, int siteType, String siteName, String siteDomain, String evaluateTeamId) {
        //查询用户的渠道
        def id = this.findById(userId, channelId)
        if (!id) {
            apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "修改渠道失败！")
        }
        if (siteType == Site.SITE_TYPE_WEBSITE) {
            evaluateChannelRepo.modifyChannel(userId, channelId, siteType, siteName, siteDomain, evaluateTeamId)
            return apiResult([status: HttpStatus.SC_OK, channelId: channelId, msg: "修改成功！"])
        }
        if (siteType == Site.SITE_TYPE_WECHAT) {
            evaluateChannelRepo.modifyChannel(userId, channelId, siteType, siteName, siteDomain, evaluateTeamId)
            return apiResult([status: HttpStatus.SC_OK, channelId: channelId, msg: "修改成功！"])
        }

        if (siteType == Site.SITE_TYPE_WEIBO) {
            evaluateChannelRepo.modifyChannel(userId, channelId, siteType, siteName, siteDomain, evaluateTeamId)
            return apiResult([status: HttpStatus.SC_OK, channelId: channelId, msg: "修改成功！"])
        }

        //查询
        def list = this.getChannelList(id.siteType, id.siteName, id.siteDomain)

        if (list == null || list.size() <= 0) {
            //evaluateChannel 0条，所以删除evaluateChannel 数据后在把channelForStat 数据删除
            evaluateChannelRepo.delChannelForStatById(id.siteType, id.siteName, id.siteDomain, [1, 2])
        } else {
            //判断evaluateChannel其他用户的数据在 channelForStat数据状态
            int official = 0
            int trial = 0
            list.each { channel ->
                Account account = accountRepo.getUserById(channel.userId)
                //试用
                if (account.userType.equals(Account.USERTYPE_TRIAL)) {
                    trial++
                }
                if (account.userType.equals(Account.USERTYPE_OFFICIAL)) {
                    official++
                }
            }

            if (trial <= 0) {
                //删除channelForStat数据状态 为2的
                evaluateChannelRepo.delChannelForStatById(id.siteType, id.siteName, id.siteDomain, [2])

            }
            if (official <= 0) {
                //删除channelForStat数据状态 为1的
                evaluateChannelRepo.delChannelForStatById(id.siteType, id.siteName, id.siteDomain, [1])
            }
        }

    }


    def modifyChannelTeam(long userId, String teamId, List channelIds) {
        evaluateChannelRepo.modifyChannelTeamId(userId, teamId, channelIds)
    }

    def findForbiddenChannel(String siteDomain, int siteType, String siteName) {
        return evaluateChannelRepo.findForbiddenChannel(siteDomain, siteType, siteName)
    }
}
