package com.istar.mediabroken.api.evaluate

import com.istar.mediabroken.api.CurrentUser
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.evaluate.EvaluateTeam
import com.istar.mediabroken.service.evaluate.EvaluateChannelService
import com.istar.mediabroken.service.evaluate.EvaluateTeamService
import groovy.util.logging.Slf4j
import org.apache.commons.httpclient.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR

/**
 * @author zxj
 * @create 2018/6/19
 */
@RestController
@Slf4j
@RequestMapping(value = '/api/evaluate/team')
class EvaluateTeamApiController {

    @Autowired
    EvaluateTeamService evaluateTeamService
    @Autowired
    EvaluateChannelService evaluateChannelService

    /**
     * 添加分组
     * @param user
     * @param teamName
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    Map addEvaluateTeam(
            @CurrentUser LoginUser user,
            @RequestParam(value = "teamName", required = true, defaultValue = "") String teamName
    ) {
        def result;
        try {
            if (teamName.length() > 20) {
                return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '组名长度不超过20个有效字符')
            }
            if (!teamName) {
                return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '请输入分组名称！')
            }
            def list = evaluateTeamService.getEvaluateTeams(user.userId)
            def nameList = []
            list.each { it ->
                nameList.add(it.teamName)
            }
            if (nameList.contains(teamName)) {
                return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '组名重复！')
            }
            if (list.size() >= EvaluateTeam.TEAM_MAX_COUNT) {
                return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "新建组最大" + EvaluateTeam.TEAM_MAX_COUNT + "个！")
            }
            result = evaluateTeamService.addEvaluateTeam(user.userId, teamName)
        } catch (Exception e) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '新建失败！')
        }
        return apiResult(HttpStatus.SC_OK, '保存成功！')
    }

    /**
     * 删除组
     * @param user
     * @return
     */
    @RequestMapping(value = "/{evaluateTeamId}", method = RequestMethod.DELETE)
    public Map delTeam(
            @CurrentUser LoginUser user,
            @PathVariable("evaluateTeamId") String evaluateTeamId
    ) {
        def channel = evaluateChannelService.checkChannel(user.userId, null, evaluateTeamId)
        if (channel) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, '此分组测评使用中请勿操作，如需操作请删除测评！');
        }
        if (evaluateTeamId.equals("0")) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '删除失败！')
        }
        try {
            evaluateTeamService.delTeam(user.userId, evaluateTeamId)
            return apiResult(HttpStatus.SC_OK, "删除成功")
        } catch (e) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "删除失败")
        }
    }

    /**
     * 查询组
     * @param user
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Map getTeamList(
            @CurrentUser LoginUser user
    ) {
        def teams = evaluateTeamService.getEvaluateTeams(user.userId)
        return apiResult([list: teams])
    }

    /**
     * 查询组
     * @param user
     * @return
     */
    @RequestMapping(value = "/{evaluateTeamId}", method = RequestMethod.GET)
    public Map modifyTeam(
            @CurrentUser LoginUser user,
            @PathVariable("evaluateTeamId") String evaluateTeamId
    ) {
        def id = evaluateTeamService.findOneBydId(user.userId, evaluateTeamId)

        return apiResult([evaluateTeam: id])
    }

    /**
     * 修改组
     * @param user
     * @return
     */
    @RequestMapping(value = "/{evaluateTeamId}", method = RequestMethod.PUT)
    public Map modifyTeam(
            @CurrentUser LoginUser user,
            @PathVariable("evaluateTeamId") String evaluateTeamId,
            @RequestParam(value = "teamName", required = true, defaultValue = "") String teamName
    ) {
        def channel = evaluateChannelService.checkChannel(user.userId, null, evaluateTeamId)
        if (channel) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, '此分组测评使用中请勿操作，如需操作请删除测评！');
        }
        if (evaluateTeamId.equals("0")) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '修改失败！')
        }
        def id = evaluateTeamService.findOneBydId(user.userId, evaluateTeamId)
        if (teamName.length() > 20) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '组名长度不超过20个有效字符')
        }
        if (!teamName) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '请输入分组名称！')
        }
        def list = evaluateTeamService.getEvaluateTeams(user.userId)
        def nameList = []
        list.each { it ->
            nameList.add(it.teamName)
        }
        if (nameList.contains(teamName)) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '组名重复！')
        }
        evaluateTeamService.modifyTeamById(user.userId, evaluateTeamId, teamName)
        return apiResult(HttpStatus.SC_OK, "修改成功")
    }
}

