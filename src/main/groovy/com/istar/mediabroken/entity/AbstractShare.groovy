package com.istar.mediabroken.entity

/**
 * Author : YCSnail
 * Date   : 2017-06-01
 * Email  : liyancai1986@163.com
 */
class AbstractShare extends ShareLog {

    String id
    String abstractId
    def newsAbstract


    AbstractShare(){
        super
    }

    String createId(){
        return UUID.randomUUID().toString()
    }

    static AbstractShare toObject(def map){
        return new AbstractShare(
                id          : map._id,
                abstractId  : map.abstractId as String,
                title       : map.title,
                newsAbstract: map.newsAbstract,
                shareChannel: map.shareChannel,
                shareContent: map.shareContent,
                orgId       : map.orgId,
                userId      : map.userId,
                createTime  : map.createTime,
                updateTime  : map.updateTime
        )
    }
}
