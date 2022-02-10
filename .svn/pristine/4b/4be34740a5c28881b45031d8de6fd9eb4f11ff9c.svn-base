package com.istar.mediabroken.entity.capture

class SiteAutoPush {
    String siteId
    String prevStartTime //上次的自动推送新闻开始时间（第一次推送系统时间-5分钟）
    String prevEndTime   //上次的自动推送新闻结束时间（最后一条新闻的时间）
    Long prevNewsCount   //上次获取的新闻条数
    Long prevPushCount   //上次推送的新闻条数
    Long totalNewsCount  //总的获取的新闻条数
    Long totalPushCount  //总的推送的新闻条数
    Date updateTime
    Date createTime
    String createId() {
        return siteId
    }
}
