package com.istar.mediabroken.entity.system

class Message {
    String msgId  // _id
    long userId
    String title
    String content
    Date createTime
    boolean isRead   // 是否已读取,只用在接口返回结果中,不实际存储

    Map<String, Object> toMap() {
        return [
                _id       : msgId ?: UUID.randomUUID().toString(),
                userId    : userId,
                title     : title ?: "",
                content   : content ?: "",
                createTime: createTime
        ]
    }

    Message() {
        super
    }

    Message(Map map) {
        msgId = map._id ?: map.msgId ?: ""
        userId = map.userId
        title = map.title
        content = map.content
        createTime = map.createTime

    }

    @Override
    public String toString() {
        return "Message{" +
                "msgId='" + msgId + '\'' +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", createTime=" + createTime +
                ", isRead=" + isRead +
                '}';
    }
}