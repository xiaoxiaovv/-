package com.istar.mediabroken.entity

import com.istar.mediabroken.utils.Md5Util;


// 新闻推送信息
public class NewsPush extends PushLog {
    // 新闻内容
    String publishTime
    String author
    String keyword
    String url
    String newsId
    String content
    String contentAbstract
    int reprintCount
    int heat
    String publishDay

    NewsPush(){
        super
    }

    NewsPush(long userId, String orgId, String newsId) {
        this.userId = userId
        this.orgId = orgId
        this.newsId = newsId
    }

    String createId(){
        return Md5Util.md5((userId as String).concat(orgId).concat(newsId))
    }

    int getHeat() {
        return News.caculateHeat(reprintCount)
    }


    static NewsPush toObject(def map){
        return new NewsPush(
                newsId      : map.newsId as String,
                title       : map.news.title as String,
                source      : map.news.source as String,
                author      : map.news.author as String,
                keyword     : map.news.keyword as String,
                url         : map.news.url as String,
                content     : map.news.content as String,
                contentAbstract    : map.news.abstract as String,
                reprintCount: map.news.reprintCount as int,
                publishTime : (map.news.publishTime.getTime()) as String,
                pushType    : map.pushType,
                orgId       : map.orgId,
                userId      : map.userId,
                status      : map.status as int,
                createTime  : map.createTime,
                updateTime  : map.updateTime,
                publishDay   : map.news.get("publishDay")? map.news.publishDay : "",
        )
    }

    @Override
    public String toString() {
        return "NewsPush{" +
                "publishTime='" + publishTime + '\'' +
                ", author='" + author + '\'' +
                ", keyword='" + keyword + '\'' +
                ", url='" + url + '\'' +
                ", newsId='" + newsId + '\'' +
                ", content='" + content + '\'' +
                ", contentAbstract='" + contentAbstract + '\'' +
                ", reprintCount=" + reprintCount +
                ", heat=" + heat +
                ", publishDay='" + publishDay + '\'' +
                '}';
    }
}
