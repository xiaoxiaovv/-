package com.istar.mediabroken.entity.copyright

import com.istar.mediabroken.utils.Md5Util

/**
 * Created by luda on 2017/5/10.
 */
class MonitorNews {
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
    Date Ctime
    String DkTime
    Date createTime
    Boolean isKeyChannel
    Integer commentCount //评论总量
    Integer visitCount //自媒体阅读
    Integer reprintCount //微博转发
    Integer likeCount //点赞量


    public String createId() {
        return Md5Util.md5((userId as String).concat(monitorId).concat(newsId))
    }

    Map<String, Object> toMap() {
        return [
                _id            : this._id,
                userId         : this.userId,
                monitorId      : this.monitorId,
                newsId         : this.newsId,
                isTort         : this.isTort,
                isBlack        : this.isBlack,
                isWhite        : this.isWhite,
                title          : this.title,
                source         : this.source,
                author         : this.author,
                url            : this.url,
                contentAbstract: this.contentAbstract,
                site           : this.site,
                newsType       : this.newsType,
                Ctime          : this.Ctime,
                DkTime         : this.DkTime,
                createTime     : this.createTime,
                isKeyChannel   : this.isKeyChannel,
                commentCount   : commentCount,
                visitCount     : visitCount,
                reprintCount   : reprintCount,
                likeCount      : likeCount

        ]
    }

    @Override
    public String toString() {
        return "MonitorNews{" +
                "_id='" + _id + '\'' +
                ", userId=" + userId +
                ", monitorId='" + monitorId + '\'' +
                ", newsId='" + newsId + '\'' +
                ", isTort=" + isTort +
                ", isBlack=" + isBlack +
                ", isWhite=" + isWhite +
                ", title='" + title + '\'' +
                ", source='" + source + '\'' +
                ", author='" + author + '\'' +
                ", url='" + url + '\'' +
                ", contentAbstract='" + contentAbstract + '\'' +
                ", site='" + site + '\'' +
                ", newsType='" + newsType + '\'' +
                ", Ctime='" + Ctime + '\'' +
                ", DkTime='" + DkTime + '\'' +
                ", createTime=" + createTime +
                ", isKeyChannel=" + isKeyChannel +
                ", commentCount=" + commentCount +
                ", visitCount=" + visitCount +
                ", reprintCount=" + reprintCount +
                ", likeCount=" + likeCount +
                '}';
    }
}
