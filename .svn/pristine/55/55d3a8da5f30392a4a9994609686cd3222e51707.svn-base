package com.istar.mediabroken.service.account

import com.istar.mediabroken.entity.account.AccountVsRole
import com.istar.mediabroken.repo.account.AccountVsRoleRepo
import com.istar.mediabroken.repo.account.RoleVsPrivilegeRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
/**
 * Author: hh
 * Time: 2018/1/23
 */
@Service
class PrivilegeService {
    @Autowired
    AccountVsRoleRepo accountVsRoleRepo

    @Autowired
    RoleVsPrivilegeRepo roleVsPrivilege


    List getPrivilegesByUserId(Long userId) {
        AccountVsRole accountVsRole = accountVsRoleRepo.getRoleIdByUserId(userId)
        def roleId = ''
        if (!accountVsRole) {
            roleId = '3'
        } else {
            roleId = accountVsRole.getRoleId()
        }
        if (roleId) {
            def privilegeIds = roleVsPrivilege.getPrivilegeByRoleId(roleId)
            return privilegeIds
        } else {
            return null
        }

    }
}
