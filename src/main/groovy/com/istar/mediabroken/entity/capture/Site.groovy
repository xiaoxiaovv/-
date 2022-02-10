package com.istar.mediabroken.entity.capture

class Site {

    public static final int SITE_TYPE_WEBSITE = 1//网站
    public static final int SITE_TYPE_WECHAT = 2//微信
    public static final int SITE_TYPE_WEIBO = 3//微博

    Long userId
    String siteId
    String siteName//只是显示名称可以修改的
    String websiteName
    String websiteDomain
    int siteType
    Boolean isAutoPush
    String domainReverse
    String pinYinPrefix
    String message
    String approved
    String dataStatus
    Date updateTime
    Date createTime
    Date resetTime       //用户查看站点的时间，用于记录下次网站进入数据的开始时间
    Date countTime      // 系统统计站点新闻数的时间
    int count           // 系统统计的用户自上次浏览过该站点后，站点采集到的新闻数量，如果没有resetTime

    Map<String, Object> toMap() {
        return [
                _id          : siteId ?: UUID.randomUUID().toString(),
                userId       : userId,
                siteName     : siteName,
                websiteName  : websiteName,
                websiteDomain: websiteDomain,
                siteType     : siteType,
                isAutoPush   : isAutoPush,
                updateTime   : updateTime,
                createTime   : createTime,
                resetTime    : resetTime,
                countTime    : countTime,
                count        : count ?: 0,
                domainReverse: domainReverse,
                pinYinPrefix : pinYinPrefix,
                message      : message ?: "",
                approved     : approved ?: "",
                dataStatus   : dataStatus ?: ""
        ]
    }

    Site() {
        super
    }

    Site(Map map) {
        siteId = map._id
        userId = map.userId
        siteName = map.siteName
        websiteName = map.websiteName
        websiteDomain = map.websiteDomain
        siteType = map.siteType
        isAutoPush = map.isAutoPush
        domainReverse = map.domainReverse
        pinYinPrefix = map.pinYinPrefix
        updateTime = map.updateTime
        createTime = map.createTime
        resetTime = map.resetTime
        countTime = map.countTime
        count = map.count ?: 0
        message = map.message ?: ""
        approved = map.approved ?: ""
        dataStatus = map.dataStatus ?: ""
    }

    @Override
    public String toString() {
        return "Site{" +
                "userId=" + userId +
                ", siteId='" + siteId + '\'' +
                ", siteName='" + siteName + '\'' +
                ", websiteName='" + websiteName + '\'' +
                ", websiteDomain='" + websiteDomain + '\'' +
                ", siteType=" + siteType +
                ", isAutoPush=" + isAutoPush +
                ", domainReverse='" + domainReverse + '\'' +
                ", pinYinPrefix='" + pinYinPrefix + '\'' +
                ", message='" + message + '\'' +
                ", approved='" + approved + '\'' +
                ", dataStatus='" + dataStatus + '\'' +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                ", resetTime=" + resetTime +
                ", countTime=" + countTime +
                ", count=" + count +
                '}';
    }

    enum SiteTypeEnum {
        weSite(1, "网站"),
        weChat(2, "公众号"),
        weiBo(3, "微博")
        private Integer key
        private String desc

        Integer getKey() {
            return key
        }

        void setKey(Integer key) {
            this.key = key
        }

        String getDesc() {
            return desc
        }

        void setDesc(String desc) {
            this.desc = desc
        }

        SiteTypeEnum(Integer key, String desc) {
            this.key = key
            this.desc = desc
        }
    }
}
