package com.istar.mediabroken.entity.account

/**
 * Author: Luda
 * Time: 2017/7/28
 */
class Organization {

    public static final String ECLOUD_CUST_PREFIX = "ecloud_"

    String id;
    String orgName;
    String agentId
    String appId;
    String secret
    boolean canPushNews; //是否支持信息推送
    Date updateTime
    Date createTime
    int maxUserCount
    Date expDate
    def ecloudOrgInfo

    Map<String, Object> toMap() {
        return [
                _id         : id,
                orgName     : orgName,
                agentId     : agentId,
                appId       : appId,
                canPushNews : canPushNews,
                updateTime  : updateTime,
                createTime  : createTime,
                expDate     : expDate,
                maxUserCount: maxUserCount,
                ecloudOrgInfo   : ecloudOrgInfo
        ]
    }

    Organization() {
        super
    }

    Organization(Map map) {
        id = map._id ?: map.id
        orgName = map.orgName
        agentId = map.agentId
        appId = map.appId ?: ""
        secret = map.secret
        canPushNews = map.canPushNews?:false
        updateTime = map.updateTime?:new Date()
        createTime = map.createTime?:new Date()
        expDate = map.expDate
        maxUserCount = map.maxUserCount
        ecloudOrgInfo = map.ecloudOrgInfo
    }

    static String getEcloudOrgId (int ecloudCustId) {
        return ECLOUD_CUST_PREFIX + ecloudCustId;
    }

}
