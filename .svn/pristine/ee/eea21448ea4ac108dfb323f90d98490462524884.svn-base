package com.istar.mediabroken.entity.capture

class SiteVsWeibo {
    String id
    Integer siteType
    String siteName
    String siteDomain
    String weiboName
    Date updateTime
    Date createTime

    Map<String, Object> toMap() {
        return [
                _id         : id ?: UUID.randomUUID().toString(),
                siteType      : siteType,
                siteName : siteName?:"",
                siteDomain    : siteDomain?:"",
                weiboName   : weiboName?:"",
                updateTime  : updateTime,
                createTime  : createTime
        ]
    }

    SiteVsWeibo() {
        super
    }

    SiteVsWeibo(Map map) {
        id = map._id
        siteType = map.siteType
        siteName = map.siteName?:""
        siteDomain = map.siteDomain?:""
        weiboName = map.weiboName?:""
        updateTime = map.updateTime
        createTime = map.createTime
    }


    @Override
    public String toString() {
        return "SiteVsWeibo{" +
                "id=" + id +
                ", siteType=" + siteType +
                ", siteName='" + siteName + '\'' +
                ", siteDomain='" + siteDomain + '\'' +
                ", weiboName='" + weiboName + '\'' +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                '}';
    }
}
