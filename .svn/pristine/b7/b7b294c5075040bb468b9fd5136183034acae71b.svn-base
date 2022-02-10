package com.istar.mediabroken.entity.copyright

/**
 * Author: Luda
 * Time: 2017/6/14
 */
class CopyRightFilter {
    String id
    long userId
    def whiteList //[[websiteName:xxx, websiteDomain:bbb ]]
    def blackList
    Date updateTime
    Date createTime

    Map<String, Object> toMap(){
        return [
                _id                : id == null?UUID.randomUUID().toString() : id,
                userId             : this.userId,
                whiteList          : this.whiteList,
                blackList          : this.blackList,
                updateTime         : this.updateTime,
                createTime         : this.createTime,
        ]
    }

    CopyRightFilter() {
        super
    }

    CopyRightFilter(Map map) {
        this.id             = map._id
        this.userId         = map.userId
        this.whiteList      = map.whiteList ? map.whiteList : []
        this.blackList      = map.blackList ? map.blackList : []
        this.updateTime     = map.updateTime
        this.createTime     = map.createTime
    }
}
