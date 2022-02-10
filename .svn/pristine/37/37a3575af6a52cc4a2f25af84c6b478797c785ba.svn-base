package com.istar.mediabroken.entity.account

/**
 * 后台模拟用户登录的实体
 * Created by zc on   2018/7/19
 */
class SimulateLoginSession {

    String id
    long userId
    long adminUserId
    String adminUsername
    boolean enable
    Date updateTime
    Date createTime

    SimulateLoginSession() {
    }

    SimulateLoginSession(String id, long userId, long adminUserId, String adminUsername, boolean enable, Date updateTime, Date createTime) {
        this.id = id
        this.userId = userId
        this.adminUserId = adminUserId
        this.adminUsername = adminUsername
        this.enable = enable
        this.updateTime = updateTime
        this.createTime = createTime
    }

    Map<String, Object> toMap() {
        return [
                _id          : id ?: "",
                userId       : userId ?: 0l,
                adminUserId  : adminUserId ?: 0l,
                adminUsername: adminUsername ?: "",
                enable       : enable ?: false,
                createTime   : createTime,
                updateTime   : updateTime
        ]
    }

    SimulateLoginSession(Map map) {
        id = map._id ?: ""
        userId = map.userId ?: 0l
        adminUserId = map.adminUserId ?: 0l
        adminUsername = map.adminUsername ?: ""
        enable = map.enable ?: false
        updateTime = map.updateTime
        createTime = map.createTime
    }

    @Override
    public String toString() {
        return "SimulateLoginSession{" +
                "id='" + id + '\'' +
                ", userId=" + userId +
                ", adminUserId=" + adminUserId +
                ", adminUsername='" + adminUsername + '\'' +
                ", enable=" + enable +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                '}';
    }
}

enum LoginSourceEnum {
    adminLogin(1, '后台模拟登录'), //后台模拟登录
    userLogin(2, '前台用户登录'), //前台用户登录

    private Integer key
    private String desc

    LoginSourceEnum(Integer key, String desc) {
        this.key = key
        this.desc = desc
    }

    Integer getKey() {
        return key
    }

    String getDesc() {
        return desc
    }
}
