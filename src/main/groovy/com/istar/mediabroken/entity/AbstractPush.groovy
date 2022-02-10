package com.istar.mediabroken.entity

// 摘要推送日志
public class AbstractPush extends PushLog {

    String id
    String abstractId
    String picUrl
    String content
    List newsDetail
    def newsAbstract

    AbstractPush(){
        super
    }

    String createId(){
        return UUID.randomUUID().toString()
    }

    static AbstractPush toObject(def map){
        return new AbstractPush(
                id  : map._id,
                abstractId  : map.abstractId,
                title       : map.title as String,
                source      : map.source as String,
                picUrl      : map.picUrl,
                content     : map.content,
                newsDetail  : map.newsDetail,
                pushType    : map.pushType,
                orgId       : map.orgId,
                userId      : map.userId,
                status      : map.status as int,
                newsAbstract: map?.newsAbstract,
                createTime  : map.createTime,
                updateTime  : map.updateTime
        )
    }

}
