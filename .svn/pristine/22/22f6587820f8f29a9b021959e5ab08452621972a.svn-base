package com.istar.mediabroken.entity

/**
 * Author : YCSnail
 * Date   : 2017-04-18
 * Email  : liyancai1986@163.com
 */
class SummaryPush extends PushLog {

    String summaryId
    String data

    SummaryPush(){
        super
    }

    String createId(){
        return UUID.randomUUID().toString()
    }

    static SummaryPush toObject(def map){
        return new SummaryPush(
                summaryId   : map.summaryId,
                data        : map.data,
                title       : map.title as String,
                source      : map.source as String,
                pushType    : map.pushType,
                orgId       : map.orgId,
                userId      : map.userId,
                status      : map.status as int,
                createTime  : map.createTime,
                updateTime  : map.updateTime
        )
    }

}
