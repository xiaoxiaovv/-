package com.istar.mediabroken.entity.statistics
/**
 * @author hanhui
 * @date 2018/4/4 11:17
 * @desc 用户留存数据统计
 * */
class RetainedDataStatistics {
    def id
    Long retainedCount
    String retainedPercent
    Long lossCount
    String lossPercent
    Long newLossCount
    String newLossIds
    Date statisticTime
    Date createTime

    RetainedDataStatistics() {
        super
    }

    Map<String, Object> toMap() {
        return [
                _id            : id,
                retainedCount  : retainedCount,
                retainedPercent: retainedPercent,
                lossCount      : lossCount,
                lossPercent    : lossPercent,
                newLossCount   : newLossCount,
                newLossIds     : newLossIds,
                statisticTime  : statisticTime,
                createTime     : createTime
        ]
    }

    RetainedDataStatistics(Map map) {
        id = map._id
        retainedCount = map.retainedCount
        retainedPercent = map.retainedPercent
        lossCount = map.lossCount
        lossPercent = map.lossPercent
        newLossCount = map.newLossCount
        newLossIds = map.newLossIds
        statisticTime = map.statisticTime
        createTime = map.createTime
    }


}
