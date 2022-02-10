package com.istar.mediabroken.entity

import com.istar.mediabroken.utils.Md5Util

/**
 * Created by luda on 2017/5/10.
 */
class CopyrightMonitorNews {
    String _id
    Long userId
    String monitorId
    String newsId
    Boolean isTort
    Boolean isBlack
    Boolean isWhite
    String title
    String source
    String author
    String url
    String contentAbstract
    String site
    String newsType
    String Ctime
    String DkTime
    Date createTime
    public String createId() {
        return Md5Util.md5((userId as String).concat(monitorId).concat(newsId))
    }
    Map<String, Object> toMap(){
        return [
                _id                 : this._id,
                userId              : this.userId,
                monitorId           : this.monitorId,
                newsId              : this.newsId,
                isTort              : this.isTort,
                isBlack             :this.isBlack,
                isWhite             :this.isWhite,
                title               : this.title,
                source              : this.source,
                author              : this.author,
                url                 : this.url,
                contentAbstract     : this.contentAbstract,
                site                 : this.site,
                newsType            : this.newsType,
                Ctime               : this.Ctime,
                DkTime              : this.DkTime,
                createTime          : this.createTime,
        ]
    }
}
