package com.istar.mediabroken.entity

import com.istar.mediabroken.utils.Md5Util

/**
 * Author : YCSnail
 * Date   : 2017-04-19
 * Email  : liyancai1986@163.com
 */
class NewsShare {

    String newsId
    def news
    String content      //用户编辑的分享文本内容
    def shareChannel    //分享的渠道信息
    def shareContent

    String userId
    String orgId

    Date createTime
    Date updateTime

    NewsShare(){
        super
    }

    NewsShare(long userId, String orgId, String newsId) {
        this.userId = userId
        this.orgId = orgId
        this.newsId = newsId
    }

    String createId(){
        return UUID.randomUUID().toString()
    }

    static NewsShare toObject(def map){
        return new NewsShare(
                newsId      : map.newsId as String,
                news        : map.news,
                shareChannel: map.shareChannel,
                shareContent: map.shareContent,
                orgId       : map.orgId,
                userId      : map.userId,
                createTime  : map.createTime,
                updateTime  : map.updateTime
        )
    }

}
