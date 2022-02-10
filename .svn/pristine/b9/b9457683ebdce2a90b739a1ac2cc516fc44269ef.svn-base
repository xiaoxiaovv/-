package com.istar.mediabroken.entity.statistics

/**
 * author:hh
 * date:2018/3/15
 */
class UserDataStatistics {
    String id
    String orgId
    String teamId
    String teamName
    Long userId
    String publisher
    Date publishTime
    Long manualPushCount
    Long autoPushCount
    Long weiboCount
    Long wechatCount
    Long toutiaoCount
    Long qqomCount
    Date createTime


    UserDataStatistics() {
        super
    }

    Map<String, Object> toMap() {
        return [
                _id            : id,
                orgId          : orgId,
                teamId         : teamId,
                teamName       : teamName,
                userId         : userId,
                publisher      : publisher,
                publishTime    : publishTime,
                manualPushCount: manualPushCount,
                autoPushCount  : autoPushCount,
                weiboCount     : weiboCount,
                wechatCount    : wechatCount,
                toutiaoCount   : toutiaoCount,
                qqomCount      : qqomCount,
                createTime     : createTime
        ]
    }

    UserDataStatistics(Map map) {
        id = map._id
        orgId = map.orgId
        teamId = map.teamId
        teamName = map.teamName
        userId = map.userId
        publisher = map.publisher
        publishTime = map.publishTime
        manualPushCount = map.manualPushCount
        autoPushCount = map.autoPushCount
        weiboCount = map.weiboCount
        wechatCount = map.wechatCount
        toutiaoCount = map.toutiaoCount
        qqomCount = map.qqomCount
        createTime = map.createTime
    }


    @Override
    public String toString() {
        return "UserDataStatistics{" +
                "id='" + id + '\'' +
                ", orgId='" + orgId + '\'' +
                ", teamId='" + teamId + '\'' +
                ", teamName='" + teamName + '\'' +
                ", userId=" + userId +
                ", publisher='" + publisher + '\'' +
                ", publishTime=" + publishTime +
                ", manualPushCount=" + manualPushCount +
                ", autoPushCount=" + autoPushCount +
                ", weiboCount=" + weiboCount +
                ", wechatCount=" + wechatCount +
                ", toutiaoCount=" + toutiaoCount +
                ", qqomCount=" + qqomCount +
                ", createTime=" + createTime +
                '}';
    }
}
