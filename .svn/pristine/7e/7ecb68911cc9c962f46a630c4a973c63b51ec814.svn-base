package com.istar.mediabroken.service

import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.favoriteGroup.FavoriteGroup
import com.istar.mediabroken.repo.ShareChannelRepo
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.repo.capture.NewsRepo
import com.istar.mediabroken.repo.favoriteGroup.FavoriteGroupRepo
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Author : YCSnail
 * Date   : 2017-11-09
 * Email  : liyancai1986@163.com
 */
@Slf4j
@Service
class FavoriteGroupService {

    @Autowired
    FavoriteGroupRepo favoriteGroupRepo
    @Autowired
    NewsRepo newsRepo
    @Autowired
    ShareChannelRepo shareChannelRepo
    @Autowired
    AccountRepo accountRepo

    Map addGroup(long userId, String groupName) {
        def result = favoriteGroupRepo.addGroup(userId, groupName)
        return result
    }

    Map updateGroup(long userId, String groupId, String groupName) {
        def result = favoriteGroupRepo.updateGroup(userId, groupId, groupName)
        return result
    }

    void delGroup(long userId, String groupId) {
        def norGroupList = favoriteGroupRepo.getGroupTypeIsNormalFavoriteGroupList(userId)
        if (norGroupList.size() >= 2) {
            favoriteGroupRepo.delGroup(userId, groupId)
            def newOpList = newsRepo.getNewsOperationListByGroupId(userId, groupId)
            if (newOpList.size() >= 1) {
                newsRepo.removeUserNewsOperationByGroupId(userId, groupId)
            }
        } else {
            1 / 0
        }
    }

    def getFavoriteGroupList(LoginUser user) {
        def userId = user.userId
        def groupList = favoriteGroupRepo.getFavoriteGroupList(userId)
        if (groupList.size() < 2) {
            def importGroup = favoriteGroupRepo.newLoadGroup(userId)
            def normalGroup = favoriteGroupRepo.addGroup(userId, FavoriteGroup.GROUP_NAME_NEW)
            //查询收藏数量
            def normalTypeList = newsRepo.findOperationListByType(3, userId)
            if (normalTypeList.size() > 0) {
                //给收藏的添加字段
                String normalGroupId = normalGroup.get("groupId")
                newsRepo.addGroupIdToNormal(normalGroupId, userId)
            }
            //查询导入数量
            def importTypeList = newsRepo.findOperationListByType(4, userId)
            if (importTypeList.size() > 0) {
                //给导入类型的newoption添加组名
                String importGroupId = importGroup.get("groupId")
                newsRepo.addGroupIdToImport(importGroupId, userId)
            }
            return favoriteGroupRepo.getFavoriteGroupList(userId);
        } else {
            return groupList
        }
    }

    def getNewsOperationList(Long userId, String groupId, int pageNo, int pageSize) {
        if (groupId != null && groupId != "") {
            def result = newsRepo.pageNewsOperationListByGroupId(userId, groupId, pageNo, pageSize)
            result?.each {
                it.news.keywords = []
                it.news.content = ""
            }
            return result
        }
    }

    def getNewsOperationSharedList(long userId, String groupId, String orgId, String teamId, Date startDate, Date endDate, int pageNo, int pageSize) {
        def result = []
        if (groupId != null && groupId != "" && (!"0".equals(teamId))) {
            def sharedList = newsRepo.pageNewsOperationSharedList(userId, groupId, orgId, teamId, startDate, endDate, pageNo, pageSize)
            sharedList?.each {
                def share = [:]
                it.news.keywords = []
                it.news.content = ""
                if (it.shareChannelCount > 1) {
                    it.shareResult = getshareResult(it.timeStamp)
                }
                share.shareResult = it.shareResult
                share.news = it.news
                share.createTime = it.createTime
                share.operationType = it.operationType
                share.realName = accountRepo.getUserById(it.userId)?.realName
                share.userName = accountRepo.getUserById(it.userId)?.userName
                share._id = it._id
                result << share
            }
            return result
        }
        return result
    }

    def getshareResult(String timeStamp) {
        return shareChannelRepo.getShareChannelResultList(timeStamp)
    }

    def getSharedByTeam(LoginUser user) {
        def list = []
        if (user.teamId || !user.teamId.equals("0")) {
            list = favoriteGroupRepo.getGroupsByUserAndType(user.teamId, user.orgId, user.agentId, FavoriteGroup.GROUP_TYPE_SHARED)
        }
        return list
    }

    def getGroupTypeByGroupId(String groupId) {
        def group = favoriteGroupRepo.getGroupById(groupId)
        return group.groupType
    }
}
