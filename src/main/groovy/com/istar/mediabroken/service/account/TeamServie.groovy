package com.istar.mediabroken.service.account

import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.account.Team
import com.istar.mediabroken.entity.favoriteGroup.FavoriteGroup
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.repo.account.TeamRepo
import com.istar.mediabroken.repo.favoriteGroup.FavoriteGroupRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Created by zxj on   2018/1/23
 */
@Service
class TeamServie {
    @Autowired
    TeamRepo teamRepo
    @Autowired
    AccountRepo accountRepo
    @Autowired
    FavoriteGroupRepo favoriteGroupRepo

    def addTeam(LoginUser user, String teamName){
        //添加分组
        def team = teamRepo.addTeam(user.orgId, user.agentId, teamName)
        //创建共享文件夹(一组有一个)
        favoriteGroupRepo.addGroupByTeam(user, team.get("teamId"), FavoriteGroup.GROUP_NAME_SHARED)
        return team
    }

    List getTeamList(String orgId){
        def list = teamRepo.getTeamList(orgId)
        return list
    }

    def modifyTeam(LoginUser user, String teamId, String teamName){
        def list = teamRepo.modifyTeam(user.orgId, teamId, teamName)
        def team = teamRepo.getTeam(teamId)
        return team
    }

    def delTeam(LoginUser user, String teamId){
        //删除分组
        teamRepo.delTeam(teamId)
        //用户teamId 改为"0"
        def accountIds = []
        def accountList = accountRepo.getAccountListByTeamIdAndAgentId(user.orgId, user.agentId, teamId, true)
        accountList.each { elem->
            accountIds.add(elem.getId())
        }
        if (accountIds.size() > 0){
            accountRepo.modifyTeamIdById(accountIds, "0")
        }
        //删除共享文件夹
        favoriteGroupRepo.delGroupByUserAndType(user, teamId,FavoriteGroup.GROUP_TYPE_SHARED)
    }

    List getTeamMembers(LoginUser user, String teamId){
        def accountList = accountRepo.getAccountListByTeamIdAndAgentId(user.orgId, user.agentId, teamId, true)
        return accountList
    }
    Team getTeam(String teamId){
        def team = teamRepo.getTeam(teamId)
        return team
    }
}
