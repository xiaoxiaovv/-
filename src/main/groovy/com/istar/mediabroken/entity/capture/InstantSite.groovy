package com.istar.mediabroken.entity.capture

class InstantSite {
    String siteId
    Long userId
    String siteName//只是显示名称可以修改的
    String websiteName
    String websiteDomain
    int urlType
    int siteType
    Boolean isAutoPush
    String domainReverse
    String pinYinPrefix
    Date updateTime
    Date createTime

    Map<String, Object> toMap() {
        return [
                _id          : siteId ?: UUID.randomUUID().toString(),
                userId       : userId,
                siteName     : siteName,
                websiteName  : websiteName,
                websiteDomain: websiteDomain,
                urlType      : urlType,
                siteType     : siteType,
                isAutoPush   : isAutoPush,
                updateTime   : updateTime,
                createTime   : createTime,
                domainReverse: domainReverse,
                pinYinPrefix : pinYinPrefix
        ]
    }

    InstantSite() {
        super
    }

    InstantSite(Map map) {
        siteId = map._id
        userId = map.userId
        siteName = map.siteName
        websiteName = map.websiteName
        websiteDomain = map.websiteDomain
        urlType = map.urlType
        siteType = map.siteType
        isAutoPush = map.isAutoPush
        domainReverse = map.domainReverse
        pinYinPrefix = map.pinYinPrefix
        updateTime = map.updateTime
        createTime = map.createTime
    }

    @Override
    public String toString() {
        return "InstantSite{" +
                "userId=" + userId +
                ", siteId='" + siteId + '\'' +
                ", siteName='" + siteName + '\'' +
                ", websiteName='" + websiteName + '\'' +
                ", websiteDomain='" + websiteDomain + '\'' +
                ", urlType=" + urlType +
                ", siteType=" + siteType +
                ", isAutoPush=" + isAutoPush +
                ", domainReverse='" + domainReverse + '\'' +
                ", pinYinPrefix='" + pinYinPrefix + '\'' +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                '}';
    }
}

enum NewsUrlTypeEnum {
    detailsUrl(1, '详情URL'), //详情URL
    channelUrl(2, '频道URL'), //频道URL

    private Integer key
    private String desc

    NewsUrlTypeEnum(Integer key, String desc) {
        this.key = key
        this.desc = desc
    }

    Integer getKey() {
        return key
    }

    String getDesc() {
        return desc
    }

    Map toMap() {
        Map map = new HashMap();
        map.put("urlType", key);
        map.put("desc", desc);
        return map;
    }
}