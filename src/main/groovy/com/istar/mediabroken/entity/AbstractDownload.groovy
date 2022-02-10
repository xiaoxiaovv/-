package com.istar.mediabroken.entity

/**
 * Author: Luda
 * Time: 2017/5/31
 */
class AbstractDownload {
    String id;
    String abstractId
    String title
    String picUrl
    String content
    List newsDetail
    String orgId
    String userId
    Date createTime
    Date updateTime

    AbstractDownload(){
        super
    }

    String createId(){
        return UUID.randomUUID().toString()
    }

    static AbstractPush toObject(def map){
        return new AbstractPush(
                id          : map._id,
                abstractId  : map.abstractId,
                title       : map.title as String,
                picUrl      : map.picUrl,
                content     : map.content,
                newsDetail  : map.newsDetail,
                orgId       : map.orgId,
                userId      : map.userId,
                createTime  : map.createTime,
                updateTime  : map.updateTime
        )
    }

    Map<String, Object> toMap(){
        return [
                abstractId : this.abstractId,
                title       : this.title as String,
                picUrl      : this.picUrl,
                content     : this.content,
                orgId        : this.orgId,
                newsDetail  : this.newsDetail,
                orgId        : this.orgId,
                userId       : this.userId,
                createTime   : this.createTime,
                updateTime   : this.updateTime,
        ]
    }
}
