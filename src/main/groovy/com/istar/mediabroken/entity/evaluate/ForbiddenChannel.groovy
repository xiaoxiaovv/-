package com.istar.mediabroken.entity.evaluate

/**
 * @author zxj
 * @create 2018/7/10
 * @desc 禁止添加的测评渠道
 */
class ForbiddenChannel {
    String id
    String siteName
    String siteDomain
    int siteType
    Date updateTime
    Date createTime
    ForbiddenChannel(Map map) {
        id = map._id
        siteName = map.siteName
        siteDomain = map.siteDomain
        siteType = map.siteType
        updateTime = map.updateTime
        createTime = map.createTime
    }
}
