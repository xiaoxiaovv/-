package com.istar.mediabroken.entity.copyright

import com.istar.mediabroken.utils.DateUitl

class Monitor {
    String monitorId  // _id
    String title
    String url
    String author
    String media
    Date startDate
    Date endDate
    Integer status
    long userId
    String contentAbstract
    List whiteList
    List blackList
    Date updateTime
    Date createTime

    Map<String, Object> toMap() {
        return [
                _id            : monitorId ?: UUID.randomUUID().toString(),
                title          : title ?: "",
                url            : url ?: "",
                author         : author ?: "",
                media          : media ?: "",
                startDate      : startDate ?: new Date(),
                endDate        : endDate ?: DateUitl.addDay(startDate, 30),
                status         : status,
                userId         : userId,
                contentAbstract: contentAbstract,
                whiteList      : whiteList,
                blackList      : blackList,
                updateTime     : updateTime,
                createTime     : createTime
        ]
    }

    Monitor() {
        super
    }

    Monitor(Map map) {
        monitorId = map._id ?: map.monitorId ?: ""
        title = map.title
        url = map.url
        author = map.author
        media = map.media
        startDate = map.startDate
        endDate = map.endDate
        status = map.status
        userId = map.userId
        contentAbstract = map.contentAbstract
        whiteList = map.whiteList
        blackList = map.blackList
        updateTime = map.updateTime
        createTime = map.createTime

    }

    @Override
    public String toString() {
        return "Monitor{" +
                "monitorId='" + monitorId + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", author='" + author + '\'' +
                ", media='" + media + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status=" + status +
                ", userId=" + userId +
                ", contentAbstract='" + contentAbstract + '\'' +
                ", whiteList=" + whiteList +
                ", blackList=" + blackList +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                '}';
    }
}