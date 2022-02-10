package com.istar.mediabroken.entity.compile

/**
 * Author: zc
 * Time: 2017/8/3
 */
class Style {

    String id  // _id, uuid
    long userId
    String content //样式内容
    Date createTime
    Date updateTime

    Map<String, Object> toMap() {
        return [
                _id            : id ?: UUID.randomUUID().toString(),
                userId         : userId,
                content        : content,
                updateTime     : updateTime,
                createTime     : createTime,
        ]
    }

    Style() {
        super
    }

    Style(Map map) {
        id = map._id ?: map.id ?: ""
        userId = map.userId ?: 0L
        content = map.content ?: ""
        createTime = map.createTime ?: new Date()
        updateTime = map.updateTime ?: new Date()
    }

    @Override
    public String toString() {
        return "Style{" +
                "id='" + id + '\'' +
                ", userId=" + userId +
                ", content='" + content + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
