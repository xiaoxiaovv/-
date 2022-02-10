package com.istar.mediabroken.api.account

import com.istar.mediabroken.api.CheckPrivilege
import com.istar.mediabroken.api.CurrentUser
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.account.Privilege
import com.istar.mediabroken.service.account.AccountService
import com.istar.mediabroken.service.account.OrganizationService
import com.istar.mediabroken.utils.DateUitl
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * Created by zxj on   2018/1/26
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/organization")
class OrganizationApiController {

    @Autowired
    OrganizationService organizationService
    @Autowired
    AccountService accountService

    @CheckPrivilege(privileges = [Privilege.ACCOUNT_MANAGE])
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    def getOrgInfo(
            @CurrentUser LoginUser user
    ) {
        def org = organizationService.getOrgInfoByOrgIdAndAgentId(user.orgId, user.agentId)
        def userList = accountService.getUsersByOrgIdAndAngetId(user.orgId, user.agentId)
        def userInfoMap = accountService.getUserById(user.userId)
        def userInfo = null
        if (userInfoMap.status == HttpStatus.SC_OK){
            userInfo = userInfoMap.msg
        }
        def map = [
                id          : org.id,
                orgName     : org.orgName,
                maxUserCount: org.maxUserCount,
                expDate     :  DateUitl.convertFormatDate(DateUitl.convertEsDate(userInfo ? userInfo.expDate ?.getTime() : 1530288000000L), "yyyy-MM-dd"),//不存在默认为18.6.30
                nowUserCount: userList.size(),
        ]
        return apiResult(map)
    }

    @CheckPrivilege(privileges = [Privilege.ACCOUNT_MANAGE])
    @RequestMapping(value = "/orgName", method = RequestMethod.GET)
    def getOrgName(
            @CurrentUser LoginUser user
    ) {
        def org = organizationService.getOrgInfoByOrgIdAndAgentId(user.orgId, user.agentId)
        def map = [
                id          : org.id,
                orgName     : org.orgName,
        ]
        return apiResult(map)
    }
}
