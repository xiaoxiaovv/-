package com.istar.mediabroken.entity.system

class MessageStatus {
    long userId  // 主键

    Date lastTime   // 读取的最新一条消息的时间
    Date updateTime

    Map<String, Object> toMap() {
        return [
                _id    : userId,
                lastTime  : lastTime,
                updateTime: updateTime
        ]
    }

    MessageStatus() {
        super
    }

    MessageStatus(Map map) {
        userId = map._id
        lastTime = map.lastTime
        updateTime = map.updateTime

    }

    @Override
    public String toString() {
        return "MessageStatus{" +
                "userId=" + userId +
                ", lastTime=" + lastTime +
                ", updateTime=" + updateTime +
                '}';
    }
}