package com.istar.mediabroken.api.evaluate

import com.istar.mediabroken.api.CurrentUser
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.entity.evaluate.EvaluateChannel
import com.istar.mediabroken.entity.evaluate.UserStatusEnum
import com.istar.mediabroken.service.evaluate.EvaluateChannelService
import com.istar.mediabroken.service.evaluate.EvaluateTeamService
import com.istar.mediabroken.utils.UrlUtils
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR
import static org.apache.http.HttpStatus.SC_OK
import static org.springframework.web.bind.annotation.RequestMethod.*

/**
 * @author zxj
 * @create 2018/6/20
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/evaluate/")
class EvaluateChannelApiController {

    @Autowired
    EvaluateChannelService evaluateChannelService
    @Autowired
    EvaluateTeamService evaluateTeamService


    @RequestMapping(value = "channel/status", method = RequestMethod.GET)
    Map getEvaluateChannelStatus(
            @CurrentUser LoginUser user

    ) {
        List list = new ArrayList();
        for (UserStatusEnum userStatusEnum : UserStatusEnum.values()) {
            list.add(userStatusEnum.toMap());
        }
        Map<String, Object> m = new HashMap<>();
        m.put("list", list)
        return apiResult(m)
    }

    @RequestMapping(value = "channel/type", method = GET)
    Map getChannelTypeList(
            @CurrentUser LoginUser user
    ) {
        def list = []
        list.add([siteType: "网站", value: 1])
        list.add([siteType: "公众号", value: 2])
        list.add([siteType: "微博", value: 3])
        return apiResult([list: list])
    }
    /**
     * 查询渠道分组
     * @param user
     * @param siteType
     * @param siteName
     * @return
     */
    @RequestMapping(value = "channel/list", method = GET)
    Map getEvaluateTeamList(
            @CurrentUser LoginUser user,
            @RequestParam(value = "siteType", required = false, defaultValue = "0") int siteType,
            @RequestParam(value = "siteName", required = false, defaultValue = "") String siteName
    ) {
        def team = evaluateChannelService.getEvaluateChannelAndTeam(user.userId, siteType, siteName)
        return apiResult([list: team])
    }

    /**
     * 添加渠道
     * @param user
     * @param siteType
     * @param siteName
     * @param siteDomain
     * @param evaluateTeamId
     * @return
     */
    @RequestMapping(value = "channel", method = POST)
    Map addEvaluateChannel(
            @CurrentUser LoginUser user,
            @RequestParam(value = "siteType", required = true) int siteType,
            @RequestParam(value = "siteName", required = false, defaultValue = "") String siteName,
            @RequestParam(value = "siteDomain", required = false, defaultValue = "") String siteDomain,
            @RequestParam(value = "evaluateTeamId", required = false, defaultValue = "0") String evaluateTeamId
    ) {

        if (siteType == Site.SITE_TYPE_WEBSITE) {
            if (siteDomain.equals("")) {
                return apiResult(SC_INTERNAL_SERVER_ERROR, '请填写正确的域名！');
            }
        } else {
            siteDomain = ""
            if (siteName.equals("") && siteType == Site.SITE_TYPE_WEIBO) {
                return apiResult(SC_INTERNAL_SERVER_ERROR, '请填写正确的微博名称！');
            }
            if (siteName.equals("") && siteType == Site.SITE_TYPE_WECHAT) {
                return apiResult(SC_INTERNAL_SERVER_ERROR, '请填写正确的微信名称！');
            }
        }
        if (!evaluateTeamId.equals("0")) {
            def id = evaluateTeamService.findOneBydId(user.userId, evaluateTeamId)
            if (!id) {
                return apiResult(SC_INTERNAL_SERVER_ERROR, '请选择正确的分组！');
            }
        }
        def forbidden = evaluateChannelService.findForbiddenChannel(UrlUtils.getTopDomain(siteDomain), siteType, siteName)
        if (forbidden) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "不支持此渠道!"])
        }
        def channel = evaluateChannelService.getEvaluateChannelByUserId(user.userId, siteType, siteName.trim(), UrlUtils.getDomainFromUrl(siteDomain.trim()))
        if (channel) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "该渠道已存在!"])
        } else {
            def res = evaluateChannelService.addEvaluateChannel(user.userId, siteType, siteName.trim(), UrlUtils.getDomainFromUrl(siteDomain.trim()), evaluateTeamId)
            return res
        }
    }

    @RequestMapping(value = "channel/{channelId}", method = GET)
    Map getEvaluateChannelById(
            @CurrentUser LoginUser user,
            @PathVariable(value = "channelId") String channelId

    ) {
        def id = evaluateChannelService.findById(user.userId, channelId)
        return apiResult([status: HttpStatus.SC_OK, info: id])
    }
    /**
     * 删除站点
     * @param user
     * @param channelId
     * @return
     */
    @RequestMapping(value = "channel/{channelId}", method = DELETE)
    Map delEvaluateChannel(
            @CurrentUser LoginUser user,
            @PathVariable(value = "channelId") String channelId

    ) {
        try {
            def channel = evaluateChannelService.checkChannel(user.userId, channelId, null)
            if (channel) {
                return apiResult(SC_INTERNAL_SERVER_ERROR, '此渠道测评使用中请勿操作，如需操作请删除测评！');
            }
            def stat = evaluateChannelService.delChannelAndChannelStat(user.userId, channelId)
            return apiResult(SC_OK, '删除成功！');
        } catch (e) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, '删除失败！');
        }
    }

    /**
     * 修改渠道
     * @param user
     * @param siteType
     * @param siteName
     * @param siteDomain
     * @param evaluateTeamId
     * @return
     */
    @RequestMapping(value = "channel/{channelId}", method = PUT)
    Map modifyEvaluateChannel(
            @CurrentUser LoginUser user,
            @PathVariable(value = "channelId") String channelId,
            @RequestParam(value = "siteType", required = true) int siteType,
            @RequestParam(value = "siteName", required = false, defaultValue = "") String siteName,
            @RequestParam(value = "siteDomain", required = false, defaultValue = "") String siteDomain,
            @RequestParam(value = "evaluateTeamId", required = true, defaultValue = "0") String evaluateTeamId
    ) {
        def flag = evaluateChannelService.checkChannel(user.userId, channelId, null)
        if (flag) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, '此渠道测评使用中请勿操作，如需操作请删除测评！');
        }
        if (siteType == Site.SITE_TYPE_WEBSITE) {
            if (siteDomain.equals("")) {
                return apiResult(SC_INTERNAL_SERVER_ERROR, '请填写正确的域名！');
            }
        } else {
            siteDomain = ""
            if (siteName.equals("") && siteType == Site.SITE_TYPE_WEIBO) {
                return apiResult(SC_INTERNAL_SERVER_ERROR, '请填写正确的微博名称！');
            }
            if (siteName.equals("") && siteType == Site.SITE_TYPE_WECHAT) {
                return apiResult(SC_INTERNAL_SERVER_ERROR, '请填写正确的微信名称！');
            }
        }
        if (!evaluateTeamId.equals("0")) {
            def id = evaluateTeamService.findOneBydId(user.userId, evaluateTeamId)
            if (!id) {
                return apiResult(SC_INTERNAL_SERVER_ERROR, '请选择正确的分组！');
            }
        }

        def team = evaluateChannelService.getEvaluateChannelByUserIdAndTeam(user.userId, siteType, siteName.trim(), UrlUtils.getDomainFromUrl(siteDomain.trim()), evaluateTeamId)
        if (team) {
            return apiResult([status: HttpStatus.SC_OK, msg: "修改成功!"])
        }
        EvaluateChannel channel = evaluateChannelService.getEvaluateChannelByUserId(user.userId, siteType, siteName.trim(), UrlUtils.getDomainFromUrl(siteDomain.trim()))
        if (channel) {
            if (!channel.id.equals(channelId)) {
                return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "该渠道已存在!"])
            }
        }

        try {
            def channel1 = evaluateChannelService.modifyEvaluateChannel(user.userId, channelId, siteType, siteName.trim(), UrlUtils.getDomainFromUrl(siteDomain.trim()), evaluateTeamId)
            return channel1
        } catch (e) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, '保存失败！');
        }

    }

    /**
     * 批量移动渠道到分组（移除组时候teamId 设置为"0"）
     * @param user
     * @param channelIds
     * @param teamId
     * @return
     */
    @RequestMapping(value = "channel/team", method = PUT)
    Map modifyChannelTeam(
            @CurrentUser LoginUser user,
            @RequestParam(value = "channelIds", required = true) String channelIds,
            @RequestParam(value = "teamId", required = true) String teamId
    ) {

        String[] split = channelIds.split(",")
        for (int i = 0; i < split.length; i++) {
            String elem = split[i];
            def channel = evaluateChannelService.checkChannel(user.userId, elem, null)
            if (channel) {
                return apiResult(SC_INTERNAL_SERVER_ERROR, '此渠道测评使用中请勿操作，如需操作请删除测评！');
            }
        }

        if (!split || split.size() <= 0) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, '移动失败，请选择您的渠道！');
        }
        if (!teamId.equals("0")) {
            def id = evaluateTeamService.findOneBydId(user.userId, teamId)
            if (!id) {
                return apiResult(SC_INTERNAL_SERVER_ERROR, '移动失败，请选择您的分组！');
            }
        }
        try {
            def team = evaluateChannelService.modifyChannelTeam(user.userId, teamId, split.toList())
            return apiResult(SC_OK, '移动成功！');
        } catch (e) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, '移动失败！');
        }

    }
}
