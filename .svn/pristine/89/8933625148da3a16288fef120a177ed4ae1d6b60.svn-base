package com.istar.mediabroken.entity.favoriteGroup

/**
 * Author : YCSnail
 * Date   : 2017-11-09
 * Email  : liyancai1986@163.com
 */
class FavoriteGroup {


    public static final int GROUP_TYPE_NORMAL = 0//默认分组
    public static final int GROUP_TYPE_IMPORT = 1//已导入分组
    public static final int GROUP_TYPE_SHARED = 2//共享
    public static final String GROUP_NAME_IMPORT = "已导入"
    public static final String GROUP_NAME_NEW = "默认分组"
    public static final String GROUP_NAME_SHARED = "共享"


    String id  //_id
    long userId
    String groupName
    String groupType
    String agentId
    String orgId
    String teamId
    Date createTime
    Date updateTime


    FavoriteGroup(Map map) {
        id = map._id
        userId = map.userId
        groupName = map.groupName
        groupType = map.groupType
        agentId = map.agentId
        orgId = map.orgId
        teamId = map.teamId
        updateTime = map.updateTime
        createTime = map.createTime
    }


    @Override
    public String toString() {
        return "FavoriteGroup{" +
                "id='" + id + '\'' +
                ", userId=" + userId +
                ", groupName='" + groupName + '\'' +
                ", groupType='" + groupType + '\'' +
                ", agentId='" + agentId + '\'' +
                ", orgId='" + orgId + '\'' +
                ", teamId='" + teamId + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
