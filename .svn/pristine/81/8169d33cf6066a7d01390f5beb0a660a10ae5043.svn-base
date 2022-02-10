package com.istar.mediabroken.entity.compile

class Report {
    String id;//_id
    Long userId
    String name  //事件名称
    String keyWords  //关键词
    Date startTime
    Date endTime
    int timeRange//时间范围
    int status
    String jobId
    String description
    Date updateTime
    Date createTime


    Map<String, Object> toMap() {
        return [
                _id       : id ?: UUID.randomUUID().toString(),
                userId    : userId,
                name      : name,
                keyWords  : keyWords,
                startTime : startTime,
                endTime   : endTime,
                timeRange : timeRange,
                status    : status,
                jobId     : jobId,
                description:description,
                updateTime: updateTime,
                createTime: createTime
        ]
    }

    Report() {
        super
    }

    Report(Map map) {
        id = map._id
        userId = map.userId
        name = map.name
        keyWords = map.keyWords
        startTime = map.startTime
        endTime = map.endTime
        timeRange = map.timeRange?:7
        status = map.status
        jobId = map.jobId
        description = map.description
        updateTime = map.updateTime
        createTime = map.createTime
    }


    @Override
    public String toString() {
        return "Report{" +
                "id='" + id + '\'' +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", keyWords='" + keyWords + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", timeRange=" + timeRange +
                ", status=" + status +
                ", jobId='" + jobId + '\'' +
                ", description='" + description + '\'' +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                '}';
    }
}
