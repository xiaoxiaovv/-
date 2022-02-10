package com.istar.mediabroken.entity.statistics

/**
 * Author: zhaochen
 * Time: 2018/3/16
 */
class OrgDataStatistics {
    String id
    String orgId
    long manualPushCount //手动推送
    long autoPushCount //自动推送
    long weiboCount
    long wechatCount
    long toutiaoCount
    long qqomCount
    Date publishTime
    Date createTime

    Map<String, Object> toMap() {
        return [
                _id            : id,
                orgId          : orgId,
                manualPushCount: manualPushCount,
                autoPushCount  : autoPushCount,
                weiboCount     : weiboCount,
                wechatCount    : wechatCount,
                toutiaoCount   : toutiaoCount,
                qqomCount      : qqomCount,
                publishTime    : publishTime,
                createTime     : createTime
        ]
    }

    OrgDataStatistics() {
        super
    }

    OrgDataStatistics(Map map) {
        id = map._id
        orgId = map.orgId
        manualPushCount = map.manualPushCount
        autoPushCount = map.autoPushCount
        weiboCount = map.weiboCount
        wechatCount = map.wechatCount
        toutiaoCount = map.toutiaoCount
        qqomCount = map.qqomCount
        publishTime = map.publishTime
        createTime = map.createTime
    }


    @Override
    public String toString() {
        return "OrgDataStatistics{" +
                "id='" + id + '\'' +
                ", orgId='" + orgId + '\'' +
                ", manualPushCount=" + manualPushCount +
                ", autoPushCount=" + autoPushCount +
                ", weiboCount=" + weiboCount +
                ", wechatCount=" + wechatCount +
                ", toutiaoCount=" + toutiaoCount +
                ", qqomCount=" + qqomCount +
                ", publishTime=" + publishTime +
                ", createTime=" + createTime +
                '}';
    }
}
