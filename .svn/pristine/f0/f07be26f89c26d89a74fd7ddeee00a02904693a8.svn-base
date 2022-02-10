package com.istar.mediabroken.entity.account

/**
 * Author: Luda
 * Time: 2017/7/24
 */
class UserRole {
    String id;
    Long userId
    String roleId
    String updateUser
    String createUser
    Date updateTime
    Date createTime

    Map<String, Object> toMap() {
        return [
                _id       : id,
                userId    : userId,
                roleId    : roleId,
                updateUser: updateUser,
                createUser: createUser,
                updateTime: updateTime,
                createTime: createTime,
        ]
    }

    UserRole() {
        super
    }

    UserRole(Map map) {
        id = map._id
        userId = map.userId
        roleId = map.roleId
        updateUser = map.updateUser
        createUser = map.createUser
        updateTime = map.updateTime
        createTime = map.createTime
    }
}
