package com.istar.mediabroken.entity

/**
 * Author: Luda
 * Time: 2017/6/9
 */
class AccountProfile {
    String id
    long userId
    Map compileSummary
    Map captureSite
    List articleTemplate
    Date updateTime
    Date createTime
    Map evaluateChannel//渠道数量

    Map<String, Object> toMap() {
        return [
                _id            : id == null ? UUID.randomUUID().toString() : id,
                userId         : this.userId,
                compileSummary : this.compileSummary,
                captureSite    : this.captureSite,
                evaluateChannel: this.evaluateChannel,
                articleTemplate: this.articleTemplate,
                updateTime     : this.updateTime,
                createTime     : this.createTime,
        ]
    }

    AccountProfile() {
        super
    }

    AccountProfile(Map map) {
        this.id = map._id
        this.userId = map.userId
        this.compileSummary = map.compileSummary ? map.compileSummary : [:]
        this.captureSite = map.captureSite ? map.captureSite : [:]
        this.evaluateChannel = map.evaluateChannel ? map.evaluateChannel : [:]
        this.articleTemplate = map.articleTemplate ?: []
        this.updateTime = map.updateTime
        this.createTime = map.createTime
    }
}
