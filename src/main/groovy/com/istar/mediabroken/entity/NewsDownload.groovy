package com.istar.mediabroken.entity

import com.istar.mediabroken.utils.Md5Util

/**
 * Created by luda on 2017/5/9.
 */
class NewsDownload {

    String newsId;
    def news;
    Long userId;
    String orgId;
    Date createTime;
    Date updateTime;

    String createId(){
        return Md5Util.md5((userId as String).concat(orgId).concat(newsId))
    }
    NewsDownload(){
        super
    }

    NewsDownload(long userId, String orgId, String newsId) {
        this.userId = userId
        this.orgId = orgId
        this.newsId = newsId
    }

    static NewsDownload toObject(def map){
        return new NewsDownload(
                newsId      : map.newsId as String,
                news        : map.news,
                orgId       : map.orgId,
                userId      : map.userId,
                createTime  : map.createTime,
                updateTime  : map.updateTime
        )
    }
    Map<String, Object> toMap(){
        return [
                newsId       : this.newsId,
                news         : this.news,
                orgId        : this.orgId,
                userId       : this.userId,
                createTime   : this.createTime,
                updateTime   : this.updateTime,
        ]
    }
}
