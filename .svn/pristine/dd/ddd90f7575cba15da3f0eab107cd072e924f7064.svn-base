package com.istar.mediabroken.entity.account

/**
 * Author: hh
 * Time: 2018/1/24
 */
class AccountVsRole {
    String id
    String roleId
    long userId


    Map<String, Object> toMap() {
        return [
                _id   : id,
                roleId: roleId,
                userId: userId,
        ]
    }

    AccountVsRole() {
        super
    }

    AccountVsRole(Map map) {
        id     = map._id
        roleId = map.roleId
        userId = map.userId
    }

    @Override
    public String toString() {
        return "AccountVsRole{" +
                "id='" + id + '\'' +
                ", roleId='" + roleId + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }

    AccountVsRole(String id, String roleId, long userId) {
        this.id = id
        this.roleId = roleId
        this.userId = userId
    }
}
