package com.istar.mediabroken.entity.statistics
/**
 * @author hanhui
 * @date 2018/4/2 17:16
 * @desc 用户账户数据统计
 * */
class AccountDataStatistics {
    def id
    Long totalCount
    Long newCount
    Long registeredCount
    Long newRegisteredCount
    Long officalCount
    Long newOfficalCount
    Long trialCount
    Long newTrialCount
    Date statisticTime
    Date createTime

    AccountDataStatistics() {
        super
    }

    Map<String, Object> toMap() {
        return [
                _id               : id,
                totalCount        : totalCount,
                newCount          : newCount,
                registeredCount   : registeredCount,
                newRegisteredCount: newRegisteredCount,
                officalCount      : officalCount,
                newOfficalCount   : newOfficalCount,
                trialCount        : trialCount,
                newTrialCount     : newTrialCount,
                statisticTime     : statisticTime,
                createTime        : createTime
        ]
    }

    AccountDataStatistics(Map map) {
        id = map._id
        totalCount = map.totalCount
        newCount = map.newCount
        registeredCount = map.registeredCount
        newRegisteredCount = map.newRegisteredCount
        officalCount = map.officalCount
        newOfficalCount = map.newOfficalCount
        trialCount = map.trialCount
        newTrialCount = map.newTrialCount
        statisticTime = map.statisticTime
        createTime = map.createTime
    }

    @Override
    public String toString() {
        return "AccountDataStatistics{" +
                "id=" + id +
                ", totalCount=" + totalCount +
                ", newCount=" + newCount +
                ", registeredCount=" + registeredCount +
                ", newRegisteredCount=" + newRegisteredCount +
                ", officalCount=" + officalCount +
                ", newOfficalCount=" + newOfficalCount +
                ", trialCount=" + trialCount +
                ", newTrialCount=" + newTrialCount +
                ", statisticTime=" + statisticTime +
                ", createTime=" + createTime +
                '}';
    }
}
