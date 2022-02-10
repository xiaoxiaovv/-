package com.istar.mediabroken.entity

/**
 * Author: Luda
 * Time: 2017/6/14
 */
class CaptureFilter {
    String id
    Long userId
    String name;
    List mediaType; //[1,2,3]
    List keyWords; //[北京,持续高温,微博]
    Integer heat; //0 全部 4 4星 3 3星
    Integer time; //-1 自定义 0 全部 4 24 7*24
    Date startTime;
    Date endTime;
    Integer classification; //0 全部
    Integer area; //0 全部
    Integer orientation; //0：全部 1：正向2：负向3：中性4：有争议
    Integer order; //1 时间 2 热度
    Date updateTime
    Date createTime

    Map<String, Object> toMap(){
        return [
                _id                : id == null?UUID.randomUUID().toString() : id,
                userId             : this.userId,
                name               : this.name,
                mediaType          : this.mediaType,
                keyWords          : this.keyWords,
                heat               : this.heat,
                time               : this.time,
                startTime          : this.startTime,
                endTime            : this.endTime,
                classification     : this.classification,
                area               : this.area,
                orientation        : this.orientation,
                order              : this.order,
                updateTime         : this.updateTime,
                createTime         : this.createTime,
        ]
    }

    CaptureFilter() {
        super
    }

    CaptureFilter(Map map) {
        this.id                 = map._id
        this.userId             = map.userId
        this.name               = map.name
        this.mediaType          = map.mediaType
        this.keyWords           =map.keyWords
        this.heat               = map.heat
        this.time               = map.time
        this.startTime          = map.startTime
        this.endTime            = map.endTime
        this.classification     = map.classification
        this.area               = map.area
        this.orientation        = map.orientation
        this.order              = map.order
        this.updateTime         = map.updateTime
        this.createTime         = map.createTime
    }
}
