package com.istar.mediabroken.entity.system

/**
 * Author: zc
 * Time: 2018/1/10
 */
class OperationLog {

    String id
    def userInfo
    String operationSource //news 最新资讯 subject 全网监控
    String queryCondition
    Date createTime


    Map<String, Object> toMap() {
        return [
                _id       : id,
                userInfo     : userInfo,
                operationSource      : operationSource,
                queryCondition       : queryCondition,
                createTime: createTime ?: new Date(),
        ]
    }

    OperationLog() {
        super
    }

    OperationLog(String id, def userInfo, String operationSource, String queryCondition) {
        this.id = id
        this.userInfo = userInfo
        this.operationSource = operationSource
        this.queryCondition = queryCondition
        this.createTime = new Date()
    }

    OperationLog(Map map) {
        id = map._id
        userInfo = map.userInfo
        operationSource = map.operationSource
        queryCondition = map.queryCondition
        createTime = map.createTime ?: new Date()
    }
}
