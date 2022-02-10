package com.istar.mediabroken.entity.account
/**
 * zc
 * 2017.12.15
 */
class AccountCustomSetting {
    String id
    long userId
    String key
    LinkedList content
    String orgId
    Date createTime
    Date updateTime

    AccountCustomSetting() {
    }

    static AccountCustomSetting toObject(def map) {
        return new AccountCustomSetting(
                id: map.id ?: map._id,
                userId: map.userId as long,
                key: map.key as String,
                content: map.content,
                orgId: map.orgId,
                createTime: map.createTime,
                updateTime: map.updateTime
        )
    }
}
