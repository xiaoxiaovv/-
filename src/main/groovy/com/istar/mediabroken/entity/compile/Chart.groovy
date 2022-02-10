package com.istar.mediabroken.entity.compile

class Chart {
    String id;//_id
    String jobId
    String chartType
    def chartData
    Date updateTime
    Date createTime

    Map<String, Object> toMap() {
        return [
                _id       : id ?: UUID.randomUUID().toString(),
                jobId     : jobId,
                chartType : chartType,
                chartData : chartData,
                updateTime: updateTime,
                createTime: createTime
        ]
    }

    Chart() {
        super
    }

    Chart(Map map) {
        id = map._id ?: map.id ?: ""
        chartType = map.chartType
        chartData = map.chartData
        jobId = map.jobId
        updateTime = map.updateTime
        createTime = map.createTime
    }


    @Override
    public String toString() {
        return "Chart{" +
                "id='" + id + '\'' +
                ", jobId='" + jobId + '\'' +
                ", chartType='" + chartType + '\'' +
                ", chartData='" + chartData + '\'' +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                '}';
    }
}
