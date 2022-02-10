package com.istar.mediabroken.api.favoriteGroup

import com.istar.mediabroken.api.CheckExpiry
import com.istar.mediabroken.api.CurrentUser
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.service.FavoriteGroupService
import com.istar.mediabroken.utils.DateUitl
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR

/**
 * Author : YCSnail
 * Date   : 2017-11-09
 * Email  : liyancai1986@163.com
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/favorite/group")
class FavoriteGroupApiController {

    @Autowired
    FavoriteGroupService favoriteGroupService
    /**
     * 添加组
     * @param user
     * @param groupName
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    @CheckExpiry
    Map addGroup(
            @CurrentUser LoginUser user,
            @RequestParam(value = "groupName", required = true, defaultValue = "新建分组") String groupName
    ) {
        def result;
        try {
            result = favoriteGroupService.addGroup(user.getUserId(), groupName)
        } catch (Exception e) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '新建失败！')
        }
        return apiResult(result)
    }
    /**
     * 修改组名
     * @param user
     * @param groupId
     * @param groupName
     * @return
     */
    @RequestMapping(value = "/{groupId}", method = RequestMethod.POST)
    @CheckExpiry
    Map modifyGroup(
            @CurrentUser LoginUser user,
            @PathVariable String groupId,
            @RequestParam(value = "groupName", required = true, defaultValue = "新建分组") String groupName
    ) {
        def result;
        try {
            def type = favoriteGroupService.getGroupTypeByGroupId(groupId)
            if (!"0".equals(type)) {
                return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '修改失败！')
            }
            result = favoriteGroupService.updateGroup(user.getUserId(), groupId, groupName)
        } catch (Exception e) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '修改失败！')
        }
        return apiResult(result)
    }
    /**
     * 删除组包含组内信息
     * @param user
     * @param groupId
     * @return
     */
    @RequestMapping(value = "/{groupId}", method = RequestMethod.DELETE)
    @CheckExpiry
    Map removeGroup(
            @CurrentUser LoginUser user,
            @PathVariable String groupId
    ) {
        try {
            def type = favoriteGroupService.getGroupTypeByGroupId(groupId)
            if (!"0".equals(type)) {
                return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '删除失败！')
            }
            favoriteGroupService.delGroup(user.getUserId(), groupId)
            return apiResult(HttpStatus.SC_OK, '删除成功！')
        } catch (Exception e) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '删除失败！')
        }
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    Map getFavoriteGroupList(
            @CurrentUser LoginUser user
    ) {
        try {
            def FavoriteGroupList = favoriteGroupService.getFavoriteGroupList(user)
            def teamList = favoriteGroupService.getSharedByTeam(user)
            FavoriteGroupList.addAll(teamList)
            return apiResult([list: FavoriteGroupList])
        } catch (Exception e) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '加载失败！')
        }
    }

    @RequestMapping(value = "/showNewsOperations", method = RequestMethod.GET)
    Map getNewsOperationList(
            @CurrentUser LoginUser user,
            @RequestParam(value = "groupId", required = false) String groupId,
            @RequestParam(value = "groupType", required = false) String groupType,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize
    ) {
        if ((pageSize * pageNo) > 5000) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "请求的数据太多，失败")
        }
        try {
            def list = []
            if (groupType && "2".equals(groupType)) {
                def endDate = null
                def startDate = null
                try {
                    endDate = new Date()
                    startDate = DateUitl.addDay(endDate, -7)
                } catch (Exception e) {
                    return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '展示7天内共享数据！')
                }
                if (startDate && endDate) {
                    list = favoriteGroupService.getNewsOperationSharedList(user.userId, groupId, user.orgId, user.teamId, startDate, endDate, pageNo, pageSize)
                }
            } else {
                list = favoriteGroupService.getNewsOperationList(user.userId, groupId, pageNo, pageSize)
            }
            return apiResult([list: list])
        } catch (Exception e) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '展开列表失败！')
        }
    }
}
