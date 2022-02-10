package com.istar.mediabroken.api.account

import com.istar.mediabroken.api.CheckPrivilege
import com.istar.mediabroken.api.CurrentUser
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.account.Privilege
import com.istar.mediabroken.entity.account.Team
import com.istar.mediabroken.service.account.AccountService
import com.istar.mediabroken.service.account.TeamServie
import groovy.util.logging.Slf4j
import org.apache.commons.httpclient.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * Created by zxj on   2018/1/23
 */
@RestController
@Slf4j
@RequestMapping(value = '/api/team')
public class TeamApiController {

    @Autowired
    TeamServie teamServie

    @Autowired
    AccountService accountService

    @CheckPrivilege(privileges = [Privilege.TEAM_MANAGE])
    @RequestMapping(value = "", method = RequestMethod.POST)
    Map addGroup(
            @CurrentUser LoginUser user,
            @RequestParam(value = "teamName", required = true, defaultValue = "新建分组") String teamName
    ) {
        def result;
        try {
            if (teamName.length() > 20){
                return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '组名长度不超过20个有效字符')
            }
            if (!teamName) {
                return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '新建失败！')
            }
            def list = teamServie.getTeamList(user.orgId)
            def nameList = []
            list.each {it ->
                nameList.add(it.teamName)
            }
            if (nameList.contains(teamName)){
                return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '组名重复！')
            }
            if (list.size() >= Team.TEAM_MAX_COUNT) {
                return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "新建组最大" + Team.TEAM_MAX_COUNT + "个！")
            }
            result = teamServie.addTeam(user, teamName)
        } catch (Exception e) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '新建失败！')
        }
        return apiResult([list: result])
    }

    @CheckPrivilege(privileges = [Privilege.TEAM_MANAGE])
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Map getTeamList(
            @CurrentUser LoginUser user
    ) {
        try {
            def list = teamServie.getTeamList(user.orgId)
            return apiResult([list: list])
        } catch (Exception e) {
            return apiResult(org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, '加载失败！')
        }
    }

    @CheckPrivilege(privileges = [Privilege.TEAM_MANAGE])
    @RequestMapping(value = "/{teamId}", method = RequestMethod.PUT)
    Map modifyTeam(
            @CurrentUser LoginUser user,
            @PathVariable String teamId,
            @RequestParam(value = "teamName", required = true, defaultValue = "新建分组") String teamName
    ) {
        def result;
        try {
            if (teamName.length() > 20){
                return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '组名长度不超过20个有效字符')
            }
            if (!teamName) {
                return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '修改失败！')
            }
            def list = teamServie.getTeamList(user.orgId)
            def nameList = []
            list.each {it ->
                nameList.add(it.teamName)
            }
            def team = teamServie.getTeam(teamId)
            if (teamName.equals(team.teamName)){
                return apiResult([list: team])
            }
            if (nameList.contains(teamName)){
                return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '组名重复！')
            }
            result = teamServie.modifyTeam(user, teamId, teamName)
        } catch (Exception e) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '修改失败！')
        }
        return apiResult([list: result])
    }

    @CheckPrivilege(privileges = [Privilege.TEAM_MANAGE])
    @RequestMapping(value = "/{teamId}", method = RequestMethod.DELETE)
    Map removeGroup(
            @CurrentUser LoginUser user,
            @PathVariable String teamId
    ) {
        try {
            if (teamId.equals("0")) {
                return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '删除失败！')
            }
            teamServie.delTeam(user, teamId)
            return apiResult(HttpStatus.SC_OK, '删除成功！')
        } catch (Exception e) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '删除失败！')
        }
    }

    /**
     * 管理员查询组成员
     * @param user
     * @param teamId
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.TEAM_MANAGE])
    @RequestMapping(value = "/members", method = RequestMethod.GET)
    Map getTeamMembers(
            @CurrentUser LoginUser user,
            @RequestParam(value = "teamId", required = false) String teamId
    ) {
        try {
            def list = teamServie.getTeamMembers(user, teamId)
            return apiResult([list: list])
        } catch (Exception e) {
            return apiResult(org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, '展开列表失败！')
        }
    }
    /**
     * 查询用户同组成员
     * @param user
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.PUSHNEWS_MANAGE])
    @RequestMapping(value = "/memberList", method = RequestMethod.GET)
    Map getTeamMember(
            @CurrentUser LoginUser user
    ) {
        try {
            if ("0".equals(user.teamId)){
                def result = accountService.getUserById(user.userId)
                def account = result.msg
                def res = []
                res.add(account)
                return apiResult([list: res])
            }
            def list = teamServie.getTeamMembers(user, user.teamId)
            return apiResult([list: list])
        } catch (Exception e) {
            return apiResult(org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, '展开列表失败！')
        }
    }

}
