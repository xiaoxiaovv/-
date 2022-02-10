package com.istar.mediabroken.entity.evaluate

/**
 * @author zxj
 * @create 2018/6/25
 * @desc top10前10的新闻
 */
class EvaluateNews {
    String id
    String siteDomain
    String siteName
    int siteType
    Date publishTime
    String title
    String poster
    String url
    long reprintCount
    long readCount
    long likeCount
    long commentCount

    EvaluateNews(Map map) {
        id = map._id
        siteDomain = map.siteDomain
        siteName = map.siteName
        siteType = map.siteType
        publishTime = map.publishTime
        title = map.title
        poster = map.poster
        url = map.url
        reprintCount = map.reprintCount ?: 0
        readCount = map.readCount ?: 0
        likeCount = map.likeCount ?: 0
        commentCount = map.commentCount ?: 0

    }
}
