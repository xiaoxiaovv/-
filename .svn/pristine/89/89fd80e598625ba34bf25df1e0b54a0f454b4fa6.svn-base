package com.istar.mediabroken.entity

/**
 * Author: Luda
 * Time: 2017/6/28
 */
class SystemSetting {
    String id;
    String type;
    String key;
    def    content;
    String description;
    Date updateTime;
    Date createTime;

    Map<String, Object> toMap(){
        return [
                _id                : id == null?UUID.randomUUID().toString() : id,
                type               : this.type,
                key                : this.key,
                content            : this.content,
                description        : this.description,
                updateTime         : this.updateTime,
                createTime         : this.createTime,
        ]
    }

    SystemSetting() {
        super
    }

    SystemSetting(Map map) {
        this.id             = map._id
        this.type           = map.type
        this.key            = map.key
        this.content        = map.content ? map.content : ""
        this.description    = map.description ? map.description : ""
        this.updateTime     = map.updateTime
        this.createTime     = map.createTime
    }
}
