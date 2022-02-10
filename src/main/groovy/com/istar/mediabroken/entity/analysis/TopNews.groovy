package com.istar.mediabroken.entity.analysis

class TopNews {
    String id
    String siteDomain
    long newsType
    String siteName
    String title
    long visitCount
    long likeCount
    long commentCount
    long reprintCount
    long publishTime
    long mediaCount

    Map<String, Object> toMap() {
        return [
                id          : id,
                siteDomain  : siteDomain,
                newsType    : newsType,
                siteName    : siteName,
                title       : title,
                visitCount  : visitCount,
                likeCount   : likeCount,
                commentCount: commentCount,
                reprintCount: reprintCount,
                publishTime : publishTime,
                mediaCount  : mediaCount

        ]
    }

    TopNews() {
        super
    }

    TopNews(Map map) {
        id = map.id
        siteDomain = map.siteDomain
        newsType = map.newsType
        siteName = map.siteName
        title = map.title
        visitCount = map.visitCount?:0
        likeCount = map.likeCount?:0
        commentCount = map.commentCount?:0
        reprintCount = map.reprintCount?:0
        publishTime = map.publishTime
        mediaCount = map.mediaCount?:0

    }


    @Override
    public String toString() {
        return "TopNews{" +
                "id='" + id + '\'' +
                ", siteDomain='" + siteDomain + '\'' +
                ", newsType=" + newsType +
                ", siteName='" + siteName + '\'' +
                ", title='" + title + '\'' +
                ", visitCount=" + visitCount +
                ", likeCount=" + likeCount +
                ", commentCount=" + commentCount +
                ", reprintCount=" + reprintCount +
                ", publishTime=" + publishTime +
                ", mediaCount=" + mediaCount +
                '}';
    }
}
