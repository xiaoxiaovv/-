package com.istar.mediabroken.entity.compile

/**
 * Author: Luda
 * Time: 2017/8/1
 */
class ArticlePush {

    String id  // _id, uuid
    def material
    int status                  // 1, 未推送  2, 已推送
    long userId
    String orgId
    Date createTime
    Date updateTime

    Map<String, Object> toMap() {
        return [
                _id       : id ?: UUID.randomUUID().toString(),
                material  : material,
                status    : status,
                userId    : userId,
                orgId     : orgId,
                updateTime: updateTime,
                createTime: createTime,
        ]
    }

    ArticlePush() {
        super
    }

    ArticlePush(Map map) {
        id = map._id ?: map.id ?: ""
        material = map.material ?: ""
        status = map.status ?: 0
        userId = map.userId ?: 0L
        orgId = map.orgId ?: "0"
        createTime = map.createTime ?: new Date()
        updateTime = map.updateTime ?: new Date()
    }

    @Override
    public String toString() {
        return "ArticlePush{" +
                "id='" + id + '\'' +
                ", material=" + material +
                ", status=" + status +
                ", userId=" + userId +
                ", orgId='" + orgId + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
