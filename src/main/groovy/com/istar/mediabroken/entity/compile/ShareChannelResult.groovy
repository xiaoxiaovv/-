package com.istar.mediabroken.entity.compile

/**
 * Author: zc
 * Time: 2018/01/26
 */
class ShareChannelResult {

    String id  // _id, uuid
    String orgId
    String timeStamp //时间戳
    def shareResult
    Date createTime

    Map<String, Object> toMap() {
        return [
                _id        : id ?: UUID.randomUUID().toString(),
                orgId      : orgId,
                timeStamp  : timeStamp,
                shareResult: shareResult,
                createTime : createTime,
        ]
    }

    ShareChannelResult() {
        super
    }

    ShareChannelResult(Map map) {
        id = map._id ?: map.id ?: ""
        orgId = map.orgId ?: ""
        timeStamp = map.timeStamp ?: ""
        createTime = map.createTime ?: new Date()
        shareResult = map.shareResult
    }

    @Override
    public String toString() {
        return "ShareChannelResult{" +
                "id='" + id + '\'' +
                ", orgId='" + orgId + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                ", shareResult=" + shareResult +
                ", createTime=" + createTime +
                '}';
    }
}
